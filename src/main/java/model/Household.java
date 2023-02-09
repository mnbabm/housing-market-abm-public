package model;

import lombok.Getter;
import lombok.Setter;
import util.DataLoader;
import util.MiscUtils;
import util.OwnFunctions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Household implements HasID, Depositable {
    public static int nextId;
    final int id;

    //state variables

    public List<Individual> members = new ArrayList<>();
    public List<Individual> children = new ArrayList<>();
    public Flat home;
    Flat homeUnderConstruction;
    List<Flat> properties = new ArrayList<>();
    public ArrayList<LoanContract> loanContracts = new ArrayList<>();
    public Flat rentHome;

    boolean shouldNotRent = false; //if a household had been a homeOwner, it should not rent until purchasing the next home
    boolean letThisYoungOverTooYoungAgeRent = true;

    public double deposit;
    double minDepositToPotentialWageIncome;

    public UtilityFunctionCES utilityFunctionCES;
    GeoLocation preferredGeoLocation;
    boolean mayRenovateWhenBuying = false;
    boolean canGetBridgeLoan = true;
    boolean isMoving = false;
    public double utilizedCSOK;
    public int nBirths = 0;
    boolean firstBuyer = true;
    boolean canChangeGeoLocation = true;

    int nPeriodsWithMinConsumption;
    int periodOfTakingUnsoldHomeToMarket = -100;
    boolean canBeAskedForFictiveDemandForNewlyBuiltFlats = true;
    boolean considerForCAHAI = true;
    boolean consideredForCAHAIInThisPeriod = false;
    boolean considerCAHAICanBuyWithoutLoan = false;
    boolean considerCAHAICantBuyMisc = false;
    boolean considerCAHAICanBuyWithLoan = false;
    double considerCAHAILoanNeed;
    double considerCAHAIDSTILimit;
    double considerCAHAILTVLimit;
    double considerCAHAIConsumptionLimit;
    boolean firstTimeBuyerPurchaseInThisPeriod = false;
    int periodOfLastConsideration = -1;
    double valueOfPeerFlat = 0;
    double valueOfPeerFlatIfCantBuy = 0;
    int lastNonPerformingPeriod = -1000;

    int nPeriodsWaitingForFlatToBuy = 0;

    //derived variables

    double wageIncome;
    double householdIncome;
    double potentialWageIncome;
    double permanentIncome;
    double lifeTimeIncome;
    int size; //upDated:refreshSize()

    double minConsumptionLevel;
    double consumption;
    double sumPayment;
    double minDeposit;
    double rent;

    double minPrice;
    double maxPrice;
    double maxLoan;
    double maxLoanWithCSOK;
    double newlyBuiltCSOK;
    double falusiCSOK;
    double falusiCSOKRenovation;
    double usedCSOK;
    Babyloan babyloan = new Babyloan();
    boolean ZOParbitrage = false;

    double minRent;
    double maxRent;

    double homeOptimalRenovation;

    Flat fictiveFlatToBuy;
    Flat fictiveFlatToRent;
    double fictiveSurplus;
    double utilityOfFictiveFlatToBuy;
    Flat flatBiddedOn;
    ArrayList<Bid> bids = new ArrayList<>();
    ArrayList<Bid> potentialBids = new ArrayList<>();

    LoanContractOffer cachedLoanContractOffer;
    double totalLoan; //excluding bridgeLoan
    double totalPayment; //excluding bridgeLoan
    boolean hasNonPerformingLoan;
    boolean isInMoratory;

    boolean flatTooExpensiveToBuy;
    boolean flatToBuyTooFarFromOptimal;
    Flat chosenFlat;
    int rankNumber;

    int rankDecilesFrom1;
    double actualDSTI;

    boolean canGetBridgeLoanInPeriod;

    int nMonthsInterestPeriodIfAskingForLoan;

    //helper variables
    double downpaymentNeededForRentersInDataLoader;



    public Household() {
        id = Household.nextId;
        Household.nextId++;
    }

    public Household(int id) {
        this.id = id;
        if (Household.nextId < id + 1) Household.nextId = id + 1;
    }

    public void canChangeLocationAccordingToRegionalProbability() {
        double randomNumber = Model.threadNextDouble(this);

        if (randomNumber>Model.canChangeGeoLocationProbability[preferredGeoLocation.getId()]) {
            setCanChangeGeoLocation(false);
        }

    }



    double calculateMinSavingsRate(double wageIncome) {

        double minSavingsRate = Math.max(0, Model.savingsRateCoeff * Math.log(wageIncome  * 12 / Model.priceLevel) + Model.savingsRateConstant);
        return minSavingsRate;
    }

    void decidesOnMoving() {

        if (tooYoungToBuyOrRent()) {
            isMoving = false;
            return;
        }

        if (home == null && homeUnderConstruction == null) {
            isMoving = true;

            return;
        }
        if (homeUnderConstruction != null) {
            isMoving = false;
            return;
        }


        if (isMoving) {
            if (periodOfTakingUnsoldHomeToMarket >= 0 && home.isForcedSale == false && Model.period - periodOfTakingUnsoldHomeToMarket >= Model.nMonthsToWithdrawHomeFromMarket && (home.loanContract != null && home.loanContract.bridgeLoanCollateral == null)) {
                home.setForSale(false);
                home.setNForSalePeriods(0);
                periodOfTakingUnsoldHomeToMarket = -100;
                isMoving = false;
            }

            return;
        }


        if (members.get(0).ageInPeriods==Model.ageInYearsForMandatoryMoving*12 && hasUnemployedMember()==false) {

            if (Model.threadNextDouble(this)<Model.probabilityOfMandatoryMoving) {
                isMoving = true;
                return;
            }
        }


        double threadNextDouble = Model.threadNextDouble(this);
        double probabilityOfAssessingPotentialNewHomes = calculateProbabilityOfAssessingPotentialNewHomes();

        if (threadNextDouble < probabilityOfAssessingPotentialNewHomes) {
            ArrayList<Flat> forSale = Model.flatsForSaleInGeoLocations.get(preferredGeoLocation);
            Flat flatToBuy;

            if (Model.simulationWithSingleGeoLocation) {
                flatToBuy = chooseBestFlat(forSale, Model.nFlatsToLookAt);
            } else if (preferredGeoLocation == Model.capital) {
                ArrayList<Flat> forSaleAgglomeration = Model.flatsForSaleInGeoLocations.get(Model.agglomeration);
                flatToBuy = chooseBestFlatFromDoubleList(forSale, Model.nFlatsToLookAt, forSaleAgglomeration, Model.nFlatsToLookAt);
            } else {
                ArrayList<Flat> forSaleCapital = Model.flatsForSaleInGeoLocations.get(Model.capital);
                flatToBuy = chooseBestFlatFromDoubleList(forSale, Model.nFlatsToLookAt, forSaleCapital, Model.nFlatsToLookAt);

            }

                if (flatToBuy != null && canBuyFlat(flatToBuy)) {
                double homeMarketPrice = home.getMarketPrice();

                double thresholdPriceDifferenceForMoving = Model.thresholdPriceDifferenceForMoving;

                if ((Math.abs(flatToBuy.forSalePrice - homeMarketPrice) / homeMarketPrice > Model.thresholdRatioForMoving || Math.abs(flatToBuy.size - home.size) / home.size > Model.thresholdRatioForMoving  || Math.abs(flatToBuy.getNeighbourhood().getQuality() - home.getNeighbourhood().getQuality()) / home.getNeighbourhood().getQuality() > Model.thresholdRatioForMoving) && Math.abs(flatToBuy.forSalePrice - homeMarketPrice) > thresholdPriceDifferenceForMoving && canBuyFlat(flatToBuy)) {

                    Flat flatToBuyWithOptimalStateIncrease = new Flat(flatToBuy);
                    double optimalStateIncrease = calculateOptimalRenovation(flatToBuyWithOptimalStateIncrease);
                    flatToBuyWithOptimalStateIncrease.state += optimalStateIncrease;
                    flatToBuyWithOptimalStateIncrease.forSalePrice += flatToBuyWithOptimalStateIncrease.getPredictedRenovationCost(optimalStateIncrease);

                    if (utilityFunctionCES.calculateUtility(flatToBuyWithOptimalStateIncrease)<utilityFunctionCES.calculateUtility(home)*(1 + Model.thresholdRatioForMoving)) return;

                    isMoving = true;
                    canBeAskedForFictiveDemandForNewlyBuiltFlats = true;
                    considerForCAHAI = true;
                    periodOfTakingUnsoldHomeToMarket = Model.period;

                }

            }


        }

    }

    public double calculateProbabilityOfAssessingPotentialNewHomes() {
        if (hasUnemployedMember()) return 0;
        int ageInYears = calculateAgeInYearsOfOldestMember();
        double probability = 0;
        if (ageInYears <= Model.baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes) {
            probability = Model.probabilityOfAssessingPotentialNewHomes;
        } else {
            probability = Model.probabilityOfAssessingPotentialNewHomes * Math.pow(Model.yearlyDiscountForProbabilityOfAssessingPotentialNewHomes, ageInYears - Model.baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes);
        }

        double quality = home.getNeighbourhood().getQuality();
        if (quality < Model.lowQualityMax) {
            probability *= Model.lowQualityNeighbourhoodMovingProbabilityMultiplier;
        } else if (quality < Model.middleQualityMax) {
            probability *= Model.middleQualityNeighbourhoodMovingProbabilityMultiplier;
        } else {
            probability *= Model.topQualityNeighbourhoodMovingProbabilityMultiplier;
        }

        probability *= (1+home.bucket.nForSaleToNSoldProbabilityAdjustment);
        if (home != null && home.getGeoLocation()==Model.capital && Model.threadNextDouble(this)<1.0-Model.period/Model.firstNPeriodsForLinearlyDecreasingCapitalProbabilityMultiplier) probability *= Model.linearlyDecreasingCapitalProbabilityMultiplierInFirstNPeriods;
        return probability;
    }


    double distanceOfBestFlatFromHomeWithUtilityFunctionCES(UtilityFunctionCES utilityFunctionCES) {

        this.utilityFunctionCES.setAbsCoeffSize(utilityFunctionCES.absCoeffSize);
        this.utilityFunctionCES.setAbsExponentSize(utilityFunctionCES.absExponentSize);
        this.utilityFunctionCES.setAbsCoeffState(utilityFunctionCES.absCoeffState);
        this.utilityFunctionCES.setAbsExponentState(utilityFunctionCES.absExponentState);
        this.utilityFunctionCES.setAbsSigmoid1(utilityFunctionCES.absSigmoid1);
        this.utilityFunctionCES.setAbsSigmoid2(utilityFunctionCES.absSigmoid2);


        Flat bestFlat = getBestFlatForUtilityParametersInCalibration();

        double distance = distanceOfFlatInAdjustmentOfAbsoluteUtiliyParameters(bestFlat);
        return distance;
    }

    private Flat getBestFlatForUtilityParametersInCalibration() {
        ArrayList<Flat> forSalePreferredGeoLocation = Model.sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(preferredGeoLocation);
        ArrayList<Flat> forSaleSecondaryGeoLocation;
        if (preferredGeoLocation == Model.capital) {
            forSaleSecondaryGeoLocation = Model.sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(Model.agglomeration);
        } else {
            forSaleSecondaryGeoLocation = Model.sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(Model.capital);
        }


        double bestSurplus = -123456789;
        Flat bestFlat = null;
        for (Flat flat : forSalePreferredGeoLocation) {
            double surplus = this.utilityFunctionCES.calculateAbsoluteReservationPriceForFlat(flat) - flat.forSalePrice;
            if (surplus>bestSurplus) {
                bestSurplus = surplus;
                bestFlat = flat;
            }
        }
        for (Flat flat : forSaleSecondaryGeoLocation) {
            double surplus = this.utilityFunctionCES.calculateAbsoluteReservationPriceForFlat(flat) - flat.forSalePrice;
            if (surplus>bestSurplus) {
                bestSurplus = surplus;
                bestFlat = flat;
            }
        }

        return bestFlat;
    }

    double distanceOfFlatInAdjustmentOfAbsoluteUtiliyParameters(Flat flat) {
        Flat chosenFlat = home;

        if (home.getGeoLocation()!=flat.getGeoLocation()) return 1000;
        double sizeDistance = Math.abs(flat.size-chosenFlat.size)/chosenFlat.size;
        double stateDistance = Math.abs(flat.state-chosenFlat.state)/chosenFlat.state;
        double qualityDistance = Math.abs(flat.getQuality()-chosenFlat.getQuality())/chosenFlat.getQuality();
        double distance = sizeDistance + stateDistance + qualityDistance;
        return distance;
    }


    void getPaid() {
        deposit += householdIncome;
    }

    void consumeAndSave() {

        calculateSumPaymentBeforeMonthlyPayments();
        calculateConsumption();

        deposit -= consumption;

        if (sumPayment > 0) payMonthlyPayments();

    }

    void calculateConsumption() {
        consumption = 0;
        double minConsumptionRatio = potentialWageIncome * Model.minConsumptionRate; //minConsuption calculated according to ratio
        double minConsumption = Math.max(minConsumptionLevel, minConsumptionRatio);

        if (deposit < minConsumption) {
            if (minConsumptionLevel < minConsumptionRatio) {
                nPeriodsWithMinConsumption++;
                minConsumption = minConsumptionRatio - Math.min(nPeriodsWithMinConsumption, Model.nPeriodsUntilMinConsumption) / (double) Model.nPeriodsUntilMinConsumption * (minConsumptionRatio - minConsumptionLevel);

            }
        } else nPeriodsWithMinConsumption = 0;


        double minSavings = calculateMinSavingsRate(householdIncome) * householdIncome;
        if (householdIncome > minConsumption + minSavings + sumPayment + rent) {
            consumption = householdIncome - minSavings - sumPayment - rent;
        } else consumption = Math.min(minConsumption, deposit);



    }

    public double calculateMinConsumptionLevel() {
        double unitConsumption = getUnitConsumption();
        double units = getUnits();

        return units * unitConsumption;
    }

    private double getUnitConsumption() {
        double minConsumptionPerCapita = 0;
        double normalizedSumPotentialWagePerCapita = getSumPotentialWagePerCapita()/Model.priceLevel/Model.realGDPLevel;
        if (normalizedSumPotentialWagePerCapita < Model.minConsumptionPerCapitaLowerThreshold) {
            minConsumptionPerCapita = Model.minConsumptionPerCapitaLower;
        } else if (normalizedSumPotentialWagePerCapita > Model.minConsumptionPerCapitaUpperThreshold) {
            minConsumptionPerCapita = Model.minConsumptionPerCapitaUpper;
        } else {
            minConsumptionPerCapita = Model.minConsumptionPerCapitaLower + (normalizedSumPotentialWagePerCapita - Model.minConsumptionPerCapitaLowerThreshold) / (Model.minConsumptionPerCapitaUpperThreshold - Model.minConsumptionPerCapitaLowerThreshold) * (Model.minConsumptionPerCapitaUpper - Model.minConsumptionPerCapitaLower);
        }
        return Model.priceLevel * Model.realGDPLevel * minConsumptionPerCapita;
    }

    private double getSumPotentialWagePerCapita() {
        return getSumPotentialWage() / members.size();
    }

    private double getUnits() {
        double units = 1.0;

        if (members.size() == 2) units += Model.weightOfAdditionalAdultsInMinConsumption;
        for (Individual child : children) {
            if (child.getAgeInPeriods() < Model.minAgeInPeriodsToCountAsAdditionalAdultInMinConsumnption) {
                units += Model.weightOfChildrenInMinConsumption;
            } else units += Model.weightOfAdditionalAdultsInMinConsumption;
        }
        return units;
    }

    private void payMonthlyPayments() {

        for (LoanContract loanContract : OwnFunctions.copyArrayList(loanContracts)) {
            if (loanContract.issuedInThisPeriod==false) {
                loanContract.monthlyPayment();
            }
        }
        if (babyloan.isIssued && babyloan.duration>0) {
            if (babyloan.isSuspended()==false) {
                chargeDeposit(babyloan.payment);
                babyloan.duration--;
                babyloan.principal -= babyloan.payment;
                if (babyloan.principal <= 0) {
                    babyloan.principal = 0;
                    babyloan.payment = 0;
                    babyloan.PTIPayment = 0;
                }
            }   else {
                babyloan.nPeriodsSinceChildBirth ++;
            }
        }
    }

    public void nullMiscDerivedVariables() {
        homeOptimalRenovation = 0;
        chosenFlat = null;
        flatTooExpensiveToBuy = false;
        flatToBuyTooFarFromOptimal = false;
        fictiveFlatToBuy = null;
        fictiveFlatToRent = null;
        utilityOfFictiveFlatToBuy = 0;
        fictiveSurplus = 0;
        flatBiddedOn = null;
        consideredForCAHAIInThisPeriod = false;
        considerCAHAICanBuyWithoutLoan = false;
        considerCAHAICantBuyMisc = false;
        considerCAHAICanBuyWithLoan = false;
        considerCAHAILoanNeed = 0;
        considerCAHAIDSTILimit = 0;
        considerCAHAILTVLimit = 0;
        considerCAHAIConsumptionLimit = 0;
        firstTimeBuyerPurchaseInThisPeriod = false;

        rankDecilesFrom1 = 0;
        actualDSTI = 0;

        isInMoratory = false;

        if (Model.simulationWithSingleGeoLocation) canChangeGeoLocation=false;
    }

    void refreshSize() {
        size = members.size();
        for (Individual child : children) {
            if (child.household == null) size++;
        }
    }

    public void refreshWageIncomeAndPotentialWageIncomeAndPermanentIncomeAndLifeTimeIncome() {


        wageIncome = 0;
        potentialWageIncome = 0;
        permanentIncome = 0;

        for (int i = 0; i < members.size(); i++) {
            wageIncome += members.get(i).wage;
            potentialWageIncome += members.get(i).potentialWage;
            permanentIncome += Model.priceLevel * Model.realGDPLevel * (1 - Model.taxRate) * members.get(i).getFirstWage();
        }

        householdIncome = wageIncome + calculateFamilyBenefits();


            if (Model.isMoratoryPeriod && deposit<Model.nMinMonthsForSavingsConsideringMoratory*(potentialWageIncome + calculateFamilyBenefits())) isInMoratory = true;


        lifeTimeIncome = 0;
        for (Individual individual : members) {
            lifeTimeIncome += individual.lifeTimeIncome;
        }

    }

    public double calculateFamilyBenefits() {
        int nChildrenUnder18 = 0;
        for (Individual child : children) {
            if (Math.floor(child.getAgeInPeriods() / 12) <= 18) {
                nChildrenUnder18++;
            }
        }

        double familyBenefit = 0;
        if (nChildrenUnder18 < Model.newlyBuiltCSOK.length) {
            familyBenefit = Model.familyBenefit[nChildrenUnder18];
        } else
            familyBenefit = Model.familyBenefit[Model.newlyBuiltCSOK.length - 1] * nChildrenUnder18 / (double) (Model.newlyBuiltCSOK.length - 1);

        return familyBenefit;

    }

    public void deleteHousehold() {

        for (Individual child : children) {
            child.parentHousehold = null;
        }

        if (rentHome != null) {
            rentHome.setNPeriodsLeftForRent(0);
            rentHome.renter = null;
        }

        Model.households.remove(id);
        Model.nHouseholds--;
        Model.householdsForParallelComputing.get(id % Model.nThreads).remove(id);
        utilityFunctionCES.deleteUtilityFunctionCES();
    }

    public void setUtilityFunctionCES(UtilityFunctionCES utilityFunctionCES) {
        this.utilityFunctionCES = utilityFunctionCES;
        utilityFunctionCES.setHousehold(this);
    }

    private double calculateMinDeposit() {
        return getPotentialWageIncome() * minDepositToPotentialWageIncome;
    }

    public int nChildrenForCSOK() {
        int childrenNum = 0;
        for (Individual child : children) {
            if (child.ageInPeriods < Model.maxAgeInPeriodsForChildForCSOK && child.household == null) {
                childrenNum++;
            }

        }
        return childrenNum;
    }

    void calculateNewlyBuiltCSOK() {
        if (members.get(0).firstWage<Model.minFirstWageForCSOK && (members.size()==1 || members.get(1).firstWage<Model.minFirstWageForCSOK)) {
            newlyBuiltCSOK = 0;
            return;
        }

        int childrenNum= nChildrenForCSOK();

        if (Model.period>=0) {
            if (childrenNum==2 && id % 100 < Model.ratioOfHouseholdsWith2ChildrenEligibleFor3childrenCSOK*100) childrenNum=3;
            if (childrenNum==1 && id % 100 < Model.ratioOfHouseholdsWith1ChildEligibleFor3childrenCSOK*100) childrenNum=3;
        }

        if (childrenNum < Model.newlyBuiltCSOK.length) {
            newlyBuiltCSOK = Model.newlyBuiltCSOK[childrenNum];
        } else newlyBuiltCSOK = Model.newlyBuiltCSOK[Model.newlyBuiltCSOK.length - 1];

        newlyBuiltCSOK -= utilizedCSOK;
        if (newlyBuiltCSOK < 0) newlyBuiltCSOK = 0;

    }

    void calculateFalusiCSOK() {

        if (members.get(0).firstWage<Model.minFirstWageForCSOK && (members.size()==1 || members.get(1).firstWage<Model.minFirstWageForCSOK)) {
            falusiCSOK = 0;
            return;
        }

        int childrenNum= nChildrenForCSOK();

        if (Model.period>=0) {
            if (childrenNum==2 && id % 100 < Model.ratioOfHouseholdsWith2ChildrenEligibleFor3childrenFalusiCSOK*100) childrenNum=3;
            if (childrenNum==1 && id % 100 < Model.ratioOfHouseholdsWith1ChildEligibleFor3childrenCSOK*100) childrenNum=3;
        }

        if (childrenNum < Model.falusiCSOK.length) {
            falusiCSOK = Model.falusiCSOK[childrenNum];
        } else falusiCSOK = Model.falusiCSOK[Model.falusiCSOK.length - 1];
        falusiCSOKRenovation = falusiCSOK;

        if (utilizedCSOK>0) {
            falusiCSOK = Math.max(0,falusiCSOK-utilizedCSOK);
            falusiCSOKRenovation = Math.min(falusiCSOKRenovation,2*falusiCSOKRenovation - utilizedCSOK - falusiCSOK);
            if (falusiCSOKRenovation<0) falusiCSOKRenovation = 0;
        }


    }

    public void calculateAndSetCanGetBridgeLoanInPeriod() {
        canGetBridgeLoanInPeriod = false;
        if (canGetBridgeLoan && home != null && home.getEstimatedMarketPrice() > Model.minMarketPriceForBridgeLoan && home.calculateMarketPrice() / home.size > home.getGeoLocation().histRenovationUnitCost[Model.nHistoryPeriods + Model.period - 1]) {
            canGetBridgeLoanInPeriod = true;
        }

    }

    public void calculateVariablesForPurchase() {

        calculateMinMaxPrice();
        calculateMinDeposit();
        calculateNewlyBuiltCSOK();
        calculateFalusiCSOK();

        double randomNumberToUse = Model.threadNextDouble(this);
        if (randomNumberToUse<Model.loanOneYearFixationShare) {
            nMonthsInterestPeriodIfAskingForLoan = 12;
        } else if (randomNumberToUse<Model.loanOneYearFixationShare + Model.loanFiveYearFixationShare) {
            nMonthsInterestPeriodIfAskingForLoan = 60;
        } else {
            nMonthsInterestPeriodIfAskingForLoan = 120;
        }



    }

    public void calculateMinMaxPrice() {
        minPrice = Model.priceRatioMinRatio * Model.priceLevel * lifeTimeIncome;
        maxPrice = Model.priceRatioMaxRatio * Model.priceLevel * lifeTimeIncome;

    }

    void calculateVariablesForRent() {
        //if we modify the calculation of maxRent, we need to modify the following part in DataLoader in switchHomesToRent: if (flat.calculateMarketPrice()*Model.rentToPrice > householdPermanentIncome * Model.maxRentRatio * 0.7) continue;
        minRent = Model.minRentRatio * permanentIncome;
        maxRent = Model.maxRentRatio * permanentIncome;
    }

    void payRent() {
        rent = 0;
        double rentPayed = 0;
        if (rentHome != null) {
            rent = rentHome.rent;

            rentPayed = Math.min(deposit, rent);
            deposit -= rentPayed;

            if (rentHome.ownerHousehold != null) {
                rentHome.ownerHousehold.deposit += rent;
            }

        }


    }

    Flat chooseBestFlatFromBucketOfNewlyBuiltBestFlat(Flat flat) {

        if (flat != null && flat.isNewlyBuilt) {
            ArrayList<Flat> flats = Model.nonNewlyBuiltFlatsForSaleInBuckets.get(flat.getBucket());

            if (flats == null) return flat;
            int nChecks = Math.min(Model.maxNChecksFromBucketOfNewlyBuiltBestFlat, flats.size());
            Flat flat1 = chooseBestFlat(flats, nChecks);
            if (flat1 == null) return flat;
            int checkToSee = flats.size();
            Flat bestFlat = chooseBetweenTwoFlats(flat, flat1);
            if (bestFlat==null) {
                return flat;
            }

            return bestFlat;

        } else {
            return flat;
        }
    }

    public Flat chooseBestFlat(ArrayList<Flat> forSale, int flatsToLookAtNum) {

        Flat bestFlat;

        if (forSale.size() == 0) {
            return null;
        }


        if (minPrice > forSale.get(forSale.size() - 1).getForSalePrice() || forSale.get(0).getForSalePrice() > maxPrice) {
            return null;
        }


        int minPriceIndex;
        int maxPriceIndex;

        minPriceIndex = MiscUtils.FlatPriceSearchForIndex(forSale, minPrice);
        maxPriceIndex = MiscUtils.FlatPriceSearchForIndex(forSale, maxPrice) - 1; //according to this, maxPrice is exclusive;

        if (maxPriceIndex == -1) return null;

        int bestIndex = -1;
        double bestUtility = 0;

        if (minPriceIndex == maxPriceIndex) {
            Flat flat = forSale.get(minPriceIndex);
            if (flat.boughtNow) return null;
            return forSale.get(minPriceIndex);
        }

        if (maxPriceIndex-minPriceIndex>Math.max(Model.minNFlatsToLookAtToShiftFlatIndices,flatsToLookAtNum)) {
            minPriceIndex += id % Model.maxShiftInFlatIndicesInChooseBestFlat;
        }


        for (int i = 0; i < flatsToLookAtNum; i++) {
            int toLookAtIndex = minPriceIndex
                    + (int) Math.floor((maxPriceIndex - minPriceIndex + 1.0) / flatsToLookAtNum * i);

            Flat toLookAtFlat = forSale.get(toLookAtIndex);

            int maxToLookAtIndex = minPriceIndex
                    + (int) Math.floor((maxPriceIndex - minPriceIndex + 1.0) / flatsToLookAtNum * (i + 1));
            while (toLookAtFlat.boughtNow && toLookAtIndex < maxToLookAtIndex - 1) {
                toLookAtIndex++;
                toLookAtFlat = forSale.get(toLookAtIndex);
            }

            if (toLookAtFlat.boughtNow) continue;
            if (toLookAtFlat.nPeriodsLeftForConstruction > 0 && members.get(0).ageInPeriods >= Model.lifespan - Model.nPeriodsForConstruction - 2)
                continue;

            double newUtility = utilityFunctionCES.calculateUtility(toLookAtFlat);

            if (newUtility > bestUtility) {
                bestIndex = toLookAtIndex;
                bestUtility = newUtility;
            }

            if (mayRenovateWhenBuying) {
                double optimalRenovation = calculateOptimalRenovation(toLookAtFlat);
                if (optimalRenovation > 0) {
                    Flat fictiveFlat = new Flat(toLookAtFlat);
                    fictiveFlat.state += optimalRenovation;
                    fictiveFlat.forSalePrice += fictiveFlat.getSize() * optimalRenovation * fictiveFlat.getGeoLocation().predictedRenovationUnitCost * (1 + Model.renovationCostBuffer);

                    double optimalRenovationUtility = utilityFunctionCES.calculateUtility(fictiveFlat);
                    if (optimalRenovationUtility > bestUtility) {
                        bestIndex = toLookAtIndex;
                        bestUtility = optimalRenovationUtility;

                    }
                }
            }


        }



        if (bestIndex >= 0) {

            bestFlat = forSale.get(bestIndex);
            if (bestFlat.isNewlyBuilt && Model.phase == Model.Phase.HOUSEHOLDPURCHASES && forSale.size() > flatsToLookAtNum) {
                bestFlat = chooseBestFlatFromBucketOfNewlyBuiltBestFlat(bestFlat);
            }

            return bestFlat;

        } else return null;


    }


    Flat chooseBestFlatToRent(ArrayList<Flat> forRent, int flatsToLookAtNum) {

        Flat bestFlat = null;

        if (forRent.size() == 0) {
            return null;
        }

        if (minRent > forRent.get(forRent.size() - 1).getRent()) return null;


        int minPriceIndex;
        int maxPriceIndex;

        minPriceIndex = MiscUtils.FlatRentSearchForIndex(forRent, minRent);
        maxPriceIndex = MiscUtils.FlatRentSearchForIndex(forRent, maxRent) - 1;


        if (maxPriceIndex == -1) {
            return null;
        }

        int bestIndex = -1;
        double bestUtility = 0;

        if (minPriceIndex == maxPriceIndex) {
            Flat flat = forRent.get(minPriceIndex);
            if (flat.boughtNow) {
                return null;
            }
            if (Model.phase == Model.Phase.RENTALMARKET && flat.nPeriodsLeftForRent > 0) {
                return null;
            }
            return forRent.get(minPriceIndex);
        }

        if (maxPriceIndex-minPriceIndex>Math.max(Model.minNFlatsToLookAtToShiftFlatIndices,flatsToLookAtNum)) {
            minPriceIndex += id % Model.maxShiftInFlatIndicesInChooseBestFlat;
        }

        for (int i = 0; i < flatsToLookAtNum; i++) {
            int toLookAtIndex = minPriceIndex
                    + (int) Math.floor((maxPriceIndex - minPriceIndex + 1.0) / flatsToLookAtNum * i);

            Flat toLookAtFlat = forRent.get(toLookAtIndex);


            if (Model.phase == Model.Phase.RENTALMARKET) {
                int maxToLookAtIndex = minPriceIndex
                        + (int) Math.floor((maxPriceIndex - minPriceIndex + 1.0) / flatsToLookAtNum * (i + 1));
                while (toLookAtFlat.renter != null && toLookAtIndex < maxToLookAtIndex - 1) {
                    toLookAtIndex++;
                    toLookAtFlat = forRent.get(toLookAtIndex);
                }
                if (toLookAtFlat.nPeriodsLeftForRent > 0) continue;
            }

            double newUtility = utilityFunctionCES.calculateUtility(toLookAtFlat);


            if (newUtility > bestUtility) {
                bestIndex = toLookAtIndex;
                bestUtility = newUtility;
            }

        }


        if (bestIndex >= 0) {
            bestFlat = forRent.get(bestIndex);
            return bestFlat;
        } else {
            if (Model.phase==Model.Phase.RENTALMARKET && preferredGeoLocation.getId()==forRent.get(minPriceIndex).getGeoLocation().getId()) {

                if (fictiveFlatToRent!=null) {
                    Flat secondaryRent = null;
                    for (Flat flat : forRent) {
                        if (flat.renter == null && flat.nPeriodsLeftForRent==0) {
                            secondaryRent = flat;
                            return secondaryRent;
                        }
                    }
                }

            }
            return null;
        }


    }

    double discountFactorForFlatUnderConstruction(Flat flat) {
        return 1 - flat.nPeriodsLeftForConstruction * Model.maxYearlyInterestRate / 12;
    }

    public void replaceFlatStateAndForSalePriceAccordingToOptimalRenovation(Flat flat) {
        double optimalRenovation = calculateOptimalRenovation(flat);
        flat.state += optimalRenovation;
        flat.forSalePrice += flat.getPredictedRenovationCost(optimalRenovation);
    }

    public double calculateOptimalRenovation(Flat flat) {

        double optimalRenovation = 0;

        double bestUtility = flat.forSalePrice;

        Flat fictiveFlat = new Flat(flat);

        while (fictiveFlat.state + Model.renovationStateIncreaseWhenBuying <= Model.highQualityStateMin) {
            fictiveFlat.state += Model.renovationStateIncreaseWhenBuying;
            double predictedRenovationCost = flat.getPredictedRenovationCost(Model.renovationStateIncreaseWhenBuying);
            if (flat.boughtNow && flat.isEligibleForFalusiCSOK()) {
                predictedRenovationCost = Math.max(0,predictedRenovationCost-falusiCSOKRenovation);
            }
            fictiveFlat.forSalePrice += predictedRenovationCost ;
            if (fictiveFlat.forSalePrice > maxPrice) break;

            double newUtility = utilityFunctionCES.calculateUtility(fictiveFlat);

            if (newUtility > bestUtility) {
                bestUtility = newUtility;
                optimalRenovation = fictiveFlat.getState() - flat.getState();
            }

        }

        return optimalRenovation;
    }

    public void renovationDemand() {
        cachedLoanContractOffer = null;
        if (home != null && homeUnderConstruction == null && deposit > minDeposit && (home.boughtNow || Model.rnd.nextDouble() < Model.renovationProbability)) {

            double optimalRenovation = calculateOptimalRenovation(home);
            double renovationCost = optimalRenovation * home.size * home.getGeoLocation().renovationUnitCost;
            if (home.boughtNow && home.isEligibleForFalusiCSOK()) {
                renovationCost = Math.max(0,renovationCost-falusiCSOKRenovation);
            }



            double loanNeed = renovationCost - (deposit - minDeposit);
            if (loanNeed > 0) {

                if (home.loanContract == null || (home.loanContract != null && home.loanContract.bank.increaseLoanForRenovation(home.loanContract, loanNeed) == false)) {
                    while (home.loanContract == null && getBestLoanContractOffer(home, loanNeed) == null) {
                        double loanDecrease = loanNeed*0.1;
                        loanNeed -= loanDecrease;
                        optimalRenovation -= loanDecrease/(size*home.getGeoLocation().renovationUnitCost);
                        if (loanDecrease<1000) {
                            optimalRenovation -= 1001/(size*home.getGeoLocation().renovationUnitCost);
                            return;
                        }
                    }
                }



            }
            setHomeOptimalRenovation(optimalRenovation);

            Model.renovationDemand += optimalRenovation * home.getSize();
        }
    }

    public Flat chooseBestFlatFromDoubleList(ArrayList<Flat> forSale0, int flatsToLookAtNum0, ArrayList<Flat> forSale1, int flatsToLookAtNum1) { //megvehető lakásokra vonatkozik

        Flat flatToBuy0 = chooseBestFlat(forSale0, flatsToLookAtNum0);
        Flat flatToBuy1 = chooseBestFlat(forSale1, flatsToLookAtNum1);

        return chooseBetweenTwoFlats(flatToBuy0, flatToBuy1);

    }

    public Flat chooseBetweenTwoFlats(Flat flatToBuy0, Flat flatToBuy1) {

        ArrayList<Flat> listOfBestFlats = new ArrayList<>();

        if (flatToBuy0 != null) listOfBestFlats.add(flatToBuy0);
        if (flatToBuy1 != null) listOfBestFlats.add(flatToBuy1);
        if (listOfBestFlats.size() == 0) {
            return null;
        } else if (listOfBestFlats.size() == 1) {
            return listOfBestFlats.get(0);
        } else if (flatToBuy0.forSalePrice > flatToBuy1.forSalePrice) {
            listOfBestFlats.remove(0);
            listOfBestFlats.add(flatToBuy0);
        }
        return chooseBestFlat(listOfBestFlats, listOfBestFlats.size());
    }

    public Flat chooseBestFlatToRentFromDoubleList(ArrayList<Flat> forRent0, int flatsToLookAtNum0, ArrayList<Flat> forRent1, int flatsToLookAtNum1) {
        Flat flatToRent0 = chooseBestFlatToRent(forRent0, flatsToLookAtNum0);
        Flat flatToRent1 = chooseBestFlatToRent(forRent1, flatsToLookAtNum1);
        return chooseBetweenTwoFlatsToRent(flatToRent0, flatToRent1);

    }

    private Flat chooseBetweenTwoFlatsToRent(Flat flatToRent0, Flat flatToRent1) {

        ArrayList<Flat> listOfBestFlatsToRent = new ArrayList<>();
        if (flatToRent0 != null) listOfBestFlatsToRent.add(flatToRent0);
        if (flatToRent1 != null) listOfBestFlatsToRent.add(flatToRent1);

        if (listOfBestFlatsToRent.size() == 0) {
            return null;
        } else if (listOfBestFlatsToRent.size() == 1) {
            return listOfBestFlatsToRent.get(0);
        } else if (flatToRent0.rent > flatToRent1.rent) {
            listOfBestFlatsToRent.remove(0);
            listOfBestFlatsToRent.add(flatToRent0);
        }

        return chooseBestFlatToRent(listOfBestFlatsToRent, listOfBestFlatsToRent.size());
    }


    public Flat selectHome() {

        ArrayList<Flat> forSale = Model.flatsForSaleInGeoLocations.get(preferredGeoLocation);
        Flat flatToBuy;

        if (canChangeGeoLocation) {
            if (Model.simulationWithSingleGeoLocation) {
                flatToBuy = chooseBestFlat(forSale,Model.nFlatsToLookAt);
            } else if (preferredGeoLocation == Model.capital) {
                ArrayList<Flat> forSaleAgglomeration = Model.flatsForSaleInGeoLocations.get(Model.agglomeration);
                flatToBuy = chooseBestFlatFromDoubleList(forSale, Model.nFlatsToLookAt, forSaleAgglomeration, Model.nFlatsToLookAt);
            } else {

                ArrayList<Flat> forSaleCapital = Model.flatsForSaleInGeoLocations.get(Model.capital);
                flatToBuy = chooseBestFlatFromDoubleList(forSale, Model.nFlatsToLookAt, forSaleCapital, Model.nFlatsToLookAt);

            }
        } else {
            flatToBuy = chooseBestFlat(forSale, Model.nFlatsToLookAt);
        }

        return flatToBuy;
    }

    boolean canBuyFlat(Flat flat) {

        if (Model.phase != Model.Phase.BEGINNINGOFPERIOD && Model.period - periodOfTakingUnsoldHomeToMarket < Model.minNMonthsOfOwnHomeOnMarketToBuyFlat) return false;

        if (deposit < minDeposit) {
            return false;
        }


        if (depositAvailableForFlat(flat) > flat.forSalePrice) {
            return true;
        } else {
            double loanNeed = flat.forSalePrice - depositAvailableForFlat(flat);

            if (loanNeed < 0) {
                return true;
            }

            if (getBestLoanContractOffer(flat, loanNeed) != null) {
                return true;
            } else {

            }

        }

        return false;
    }

    public Flat selectWithFictiveSupply() {

        Flat flatToBuy0 = selectHome();

        Flat flatToBuy1 = chooseBestFlat(Model.fictiveNewlyBuiltFlatsForSale, Model.fictiveNewlyBuiltFlatsForSale.size());
        Flat flatToBuy = chooseBetweenTwoFlats(flatToBuy0, flatToBuy1);
        if (flatToBuy != null && flatToBuy.isNewlyBuilt) {
            flatToBuy = chooseBestFlatFromBucketOfNewlyBuiltBestFlat(flatToBuy);
        }


        if (flatToBuy == null) {
            flatTooExpensiveToBuy = true;
            return null;
        }
        if (canBuyFlat(flatToBuy) == false) {
            flatTooExpensiveToBuy = true;
        }

        return flatToBuy;
    }

    public Flat selectRent() {
        ArrayList<Flat> forRent = Model.flatsForRentInGeoLocations.get(preferredGeoLocation);
        Flat flatToRent;

        if (preferredGeoLocation == Model.capital) {
            flatToRent = chooseBestFlatToRent(forRent, Model.nFlatsToLookAt);
        } else {
            ArrayList<Flat> forRentCapital = Model.flatsForRentInGeoLocations.get(Model.capital);
            flatToRent = chooseBestFlatToRentFromDoubleList(forRent, Model.nFlatsToLookAt, forRentCapital, Model.nFlatsToLookAt);
        }

        return flatToRent;
    }

    public Flat selectFictiveRent() {
        ArrayList<Flat> forRent = Model.fictiveFlatsForRentInGeoLocations.get(preferredGeoLocation);
        Flat flatToRent;

        if (preferredGeoLocation == Model.capital) {
            flatToRent = chooseBestFlatToRent(forRent, Model.nFlatsToLookAt);
        } else {
            ArrayList<Flat> forRentCapital = Model.fictiveFlatsForRentInGeoLocations.get(Model.capital);
            flatToRent = chooseBestFlatToRentFromDoubleList(forRent, Model.nFlatsToLookAt, forRentCapital, Model.nFlatsToLookAt);
        }
        fictiveFlatToRent = flatToRent;

        return flatToRent;
    }

    public void placeBidsOnFlats() {

        potentialBids.clear();

        ArrayList<Flat> forSale = Model.flatsForSaleInGeoLocations.get(preferredGeoLocation);
        placeBidsOnFlatsOfForSaleArray(forSale);


        if (canChangeGeoLocation && !Model.simulationWithSingleGeoLocation) {
            if (preferredGeoLocation == Model.capital) {
                ArrayList<Flat> forSaleAgglomeration = Model.flatsForSaleInGeoLocations.get(Model.agglomeration);
                placeBidsOnFlatsOfForSaleArray(forSaleAgglomeration);
            } else {
                ArrayList<Flat> forSaleCapital = Model.flatsForSaleInGeoLocations.get(Model.capital);
                placeBidsOnFlatsOfForSaleArray(forSaleCapital);
            }
        }

        Collections.sort(potentialBids,Bid.comparatorSurplusDecrease);

        for (int i = 0; i < potentialBids.size(); i++) {
            if (i==Model.maxNBidsPlacedPerHousehold) break;

            Bid bid = potentialBids.get(i);
            bid.flat.accountBidOnFlat(bid);
            bids.add(bid);

        }

    }

    public void placeBidsOnFlatsOfForSaleArray(ArrayList<Flat> forSale) {

        int nFlatsToLookAt = Model.nFlatsToLookAt;
        if (Model.phase == Model.Phase.SETUP) nFlatsToLookAt = forSale.size();
        if (fictiveFlatToBuy!=null && fictiveFlatToBuy.isNewlyBuilt) nFlatsToLookAt = forSale.size();
        int firstIndex = 0;
        if (nFlatsToLookAt<forSale.size()) {
            firstIndex = (int) Math.round(forSale.size() / (double) nFlatsToLookAt * Model.threadNextDouble(this));
        }
        for (int i = 0; i < nFlatsToLookAt; i++) {

            int toLookAtIndex = firstIndex + (int) Math.floor((forSale.size() - firstIndex) / (double) nFlatsToLookAt * i);
            if (toLookAtIndex == firstIndex + (int) Math.floor((forSale.size() - firstIndex) / (double) nFlatsToLookAt * (i-1))) continue;
            Flat flat = forSale.get(toLookAtIndex);

            if (flat.boughtNow) continue;
            if (flat.nPeriodsLeftForConstruction > 0 && members.get(0).ageInPeriods >= Model.lifespan - Model.nPeriodsForConstruction - 2)
                continue;

            double reservationPrice = calculateBidBasedOnSurplusConsideringProbability(flat);

            if (reservationPrice > flat.reservationPrice) {

                if (reservationPrice>depositAvailableForFlat(flat)) {
                    LoanContractOffer loanContractOffer = getBestLoanContractOffer(flat,1);

                    if (loanContractOffer != null) {
                        reservationPrice = Math.min(reservationPrice,loanContractOffer.maxLoan + depositAvailableForFlat(flat));
                    } else {
                        reservationPrice = 0;
                    }

                }

                reservationPrice -= 1.0/id; //so that two reservation prices would not be the same
                if (reservationPrice > flat.reservationPrice) {

                    Flat cloneFlatToRewriteForSalePriceToReservationPrice = new Flat(flat);
                    cloneFlatToRewriteForSalePriceToReservationPrice.forSalePrice = reservationPrice;
                    if (canBuyFlat(cloneFlatToRewriteForSalePriceToReservationPrice)) {

                        Bid bid = new Bid(reservationPrice,this, flat);
                        potentialBids.add(bid);

                    }

                }
            }


        }

    }

    public double calculateBidBasedOnSurplusConsideringProbability(Flat flat) {
        double reservationPrice = utilityFunctionCES.calculateAbsoluteReservationPriceForFlat(flat);

        reservationPrice = adjustReservationPriceAsAFunctionOfNPeriodsWaitingForFlatToBuy(reservationPrice, flat);

        boolean reservationBid = false;
        if (reservationBid) {
            return reservationPrice - fictiveSurplus;
        }

        double surplus = reservationPrice - flat.forSalePrice;
        if (mayRenovateWhenBuying) {

            Flat fictiveFlat = new Flat(flat);
            while (fictiveFlat.state + Model.renovationStateIncreaseWhenBuying <= Model.highQualityStateMin) {
                fictiveFlat.state += Model.renovationStateIncreaseWhenBuying;
                double costOfRenovation = flat.getPredictedRenovationCost(fictiveFlat.state - flat.state);
                if (flat.isEligibleForFalusiCSOK()) {
                    costOfRenovation -= falusiCSOKRenovation;
                    costOfRenovation = Math.max(0,costOfRenovation);
                }
                double absoluteReservationPriceForFlat = utilityFunctionCES.calculateAbsoluteReservationPriceForFlat(fictiveFlat);
                absoluteReservationPriceForFlat = adjustReservationPriceAsAFunctionOfNPeriodsWaitingForFlatToBuy(absoluteReservationPriceForFlat, flat);
                double newSurplus = absoluteReservationPriceForFlat - costOfRenovation - flat.forSalePrice;
                if (newSurplus > surplus) {
                    surplus = newSurplus;
                    reservationPrice = absoluteReservationPriceForFlat - costOfRenovation;
                }

            }
        }

        double probabilityOfPlacingBid = (Model.adjusterProbabilityOfPlacingBid * nPeriodsWaitingForFlatToBuy + 1) * 0.75 * surplus/fictiveSurplus;

        if (Model.threadNextDouble(this) < probabilityOfPlacingBid) {
            return Math.min(Model.reservationPriceShare * reservationPrice + (1 - Model.reservationPriceShare) * flat.forSalePrice,Model.maxForSaleMultiplier*flat.forSalePrice);
        } else return 0;
    }

    public double adjustReservationPriceAsAFunctionOfNPeriodsWaitingForFlatToBuy(double absoluteReservationPrice, Flat flat) {
        if (nPeriodsWaitingForFlatToBuy>1 && absoluteReservationPrice>flat.forSalePrice) {
            double actualSurplus = absoluteReservationPrice - flat.forSalePrice;
            absoluteReservationPrice += (fictiveSurplus - actualSurplus) * Math.min(Model.coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy * Math.pow(nPeriodsWaitingForFlatToBuy, Model.powerInAdjustmentInReservationPrice), Model.maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat);
        }
        return absoluteReservationPrice;
    }


    public void renovateFlat(Flat flat) {

        if (flat == home && homeOptimalRenovation == 0) return;
        double renovation = 0;
        if (flat == home) {
            renovation = homeOptimalRenovation;
        } else renovation = flat.investmentStateIncrease;


        double renovationCost = flat.getGeoLocation().renovationUnitCost * flat.size * renovation;

        if (flat.boughtNow && flat.isEligibleForFalusiCSOK()) {
            double CSOKTransfer = Math.min(renovationCost,falusiCSOKRenovation);
            utilizedCSOK += CSOKTransfer;
            creditDeposit(CSOKTransfer);

        }

        double renovationLoanNeed = renovationCost - (deposit - minDeposit);

        if (flat.isForRent && flat.investmentStateIncrease <= Model.stateDepreciation && renovationLoanNeed > 0) {
            //stateDepreciation is always offset as if the rental price contained the depreciation
            creditDeposit(renovationLoanNeed);
            renovationLoanNeed = 0;
        }


        if (renovationLoanNeed > 0) {

            if (flat.loanContract != null && flat.loanContract.bank.increaseLoanForRenovation(flat.loanContract, renovationLoanNeed)) {

            } else {

                LoanContractOffer loanContractOffer = getBestLoanContractOffer(flat, renovationLoanNeed);
                if (loanContractOffer == null) {
                    return;
                } else {
                    LoanContract loanContract = Model.createLoanContract();
                    Bank bank = loanContractOffer.getBank();

                    loanContract.setPrincipal(loanContractOffer.getPrincipal());
                    loanContract.setRenovationPrincipal(loanContractOffer.getPrincipal());
                    loanContract.setDuration(loanContractOffer.getDuration());
                    loanContract.setMonthlyInterestRate(loanContractOffer.getMonthlyInterestRate());
                    loanContract.setPayment(loanContractOffer.getPayment());

                    loanContract.setNMonthsInterestPeriod(loanContractOffer.nMonthsInterestPeriod);
                    loanContract.setPeriodOfLastInterestCalculation(loanContractOffer.periodOfLastInterestCalculation);
                    loanContract.setYearlyInterestSpread(loanContractOffer.yearlyInterestSpread);
                    loanContract.setYearlyBaseRateAtLastInterestCalculation(loanContractOffer.yearlyBaseRateAtLastInterestCalculation);
                    loanContract.setPeriodOfIssuance(Model.period);
                    loanContract.setDebtor(this);
                    loanContract.setBank(bank);

                    loanContract.setIssuedInThisPeriod(true);
                    loanContract.setHousingLoan(false);
                    bank.getLoanContracts().put(loanContract.getId(), loanContract);

                    this.creditDeposit(loanContract.getPrincipal());
                    loanContracts.add(loanContract);

                }
            }
        }

        chargeDeposit(renovationCost);
        Model.renovationQuantity += flat.size * renovation;

        flat.setState(flat.state + renovation);
        flat.getGeoLocation().renovationQuantity += flat.size * renovation;

    }

    public void makeLoanContractForFlat(Flat flat) {

        double loanNeed = Math.max(0,flat.forSalePrice - (deposit - minDeposit));
        double maxLoanLTV = flat.forSalePrice*Model.banks.get(0).LTVForHousehold(this,flat)-100;
        if (loanNeed>maxLoanLTV) {
            //according to prior checks in loanContractOffer, the household should be eligible for enough loan but in some cases a slight increase in prices might occur and we handle this problem by granting that marginal liquidity need
            double depositIncrease = loanNeed - maxLoanLTV;
            loanNeed -= depositIncrease;
            creditDeposit(depositIncrease);
        }
        if (Model.banks.get(0).flatEligibleForZOP(flat) && Model.phase==Model.Phase.HOUSEHOLDPURCHASES){
            double maxLoan = maxZOPLoanForFlat(flat, this);
            if (maxLoan>loanNeed) {
                loanNeed = maxLoan;
            }
        }
        LoanContractOffer loanContractOffer = getBestLoanContractOffer(flat, loanNeed);

        //another way to handle unexpected liquidity issues:
        int maxIterHere = 5000;
        int nIterHere = 0;
        while ((loanContractOffer==null || loanContractOffer.invalid) && nIterHere < maxIterHere) {
            nIterHere++;
            if (nIterHere == maxIterHere) {
                deposit = flat.forSalePrice * 1.1;
            }
            double depositIncrease = 100000;
            creditDeposit(depositIncrease);
            loanNeed -= depositIncrease;
            if (loanNeed < 0) loanNeed = 0;
            loanContractOffer = getBestLoanContractOffer(flat, loanNeed);

        }

        if (loanContractOffer == null) {
            loanContractOffer = new LoanContractOffer();
            loanContractOffer.bank = Model.banks.get(0);

            loanContractOffer.principal = 0;
            loanContractOffer.duration = 250;
            loanContractOffer.monthlyInterestRate = 0.002;
            loanContractOffer.payment = 1;
        }

        Bank bank = loanContractOffer.getBank();
        LoanContract loanContract = Model.createLoanContract();

        loanContracts.add(loanContract);
        creditDeposit(loanContractOffer.getPrincipal());

        if (loanContractOffer.getBridgeLoanPrincipal() > 0) {
            loanContract.setBridgeLoanPrincipal(loanContractOffer.getBridgeLoanPrincipal());
            loanContract.setBridgeLoanPrincipalOriginal(loanContractOffer.getBridgeLoanPrincipal());
            loanContract.setBridgeLoanDuration(loanContractOffer.getBridgeLoanDuration());
            loanContract.setBridgeLoanMonthlyInterestRate(loanContractOffer.getBridgeLoanMonthlyInterestRate());

            creditDeposit(loanContract.getBridgeLoanPrincipal());
            if (home.loanContract != null) {
                home.loanContract.endLoanContract();
            }

            loanContract.setBridgeLoanCollateral(home);
            home.setLoanContract(loanContract);
        }


        loanContract.setPrincipal(loanContractOffer.getPrincipal());
        loanContract.setDuration(loanContractOffer.getDuration());
        loanContract.setMonthlyInterestRate(loanContractOffer.getMonthlyInterestRate());
        loanContract.setPayment(loanContractOffer.getPayment());

        loanContract.setNMonthsInterestPeriod(loanContractOffer.nMonthsInterestPeriod);
        loanContract.setPeriodOfLastInterestCalculation(loanContractOffer.periodOfLastInterestCalculation);
        loanContract.setYearlyInterestSpread(loanContractOffer.yearlyInterestSpread);
        loanContract.setYearlyBaseRateAtLastInterestCalculation(loanContractOffer.yearlyBaseRateAtLastInterestCalculation);

        loanContract.setCollateral(flat);
        loanContract.setDebtor(this);
        loanContract.setBank(bank);


        loanContract.setIssuedInThisPeriod(true);
        loanContract.setPeriodOfIssuance(Model.period);
        flat.setLoanContract(loanContract);
        bank.getLoanContracts().put(loanContract.getId(), loanContract);
        bank.loanTotal += loanContract.getPrincipal();


    }

    public LoanContractOffer getBestLoanContractOffer(Flat flat, double loan) {

        LoanContractOffer bestLoanContractOffer = null;
        double lowestInterestRate = 10000;
        for (Bank bank : Model.banks.values()) {
            LoanContractOffer loanContractOffer = bank.loanContractOffer(this, flat, loan);
            if (loanContractOffer.invalid==false && loanContractOffer.monthlyInterestRate < lowestInterestRate) {
                lowestInterestRate = loanContractOffer.monthlyInterestRate;
                bestLoanContractOffer = loanContractOffer;
                cachedLoanContractOffer = loanContractOffer;
            }
        }


        return bestLoanContractOffer;

    }

    public void calculateSumPaymentBeforeMonthlyPayments() {
        sumPayment = 0;
        if (isInMoratory == false) {
            for (LoanContract loanContract : loanContracts) {
                if (loanContract.issuedInThisPeriod==false) {
                    sumPayment += loanContract.payment;
                }
            }
            if (babyloan.isIssued && babyloan.isSuspended()==false) {
                sumPayment += babyloan.payment;
            }
        }
        actualDSTI = sumPayment/getPotentialWageIncome();

    }

    public double calculateInvestmentProbabilityAccordingToExpectedReturnSpread(double expectedReturnSpread) {
        if (expectedReturnSpread < 0) return 0;
        double investmentProbability = Math.min(Model.maxHouseholdInvestmentProbability, Model.householdInvestmentProbabilityCoeff * Math.pow(expectedReturnSpread, Model.householdInvestmentProbabilityPower));
        if (Model.period==0) investmentProbability *= 0.1;
        return investmentProbability;
    }

    public void investIfPossible() {
        if (home == null || deposit < Math.max(minDeposit, Model.minDepositToInvest)) return;

        double nextDoubleForNeighbourhood = Model.rnd.nextDouble();
        Neighbourhood neighbourhood = Model.neighbourhoodInvestmentProbabilities.selectObjectAccordingToCumulativeProbability(nextDoubleForNeighbourhood);
        if (neighbourhood == null) return;
        if (neighbourhood.cumulativeInvestmentProbabilities.getSize() == 0) return;

        if (Model.rnd.nextDouble() < calculateInvestmentProbabilityAccordingToExpectedReturnSpread(neighbourhood.expectedReturnSpread)) {

            double nextDoubleForBucket = Model.rnd.nextDouble();
            Bucket bucket = neighbourhood.cumulativeInvestmentProbabilities.selectObjectAccordingToCumulativeProbability(nextDoubleForBucket);


            if (bucket != null && Model.flatsForSaleForInvestment.get(bucket).size() > 0) {
                Flat flat = Model.getBestInvestmentFlatInBucket(bucket,this);
                if (flat != null) {
                    flat.investmentStateIncrease = bucket.stateMax - flat.state;
                    double loanNeed = flat.forSalePrice + flat.investmentStateIncrease * flat.getGeoLocation().predictedRenovationUnitCost * (1 + Model.renovationCostBuffer) - (deposit - minDeposit);
                    if (loanNeed > 0) {
                        if (getBestLoanContractOffer(flat, loanNeed) == null) {
                            flat.investmentStateIncrease = 0;
                            return;
                        }

                    }
                } else {
                    return;
                }

                if (Model.phase == Model.Phase.INVESTMENTPURCHASES) {
                    Model.buyFlatForInvestment(flat, this);
                    neighbourhood.householdInvestmentValue += flat.forSalePrice;
                } else if (Model.phase == Model.Phase.BIDSFORFLATS) {
                    flat.incrementNBids(null);
                }


            }

        }

    }

    void calculateAndSetHasNonPerformingLoan() {
        hasNonPerformingLoan = false;
        if (home != null && home.loanContract != null && home.loanContract.isNonPerforming) hasNonPerformingLoan = true;
        if (homeUnderConstruction != null && homeUnderConstruction.loanContract != null && homeUnderConstruction.loanContract.isNonPerforming)
            hasNonPerformingLoan = true;
        for (Flat flat : properties) {
            if (flat.loanContract != null && flat.loanContract.isNonPerforming) hasNonPerformingLoan = true;
        }
    }

    public int calculateAgeOfOldestMember() {

        int ageInPeriods = getMembers().get(0).getAgeInPeriods();
        if (getMembers().size() == 2)
            ageInPeriods = Math.max(getMembers().get(0).getAgeInPeriods(), getMembers().get(1).getAgeInPeriods());
        return ageInPeriods;
    }

    public void chargeDeposit(double amount) {
        deposit -= amount;
    }

    public void creditDeposit(double amount) {
        deposit += amount;
    }

    public void updateLetThisYoungOverTooYoungAgeRent() {
        if (calculateAgeInYearsOfOldestMember() >= Model.ageInYearsToConsiderSomebodyOldEnoughToRentForSure) {
            letThisYoungOverTooYoungAgeRent = true;
        } else {
            if (Model.threadNextDouble(this) < Model.coeffOfFirstWageRegardingRentalProbability * members.get(0).firstWage) {
                letThisYoungOverTooYoungAgeRent = true;
            }
        }
    }

    public boolean tooYoungToBuyOrRent() {
        if (members.size() == 1 && members.get(0).ageInPeriods < Model.minAgeInPeriodsToBuyOrRentAFlatAsSingleHousehold)
            return true;
        return false;
    }

    public boolean mayTryToRent() {
        if (tooYoungToBuyOrRent() || home != null || rentHome != null || shouldNotRent == true || letThisYoungOverTooYoungAgeRent == false || flatTooExpensiveToBuy == false) {
            return false;
        } else {
            return true;
        }
    }

    public int calculateAgeInYearsOfOldestMember() {
        int ageInPeriods = members.get(0).ageInPeriods;
        if (members.size() == 2 && members.get(1).ageInPeriods > ageInPeriods)
            ageInPeriods = members.get(1).ageInPeriods;
        return (int) Math.floor(ageInPeriods / 12.0);
    }

    public int calculateAgeInYearsOfYoungestMember() {
        int ageInPeriods = members.get(0).ageInPeriods;
        if (members.size() == 2 && members.get(1).ageInPeriods < ageInPeriods)
            ageInPeriods = members.get(1).ageInPeriods;
        return (int) Math.floor(ageInPeriods / 12.0);
    }

    public boolean consideredZOPFirstBuyer() {
        boolean consideredZOPFirstBuyer = false;
        if (Model.firstBuyerZOPSpecialCriteria) {
            if (loanContracts.size()==0 && calculateAgeInYearsOfOldestMember()<=Model.firstBuyerZOPSpecialCriteriaMaxAge) consideredZOPFirstBuyer = true;
        } else {
            if (firstBuyer) consideredZOPFirstBuyer = true;
        }
        return consideredZOPFirstBuyer;
    }

    public boolean maySelectHome() {
        if (Model.phase == Model.Phase.CAHAICALCULATIONS) return isMoving && rentHome == null && tooYoungToBuyOrRent() == false && hasUnemployedMember()==false;
        return isMoving && rentHome == null && tooYoungToBuyOrRent() == false && (firstBuyer || flatTooExpensiveToBuy == false) && fictiveFlatToBuy != null  && hasUnemployedMember()==false;
    }

    public boolean hasUnemployedMember() {
        boolean hasUnemployedMember = false;
        for (Individual individual : members) {
            if (individual.nPeriodsInUnemployment>0) hasUnemployedMember=true;
        }
        return hasUnemployedMember;
    }

    public double getSumFirstWage() {
        double sumFirstWage = members.get(0).firstWage;
        if (members.size() == 2) sumFirstWage += members.get(1).firstWage;
        return sumFirstWage;
    }

    public double getSumPotentialWage() {
        double sumPotentialWage = members.get(0).potentialWage;
        if (members.size() == 2) sumPotentialWage += members.get(1).potentialWage;
        return sumPotentialWage;
    }

    public int getTypeOfMemberWihLowerEducationalLevel() {
        if (members.size()==1) {
            return members.get(0).typeIndex;
        } else {
            return Math.max(members.get(0).typeIndex,members.get(1).typeIndex);
        }
    }

    public double depositAvailableForFlat(Flat flat) {
        if (flat.isNewlyBuilt && flat.isEligibleForCSOK() && (Model.phase == Model.Phase.HOUSEHOLDPURCHASES || Model.phase == Model.Phase.FICTIVECHOICEOFHOUSEHOLDS || Model.phase == Model.Phase.BEGINNINGOFPERIOD)) {
            return deposit-minDeposit+newlyBuiltCSOK;
        } else if (flat.isEligibleForFalusiCSOK() && (Model.phase == Model.Phase.HOUSEHOLDPURCHASES || Model.phase == Model.Phase.FICTIVECHOICEOFHOUSEHOLDS || Model.phase == Model.Phase.BEGINNINGOFPERIOD)) {
            return deposit-minDeposit+falusiCSOK;
        } else {
            return deposit-minDeposit;
        }
    }

    public void applyForBabyloan() {
        boolean hasNewlyBorn = false;
        if (Model.period<Model.introductionOfBabyloan || Model.period>Model.endOfBabyloan || members.size()<2) return;
        if (children.size()>0 && children.get(children.size()-1).ageInPeriods==0) hasNewlyBorn = true;
        if (members.get(0).ageInPeriods<Model.maxAgeInYearsForBabyloan*12 && ((isMoving && members.get(0).ageInPeriods<Model.maxAgeInYearsForBabyloanIsMoving*12 && children.size()<Model.nOfMaxChildrenBeforeBabyloan && Model.nBabyLoanApplications<=Model.maxCapacityOfNewBabyloan) || hasNewlyBorn) && babyloan.isIssued==false) {
            double totalPayment = 0;
            for (LoanContract loanContract : getLoanContracts()) {
                totalPayment += loanContract.payment;
            }
            double PTIpayment = Model.babyloanDSTIPayment;
            if (hasNewlyBorn) PTIpayment = Model.babyloanPayment;
            if (Model.banks.get(0).appropriateDSTI(this,PTIpayment, totalPayment)) {
                creditDeposit(Model.babyloanAmount);
                babyloan.isIssued=true;
                babyloan.nChildrenSinceIssuance=0;
                if (hasNewlyBorn) {
                    babyloan.nChildrenSinceIssuance = 1;
                    babyloan.nPeriodsSinceChildBirth = 0;
                }
                babyloan.principal = Model.babyloanAmount;
                babyloan.duration = Model.babyloanDuration;
                babyloan.payment = Model.babyloanPayment;
                babyloan.PTIPayment = PTIpayment;

                Model.nBabyLoanApplications++;

            }
        }

    }

    public double maxZOPLoanForFlat(Flat flat, Household household) {
        double totalLoan = 0;
        double totalPayment = 0;

        for (LoanContract loanContract : getLoanContracts()) {
            totalLoan += loanContract.principal;
            totalPayment += loanContract.payment;
        }

        if (babyloan.isIssued) totalPayment += babyloan.payment;

        int ZOPduration = 240;
        double maxZOPLoan = Model.banks.get(0).calculateMaxLoanForLoanContractOffer(this,flat,totalLoan,totalPayment,ZOPduration);
        maxZOPLoan = Math.min(maxZOPLoan,70000000);
        if (ZOParbitrage == false) maxZOPLoan = Math.min(maxZOPLoan, flat.forSalePrice - household.deposit + household.minDeposit);
        return maxZOPLoan;



    }

    public double surplusIncreaseForZOPFlat(Flat flat) {
        double maxZOPLoan = maxZOPLoanForFlat(flat, this);
        return Math.max(0,(maxZOPLoan - (Math.max(0,flat.forSalePrice - depositAvailableForFlat(flat)))) * 0.25);
    }

    public int ageGroupIndexForAffordability() {
        if (Model.CAHAIForFTB == false) {
            for (int i = 0; i < Model.affordabilityAgeIntervals.length/2; i++) {
                int ageInYearsOfOldestMember = calculateAgeInYearsOfOldestMember();
                if (ageInYearsOfOldestMember>=Model.affordabilityAgeIntervals[i*2] && ageInYearsOfOldestMember<=Model.affordabilityAgeIntervals[i*2+1]) {
                    return i;
                }
            }
            return -1;
        } else {
            if (firstTimeBuyerPurchaseInThisPeriod) {
                return 0;
            } else {
                return 1;
            }
        }

    }











}