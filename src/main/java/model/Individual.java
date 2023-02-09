package model;

import lombok.Getter;
import lombok.Setter;
import util.OwnFunctions;
import util.OwnStopper;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Individual implements HasID {

    public static int nextId;
    int id;

    //state variables

    public int ageInPeriods;
    int typeIndex;  //0 is the highest educational level
    public double firstWage;
    double lifeTimeIncomeEarned;
    int workExperience;
    int nPeriodsInUnemployment;
    Household household;
    Household parentHousehold;
    public List<Individual> siblings = new ArrayList<>();
    boolean isMale = true;

    //derived variables
    double wage;
    double potentialWage;
    double lifeTimeIncome;

    boolean womanMarrying;
    public long manBucketCode;

    public Individual() {
        id = Individual.nextId;
        Individual.nextId++;
    }

    public Individual(int id) {
        this.id = id;
        if (Individual.nextId < id + 1) Individual.nextId = id + 1;
    }

    void aging() {

        if (Model.simulationWithSingleGeoLocation) {
            if (household!=null && household.preferredGeoLocation!=Model.singleGeoLocation) {
                endLife();
            } else if (parentHousehold!=null && parentHousehold.preferredGeoLocation!=Model.singleGeoLocation) {
                endLife();
            }
        }
        ageInPeriods++;
        if (ageInPeriods == Model.lifespan) {
            endLife();
            return;
        }

        if (Model.rnd.nextDouble()<Model.deathProbability[isMale ? 0 : 1][typeIndex][ageInPeriods]) {
            if (household != null && household.homeUnderConstruction != null && household.members.size()==1) {
                //it would make the code more complex if we had to handle the inheritance of homeUnderConstruction and so we rather decided to eliminate these cases
            } else {
                endLife();
            }

        }

        if (ageInPeriods >= Model.ageInPeriodsForFirstWorkExperience[typeIndex] && household == null) {
            formOwnHousehold();
        }
    }

    public void refreshLabourMarketStatus() {

        if (household == null) return;

        potentialWage = Model.priceLevel*Model.realGDPLevel*(1-Model.taxRate)*firstWage*Model.wageRatio[typeIndex][workExperience];

        if (ageInPeriods<Model.retirementAgeInPeriods) {

            if (Model.phase != Model.Phase.SETUP) {
                if (nPeriodsInUnemployment > 0) {
                    nPeriodsInUnemployment++;
                    if (nPeriodsInUnemployment > Model.minUnemploymentPeriods[typeIndex] && Model.threadNextDouble(this)<Model.unemploymentEndingProbabilities[typeIndex]){
                        nPeriodsInUnemployment = 0;
                    }
                } else if (nPeriodsInUnemployment == 0 && Model.threadNextDouble(this) < Model.unemploymentProbabilities[typeIndex]) {
                    nPeriodsInUnemployment = 1;

                }
            }



            if (nPeriodsInUnemployment == 0) {
                wage = potentialWage;
                if (workExperience<Model.lifespan-1) {
                    workExperience++;
                }

            } else {
                wage = 0;
            }
        } else {
            nPeriodsInUnemployment = 0;
            potentialWage = Model.pensionReplacementRate[typeIndex]*potentialWage;
            wage = potentialWage;
        }

    }

    public void calculateLifeTimeIncomeEarnedAndLifeTimeIncome() {

        lifeTimeIncomeEarned += Model.realGDPLevel*firstWage*Model.wageRatio[typeIndex][workExperience];
        lifeTimeIncome = lifeTimeIncomeEarned;
        for (int i = 1; i < Model.nPeriodsToLookAheadToCalculateLifeTimeIncome; i++) { //lifeTimeIncomeEarned already contains the wage for the actual month
            if (ageInPeriods + i < Model.retirementAgeInPeriods) {

                    double realGDPLevel = Model.realGDPLevelPath[Model.period + i];
                    if (Model.simulationWithShock && Model.realGDPLevelShockPath[Model.period] != 1) realGDPLevel *= (1-Model.unemploymentRates[typeIndex])*Model.realGDPLevelShockPath[Model.period + i];
                    lifeTimeIncome += realGDPLevel*firstWage*Model.wageRatio[typeIndex][workExperience+i];

            }

        }

        if (Model.sensitivityInLifetimeIncomeMultiplier>0 && Model.phase != Model.Phase.SETUP) {
            if (Model.period>0 && household != null) {
                GeoLocation geoLocation = household.preferredGeoLocation;
                double priceToIncomeRatio = geoLocation.priceIndexToAverageWageIncome / geoLocation.basePriceIndexToAverageWageIncome;
                lifeTimeIncome *= (1 + Model.sensitivityInLifetimeIncomeMultiplier * (priceToIncomeRatio-1));
            }

        }


    }


    void endLife() {

        if (household != null) {

            household.members.remove(this);

            if (household.members.size() == 0) {

                for (Individual child : household.children) {
                    if (child.household == null) {
                        child.formOwnHousehold();
                    }
                }
                bequeath();
                for (LoanContract loanContract : OwnFunctions.copyArrayList(household.loanContracts)) {
                    loanContract.endLoanContract();
                }
                household.deleteHousehold();
            }
        }

        if (siblings.size() > 0) {
            for (Individual sibling : siblings) {
                sibling.siblings.remove(this);
            }
        }

        if (parentHousehold != null) {
            parentHousehold.children.remove(this);
        }

       Model.individualsToRemoveFromMap.put(id,this);

    }

    public void deleteIndividual() {
        Model.individuals.remove(id);
        Model.nIndividuals--;
        Model.individualsForParallelComputing.get(id % Model.nThreads).remove(id);
    }

    public GeoLocation getPreferredGeoLocation() {

        GeoLocation preferredGeoLocation = household != null ? household.preferredGeoLocation : parentHousehold.preferredGeoLocation;
        return preferredGeoLocation;

    }

    void bequeath() {

        if (Model.simulationWithSingleGeoLocation && household.preferredGeoLocation != Model.singleGeoLocation) {
            deleteFlatsAndLoanContracts();
            return;
        }
        Individual inheritor;
        if (household.children.size() > 0) {
            inheritor = household.children.get(0);
        } else {
                OwnStopper stopperFindInheritor = new OwnStopper();
                inheritor = findInheritor();
                if (inheritor==null || inheritor==this) {
                    while (inheritor==null) {
                        int randomId = Model.rnd.nextInt(Model.individuals.size());
                    }
                    deleteFlatsAndLoanContracts();
                    return;
                }
        }


        if (household.home != null) {
            bequeathFlat(household.home,inheritor);
            household.home = null;
        }

        for (Flat flat : household.properties) {
            bequeathFlat(flat,inheritor);
        }
        if (household.homeUnderConstruction != null) {
            household.homeUnderConstruction.giveHomeUnderConstructionBackToConstructor();
        }
        household.properties = new ArrayList<>();
        if (inheritor != null) {
            if (inheritor.household != null) {
                inheritor.household.creditDeposit(household.deposit);
            } else {
                inheritor.parentHousehold.creditDeposit(household.deposit);
            }
            household.chargeDeposit(household.deposit);
        }


    }

    private void deleteFlatsAndLoanContracts() {
        for (LoanContract loanContract : OwnFunctions.copyArrayList(household.loanContracts)) {
            loanContract.endLoanContract();
        }
        if (household.home != null) {
            household.home.deleteFlat();
        }
        if (household.homeUnderConstruction != null) {
            household.homeUnderConstruction.giveHomeUnderConstructionBackToConstructor();
        }
        for (Flat flat : household.properties) {
            flat.deleteFlat();
        }
    }

    Individual findInheritor() {
        Individual inheritor = null;
        double smallestDistance = 1000000.0;
        while (inheritor==null) {
            int randomId = Model.rnd.nextInt(Model.individualsInArrayListAtBeginningOfPeriod.size());

            Individual potentialInheritor = Model.individuals.get(Model.individualsInArrayListAtBeginningOfPeriod.get(randomId).getId());
            if (potentialInheritor != null && potentialInheritor != this && Model.individualsToRemoveFromMap.get(potentialInheritor.getId())==null) {
                double distance = inheritorDistance(potentialInheritor);
                if (distance<smallestDistance) {
                    smallestDistance = distance;
                    inheritor = potentialInheritor;
                }
            }
        }

        return inheritor;
    }

    double inheritorDistance(Individual individual) {

        double distanceType = Math.abs(individual.typeIndex-this.typeIndex);
        double distanceFirstWageRatio = Math.max(individual.firstWage,this.firstWage) / Math.min(individual.firstWage,this.firstWage);
        if (firstWage < 1000) distanceFirstWageRatio = 0;

        GeoLocation preferredGeoLocationInheritor = individual.household != null ? individual.household.preferredGeoLocation : individual.parentHousehold.preferredGeoLocation;
        GeoLocation preferredGeoLocationBequeather = this.household != null ? this.household.preferredGeoLocation : this.parentHousehold.preferredGeoLocation;

        double distanceGeoLocation = preferredGeoLocationInheritor == preferredGeoLocationBequeather ? 1 : 0;

        return Model.inheritorDistanceTypeCoeff*distanceType + Model.inheritorDistanceFirstWageRatioCoeff * distanceFirstWageRatio + Model.inheritorDistancePreferredGeoLocationCoeff * distanceGeoLocation ;
    }

    void bequeathFlat(Flat flat, Individual individual) {

        Household inheritorHousehold = individual.getHousehold() != null ? individual.getHousehold() : individual.getParentHousehold();
        flat.ownerHousehold = inheritorHousehold;

        inheritorHousehold.properties.add(flat);
        if (flat.nPeriodsLeftForRent>1) flat.setNPeriodsLeftForRent(1);
        flat.setForSale(true);

        if (flat.loanContract != null) {
            LoanContract.changeDebtor(flat.loanContract,inheritorHousehold);

        }

    }

    void formOwnHousehold() {

        Household household = Model.createHousehold();
        this.household = household;
        household.members.add(this);
        household.letThisYoungOverTooYoungAgeRent = false;

        household.minDepositToPotentialWageIncome = parentHousehold.minDepositToPotentialWageIncome;
        household.preferredGeoLocation = parentHousehold.preferredGeoLocation;
        household.canChangeLocationAccordingToRegionalProbability();
        household.mayRenovateWhenBuying = parentHousehold.mayRenovateWhenBuying;
        household.canGetBridgeLoan = parentHousehold.canGetBridgeLoan;

        household.utilityFunctionCES = Model.createUtilityFunctionCES();
        household.utilityFunctionCES.setHousehold(household);
        parentHousehold.utilityFunctionCES.cloneParametersTo(household.utilityFunctionCES);
        household.ZOParbitrage = parentHousehold.ZOParbitrage;

        if (workExperience == 0) { //if data not properly set in DataLoader
            workExperience = Math.max(ageInPeriods - Model.ageInPeriodsForFirstWorkExperience[typeIndex],0);
            household.creditDeposit(calculateCumulatedSavingsForMissingWorkingYears());
        }

        if (parentHousehold != null) {

            if (parentHousehold.members.size()==0) {
                double depositInheritance = parentHousehold.deposit;
                parentHousehold.chargeDeposit(depositInheritance);
                household.creditDeposit(depositInheritance);
            } else if (parentHousehold.getHome()!= null && parentHousehold.getHome().getLoanContract() == null) {

                double inheritanceFromParents = parentHousehold.getDeposit() * Model.depositInheritanceRatio;

                parentHousehold.chargeDeposit(inheritanceFromParents);
                household.creditDeposit(inheritanceFromParents);

            }

        }

    }

    void marry() {

            Individual husband = MarriageWomenBucket.findHusband(this);

            if (husband != null) {
                uniteHouseholds(husband);
            }

    }


    void uniteHouseholds(Individual husband) {
        if (household == null) {
            formOwnHousehold();
        }

        household.letThisYoungOverTooYoungAgeRent = true;


        if (husband.household != null) {
            Household husbandHousehold = husband.household;
            if (husbandHousehold.firstBuyer==false) household.firstBuyer = false;
            household.deposit += husbandHousehold.deposit;
            if (husbandHousehold.home != null) {
                husband.bequeathFlat(husbandHousehold.home,this);
            }
            for (Flat flat : husbandHousehold.properties) {
                husband.bequeathFlat(flat,this);
            }
            if (husbandHousehold.loanContracts.size()>0) {
                ArrayList<LoanContract> husbandHouseholdLoanContracts = new ArrayList<>();
                for (LoanContract loanContract : husband.household.loanContracts) {
                    husbandHouseholdLoanContracts.add(loanContract);
                }
                for (LoanContract loanContract : husbandHouseholdLoanContracts) {
                    if (loanContract.isHousingLoan==false) {
                        LoanContract.changeDebtor(loanContract,household);
                    }
                }
            }


            if (husband.household.rentHome != null) {
                husband.household.rentHome.renter = null;
            }

            husbandHousehold.deleteHousehold();
        } else {
            husband.formOwnHousehold();
        }



        husband.household = household;
        household.members.add(husband);

    }

    void tryToGiveBirth() {
        if (isMale == true || household == null || household.members.size()<2 || household.nBirths >= Model.maxNChildren) return;

        if (Model.rnd.nextDouble() < Model.birthProbability[typeIndex][ageInPeriods][Math.min(household.nBirths,Model.maxNChildren)]) {
            giveBirth();
            int childrenNum = household.nChildrenForCSOK();
            double CSOK = Math.max(0,Model.usedCSOK[Math.min(Model.usedCSOK.length-1,childrenNum)]-household.utilizedCSOK);
            household.creditDeposit(CSOK);
            household.utilizedCSOK += CSOK;
            if (household.babyloan.isIssued) {
                household.babyloan.nChildrenSinceIssuance ++;
                household.babyloan.nPeriodsSinceChildBirth = 0;
                if (household.babyloan.nChildrenSinceIssuance == 1) {
                    household.babyloan.PTIPayment = Model.babyloanPayment;
                }
                if (household.babyloan.nChildrenSinceIssuance==2) {
                    household.babyloan.principal *= (1-Model.ratioOfDecreasePrincipalInCaseOfChildBirth);
                    household.babyloan.payment *= (1-Model.ratioOfDecreasePrincipalInCaseOfChildBirth);
                    household.babyloan.PTIPayment *= (1-Model.ratioOfDecreasePrincipalInCaseOfChildBirth);
                }
                if (household.babyloan.nChildrenSinceIssuance==3) {
                    household.babyloan.principal = 0;
                    household.babyloan.payment = 0;
                    household.babyloan.duration = 0;
                    household.babyloan.PTIPayment = 0;
                }
            }

        }

    }

    public void giveBirth() {
        Individual child = Model.createIndividual();
        household.children.add(child);
        household.nBirths++;
        child.ageInPeriods = 0;
        child.typeIndex = Math.min(this.typeIndex,household.members.get(1).typeIndex);
        child.parentHousehold = this.household;
        child.firstWage = Math.max(this.firstWage,household.members.get(1).firstWage);

        if (Model.rnd.nextDouble() < 0.5) {
            child.isMale = false;
        } else child.isMale = true;
        for (Individual sibling : household.children) {
            if (sibling != child) {
                child.siblings.add(sibling);
                sibling.siblings.add(child);
            }
        }
    }

    int calculateBirthYear() {
        return Model.zeroPeriodYear - (int) Math.floor((ageInPeriods-Model.period-Model.zeroPeriodMonth+1)/12);
    }

    double calculateCumulatedSavingsForMissingWorkingYears() {

        double cumulatedSavings = 0;
        for (int i = 0; workExperience - i >= 0 ; i++) {
            double actualWageIncome = Model.priceLevel * Model.realGDPLevel * firstWage * Model.wageRatio[typeIndex][workExperience-i];
            double savingsRate = household.calculateMinSavingsRate(actualWageIncome);
            cumulatedSavings += savingsRate * actualWageIncome;
        }
        return cumulatedSavings;
    }

    public void calculateAndSetBucketCodeForMan() {
        manBucketCode = -1;
        if (isMale && (household==null || (household.members.size()==1 && household.children.size()==0 && household.homeUnderConstruction==null))) {
            manBucketCode = MarriageWomenBucket.getCodeForMan(this);
        }
    }

    public void calculateAndSetWomanMarrying() {
        womanMarrying = false;
        if (isMale == true) return;
        if (household != null && household.children.size() != 0) return;
        if (household != null && household.members.size() == 2) return;

        double probabilityOfMarriage = Model.marriageProbability[typeIndex][ageInPeriods];

        if (Model.threadNextDouble(this)<probabilityOfMarriage) {
            womanMarrying = true;
        }
    }

    public int calculateAgeInYears() {
        return (int) Math.floor(ageInPeriods / 12.0);
    }

    public int ageGroupIndexForAffordability() {
        for (int i = 0; i < Model.affordabilityAgeIntervals.length/2; i++) {
            double ageInYears= Math.floor(ageInPeriods / 12.0);;
            if (ageInYears>=Model.affordabilityAgeIntervals[i*2] && ageInYears<=Model.affordabilityAgeIntervals[i*2]+1) {
                return i;
            }
        }
        return -1;
    }
    




}
