package util;

import model.*;
import parallel.ParallelComputer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class InputDataGenerator {

    public static String folderName = "src/main/java/sampleResources";

    public static boolean rerunCreateAbsoluteUtilityParametersCsv = true; //if you change any parameters which may alter the connections between households and flats, or flat or income characteristics, then you need to recalibrate the utility parameters so please set it to true

    public static int nHouseholds = 100000;
    public static double flatsToHouseholds = 1.01;
    public static int lifespan = 1080;
    public static int nTypeIndex = 3;
    public static int nPeriodsForTimeSeriesGeneration = 3000;

    public static double[] typeShare = {0.3, 0.3, 0.4};

    public static int marriageAgeInPeriods = 26*12;
    public static double marriageProbability = 1;

    public static int[] ageInPeriodsForBirths = {27*12, 30*12}; //now every woman gives birth to a child at the age of 27 and 30

    public static double historicalYearlyPriceIncrease = 0.06;
    public static double priceRegressionNeighbourhoodQualityForSizeBaseCoeff = 5;
    public static double priceRegressionSizeBaseCoeff = 200000;
    public static double priceRegressionSizeStateCoeff = 100000;
    public static double newlyBuiltMarkup = 0.3;

    public static double sizeMin = 25;
    public static double sizeMax = 140;
    public static double stateMin = 1.0;
    public static double stateMax = 2.7;
    public static double newlyBuiltSizeMin = 50;
    public static double newlyBuiltSizeMax = 90;

    public static int zeroPeriodYear = 2018; //it should be in accordance with the configuration file used later

    public static double homeMarketPriceToFirstWage = 100;
    public static double LTVforInitialMortgageLoans = 0.7;
    public static int durationOfMortgageLoansFromIssuanceInYears = 20;
    public static double yearlyInterestRateForMortgageLoans = 0.05;

    public static double[][] neighbourhoodData = new double[20][];

    public static double[][] temporaryFlatData = new double[(int) (nHouseholds*flatsToHouseholds)][];
    public static double[][] temporaryHouseholdData = new double[(int) (nHouseholds)][];
    public static double[][] temporaryIndividualData = new double[(int) (nHouseholds*4)][];
    public static double[][] temporaryLoanContractData = new double[nHouseholds][];

    public static ArrayList<Double[]> utilityFunctionParameterSets = new ArrayList<>();
    public static ArrayList<Double[]> sampleFlatCharacteristicsForUtilityFunctionCalibration = new ArrayList<>();
    public static int nSampleFlatsForUtilityFunctionCalibration = 2000;

    public static double[][] utilityParameters = new double[nHouseholds][];


    public static int individualIndex = 0;

    public static ArrayList<ArrayList<Integer>> householdsForParallelComputing = new ArrayList<>();
    public static int nThreads = 6;


    public static void inputDataGeneration() {

        //boolean one = OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.deathProbabilityFile));


        System.out.println("number of available processors: " + Runtime.getRuntime().availableProcessors());
        createFlatsCsv();
        createHouseholdsCsv();
        createIndividualsCsv();
        createLoanContractsCsv();
        if (rerunCreateAbsoluteUtilityParametersCsv) createAbsoluteUtilityParametersCsv();
        createBirthProbabilityCsv();
        createDeathProbabilityCsv();
        createMarriageProbabilityCsv();
        createExternalDemandCsv();
        createMacroPathCsv();
        createMarriageDataCsv();
        createNewlyBuiltCsv();
        createPriceIndexCsv();
        createPriceRegressionCsv();
        createTransactionsCsv();
    }

    public static void createFlatsCsv() {

        //first we can define neighbourhoodlevel data which will be used to generate the flats

        //the columns are the following: 0 - id of geolocation;
        //                               1 - share of flats in neighbourhood (as a percent of all flats in the country)
        //                               2 - quality of neighbourhood
        //                               3 - share of flats in cities within neighbourhood (used for switchHomesToRent() in DataLoader)
        //                               4 - share of newlyBuiltFlats
        //                               5 - share of rents
        //                               6 - share of homeOwners with a loan contract
        //                               7 - share of external demand
        //                               8 - share of newlyBuilt transactions (for newlyBuiltCsv) - it is used to generate newly built flats in addition to flats in flatsCsv, but the exact (absolute) number of newly built flats added to the model is the sum of DLnNewlyBuilt2016 and DLnNewlyBuilt2017, defined in the config file
        //                               9 - share of transactions (for transactionsCsv)
        neighbourhoodData[0] = new double[]{0, 0.05, 12.0, 1, 0.02, 0.1, 0.2, 0.05, 0.02, 0.06};
        neighbourhoodData[1] = new double[]{0, 0.05, 10.0, 1, 0.02, 0.1, 0.2, 0.05, 0.02,0.06};
        neighbourhoodData[2] = new double[]{0, 0.05, 8.0, 1, 0.02, 0.1, 0.2, 0.00, 0.02, 0.06};
        neighbourhoodData[3] = new double[]{0, 0.05, 7.0, 1, 0.02, 0.1, 0.2, 0.00, 0.01, 0.05};
        neighbourhoodData[4] = new double[]{0, 0.05, 6.0, 1, 0.02, 0.1, 0.2, 0.00, 0.01, 0.05};
        neighbourhoodData[5] = new double[]{0, 0.05, 5.0, 1, 0.0, 0.0, 0.2, 0.00, 0.01, 0.035};
        neighbourhoodData[6] = new double[]{0, 0.05, 4.0, 1, 0.0, 0.0, 0.2, 0.00, 0.01, 0.035};
        neighbourhoodData[7] = new double[]{0, 0.05, 3.0, 1, 0.0, 0.0, 0.0, 0.00, 0.01, 0.035};
        neighbourhoodData[8] = new double[]{0, 0.05, 2.0, 1, 0.0, 0.0, 0.0, 0.00, 0.01, 0.035};
        neighbourhoodData[9] = new double[]{0, 0.05, 1.0, 1, 0.0, 0.0, 0.0, 0.00, 0.01, 0.035};
        neighbourhoodData[10] = new double[]{1, 0.05, 10.0, 1, 0.02, 0.1, 0.2, 0.00, 0.02, 0.06};
        neighbourhoodData[11] = new double[]{1, 0.05, 9.0, 1, 0.02, 0.1, 0.2, 0.00, 0.02, 0.06};
        neighbourhoodData[12] = new double[]{1, 0.05, 8.0, 1, 0.02, 0.1, 0.2, 0.00, 0.02, 0.05};
        neighbourhoodData[13] = new double[]{1, 0.05, 7.0, 1, 0.02, 0.1, 0.2, 0.00, 0.01, 0.05};
        neighbourhoodData[14] = new double[]{1, 0.05, 6.0, 1, 0.02, 0.1, 0.2, 0.00, 0.01, 0.05};
        neighbourhoodData[15] = new double[]{1, 0.05, 5.0, 0.0, 0.0, 0.0, 0.2, 0.00, 0.01, 0.035};
        neighbourhoodData[16] = new double[]{1, 0.05, 4.0, 0.0, 0.0, 0.0, 0.2, 0.00, 0.01, 0.035};
        neighbourhoodData[17] = new double[]{1, 0.05, 3.0, 0.0, 0.0, 0.0, 0.0, 0.00, 0.01, 0.035};
        neighbourhoodData[18] = new double[]{1, 0.05, 2.0, 0.0, 0.0, 0.0, 0.0, 0.00, 0.01, 0.035};
        neighbourhoodData[19] = new double[]{1, 0.05, 1.0, 0.0, 0.0, 0.0, 0.0, 0.00, 0.01, 0.035};

        MappingWithWeights<Integer> neighbourhoodMappingWithWeights = new MappingWithWeights<>();
        for (int i = 0; i < neighbourhoodData.length; i++) {
            neighbourhoodMappingWithWeights.put(neighbourhoodData[i][1],i);
        }

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/flats.csv", "UTF-8");
            for (int i = 0; i < nHouseholds * flatsToHouseholds; i++) {
                int neighbourhoodId = (int) neighbourhoodMappingWithWeights.selectObjectAccordingToCumulativeProbability(Model.rnd.nextDouble());
                int geoLocationId = (int) neighbourhoodData[neighbourhoodId][0];
                double size = sizeMin + Model.rnd.nextDouble() * (sizeMax - sizeMin);
                double state = stateMin + Model.rnd.nextDouble() * (stateMax - stateMin);
                double neighbourhoodQuality = neighbourhoodData[neighbourhoodId][2];
                int municipalityType = 1;
                if (Model.rnd.nextDouble()>neighbourhoodData[neighbourhoodId][3]) municipalityType = 3;
                double marketPrice = size * priceRegressionSizeBaseCoeff * neighbourhoodQuality/priceRegressionNeighbourhoodQualityForSizeBaseCoeff + size * state * priceRegressionSizeStateCoeff;
                int rented = 0;
                if (Model.rnd.nextDouble()<neighbourhoodData[neighbourhoodId][5]) rented = 1;
                //double marketPriceThreeYearsEarlier = marketPrice / Math.pow((1 + historicalYearlyPriceIncrease),3);
                //double marketPriceFourYearsEarlier = marketPrice / Math.pow((1 + historicalYearlyPriceIncrease),4);

                //0. flat_id, 1. region, 2. municipality_type, 3. nh, 4. lat, 5. state, 6. state_cat, 7. nh_cat, 8. price_indexedFor2014, 9. price_indexedFor2015, 10. price_indexedFor2018, 11. year_of_sale, 12. year_of_construction, 13. rented

                StringBuilder newRow = new StringBuilder();
                newRow.append(i + "," + geoLocationId + "," + municipalityType + "," + neighbourhoodQuality + "," + size + "," + state + "," + 0 + "," + neighbourhoodId + "," + 0 + "," + 0 + "," + marketPrice + "," + 0 + "," + 0 + "," + rented);
                printWriter.println(newRow);

                temporaryFlatData[i] = new double[]{i,geoLocationId,municipalityType,neighbourhoodQuality,size,state,neighbourhoodId,0,0,marketPrice,0,0,rented};

            }



            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating flatsCsv");
        }






    }

    public static void createHouseholdsCsv() {
        List<Integer> flatIds = new ArrayList<>();
        for (int i = 0; i < temporaryFlatData.length ; i++) {
            flatIds.add((int) temporaryFlatData[i][0]);
        }

        Collections.shuffle(flatIds,Model.rnd);

        int loanContractIndex = 0;

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/households.csv", "UTF-8");

            for (int i = 0; i < nHouseholds; i++) {
                StringBuffer newRow = new StringBuffer();
                int id1 = ++individualIndex;
                int id2 = ++individualIndex;
                int flatId = flatIds.get(i);
                int loanContractId = -1;
                if (Model.rnd.nextDouble()<neighbourhoodData[(int) temporaryFlatData[flatId][7]][6]) loanContractId = loanContractIndex++;


                temporaryHouseholdData[i] = new double[]{i, id1, id2, flatId, loanContractId};
                newRow.append(i + "," + id1 + "," + id2 + "," + flatId + "," + loanContractId);
                printWriter.println(newRow);

            }


            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating householdsCsv");
        }


    }

    public static void createIndividualsCsv() {

        MappingWithWeights<Integer> typeShareWithWeights = new MappingWithWeights<>();
        for (int i = 0; i < typeShare.length; i++) {
            typeShareWithWeights.put(typeShare[i],i);
        }

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/individuals.csv", "UTF-8");

            for (int i = 0; i < nHouseholds; i++) {

                int age = marriageAgeInPeriods/12 + Model.rnd.nextInt((lifespan - marriageAgeInPeriods) / 12);
                int parentBirthYear = zeroPeriodYear - age;
                int type = (int) typeShareWithWeights.selectObjectAccordingToCumulativeProbability(Model.rnd.nextDouble()) + 1; //in DataLoader we substract 1
                double homeMarketPrice = temporaryFlatData[(int) temporaryHouseholdData[i][3]][9];
                double firstWage = homeMarketPrice/homeMarketPriceToFirstWage;

                for (int j = 0; j < 2; j++) {
                    int id = 0;
                    int gender = 1; //0-male, 1-female

                    if (j == 0) {
                        id = (int) temporaryHouseholdData[i][1];
                    } else {
                        id = (int) temporaryHouseholdData[i][2];
                        gender = 0;
                    }




                    temporaryIndividualData[id] = new double[]{id,firstWage,type,parentBirthYear};

                    StringBuffer newRow = new StringBuffer();
                    newRow.append(id + "," + gender + "," + parentBirthYear + "," + 0 + "," + 0 + "," + 0 + "," + type + "," + firstWage + "," + 0 + "," + 0 + "," + 0 + "," + 0);
                    printWriter.println(newRow);


                    if (j==0 && age>ageInPeriodsForBirths[0]/12 && age<ageInPeriodsForBirths[0]/12 + marriageAgeInPeriods/12) {
                        int childId = ++individualIndex;
                        if (Model.rnd.nextDouble()<0.5) {
                            gender = 1;
                        } else gender = 0;
                        int childBirthYear = parentBirthYear + ageInPeriodsForBirths[0]/12;
                        temporaryIndividualData[childId] = new double[]{id,firstWage,type,childBirthYear};
                        newRow = new StringBuffer();
                        newRow.append(childId + "," + gender + "," + childBirthYear + "," + 0 + "," + 0 + "," + 0 + "," + type + "," + firstWage + "," + 0 + "," + 0 + "," + 0 + "," + id);
                        printWriter.println(newRow);
                    }

                    if (j==0 && age>ageInPeriodsForBirths[1]/12 && age<ageInPeriodsForBirths[1]/12 + marriageAgeInPeriods/12) {
                        int childId = ++individualIndex;
                        if (Model.rnd.nextDouble()<0.5) {
                            gender = 1;
                        } else gender = 0;
                        int childBirthYear = parentBirthYear + ageInPeriodsForBirths[1]/12;
                        temporaryIndividualData[childId] = new double[]{id,firstWage,type,childBirthYear};
                        newRow = new StringBuffer();
                        newRow.append(childId + "," + gender + "," + childBirthYear + "," + 0 + "," + 0 + "," + 0 + "," + type + "," + firstWage + "," + 0 + "," + 0 + "," + 0 + "," + id);
                        printWriter.println(newRow);
                    }

                    if (temporaryHouseholdData[i][4]>=0) {
                        int loanContractId = (int) temporaryHouseholdData[i][4];
                        double startingPrincipal = homeMarketPrice * LTVforInitialMortgageLoans;
                        double durationInYears = durationOfMortgageLoansFromIssuanceInYears - Math.min(1,(age-marriageAgeInPeriods/12));
                        int durationInMonths = (int) (durationInYears * 12);
                        double principal = startingPrincipal * (1.0 - durationInYears / durationOfMortgageLoansFromIssuanceInYears);
                        double yearlyInterestRate = yearlyInterestRateForMortgageLoans * 100;
                        double payment = startingPrincipal * (yearlyInterestRate/120 / (1 - Math.pow(1 + yearlyInterestRate/120, -durationOfMortgageLoansFromIssuanceInYears*12)));

                        temporaryLoanContractData[(int) temporaryHouseholdData[i][4]] = new double[]{loanContractId,principal,startingPrincipal,durationInMonths,payment,yearlyInterestRate,0,0,firstWage,zeroPeriodYear + durationInYears - durationOfMortgageLoansFromIssuanceInYears, 0, durationOfMortgageLoansFromIssuanceInYears * 12, -1};

                    }

                }


            }

            printWriter.close();


        } catch (Exception e) {
            System.out.println("Problem while generating individualsCsv");
        }
    }

    public static void createLoanContractsCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/loanContracts.csv", "UTF-8");

            StringBuilder headerLine = new StringBuilder();
            headerLine.append("0. loanContract_id, 1. principal, 2. startingPrincipal, 3. duration, 4. payment, 5. yearlyInterestRate (%), 6. isNonPerforming, 7. nNonPerformingPeriods, 8. adjustedFirstWageForFirstMember, 9. year_of_issuance, 10. -, 11. nMonthsInterestPeriod, 12. periodOfLastInterestCalculation");
            printWriter.println(headerLine);
            for (int i = 0; i < temporaryLoanContractData.length; i++) {

                if (temporaryLoanContractData[i] != null && temporaryLoanContractData[i][0]>=0) {
                    StringBuilder newRow = new StringBuilder();
                    newRow.append((int) temporaryLoanContractData[i][0] + "," + temporaryLoanContractData[i][1] + "," + temporaryLoanContractData[i][2] + "," + (int) temporaryLoanContractData[i][3] + "," + temporaryLoanContractData[i][4] + "," + temporaryLoanContractData[i][5] + "," + (int) temporaryLoanContractData[i][6] + "," + (int) temporaryLoanContractData[i][7] + "," + temporaryLoanContractData[i][8] + "," + (int) temporaryLoanContractData[i][9] + "," + temporaryLoanContractData[i][10] + "," + (int) temporaryLoanContractData[i][11] + "," + (int) temporaryLoanContractData[i][12]);
                    printWriter.println(newRow);
                }



            }


            printWriter.close();


        } catch (Exception e) {
            System.out.println("Problem while generating loanContractsCsv");
        }
    }

    public static void createAbsoluteUtilityParametersCsv() {



        addParameterSets();

        for (int i = 0; i < temporaryFlatData.length; i++) {
            if (Model.rnd.nextDouble()<(double) nSampleFlatsForUtilityFunctionCalibration/(double) temporaryFlatData.length) {
                sampleFlatCharacteristicsForUtilityFunctionCalibration.add(new Double[]{temporaryFlatData[i][4],temporaryFlatData[i][5],temporaryFlatData[i][3],temporaryFlatData[i][9]});
            }
        }

        for (int i = 0; i < nThreads; i++) {
            householdsForParallelComputing.add(new ArrayList<>());
        }

        for (int i = 0; i < nHouseholds; i++) {
            householdsForParallelComputing.get(i % nThreads).add(i);
        }

        List<Runnable> tasksCalibrateAbsoluteUtilityParametersForHousehold = householdsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (Integer integer : a) {
                    calibrateAbsoluteUtilityParametersForHousehold(integer);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksCalibrateAbsoluteUtilityParametersForHousehold);

        //for (int i = 0; i < nHouseholds; i++) {
        //    calibrateAbsoluteUtilityParametersForHousehold(i);
        //}

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/absoluteUtilityParameters.csv", "UTF-8");

            StringBuilder headerLine = new StringBuilder();
            headerLine.append("hh_id,absCoeffSize,absExponentSize,absCoeffState,absExponentState,absSigmoid1,absSigmoid2");
            printWriter.println(headerLine);
            for (int i = 0; i < temporaryHouseholdData.length; i++) {
                StringBuilder newRow = new StringBuilder();
                newRow.append(i + "," + utilityParameters[i][0] + "," + utilityParameters[i][1]  + "," + utilityParameters[i][2]  + "," + utilityParameters[i][3]  + "," + utilityParameters[i][4]  + "," + utilityParameters[i][5]);
                printWriter.println(newRow);

            }


            printWriter.close();


        } catch (Exception e) {
            System.out.println("Problem while generating absoluteUtilityParametersCsv");
        }
    }



    public static void createBirthProbabilityCsv() {
        //data in a row: typeIndex (1, 2 and 3 (and not starting from 0)
        //               ageInPeriods
        //               childrenIndex
        //               probability
        //probability indicates the probability that a woman with the given typeIndex with age ageInPeriods, having childrenIndex number of children (childrenIndex==1 - no children; childrenIndex == 2 or 3 - 1 or two children; childrenIndex == 4 - 3 or more children) gives birth to a child
        //csv does not contain a header line

        int maxChildrenIndex = 4;



        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/birthProbability.csv", "UTF-8");

            for (int i = 0; i < nTypeIndex; i++) {
                for (int j = 0; j < lifespan; j++) {
                    for (int k = 1; k <= maxChildrenIndex; k++) {
                        int probability = 0;
                        if (k == 1 && j == ageInPeriodsForBirths[0]) probability = 1;
                        if (k == 2 && j == ageInPeriodsForBirths[1]) probability = 1;

                        StringBuilder newRow = new StringBuilder();
                        newRow.append(Integer.toString(i + 1) + "," + j + "," + k + "," + probability);
                        printWriter.println(newRow);
                    }
                }
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating birthProbabilityCsv");
        }


    }

    public static void createDeathProbabilityCsv() {
        //the csv does not have a headerline, the first nTypeIndex rows stand for men (the first row for typeIndex 0), the second nTypeIndex rows for women, and in each row there are lifespan probabilities (every person reaching lifespan ageInPeriods dies)
        //men above age thresholdAgeInPeriodsForMenForDeathProbability die with probability menDeathProbabilityAboveThreshold (in each month), and similarly, women above age thresholdAgeInPeriodsForWomenForDeathProbability die with probability womenDeathProbabilityAboveThreshold
        int thresholdAgeInPeriodsForMenForDeathProbability = 70*12;
        double menDeathProbabilityAboveThreshold = 0.03/12;
        int thresholdAgeInPeriodsForWomenForDeathProbability = 70*12;
        double womenDeathProbabilityAboveThreshold = 0.025/12;

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/deathProbability.csv", "UTF-8");

            for (int i = 0; i < 2*nTypeIndex; i++) {
                StringBuilder newRow = new StringBuilder();
                for (int j = 0; j < lifespan; j++) {
                    double probability = 0;
                    if (i<nTypeIndex && j>thresholdAgeInPeriodsForMenForDeathProbability) probability = menDeathProbabilityAboveThreshold;
                    if (i>=nTypeIndex && j>thresholdAgeInPeriodsForWomenForDeathProbability) probability = womenDeathProbabilityAboveThreshold;
                    newRow.append(probability + ",");
                }
                newRow.deleteCharAt(newRow.length()-1);
                printWriter.println(newRow);

            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating deathProbabilityCsv");
        }

    }

    public static void createMarriageProbabilityCsv() {
        //the csv does not have a headerline, the first nTypeIndex rows stand for women (the first row for typeIndex 0), and in each row there are lifespan probabilities
        //men above age thresholdAgeInPeriodsForMenForDeathProbability die with probability menDeathProbabilityAboveThreshold (in each month), and similarly, women above age thresholdAgeInPeriodsForWomenForDeathProbability die with probability womenDeathProbabilityAboveThreshold


        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/marriageProbability.csv", "UTF-8");

            for (int i = 0; i < nTypeIndex; i++) {
                StringBuilder newRow = new StringBuilder();
                for (int j = 0; j < lifespan; j++) {
                    double probability = 0;
                    if (j==marriageAgeInPeriods) probability = marriageProbability;
                    newRow.append(probability + ",");
                }
                newRow.deleteCharAt(newRow.length()-1);
                printWriter.println(newRow);

            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating marriageProbabilityCsv");
        }

    }

    public static void createExternalDemandCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/externalDemand.csv", "UTF-8");

            for (int i = 0; i < neighbourhoodData.length; i++) {
                if (neighbourhoodData[i][7]>0) {
                    int nFlatsInNeighbourhood = 0;
                    for (int j = 0; j < temporaryFlatData.length; j++) {
                        if (temporaryFlatData[j][6] == i) nFlatsInNeighbourhood++;
                    }
                    StringBuilder newRow = new StringBuilder();
                    newRow.append(i + "," + "55,2.1"); //bucket which contains flats with size 55 and state 2.1
                    for (int j = 0; j < nPeriodsForTimeSeriesGeneration; j++) {
                        newRow.append("," + (int) (nFlatsInNeighbourhood * neighbourhoodData[i][7]/2)); //we divide by two since we allocate the flats into two buckets
                    }
                    printWriter.println(newRow);

                    StringBuilder newRow2 = new StringBuilder();
                    newRow2.append(i + "," + "75,2.1"); //bucket which contains flats with size 75 and state 2.1
                    for (int j = 0; j < nPeriodsForTimeSeriesGeneration; j++) {
                        newRow2.append("," + (int) (nFlatsInNeighbourhood * neighbourhoodData[i][7]/2)); //we divide by two since we allocate the flats into two buckets
                    }
                    printWriter.println(newRow2);
                }


            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating externalDemandCsv");
        }
    }

    public static void createMacroPathCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/macroPath.csv", "UTF-8");

            double realGDPLevel = 1;
            double priceLevel = 1;
            double baseRate = 0.03;
            double constructionUnitCostLevel = 1;

            double monthlyRealGDPGrowth = 0.03/12;
            double monthlyPriceLevelIncrease = 0.03/12;

            for (int i = 0; i < nPeriodsForTimeSeriesGeneration ; i++) {
                if (i>0) {
                    realGDPLevel *= 1 + monthlyRealGDPGrowth;
                    priceLevel *= 1 + monthlyPriceLevelIncrease;
                    constructionUnitCostLevel = priceLevel;
                }

                StringBuilder newRow = new StringBuilder();
                newRow.append(i + "," + realGDPLevel + "," + priceLevel + "," + baseRate + "," + constructionUnitCostLevel);
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating macroPathCsv");
        }
    }

    public static void createMarriageDataCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/marriageData.csv", "UTF-8");

            for (int i = 0; i < temporaryHouseholdData.length ; i++) {
                if (temporaryHouseholdData[i][1]>0 && temporaryHouseholdData[i][2]>0) {

                    StringBuilder newRow = new StringBuilder();
                    newRow.append((int) temporaryIndividualData[(int) temporaryHouseholdData[i][1]][3] + "," + (int) temporaryIndividualData[(int) temporaryHouseholdData[i][1]][2] + "," + (int) temporaryIndividualData[(int) temporaryHouseholdData[i][1]][1] + "," + (int) temporaryIndividualData[(int) temporaryHouseholdData[i][2]][3] + "," + (int) temporaryIndividualData[(int) temporaryHouseholdData[i][2]][2] + "," + (int) temporaryIndividualData[(int) temporaryHouseholdData[i][2]][1]);
                    //newRow.append(i + "," + temporaryIndividualData[(int) temporaryHouseholdData[i][1]][3] +  "," + temporaryIndividualData[(int) temporaryHouseholdData[i][1]][2]) + "," + temporaryIndividualData[(int) temporaryHouseholdData[i][1]][1] + "," + temporaryIndividualData[(int) temporaryHouseholdData[i][2]][3] + "," + temporaryIndividualData[(int) temporaryHouseholdData[i][1]][2] + ","); //+ temporaryIndividualData[(int) temporaryHouseholdData[i][1]][1]);
                    printWriter.println(newRow);
                }


            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating marriageDataCsv");
        }
    }

    public static void createNewlyBuiltCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/newlyBuilt.csv", "UTF-8");

            for (int i = 0; i < neighbourhoodData.length; i++) {
                if (neighbourhoodData[i][8]>0) {
                    int nFlatsInNeighbourhood = 0;
                    double neighbourhoodQuality = neighbourhoodData[i][2];
                    for (int j = 0; j < temporaryFlatData.length; j++) {
                        if (temporaryFlatData[j][6] == i) nFlatsInNeighbourhood++;
                    }

                    for (int j = 0; j < nFlatsInNeighbourhood*neighbourhoodData[i][8]; j++) {
                        StringBuilder newRow = new StringBuilder();
                        int month = 1 + Model.rnd.nextInt(12);
                        double size = newlyBuiltSizeMin + Model.rnd.nextDouble() * (newlyBuiltSizeMax - newlyBuiltSizeMin);
                        double price = (1 + newlyBuiltMarkup) * (size * priceRegressionSizeBaseCoeff * neighbourhoodQuality/priceRegressionNeighbourhoodQualityForSizeBaseCoeff + size * stateMax * priceRegressionSizeStateCoeff);
                        newRow.append("2017," + month + "," + (int) price + "," + i + "," + (int) size); //bucket which contains flats with size 55 and state 2.1
                        printWriter.println(newRow);

                        newRow.replace(0,4,"2016");
                        printWriter.println(newRow);
                    }

                }


            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating newlyBuiltCsv");
        }
    }

    public static void createPriceIndexCsv() {

        try {
            int nPeriodsToWrite = 100;
            int priceIndexFirstObservationYear = 2010;
            int priceIndexFirstObservationMonth = 1;
            double initialPriceIndex = 100;

            PrintWriter printWriter = new PrintWriter(folderName + "/priceIndex.csv", "UTF-8");
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("NeighbourhoodId,NeighbourhoodName (not used in DataLoader)");
            int priceIndexYear = priceIndexFirstObservationYear;
            int priceIndexMonth = priceIndexFirstObservationMonth;
            for (int i = 0; i < nPeriodsToWrite; i++) {
                String additionalCharacterBeforeMonth = "";
                if (priceIndexMonth<10) additionalCharacterBeforeMonth = "0";
                headerLine.append("," + priceIndexYear + "." + additionalCharacterBeforeMonth + priceIndexMonth);
                        priceIndexMonth++;
                        if (priceIndexMonth == 13) {
                            priceIndexMonth = 1;
                            priceIndexYear++;
                        }
            }
            printWriter.println(headerLine);

            for (int i = 0; i < neighbourhoodData.length; i++) {
                StringBuilder newRow = new StringBuilder();
                newRow.append(i + "," + "NH" + i);
                for (int j = 0; j < nPeriodsToWrite; j++) {
                    double priceIndex = initialPriceIndex * Math.pow(1 + historicalYearlyPriceIncrease/12,j);
                    newRow.append("," + priceIndex);
                }
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating priceIndexCsv");
        }
    }

    public static void createPriceRegressionCsv() {

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/priceRegression.csv", "UTF-8");
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("nh_id,size,size2,size_state,cons");
            printWriter.println(headerLine);

            for (int i = 0; i < neighbourhoodData.length; i++) {
                StringBuilder newRow = new StringBuilder();
                double neighbourhoodQuality = neighbourhoodData[i][2];
                double sizeCoeff = priceRegressionSizeBaseCoeff * neighbourhoodQuality/priceRegressionNeighbourhoodQualityForSizeBaseCoeff;
                newRow.append(i + "," + sizeCoeff + ",0," + priceRegressionSizeStateCoeff + ",0");
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating priceRegressionCsv");
        }
    }

    public static void createTransactionsCsv() {

        try {

            PrintWriter printWriter = new PrintWriter(folderName + "/transactions.csv", "UTF-8");
            StringBuilder headerLine = new StringBuilder();
            headerLine.append("price,neighbourhoodId,year of transaction (not used in DataLoader),month of transaction (not used in DataLoader),state,size");
            printWriter.println(headerLine);

            for (int i = 0; i < neighbourhoodData.length; i++) {
                double transactionProbability = neighbourhoodData[i][9];
                if (transactionProbability>0) {
                    int nFlatsInNeighbourhood = 0;
                    double neighbourhoodQuality = neighbourhoodData[i][2];
                    for (int j = 0; j < temporaryFlatData.length; j++) {
                        if (temporaryFlatData[j][6] == i && Model.rnd.nextDouble()<transactionProbability) {
                            StringBuilder newRow = new StringBuilder();
                            double price = temporaryFlatData[j][9];
                            double state = temporaryFlatData[j][5];
                            double size = temporaryFlatData[j][4];
                            newRow.append(price + "," + i + ",0,0," + state + "," + size);
                            printWriter.println(newRow);
                        }
                    }

                }


            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating transactionsCsv");
        }
    }


    public static synchronized void writeUtilityParameters(int id, double absCoeffSize, double absExponentSize, double absCoeffState, double absExponentState, double absSigmoid1,double absSigmoid2) {
        utilityParameters[id] = new double[]{absCoeffSize, absExponentSize, absCoeffState, absExponentState, absSigmoid1, absSigmoid2};
    }

    public static void calibrateAbsoluteUtilityParametersForHousehold(int id) {

        double lifeTimeIncome = (temporaryIndividualData[(int) temporaryHouseholdData[id][1]][1] + temporaryIndividualData[(int) temporaryHouseholdData[id][2]][1]) * 250;
        double homeSize = temporaryFlatData[(int) temporaryHouseholdData[id][3]][4];
        double homeState = temporaryFlatData[(int) temporaryHouseholdData[id][3]][5];
        double homeNeighbourhoodQuality = temporaryFlatData[(int) temporaryHouseholdData[id][3]][3];

        double bestDistance = 1000;
        Double[] bestParameters = utilityFunctionParameterSets.get(0);
        double bestDistanceAtFirstIteration = bestDistance;




            for (Double[] parameterSet : utilityFunctionParameterSets) {
                double absExponentSize = parameterSet[0];
                double absExponentState = parameterSet[1];
                double absCoeffSize = parameterSet[2];
                double absCoeffState = parameterSet[3];
                double absSigmoid1 = parameterSet[4];
                double absSigmoid2 = parameterSet[5];

                boolean iterate = true;
                int iterationIndex = 0;

                while (iterate) {


                    int bestFlatIndex = 0;
                    double bestSurplus = 0;
                    for (int i = 0; i < sampleFlatCharacteristicsForUtilityFunctionCalibration.size(); i++) {
                        Double[] sampleFlatCharacteristics = sampleFlatCharacteristicsForUtilityFunctionCalibration.get(i);
                        double flatSize = sampleFlatCharacteristics[0];
                        double flatState = sampleFlatCharacteristics[1];
                        double flatNeighbourhoodQuality = sampleFlatCharacteristics[2];
                        double flatPrice = sampleFlatCharacteristics[3];
                        double surplus = (absCoeffSize * Math.pow(flatSize, absExponentSize) * (1 + absCoeffState * Math.pow(flatState, absExponentState)) + absSigmoid1 / Math.pow(1 + Math.exp(-flatNeighbourhoodQuality), 1 / absSigmoid2)) * lifeTimeIncome - flatPrice;
                        if (surplus > bestSurplus) {
                            bestFlatIndex = i;
                            bestSurplus = surplus;
                        }
                    }

                    Double[] sampleFlatCharacteristics = sampleFlatCharacteristicsForUtilityFunctionCalibration.get(bestFlatIndex);
                    double flatSize = sampleFlatCharacteristics[0];
                    double flatState = sampleFlatCharacteristics[1];
                    double flatNeighbourhoodQuality = sampleFlatCharacteristics[2];

                    double distance = 3 - Math.min(flatSize / homeSize, homeSize / flatSize) - Math.min(flatState / homeState, homeState / flatState) - Math.min(flatNeighbourhoodQuality / homeNeighbourhoodQuality, homeNeighbourhoodQuality / flatNeighbourhoodQuality);

                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestParameters = new Double[]{absExponentSize, absExponentState, absCoeffSize, absCoeffState, absSigmoid1, absSigmoid2};
                        double part1 = Math.min(flatSize / homeSize, homeSize / flatSize);
                        double part2 = Math.min(flatState / homeState, homeState / flatState);
                        double part3 = Math.min(flatNeighbourhoodQuality / homeNeighbourhoodQuality, homeNeighbourhoodQuality / flatNeighbourhoodQuality);
                        //System.out.println(id + " " + distance + " " + part1 + " " + part2 + " " + part3);
                        writeUtilityParameters(id,bestParameters[0],bestParameters[1],bestParameters[2],bestParameters[3],bestParameters[4],bestParameters[5]);
                    }

                    if (iterationIndex==0) {
                        if (distance<bestDistanceAtFirstIteration) {
                            bestDistanceAtFirstIteration = distance;

                        } else {
                            iterate = false;
                        }
                    }


                    iterationIndex ++;
                    if (iterationIndex<1000) {
                        absCoeffSize *= (1 + 0.1 * (Model.rnd.nextDouble() - 0.5));
                        absCoeffState *= (1 + 0.1 * (Model.rnd.nextDouble() - 0.5));
                        absExponentSize *= (1 + 0.1 * (Model.rnd.nextDouble() - 0.5));
                        absExponentState *= (1 + 0.1 * (Model.rnd.nextDouble() - 0.5));
                        absSigmoid1 *= (1 + 0.1 * (Model.rnd.nextDouble() - 0.5));
                        absSigmoid2 *= (1 + 0.1 * (Model.rnd.nextDouble() - 0.5));
                    } else {
                        System.out.println(id + " " + bestDistance);
                        iterate = false;
                    }




                }

                //System.out.println("distance: " + distance + "/" + bestDistance);
            }




    }

    public static void addParameterSets() {

        utilityFunctionParameterSets.add(new Double[]{0.65,0.7,0.007,0.5,0.1,0.1});

        try {
            Scanner scannerForCSV = new Scanner(new File("src/main/java/sampleResources/utilityFunctionParameterSets.csv"));

            scannerForCSV.nextLine();//header line
            //0.ar_indexalt,1. lat,2. state,3.nh,4.wage,5.par_coeffsize,6.par_expsize,7.par_coeffstate,8.par_expstate,9.par_sigmoidscale,10.par_sigmoidslope,11.dist_measure,12.atlagber1,13.atlagber2
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                double dist_measure = Double.parseDouble(dataLineArray[11]);
                double absCoeffSize = Double.parseDouble(dataLineArray[5]);
                double absExponentSize = Double.parseDouble(dataLineArray[6]);
                double absCoeffState = Double.parseDouble(dataLineArray[7]);
                double absExponentState = Double.parseDouble(dataLineArray[8]);
                double absSigmoid1 = Double.parseDouble(dataLineArray[9]);
                double absSigmoid2 = Double.parseDouble(dataLineArray[10]);
                double neighbourhoodQuality = Double.parseDouble(dataLineArray[3]);
                double lifeTimeIncome = Double.parseDouble(dataLineArray[4]);
                UtilityFunctionCES utilityFunctionCES = new UtilityFunctionCES();
                utilityFunctionCES.absCoeffSize = absCoeffSize;
                utilityFunctionCES.absExponentSize = absExponentSize;
                utilityFunctionCES.absCoeffState = absCoeffState;
                utilityFunctionCES.absExponentState = absExponentState;
                utilityFunctionCES.absSigmoid1 = absSigmoid1;
                utilityFunctionCES.absSigmoid2 = absSigmoid2;
                utilityFunctionCES.sampleNeighbourhoodQuality = neighbourhoodQuality;
                utilityFunctionCES.sampleLifeTimeIncome= lifeTimeIncome;
                if (dist_measure < 0.1 && Model.rnd.nextDouble()<1) {
                    StringBuilder newRow = new StringBuilder();
                    newRow.append("utilityFunctionParameterSets.add(new Double[]{" + absExponentSize + "," + absExponentState + "," + absCoeffSize + "," + absCoeffState + "," + absSigmoid1 + "," + absSigmoid2 + "});");

                    System.out.println(newRow);
                    utilityFunctionParameterSets.add(new Double[]{absExponentSize,absExponentState,absCoeffSize,absCoeffState,absSigmoid1,absSigmoid2});

                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
