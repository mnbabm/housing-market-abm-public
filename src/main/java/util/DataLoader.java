package util;

import model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DataLoader {

    static Household[] households;
    public static Map<Bucket,Integer> nFlatsRented = new HashMap<>();
    public static ArrayList<UtilityFunctionCES> sampleUtilityFunctions = new ArrayList<>();
    public static Map<LoanContract,Integer> loanContractYear = new HashMap<>();
    public static Map<Integer,MappingWithWeights<Integer>> ageProbabilities = new HashMap<>();
    public static Map<GeoLocation,Double> rentalsNeededInGeoLocations = new HashMap<>();
    public static Map<Integer,Double> probabilityOfRentAccordingToAge = new HashMap<>();


    public static void setup() {

        OwnStopper stopperEmpiricalDataLoader = new OwnStopper();

        setupMaps();
        Model.setPeriodicalValues();

        setupHouseholds();
        setupIndividuals();
        setupFlats();
        setupLoanContracts();
        setupPriceRegressions();
        setupBanks();

        adjustFirstWagesInAccordanceWithMinConsumptionAndDebtBurdenAndDeleteUnemploymentForPerformingHouseholds();
        setPreferredGeoLocation();
        setAbsoluteUtilityParameters();

        switchHomesToRent();
        setShouldNotRent();
        setupExternalDemand();
        createFlatsForExternalDemand();
        createFlatsForRentalMarketBuffer();
        setBucketRentalHist();
        setNeighbourhoodRentMarkups();
        setRents();
        setNeighbourhoodHistReturn();
        createFlatsForSale();
        calculateHouseholdDepositsAndSetFirstBuyer();
        setUsedCSOK();

        newlyBuiltDemand();
        calculateAndSetNeighbourhoodAreas();
        calculateLandPrices();
        setupFlatsReadyAndFlatsUnderConstruction();
        calculateRegionalConstructionAndRenovationCosts();
        calculateHistInvestmentValues();
        calculateNeighbourhoodHistPriceIndices();
        calculateGeoLocationHistPriceIndices();
        calculateLastMarketPrices();
        createForSaleAndFlatSaleRecords();

        double elapsedTimeEmpiricaldataLoader = stopperEmpiricalDataLoader.getElapsedTimeInMilliseconds();
        System.out.println("EmpiricalDataLoader runtime in milliseconds: " + elapsedTimeEmpiricaldataLoader);


    }

    public static void setupMaps() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.householdsFileName));

            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.householdsFileName))) scannerForCSV.nextLine();//header line

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int householdId = Integer.parseInt(dataLineArray[0]);
                Household household = Model.createHousehold(householdId);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.individualsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.individualsFileName))) scannerForCSV.nextLine();

                while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int individualId = Integer.parseInt(dataLineArray[0]);
                Individual individual = Model.createIndividual(individualId);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try { //flats-geoLocations-neighbourhoods-buckets
            Scanner scannerForCSV = new Scanner(new File(MainRun.flatsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.flatsFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int flatId = (int) Double.parseDouble(dataLineArray[0]);
                Flat flat = Model.createFlat(flatId);

                int geoLocationId = (int) Double.parseDouble(dataLineArray[1]);
                int teltip = (int) Integer.parseInt(dataLineArray[2]);
                if (geoLocationId==1 && teltip==0) geoLocationId=Model.capitalId;
                if (Model.geoLocations.get(geoLocationId) == null) {
                    Model.createGeoLocation(geoLocationId);
                    Model.maxAreaUnderConstructionInGeoLocations.replace(Model.geoLocations.get(geoLocationId),0.0);
                }
                int neighbourhoodId = (int) Double.parseDouble(dataLineArray[7]);

                if (Model.neighbourhoods.get(neighbourhoodId) == null) {
                    Neighbourhood neighbourhood = Model.createNeighbourhood((int) neighbourhoodId);

                    Model.geoLocations.get(geoLocationId).getNeighbourhoods().add(neighbourhood);

                    neighbourhood.setGeoLocation(Model.geoLocations.get(geoLocationId));
                    for (int k = 0; k < Model.bucketSizeIntervals.length-1; k++) {
                        for (int l = 0; l < Model.bucketStateIntervals.length-1; l++) {
                            Bucket bucket = Model.createBucket();
                            neighbourhood.getBuckets().add(bucket);
                            bucket.setNeighbourhood(neighbourhood);
                            bucket.setSizeMin(Model.bucketSizeIntervals[k]);
                            bucket.setSizeMax(Model.bucketSizeIntervals[k+1]);
                            bucket.setStateMin(Model.bucketStateIntervals[l]);
                            bucket.setStateMax(Model.bucketStateIntervals[l+1]);
                            bucket.setSizeIndex(k+1);
                            bucket.setStateIndex(l+1);

                            if (l == Model.bucketStateIntervals.length-2) {
                                bucket.setHighQuality(true);
                            }

                        }
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.loanContractsFileName));

            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.loanContractsFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int loanContractId = Integer.parseInt(dataLineArray[0]);
                LoanContract loanContract = Model.createLoanContract(loanContractId);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Model.createInvestor();
        Model.createConstructor();
        Model.createBank();

        households = new Household[Model.households.size()];
        int householdIndex = 0;
        for (Household household : Model.households.values()) {
            households[householdIndex] = household;
            householdIndex++;
        }


        for (Bucket bucket: Model.buckets.values()) {
            nFlatsRented.put(bucket,0);
        }

        for (GeoLocation geoLocation: Model.geoLocations.values()) {
            rentalsNeededInGeoLocations.put(geoLocation,0.0);
        }

        Model.capital = Model.geoLocations.get(Model.capitalId);
        Model.agglomeration = Model.geoLocations.get(Model.agglomerationId);
    }

    public static void setupHouseholds() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.householdsFileName));

            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.householdsFileName))) scannerForCSV.nextLine();

            //0. id, 1. ONYF_id1, 2. ONYF_id2, 3. flatId, 4. loanContractId
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int householdId = Integer.parseInt(dataLineArray[0]);
                int ONYF_id1 = Integer.parseInt(dataLineArray[1]);
                int ONYF_id2 = Integer.parseInt(dataLineArray[2]);
                int flatId = Integer.parseInt(dataLineArray[3]);
                int loanContractId = Integer.parseInt(dataLineArray[4]);

                Household household = Model.households.get(householdId);
                Individual individual1 = Model.individuals.get(ONYF_id1);
                household.getMembers().add(individual1);
                individual1.setHousehold(household);

                if (ONYF_id2>=0) {
                    Individual individual2 = Model.individuals.get(ONYF_id2);
                    household.getMembers().add(individual2);
                    individual2.setHousehold(household);
                }

                Flat flat = Model.flats.get(flatId);
                if (flatId>=0) {
                    household.setHome(flat); //default is home, but when looking at flatData, it might be switched for a rent
                    flat.setOwnerHousehold(household);
                }

                if (loanContractId>=0) {
                    LoanContract loanContract = Model.loanContracts.get(loanContractId);
                    flat.setLoanContract(loanContract);
                    loanContract.setDebtor(household);
                    loanContract.setCollateral(flat);
                }


                if (Model.rnd.nextDouble()<Model.mayRenovateWhenBuyingRatio) {
                    household.setMayRenovateWhenBuying(true);
                } else {
                    household.setMayRenovateWhenBuying(false);
                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setupIndividuals() {
        double[] nActive = new double[3];
        double[] nUnemployed = new double[3];
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.individualsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.individualsFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int id2 = Integer.parseInt(dataLineArray[0]);
                int gender = Integer.parseInt(dataLineArray[1]);
                int birthYear = Integer.parseInt(dataLineArray[2]);
                int region = Integer.parseInt(dataLineArray[3]);
                int county = Integer.parseInt(dataLineArray[4]);
                int jaras = Integer.parseInt(dataLineArray[5]);
                int type = Integer.parseInt(dataLineArray[6]);
                double firstWage = Double.parseDouble(dataLineArray[7]);
                int loanContractId = Integer.parseInt(dataLineArray[8]);
                int id2husband = Integer.parseInt(dataLineArray[9]);
                int id2wife = Integer.parseInt(dataLineArray[10]);
                int id2parent = Integer.parseInt(dataLineArray[11]);

                if (birthYear==964) birthYear=1964;

                Individual individual = Model.individuals.get(id2);

                if (gender==0) {
                    individual.setMale(true);
                } else individual.setMale(false);

                int ageInPeriods =(Model.zeroPeriodYear-birthYear-1)*12+Model.zeroPeriodMonth+Model.rnd.nextInt(12);
                if (ageInPeriods>=Model.lifespan) ageInPeriods=Model.lifespan-1-Model.rnd.nextInt(24);
                individual.setAgeInPeriods(ageInPeriods);

                if (type==1) {
                    individual.setTypeIndex(0);
                } else if (type==2) {
                    individual.setTypeIndex(1);
                } else {
                    individual.setTypeIndex(2);
                }

                double minFirstWageNow = Model.DLminFirstWage;
                if (county!=1 && county!=13) minFirstWageNow = 70000;

                firstWage = Math.max(firstWage,minFirstWageNow);
                individual.setFirstWage(firstWage);

                if (id2parent>0) {
                    Household parentHousehold = Model.individuals.get(id2parent).getHousehold();

                    parentHousehold.getChildren().add(individual);
                    parentHousehold.nBirths++;
                    individual.setParentHousehold(parentHousehold);

                }

                double lifeTimeIncomeEarned = 0;
                int workExperience = 0;

                for (int i = Model.ageInPeriodsForFirstWorkExperience[individual.getTypeIndex()]; i < ageInPeriods; i++) {
                    lifeTimeIncomeEarned += Model.wageRatio[individual.getTypeIndex()][workExperience] * individual.getFirstWage();
                    workExperience++;
                }

                individual.setWorkExperience(workExperience);
                individual.setLifeTimeIncomeEarned(lifeTimeIncomeEarned);

                if (individual.getWorkExperience()>0 && individual.ageInPeriods<Model.retirementAgeInPeriods-1) nActive[individual.getTypeIndex()]++;
                if (individual.getWorkExperience()>0 && individual.ageInPeriods<Model.retirementAgeInPeriods-1 && Model.rnd.nextDouble()<Model.unemploymentRatesPath[0][individual.getTypeIndex()]) {
                    nUnemployed[individual.getTypeIndex()]++;
                    individual.setNPeriodsInUnemployment(100);
                }


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //siblings
        for (Household household: Model.households.values()) {
            if (household.getChildren() != null) {
                for (Individual child : household.getChildren()) {
                    for (Individual sibling : household.getChildren()) {
                        if (sibling!=child) {
                            child.getSiblings().add(sibling);
                        }
                    }
                }
            }
        }

    }

    public static void setupFlats() {

        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.flatsFileName));
            //0. flat_id, 1. region, 2. municipality_type, 3. nh, 4. lat, 5. state, 6. state_cat, 7. nh_cat, 8. price_indexedFor2014, 9. price_indexedFor2015, 10. price_indexedFor2018, 11. year_of_sale, 12. year_of_construction, 13. rented
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.flatsFileName))) scannerForCSV.nextLine();
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int flatId = Integer.parseInt(dataLineArray[0]);
                int municipality_type = Integer.parseInt(dataLineArray[2]);
                double size = Double.parseDouble(dataLineArray[4]);
                    if (size<=Model.bucketSizeIntervals[0]) {
                        size = Math.max(size,Model.bucketSizeIntervals[0]*1.001);
                    }
                double state = Double.parseDouble(dataLineArray[5]);
                int neighbourhoodId = Integer.parseInt(dataLineArray[7]);
                int isRentedInteger = Integer.parseInt(dataLineArray[13]);
                double marketPriceEmpiricalDataLoader = Double.parseDouble(dataLineArray[10]);

                double neighbourhoodQuality = Double.parseDouble(dataLineArray[3]);

                Flat flat = Model.flats.get(flatId);
                if (flat.getOwnerHousehold() == null) {
                    flat.deleteFlat();
                    continue;
                }
                flat.setSize(size);
                flat.setState(state);
                Neighbourhood neighbourhood = Model.neighbourhoods.get(neighbourhoodId);
                neighbourhood.setQuality(neighbourhoodQuality);
                flat.setBucket(neighbourhood);
                flat.setMarketPriceDataLoader(marketPriceEmpiricalDataLoader);

                GeoLocation geoLocation = flat.getBucket().getNeighbourhood().getGeoLocation();
                rentalsNeededInGeoLocations.replace(geoLocation,rentalsNeededInGeoLocations.get(geoLocation)+Model.DLrentalRatio[geoLocation.getId()]);
                if (municipality_type==3) {
                    flat.getBucket().getNeighbourhood().nFlatsInVillages++;
                } else {
                    flat.getBucket().getNeighbourhood().nFlatsInTowns++;
                }


            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void setupLoanContracts() {

        Map<Integer,Double> mapYearlyBaseRateAtLastInterestCalculation = new HashMap<>();
        double yearlyBuborInPeriod0 = 0.0003;
        mapYearlyBaseRateAtLastInterestCalculation.put(0,0.0003-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-1,0.0003-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-2,0.0003-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-3,0.0003-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-4,0.0004-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-5,0.0015-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-6,0.0015-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-7,0.0015-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-8,0.0015-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-9,0.0016-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-10,0.0018-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-11,0.0023-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-12,0.0025-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);

        mapYearlyBaseRateAtLastInterestCalculation.put(-13,0.0037-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-14,0.0055-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-15,0.0078-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-16,0.0088-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-17,0.0088-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-18,0.0093-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-19,0.0101-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-20,0.0101-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-21,0.0105-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-22,0.0120-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-23,0.0134-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        mapYearlyBaseRateAtLastInterestCalculation.put(-24,0.0135-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);

        for (int i = 25; i <= 30; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0135-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 31; i <= 36; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0141-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 37; i <= 42; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0210-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 43; i <= 48; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0234-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 49; i <= 54; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0299-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 55; i <= 60; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0420-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 61; i <= 66; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0575-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 67; i <= 72; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0720-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 73; i <= 78; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0724-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 79; i <= 84; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0610-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }
        for (int i = 85; i <= 130; i++) {
            mapYearlyBaseRateAtLastInterestCalculation.put(-i,0.0600-yearlyBuborInPeriod0+Model.yearlyBaseRatePath[0]);
        }





        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.loanContractsFileName));
            //0. loanContract_id, 1. principal, 2. startingPrincipal, 3. duration, 4. payment, 5. yearlyInterestRate (%), 6. isNonPerforming, 7. nNonPerformingPeriods, 8. adjustedFirstWageForFirstMember, 9. year_of_issuance, 10. khr_id, 11. nMonthsInterestPeriod, 12. periodOfLastInterestCalculation
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.loanContractsFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int loanContractId = Integer.parseInt(dataLineArray[0]);
                double principal = Double.parseDouble(dataLineArray[1]);
                double startingPrincipal = Double.parseDouble(dataLineArray[2]);
                int duration = Integer.parseInt(dataLineArray[3]);
                double payment = Double.parseDouble(dataLineArray[4]);
                double yearlyInterestRate = Double.parseDouble(dataLineArray[5])/100;
                int isNonPerformingInteger = Integer.parseInt(dataLineArray[6]);
                int nNonPerformingPeriods = Integer.parseInt(dataLineArray[7]);
                double adjustedFirstWageForFirstMember = Double.parseDouble(dataLineArray[8]);
                int year = Integer.parseInt(dataLineArray[9]);
                int nMonthsInterestPeriod = Integer.parseInt(dataLineArray[11]);
                int periodOfLastInterestRateCalculation = Integer.parseInt(dataLineArray[12]);

                if (periodOfLastInterestRateCalculation<-120) periodOfLastInterestRateCalculation=-120+Model.rnd.nextInt(12);


                LoanContract loanContract = Model.loanContracts.get(loanContractId);
                loanContract.setPrincipal(principal);
                loanContract.setDuration(duration);
                loanContract.setPayment(payment);
                loanContract.setMonthlyInterestRate(yearlyInterestRate/12);
                if (isNonPerformingInteger==1) {
                    loanContract.setNonPerforming(true);
                    loanContract.setNNonPerformingPeriods(nNonPerformingPeriods);
                }

                if (adjustedFirstWageForFirstMember>Model.DLminFirstWage) {
                    loanContract.getDebtor().getMembers().get(0).setFirstWage(adjustedFirstWageForFirstMember);
                }

                if (year>=2016 && loanContract.getDebtor().getChildren().size()>0) {
                    loanContract.getDebtor().setUtilizedCSOK(10000000);
                }

                loanContract.setPeriodOfLastInterestCalculation(periodOfLastInterestRateCalculation);
                loanContract.setYearlyBaseRateAtLastInterestCalculation(mapYearlyBaseRateAtLastInterestCalculation.get(periodOfLastInterestRateCalculation));

                loanContract.getDebtor().getLoanContracts().add(loanContract);

                loanContract.setBank(Model.banks.get(0));
                Model.banks.get(0).getLoanContracts().put(loanContract.getId(),loanContract);
                Model.banks.get(0).setLoanTotal(Model.banks.get(0).getLoanTotal()+loanContract.getPrincipal());

                loanContractYear.put(loanContract,year);


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setupPriceRegressions() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.priceRegressionFileName));

            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.priceRegressionFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int neighbourhoodId = Integer.parseInt(dataLineArray[0]);
                double coeffSize = Double.parseDouble(dataLineArray[1]);
                double coeffSize2 = Double.parseDouble(dataLineArray[2]);
                double coeffSizeState = Double.parseDouble(dataLineArray[3]);
                double constant = Double.parseDouble(dataLineArray[4]);

                PriceRegressionFunctionLinear priceRegressionFunctionLinear = Model.createPriceRegressionFunctionLinear();
                Model.neighbourhoods.get(neighbourhoodId).setPriceRegressionFunctionLinear(priceRegressionFunctionLinear);
                priceRegressionFunctionLinear.setCoeffSize(coeffSize);
                priceRegressionFunctionLinear.setCoeffSize2(coeffSize2);
                priceRegressionFunctionLinear.setCoeffSizeState(coeffSizeState);
                priceRegressionFunctionLinear.setConstant(constant);

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void setupBanks() {
        for (Bank bank: Model.banks.values()) {
            bank.setYearlyInterestRateRegressionConstant(Model.DLyearlyInterestRateRegressionConstant);
            bank.setYearlyInterestRateRegressionCoeffLTV(Model.DLyearlyInterestRateRegressionCoeffLTV);
            bank.setYearlyInterestRateRegressionCoeffLnWageIncome(Model.DLyearlyInterestRateRegressionCoeffLnWageIncome);
            bank.setYearlyInterestRateRegressionCoeffAgeCategory1(Model.DLyearlyInterestRateRegressionCoeffAgeCategory1);
            bank.setYearlyInterestRateRegressionCoeffAgeCategory2(Model.DLyearlyInterestRateRegressionCoeffAgeCategory2);
            bank.setBridgeLoanToValue(Model.DLbridgeLoanToValue);
        }
    }

    public static void adjustFirstWagesInAccordanceWithMinConsumptionAndDebtBurdenAndDeleteUnemploymentForPerformingHouseholds() {

        for (LoanContract loanContract : Model.loanContracts.values()) {
            if (loanContract.isNonPerforming() == false) {
                for (Individual individual : loanContract.getDebtor().members) {
                    individual.setNPeriodsInUnemployment(0);
                }
            }
        }

        for (Household household : Model.households.values()) {
            if (household.getHome() != null && household.getHome().getLoanContract() != null) {
                LoanContract loanContract = household.getHome().getLoanContract();
                loanContract.setNonPerforming(false);
                loanContract.setNNonPerformingPeriods(0);
                if (loanContract.isNonPerforming()==false) {



                    double minConsumption = household.calculateMinConsumptionLevel();
                    double minWageIncomeNeed = (minConsumption + loanContract.getPayment()*1.1)/(1 - Model.taxRate);

                    if (household.getSumFirstWage()<minWageIncomeNeed) {
                        for (Individual individual : household.members) {
                            if (household.getSumFirstWage() == 0) {
                                individual.firstWage = minWageIncomeNeed/household.members.size();
                            } else {
                                individual.firstWage *= minWageIncomeNeed/household.getSumFirstWage();
                            }

                            individual.firstWage += (Model.minConsumptionPerCapitaUpper-Model.minConsumptionPerCapitaLower)/(1-Model.taxRate)*(1+household.getChildren().size()/household.members.size());
                            if (individual.getWorkExperience()<120 && Model.retirementAgeInPeriods-individual.getAgeInPeriods()+individual.getWorkExperience()<120) {
                                individual.firstWage *= 1/Model.pensionReplacementRate[individual.getTypeIndex()];
                            }

                            //the following part is like in setupIndividuals( + calculating ageInPeriods)
                            double lifeTimeIncomeEarned = 0;
                            int workExperience = 0;
                            int ageInPeriods = individual.getAgeInPeriods();

                            for (int i = Model.ageInPeriodsForFirstWorkExperience[individual.getTypeIndex()]; i < ageInPeriods; i++) {
                                lifeTimeIncomeEarned += Model.wageRatio[individual.getTypeIndex()][workExperience] * individual.getFirstWage();
                                workExperience++;
                            }

                            individual.setWorkExperience(workExperience);
                            individual.setLifeTimeIncomeEarned(lifeTimeIncomeEarned);
                        }
                    }
                }
            }
        }

        for (Individual individual : Model.individuals.values()) {
                individual.firstWage += Model.threadNextDouble(individual);
        }
    }

    public static void switchHomesToRent() {

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            if (neighbourhood.nFlatsInVillages/(double) (neighbourhood.nFlatsInVillages+neighbourhood.nFlatsInTowns)<Model.DLthresholdRatioForRentalNeighbourhood) {
                neighbourhood.rentalNeighbourhood = true;
            } else {
                neighbourhood.rentalNeighbourhood = false;
            }
        }

        for (int i = 0; i < Model.lifespan/12; i++) {
            if (i<Model.DLminAgeForRentalProbability || i>Model.DLmaxAgeForRentalProbability) {
                probabilityOfRentAccordingToAge.put(i,0.0);
            } else {
                double probabilityOfRent = Model.DLrentalProbabilityForMinAge - (i-Model.DLminAgeForRentalProbability) * (Model.DLrentalProbabilityForMinAge-Model.DLrentalProbabilityForMaxAge)/(double) (Model.DLmaxAgeForRentalProbability-Model.DLminAgeForRentalProbability);
                probabilityOfRentAccordingToAge.put(i,probabilityOfRent);
            }
        }

        int nItersToSwitchHomesToRent = 0;

        MappingWithWeights<Integer> probabilitiesForNPeriodsLeftForRent = new MappingWithWeights<>();
        double probability = 1;
        for (int i = 0; i < 300; i++) {
            probability *= 0.97;
            probabilitiesForNPeriodsLeftForRent.put(probability,i);
        }

        while (OwnFunctions.maxOfDoubleMapValues(rentalsNeededInGeoLocations)>0 && nItersToSwitchHomesToRent < Model.DLmaxItersToSwitchHomesToRent) {
            nItersToSwitchHomesToRent++;
            for (Household household : Model.households.values()) {
                Flat flat = household.getHome();
                double householdPermanentIncome = Model.priceLevel * Model.realGDPLevel * (1-Model.taxRate) * household.members.get(0).getFirstWage();
                if (household.members.size()==2) householdPermanentIncome += Model.priceLevel * Model.realGDPLevel * (1-Model.taxRate) * household.members.get(1).getFirstWage();
                if (flat != null && flat.getEstimatedMarketPrice()*Model.rentToPrice > householdPermanentIncome * Model.maxRentRatio * 0.7) continue;
                if (flat != null && flat.getNeighbourhood().rentalNeighbourhood == true && rentalsNeededInGeoLocations.get(flat.getGeoLocation())>0 && flat.getLoanContract()==null) {
                    if (Model.rnd.nextDouble()<probabilityOfRentAccordingToAge.get((int) Math.floor(household.calculateAgeOfOldestMember()/12))) {

                        flat.setNPeriodsLeftForRent(1+Model.rnd.nextInt(Model.nMaxPeriodsForRent));
                        household.setHome(null);
                        household.setRentHome(flat);
                        flat.setRenter(household);
                        flat.setOwnerHousehold(null);
                        flat.setForRent(true);

                        findOwnerForFlat(flat);

                        int oldValue = nFlatsRented.get(flat.getBucket());
                        nFlatsRented.replace(flat.getBucket(),oldValue + 1);
                        OwnFunctions.increaseDoubleMapValue(rentalsNeededInGeoLocations,flat.getGeoLocation(),-1);

                    }
                }
            }



        }


    }

    public static void setShouldNotRent() {
        for (Household household : Model.households.values())  {
            if (household.getHome() != null) household.setShouldNotRent(true);
        }
    }

    public static void setPreferredGeoLocation() {
        for (Household household : Model.households.values()) {
            if (household.getHome() != null) household.setPreferredGeoLocation(household.getHome().getBucket().getNeighbourhood().getGeoLocation());
            if (household.getRentHome() != null) household.setPreferredGeoLocation(household.getRentHome().getBucket().getNeighbourhood().getGeoLocation());
            household.canChangeLocationAccordingToRegionalProbability();
        }
    }

    public static void setupExternalDemand() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.externalDemandFileName));
            //0. neighbourhoodId, 1. size, 2. state 3. nFlatsNow, 4- externalDemandInPeriods
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.externalDemandFileName))) scannerForCSV.nextLine();
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int neighbourhoodId = Integer.parseInt(dataLineArray[0]);
                double size = Double.parseDouble(dataLineArray[1]);
                double state = Double.parseDouble(dataLineArray[2]);
                int nFlats = Integer.parseInt(dataLineArray[3]);
                int[] externalDemand  = new int[Model.nPeriods];

                Flat flat = Model.createFlat();
                flat.setSize(size);
                flat.setState(state);
                Neighbourhood neighbourhood = Model.neighbourhoods.get(neighbourhoodId);

                flat.setBucket(neighbourhood);
                Bucket bucket = flat.getBucket();

                Model.externalDemand.put(bucket,externalDemand);


            }

            //zero demand for buckets not contained in the externalDmenad.csv
            for (Bucket bucket : Model.buckets.values()) {
                if (Model.externalDemand.containsKey(bucket)==false) {
                    int[] demand = new int[Model.nPeriods];
                    for (int i = 0; i < demand.length; i++) {
                        demand[i] = 0;
                    }
                    Model.externalDemand.put(bucket,demand);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void createFlatsForExternalDemand() {


        for (Bucket bucket : Model.buckets.values()) {
            int[] externalDemand = Model.externalDemand.get(bucket);
            double nFlatsToCreate = Math.ceil(externalDemand[0]*(1 + Model.DLrentalMarketBuffer));
            for (int i = 0; i < nFlatsToCreate; i++) {
                Flat flat = Model.createFlat();
                flat.setBucket(bucket);
                flat.setSize((bucket.getSizeMin()+bucket.getSizeMax())/2);
                flat.setState((bucket.getStateMin()+bucket.getStateMax())/2);
                flat.setForRent(true);

                findOwnerForFlat(flat);

                if (i<externalDemand[0]) {
                    flat.setRentedByExternal(true);
                    flat.setNPeriodsLeftForRent(1);
                }


            }
        }
    }

    public static void createFlatsForRentalMarketBuffer() {

            for (Bucket bucket: Model.buckets.values()) {
                int nFlatsRentedInBucket = nFlatsRented.get(bucket);
                double nFlatsToCreate = Math.ceil(nFlatsRentedInBucket*Model.DLrentalMarketBuffer);
                for (int i = 0; i < nFlatsToCreate; i++) {
                    Flat flat = Model.createFlat();
                    flat.setBucket(bucket);
                    flat.setSize((bucket.getSizeMin()+bucket.getSizeMax())/2);
                    flat.setState((bucket.getStateMin()+bucket.getStateMax())/2);
                    flat.setForRent(true);

                    findOwnerForFlat(flat);


                }
            }

    }

    public static void setBucketRentalHist() {
        for (Flat flat: Model.flats.values()) {
            if (flat.isForRent()==true) {
                int nFlatsForRent = flat.getBucket().getNFlatsForRent()+1;
                flat.getBucket().setNFlatsForRent(nFlatsForRent);
            }
            if (flat.getNPeriodsLeftForRent()>0) {
                int nFlatsRented = flat.getBucket().getNFlatsRented()+1;
                flat.getBucket().setNFlatsRented(nFlatsRented);
            }
        }

        for (Bucket bucket: Model.buckets.values()) {
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                bucket.histNFlatsForRent[i] = bucket.getNFlatsForRent();
                bucket.histNFlatsRented[i] = bucket.getNFlatsRented();
                bucket.histNFictiveRentDemand[i] = bucket.histNFlatsRented[i];
            }
        }

    }

    public static void setNeighbourhoodRentMarkups() {
        for (Neighbourhood neighbourhood: Model.neighbourhoods.values()) {
            neighbourhood.calculateAndSetAggregateFlatInfo();
            neighbourhood.calculateAndSetRentMarkup();
        }
    }

    public static void setRents() {
        for (Flat flat: Model.flats.values()) {
            if (flat.isForRent()) flat.calculateAndSetRent();
        }
    }

    public static void setNeighbourhoodHistReturn() {
        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            neighbourhood.rentIncome = 0;
            neighbourhood.forRentValue = 0;
        }

        for (Flat flat: Model.flats.values()) {
            if (flat.isForRent) {
                flat.bucket.neighbourhood.forRentValue += flat.getEstimatedMarketPrice();
                if (flat.renter != null || flat.rentedByExternal) flat.bucket.neighbourhood.rentIncome += flat.rent;
            }
        }

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                neighbourhood.histReturn[i] = neighbourhood.rentIncome/neighbourhood.forRentValue;
                if (neighbourhood.forRentValue==0) neighbourhood.histReturn[i]=0;
                neighbourhood.histForRentValue[i] = neighbourhood.forRentValue;
            }
        }
    }


    public static void createFlatsForSale() {

        int nTransactions = 0;
        Map<Neighbourhood,Double> forSaleRatioInNeighbourhoods = new HashMap<>();
        Map<Neighbourhood,Double> inheritedForSaleRatioInNeighbourhoods = new HashMap<>();

        for (Neighbourhood neighbourhood: Model.neighbourhoods.values()) {
            forSaleRatioInNeighbourhoods.put(neighbourhood,Model.DLyearlyForSaleRatio); //yearly forSaleRatio
            inheritedForSaleRatioInNeighbourhoods.put(neighbourhood,Model.DLinheritedYearlyForSaleRatio); //yearly forSaleRatio
        }

        Map<Bucket,Double> forSaleInBuckets = new HashMap<>();
        Map<Bucket,Double> inheritedForSaleInBuckets = new HashMap<>();
        Map<Bucket,Double> nSoldInBuckets = new HashMap<>();
        for (Bucket bucket: Model.buckets.values()) {
            forSaleInBuckets.put(bucket,0.0);
            inheritedForSaleInBuckets.put(bucket,0.0);
            nSoldInBuckets.put(bucket,0.0);
        }

        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.transactionsFileName));
            //0. price, 1. neighbourhoodId, 2. year, 3. month, 4. state, 5. size

            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.transactionsFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                nTransactions++;
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int neighbourhoodId = Integer.parseInt(dataLineArray[1]);
                double size = Double.parseDouble(dataLineArray[5]);
                if (size<=Model.bucketSizeIntervals[0]) {
                    size = Math.max(size,Model.bucketSizeIntervals[0]*1.001);
                }
                double state = Double.parseDouble(dataLineArray[4]);
                double price = Double.parseDouble(dataLineArray[0]);




                    Flat flat = new Flat();
                    flat.setSize(size);
                    flat.setState(state);
                    Neighbourhood neighbourhood = Model.neighbourhoods.get(neighbourhoodId);
                    Bucket bucket = flat.findBucket(neighbourhood);


                    FlatSaleRecord flatSaleRecord = Model.createFlatSaleRecord();
                    flatSaleRecord.setPeriodOfRecord(-1-Model.rnd.nextInt(12));
                    flatSaleRecord.setPrice(price);
                    flatSaleRecord.setSize(size);
                    flatSaleRecord.setState(state);
                    flatSaleRecord.setBucket(bucket);
                    flatSaleRecord.setNeighbourhoodQuality(neighbourhood.getQuality());
                    flatSaleRecord.setNewlyBuilt(false);

                    double oldValue = forSaleInBuckets.get(bucket);
                    forSaleInBuckets.replace(bucket,oldValue + forSaleRatioInNeighbourhoods.get(neighbourhood));
                    double oldValueInherited = inheritedForSaleInBuckets.get(bucket);
                    inheritedForSaleInBuckets.replace(bucket,oldValueInherited + inheritedForSaleRatioInNeighbourhoods.get(neighbourhood));
                    double oldValueNSold = nSoldInBuckets.get(bucket);
                    nSoldInBuckets.replace(bucket,oldValueNSold + 1);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Map<Neighbourhood,Double> neighbourhoodsWithForSale = new HashMap<>();
        for (Bucket bucket: forSaleInBuckets.keySet()) {
            neighbourhoodsWithForSale.putIfAbsent(bucket.getNeighbourhood(),forSaleInBuckets.get(bucket));
        }


        for (Flat flat : Model.flats.values()) {
            Bucket bucket = flat.getBucket();

            if (flat.isForRent == false && flat.getOwnerHousehold() != null && Model.rnd.nextDouble()<Model.DLyearlyForSaleRatio) {

                double oldValue = forSaleInBuckets.get(bucket);
                forSaleInBuckets.replace(bucket,oldValue - 1);
                double forSaleRatio = forSaleRatioInNeighbourhoods.get(bucket.getNeighbourhood());
                //nextInt - upper bound is exclusive
                int forSalePeriods = Model.rnd.nextInt((int) Math.ceil(forSaleRatio*12));
                flat.setNForSalePeriods(forSalePeriods+1); //in the month when a flat is taken to the market, nForSalePeriods already takes the value of 1
                flat.setForSalePrice(flat.getEstimatedMarketPrice());
                flat.setForSale(true);

                if (flat.getOwnerHousehold() != null) {
                    flat.getOwnerHousehold().setMoving(true);
                }
            }
        }

        Map<Neighbourhood,ArrayList<Household>> householdsInNeighbourhood = new HashMap<>();
        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            householdsInNeighbourhood.put(neighbourhood,new ArrayList<>());
        }
        for (Household household : Model.households.values()) {
            if (household.getHome() != null) {
                householdsInNeighbourhood.get(household.getHome().getNeighbourhood()).add(household);
            }
        }
        for (Bucket bucket : Model.buckets.values()) {
            int nInheritedFlatsToSell = (int) Math.ceil(inheritedForSaleInBuckets.get(bucket));
            for (int i = 1; i < nInheritedFlatsToSell; i++) {
                Flat flat = Model.createFlat();
                flat.size = bucket.sizeMin + Model.rnd.nextDouble() * (bucket.sizeMax-bucket.sizeMin);
                flat.state = bucket.stateMin + Model.rnd.nextDouble() * (bucket.stateMax-bucket.stateMin);
                flat.setBucket(bucket);

                double forSaleRatio = inheritedForSaleRatioInNeighbourhoods.get(bucket.getNeighbourhood());
                //nextInt - upper bound is exclusive
                int forSalePeriods = Model.rnd.nextInt((int) Math.ceil(forSaleRatio*12));
                flat.setNForSalePeriods(forSalePeriods+1); //in the month when a flat is taken to the market, nForSalePeriods already takes the value of 1
                flat.setForSalePrice(flat.getEstimatedMarketPrice());
                flat.setForSale(true);

                ArrayList<Household> householdsToDrawFrom = householdsInNeighbourhood.get(flat.getNeighbourhood());
                Household ownerHousehold = householdsToDrawFrom.get(Model.rnd.nextInt(householdsToDrawFrom.size()));

                flat.setOwnerHousehold(ownerHousehold);
                ownerHousehold.getProperties().add(flat);
            }
        }


        for (Bucket bucket : Model.buckets.values()) {
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                bucket.histNSold[i] = (int) Math.ceil(nSoldInBuckets.get(bucket)/12.0);
                bucket.histNForSale[i] = (int) Math.ceil(nSoldInBuckets.get(bucket)/12.0*Model.DLhistBucketMonthlyForSaleToSoldRatio);
            }
        }

    }

    public static void calculateHouseholdDepositsAndSetFirstBuyer() {

        setupAgeProbabilities();

        for (Household household : Model.households.values()) {
            if (household.getHome()!=null || household.getHomeUnderConstruction()!= null) {
                household.setFirstBuyer(false);
                household.setCanChangeGeoLocation(false);
                if (household.getHome()!=null) {
                    household.setPreferredGeoLocation(household.getHome().getGeoLocation());
                }
            }
            int nPeriodsSinceHouseholdPurchase;

            if (household.getHome() != null && household.getHome().getLoanContract() != null) {
                int year = loanContractYear.get(household.getHome().getLoanContract());

                nPeriodsSinceHouseholdPurchase = (Model.zeroPeriodYear-year)*12-6+Model.zeroPeriodMonth-1;

            } else if (household.getHome() != null) {
                int ageOfOldestMember = household.calculateAgeOfOldestMember();

                int ageAtHouseholdPurchase = ageProbabilities.get(ageOfOldestMember).selectObjectAccordingToCumulativeProbability(Model.rnd.nextDouble());

                nPeriodsSinceHouseholdPurchase = ageOfOldestMember-ageAtHouseholdPurchase;

            } else {
                //does not have an own home so it is like the household has been accumulating savings from the beginning
                nPeriodsSinceHouseholdPurchase = household.calculateAgeOfOldestMember();
            }



            double deposit = 0;
            int size = household.getMembers().size() + household.getChildren().size();
            Individual individual0 = household.getMembers().get(0);
            double firstWage0 = individual0.getFirstWage();
            int typeIndex0 = individual0.getTypeIndex();
            int workExperience0 = individual0.getWorkExperience();

            Individual individual1 = null;
            double firstWage1 =0;
            int typeIndex1 = 0;
            int workExperience1 = 0;
            if (household.getMembers().size()>1) {
                individual1 = household.getMembers().get(1);
                firstWage1 = individual1.getFirstWage();
                typeIndex1 = individual1.getTypeIndex();
                workExperience1 = individual1.getWorkExperience();
            }

            for (int i = 0; i < nPeriodsSinceHouseholdPurchase ; i++) {

                double wageIncome = 0;
                if (workExperience0-i>=0) {
                    wageIncome = (1-Model.taxRatePath[0])*firstWage0*Model.wageRatio[typeIndex0][workExperience0-i];
                }
                if (individual1 != null && workExperience1-i>=0) {
                    wageIncome += (1-Model.taxRatePath[0])*firstWage1*Model.wageRatio[typeIndex1][workExperience1-i];
                }

                double minSavingsRate = Math.min(1,Math.max(0,Model.savingsRateCoeff * Math.log(wageIncome*12) + Model.savingsRateConstant));

                deposit += wageIncome * minSavingsRate;
            }


            if (household.getRentHome() != null) {
                double depositMultiplier = 0.1;
                if (household.getRentHome().getGeoLocation()!=Model.capital) depositMultiplier = 0.1;
                deposit *= depositMultiplier * (1 + 0*0.1 * Model.rnd.nextDouble());
            }

            if (household.getHome()!= null && household.getHome().getLoanContract() == null) {
                double averageFirstWage = household.members.get(0).firstWage;
                if (household.members.size()==2) {
                    averageFirstWage = (household.members.get(0).firstWage + household.members.get(1).firstWage)/2;
                }

                double luckMultiplier = Model.rnd.nextDouble();

                double additionalMonthlyAverageWage = luckMultiplier*0*1;
                if (household.getPreferredGeoLocation().getId()==0) {
                    additionalMonthlyAverageWage *= 1.5;
                }
                double minAgeInYearsInMultiplierFormula = 30;
                double maxAgeInYearsInMultiplierFormula = 50;
                double multiplierForMinAge = 0.5;
                double multiplierForMaxAge = 1.5;
                additionalMonthlyAverageWage *= multiplierForMinAge + (multiplierForMaxAge-multiplierForMinAge)/(maxAgeInYearsInMultiplierFormula-minAgeInYearsInMultiplierFormula)*(OwnFunctions.doubleInRange(household.calculateAgeInYearsOfOldestMember(),minAgeInYearsInMultiplierFormula,maxAgeInYearsInMultiplierFormula)-minAgeInYearsInMultiplierFormula);
                deposit += averageFirstWage*additionalMonthlyAverageWage;
            }

            if (household.getHome()!= null && household.getHome().getLoanContract() != null) {
                LoanContract loanContract = household.getHome().getLoanContract();
                deposit += (1-Model.taxRatePath[0])*firstWage0*Model.wageRatio[typeIndex0][workExperience0] * 5;
                deposit += (1-Model.taxRatePath[0])*firstWage1*Model.wageRatio[typeIndex1][workExperience1] * 5;
            }
            household.setDeposit(deposit);



        }
    }

    public static void setUsedCSOK() {
        for (Household household : Model.households.values()) {
            if (household.children.size()>0) {
                int childrenNum = household.nChildrenForCSOK();
                double CSOK = Math.max(0,Model.usedCSOK[Math.min(Model.usedCSOK.length-1,childrenNum)]-household.utilizedCSOK);
                household.creditDeposit(CSOK);
                household.utilizedCSOK += CSOK;
            }
        }
    }

    public static void setupAgeProbabilities() {
        for (int i = 0; i < Model.lifespan; i++) {
            MappingWithWeights<Integer> mappingWithWeights = new MappingWithWeights<>();
            for (int j = 0; j <= i; j++) {
                double weight = 0;
                if (j>=18*12 && j<20*12)  weight=0.2/(2*12);
                if (j>=20*12 && j<25*12)  weight=2.1/(5*12);
                if (j>=25*12 && j<30*12)  weight=6.7/(5*12);
                if (j>=30*12 && j<35*12)  weight=12.0/(5*12);
                if (j>=35*12 && j<40*12)  weight=13.3/(5*12);
                if (j>=40*12 && j<45*12)  weight=15.4/(5*12);
                if (j>=45*12 && j<50*12)  weight=13.7/(5*12);
                if (j>=50*12 && j<55*12)  weight=10.0/(5*12);
                if (j>=55*12 && j<60*12)  weight=6.9/(5*12);
                if (j>=60*12 && j<65*12)  weight=6.2/(5*12);
                if (j>=65*12 && j<75*12)  weight=13.5/(10*12);
                if (j>=75*12) weight=0;

                mappingWithWeights.put(weight,j);

            }
            ageProbabilities.put(i,mappingWithWeights);
        }
    }

    public static void newlyBuiltDemand() {

        int demandTwoYearsBeforeZeroPeriodYear = 0;
        int demandOneYearBeforeZeroPeriodYear = 0;

        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.newlyBuiltFileName));
            //0. year of transaction, 1. price of transaction, 2. price, 3. neighbourhoodId, 4. size
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.newlyBuiltFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int year = Integer.parseInt(dataLineArray[0]);
                int month = Integer.parseInt(dataLineArray[1]);
                double price = Double.parseDouble(dataLineArray[2]);
                int neighbourhoodId = Integer.parseInt(dataLineArray[3]);
                double size = Double.parseDouble(dataLineArray[4]);

                if (size<=Model.bucketSizeIntervals[0]) {
                    size = Math.max(size,Model.bucketSizeIntervals[0]*1.001);
                }

                Flat flat = new Flat();
                flat.setSize(size);

                flat.setState(Model.highQualityStateMax);
                Bucket bucket = flat.findBucket(Model.neighbourhoods.get(neighbourhoodId));

                int historyIndex = Model.nHistoryPeriods-Model.zeroPeriodMonth-(Model.zeroPeriodYear-year)*12+month;

                if (year==Model.zeroPeriodYear - 2) demandTwoYearsBeforeZeroPeriodYear++;
                if (year==Model.zeroPeriodYear - 1) demandOneYearBeforeZeroPeriodYear++;

                if (historyIndex>=0) {
                    bucket.histNewlyBuiltDemand[historyIndex]++;
                }




            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int historyStartIndexPeriodOneYearBeforeZeroPeriodYear = Model.nHistoryPeriods-Model.zeroPeriodMonth-1*12+1;
        int historyEndIndexPeriodOneYearBeforeZeroPeriodYear = historyStartIndexPeriodOneYearBeforeZeroPeriodYear+11;
        int historyStartIndexPeriodTwoYearsBeforeZeroPeriodYear = Model.nHistoryPeriods-Model.zeroPeriodMonth-2*12+1;
        int historyEndIndexPeriodTwoYearsBeforeZeroPeriodYear = historyStartIndexPeriodTwoYearsBeforeZeroPeriodYear+11;

        for (Bucket bucket: Model.buckets.values()) {
            int[] histNewlyBuiltDemand = bucket.getHistNewlyBuiltDemand();
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                if (i>=historyStartIndexPeriodTwoYearsBeforeZeroPeriodYear && i<=historyEndIndexPeriodTwoYearsBeforeZeroPeriodYear) {
                    histNewlyBuiltDemand[i] = (int) Math.ceil(histNewlyBuiltDemand[i] * (double) Model.DLnNewlyBuiltTwoYearsBeforeZeroPeriodYear /(double) demandTwoYearsBeforeZeroPeriodYear);
                }
                if (i>=historyStartIndexPeriodOneYearBeforeZeroPeriodYear && i<=historyEndIndexPeriodOneYearBeforeZeroPeriodYear) {
                    histNewlyBuiltDemand[i] = (int) Math.ceil(histNewlyBuiltDemand[i] * (double) Model.DLnNewlyBuiltOneYearBeforeZeroPeriodYear /(double) demandOneYearBeforeZeroPeriodYear);
                }

            }
        }
    }

    public static void calculateAndSetNeighbourhoodAreas() {

        for (Neighbourhood neighbourhood: Model.neighbourhoods.values()) {
            Model.constructors.get(0).getNeighbourhoodArea().put(neighbourhood,0.0);
        }

        for (Bucket bucket : Model.buckets.values()) {
            int[] histNewlyBuiltDemand = bucket.getHistNewlyBuiltDemand();
            double additionalNeighbourhoodArea = 0;
            for (int i = Model.nHistoryPeriods - Model.DLnMonthsForNeighbourhoodAreaRatio; i < Model.nHistoryPeriods; i++) {
                additionalNeighbourhoodArea += histNewlyBuiltDemand[i] * (bucket.getSizeMin()+bucket.getSizeMax())/2;
            }
            additionalNeighbourhoodArea *= Model.DLneighbourhoodAreaRatio;
            double oldValue = Model.constructors.get(0).getNeighbourhoodArea().get(bucket.getNeighbourhood());
            double newValue = oldValue + additionalNeighbourhoodArea;
            Model.constructors.get(0).getNeighbourhoodArea().replace(bucket.getNeighbourhood(),newValue);
        }
    }

    public static void calculateLandPrices() {
            for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
                neighbourhood.setLandPrice(500000);
            }
            Model.calculateLandPrices();

            Model.priceLevel = 1; //priceLevel and realGDPLevel are needed to calculate constructionUnitCost
            Model.realGDPLevel = 1;

    }

    public static void setupFlatsReadyAndFlatsUnderConstruction() {
        for (Bucket bucket : Model.buckets.values()) {

            double averageNewlyBuiltDemand = OwnFunctions.average(bucket.histNewlyBuiltDemand,
                    Model.nHistoryPeriods + Model.period - Model.nPeriodsForAverageNewlyBuiltDemand,
                    Model.nHistoryPeriods + Model.period - 1);
            double nFlatsReady = averageNewlyBuiltDemand * Model.nPeriodsForConstruction * Model.targetNewlyBuiltBuffer * 1;
            double nFlatsUnderConstruction = averageNewlyBuiltDemand * (Model.nPeriodsForConstruction - 1) * 1; //an amount for one month will be built in period 0


            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                bucket.getNeighbourhood().getGeoLocation().histNNewlyBuiltFlats[i] += nFlatsReady + nFlatsUnderConstruction;
                bucket.getNeighbourhood().getGeoLocation().histNNewlyBuiltFlatsSold[i] += (int) Math.ceil((nFlatsReady + nFlatsUnderConstruction) * Model.DLsoldRatioOfNewlyBuiltFlats);
            }


            for (int i = 0; i <= Math.ceil(nFlatsReady+nFlatsUnderConstruction); i++) {
                Flat flat = Model.createFlat();
                flat.setBucket(bucket);
                flat.setSize(bucket.getSizeMaxForConstructionFlats() - Model.rnd.nextDouble() * (bucket.getSizeMaxForConstructionFlats() - bucket.sizeMin));
                flat.setState(Model.highQualityStateMax);
                flat.setForSale(true);
                flat.setNewlyBuilt(true);
                flat.setOwnerConstructor(Model.constructors.get(0));
                flat.calculateAndSetForSalePrice();


                if (i<=nFlatsUnderConstruction) {
                    flat.setNPeriodsLeftForConstruction(1+Model.rnd.nextInt(Model.nPeriodsForConstruction));
                    Model.constructors.get(0).getFlatsUnderConstruction().add(flat);
                    OwnFunctions.increaseDoubleMapValue(Model.maxAreaUnderConstructionInGeoLocations,flat.getGeoLocation(),flat.size*Model.DLmaxAreaUnderConstructionRatio);
                } else {
                    flat.setNForSalePeriods(1);
                    Model.constructors.get(0).getFlatsReady().add(flat);
                }

            }

        }
    }

    public static void calculateRegionalConstructionAndRenovationCosts() {
        for (Individual individual : Model.individuals.values()) {
            individual.refreshLabourMarketStatus();
        }
        Model.calculateAverageWageInGeoLocations();

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateRenovationUnitCostBase();
            geoLocation.calculateConstructionUnitCostBase();
        }

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetRenovationUnitCost();
            geoLocation.calculateAndSetConstructionUnitCost();
        }

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                geoLocation.histRenovationUnitCost[i] = geoLocation.getRenovationUnitCost();
                geoLocation.histConstructionUnitCost[i] = geoLocation.getConstructionUnitCost();
            }
        }


    }

    public static void calculateHistInvestmentValues() {
        Map<Neighbourhood,Double> investmentValue = new HashMap<>();
        for (Neighbourhood neighbourhood: Model.neighbourhoods.values()) {
            investmentValue.put(neighbourhood,0.0);
        }
        for (Flat flat: Model.flats.values()) {
            if (flat.isForRent()) {
                Neighbourhood neighbourhood = flat.getBucket().getNeighbourhood();
                double additionalInvestmentvalue = flat.getEstimatedMarketPrice()*Model.DLmonthlyInvestmentRatio;
                double oldValue = investmentValue.get(neighbourhood);
                investmentValue.replace(neighbourhood,oldValue + additionalInvestmentvalue);
            }
        }

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            double centralInvestmentValue = investmentValue.get(neighbourhood)*Model.DLinvestorShare;
            double householdInvestmentValue = investmentValue.get(neighbourhood)*(1-Model.DLinvestorShare);
            double[] histCentralInvestmentValue = neighbourhood.getHistCentralInvestmentValue();
            double[] histHouseholdInvestmentValue = neighbourhood.getHistHouseholdInvestmentValue();
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                histCentralInvestmentValue[i]=centralInvestmentValue;
                histHouseholdInvestmentValue[i]=householdInvestmentValue;
            }
        }
    }

    public static void calculateNeighbourhoodHistPriceIndices() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.priceIndexFileName),"Windows-1250");

            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.priceIndexFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int neighbourhoodId = Integer.parseInt(dataLineArray[0]);
                Neighbourhood neighbourhood = Model.neighbourhoods.get(neighbourhoodId);
                double[] histPriceIndex = neighbourhood.getHistPriceIndex();

                for (int i = 2; i < dataLineArray.length; i++) {
                    int priceIndexFirstObservationIndex = Model.nHistoryPeriods-(Model.zeroPeriodYear-Model.DLpriceIndexFirstObservationYear)*12+Model.DLpriceIndexFirstObservationMonth-Model.zeroPeriodMonth;

                    int index=priceIndexFirstObservationIndex+(i-2)*3;
                    if (index>=0 && index<Model.nHistoryPeriods) histPriceIndex[index]=Double.parseDouble(dataLineArray[i]);
                    index++;
                    if (index>=0 && index<Model.nHistoryPeriods) histPriceIndex[index]=Double.parseDouble(dataLineArray[i])+(Double.parseDouble(dataLineArray[i+1])-Double.parseDouble(dataLineArray[i]))/3*2;
                    index++;
                    if (index>=0 && index<Model.nHistoryPeriods) histPriceIndex[index]=Double.parseDouble(dataLineArray[i])+(Double.parseDouble(dataLineArray[i+1])-Double.parseDouble(dataLineArray[i]))/3*1;

                    if (index>Model.nHistoryPeriods) break;

                }


            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                neighbourhood.histPriceIndex[i] = neighbourhood.histPriceIndex[i]/neighbourhood.histPriceIndex[Model.nHistoryPeriods-1];
                neighbourhood.histPriceIndexToBeginning[i] = neighbourhood.histPriceIndex[i];
            }
        }

    }

    public static void calculateGeoLocationHistPriceIndices() {

        for (Neighbourhood neighbourhood: Model.neighbourhoods.values()) {
                neighbourhood.nFlats = 0;
        }
        for (Flat flat: Model.flats.values()) {
                flat.getBucket().getNeighbourhood().nFlats++;
        }

        for (GeoLocation geoLocation: Model.geoLocations.values()) {
            geoLocation.histPriceIndex[0] = 1;

            for (int i = 1; i < Model.nHistoryPeriods ; i++) {
                double monthlyPriceIndex = 0;
                int nFlats = 0;
                for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
                    if (neighbourhood.getGeoLocation() == geoLocation) {
                        double neighbourhoodMonthlyPriceIndex = neighbourhood.histPriceIndex[i]/neighbourhood.histPriceIndex[i-1];
                        nFlats += neighbourhood.nFlats;
                        monthlyPriceIndex += neighbourhoodMonthlyPriceIndex*neighbourhood.nFlats;
                    }
                }
                monthlyPriceIndex /= nFlats;
                geoLocation.histPriceIndex[i]=geoLocation.histPriceIndex[i-1]*monthlyPriceIndex;
            }

            for (int i = 0; i < Model.nHistoryPeriods; i++) {
                geoLocation.histPriceIndex[i] = geoLocation.histPriceIndex[i]/geoLocation.histPriceIndex[Model.nHistoryPeriods-1];
                geoLocation.histPriceIndexToBeginning[i] = geoLocation.histPriceIndex[i];
                geoLocation.histDirectPriceIndexWithNewlyBuilt[i] = 1.0;
                geoLocation.histDirectPriceIndexUsed[i] = 1.0;
                geoLocation.histDirectPriceIndexNewlyBuilt[i] = 1.0;
            }
        }


    }

    public static void calculateLastMarketPrices() {
        for (Flat flat: Model.flats.values()) {

            if (flat.forSalePrice == 0) {
                flat.calculateAndSetForSalePrice();
            }
            flat.lastMarketPrice = flat.forSalePrice;

        }
    }

    public static void createForSaleAndFlatSaleRecords() {
        for (Flat flat : Model.flats.values()) {
            if (flat.getMarketPriceDataLoader()>0 && Model.rnd.nextDouble()<0.0075) {
                ForSaleRecordTemporary forSaleRecord = new ForSaleRecordTemporary();
                forSaleRecord.periodOfRecord = -1; //DB
                forSaleRecord.bucket = flat.bucket;
                forSaleRecord.size = flat.size;
                forSaleRecord.state = flat.state;
                forSaleRecord.neighbourhoodQuality = flat.getQuality();
                forSaleRecord.isNewlyBuilt = false;
                forSaleRecord.isForcedSale = false;
                Model.forSaleRecords.add(forSaleRecord);

                FlatSaleRecord flatSaleRecord = Model.createFlatSaleRecord();
                flatSaleRecord.price = flat.getMarketPriceDataLoader();
                flatSaleRecord.periodOfRecord = -1;
                flatSaleRecord.bucket = flat.bucket;
                flatSaleRecord.size = flat.size;
                flatSaleRecord.state = flat.state;
                flatSaleRecord.neighbourhoodQuality = flat.getQuality();
                flatSaleRecord.isNewlyBuilt = false;
                flatSaleRecord.isForcedSale = false;
            }
        }
    }


    public static void findOwnerForFlat(Flat flat) {
        if (Model.rnd.nextDouble()<Model.DLinvestorShare && flat.getLoanContract() == null) {
            flat.setOwnerInvestor(Model.investors.get(0));
            Model.investors.get(0).getProperties().add(flat);
        } else {
            Household household = findHouseholdOwnerForFlat(flat);

            flat.setOwnerHousehold(household);
            household.getProperties().add(flat);

            if (flat.getLoanContract() != null) {
                flat.getLoanContract().setDebtor(household);
            }
        }


    }

    public static Household findHouseholdOwnerForFlat(Flat flat) {

        Household household = households[Model.rnd.nextInt(households.length)];
        while (household.getMembers().get(0).getFirstWage()<Model.DLminFirstWageForProperty) {
            household = households[Model.rnd.nextInt(households.length)];
        }
        return household;
    }



    public static void setAbsoluteUtilityParameters() {


            try {
                Scanner scannerForCSV = new Scanner(new File(MainRun.absoluteUtilityParametersFileName));

                if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.absoluteUtilityParametersFileName))) scannerForCSV.nextLine();

                //0. id, 1. absCoeffSize, 2. absExponentSize, 3. absCoeffState, 4. absExponentState, 5. absSigmoid1, 6. absSigmoid2;
                while (scannerForCSV.hasNextLine()) {
                    String dataLine = scannerForCSV.nextLine();
                    String[] dataLineArray = dataLine.split(",");

                    int householdId = Integer.parseInt(dataLineArray[0]);
                    double absCoeffSize = Double.parseDouble(dataLineArray[1]);
                    double absExponentSize = Double.parseDouble(dataLineArray[2]);
                    double absCoeffState = Double.parseDouble(dataLineArray[3]);
                    double absExponentState = Double.parseDouble(dataLineArray[4]);
                    double absSigmoid1 = Double.parseDouble(dataLineArray[5]);
                    double absSigmoid2 = Double.parseDouble(dataLineArray[6]);

                    Household household = Model.households.get(householdId);

                    UtilityFunctionCES utilityFunctionCES = Model.createUtilityFunctionCES();

                    household.setUtilityFunctionCES(utilityFunctionCES);
                    utilityFunctionCES.setHousehold(household);

                    household.getUtilityFunctionCES().setAbsCoeffSize(absCoeffSize);
                    household.getUtilityFunctionCES().setAbsExponentSize(absExponentSize);
                    household.getUtilityFunctionCES().setAbsCoeffState(absCoeffState);
                    household.getUtilityFunctionCES().setAbsExponentState(absExponentState);
                    household.getUtilityFunctionCES().setAbsSigmoid1(absSigmoid1);
                    household.getUtilityFunctionCES().setAbsSigmoid2(absSigmoid2);

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


    }





}
