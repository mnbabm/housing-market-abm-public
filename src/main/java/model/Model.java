package model;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parallel.ParallelComputer;
import util.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public class Model {

    public static final Logger logger = LoggerFactory.getLogger(Model.class);
    public static ParametersToOverride parametersToOverride;
    public static Random rnd = new Random();
    public static Random[] rndArray;
    public static int numberOfRun = 0;
    public static int nThreads = 8; //(int) Math.ceil(2 * Runtime.getRuntime().availableProcessors());
    public static String csvNameForPrintGivenSeriesToCsv = "src/outputs/selectedSeries.csv";
    public static Map<Integer, Household> miscHH = new HashMap<>();
    public static int sobolRowIndex = 0; //for sensitivity analysis; if it is set to 0, parameter randomization is not set according to the sobol points, if it is 1, it refers to the row with 0.5-0.5
    public static int absoluteMaxDeviationForParameters = 0; //for sensitivity analysis; if it is set to 0, parameter randomization is omitted
    public enum Phase {
        SETUP, //including EmpiricalDataLoader
        BEGINNINGOFPERIOD,
        INVESTMENTDECISIONS,
        HOUSINGMARKETSUPPLY,
        FICTIVEDEMANDFORRENT,
        CAHAICALCULATIONS,
        FICTIVECHOICEOFHOUSEHOLDS,
        BIDSFORFLATS,
        CONSTRUCTIONPURCHASES,
        INVESTMENTPURCHASES,
        HOUSEHOLDPURCHASES,
        CONSTRUCTFLATS,
        RENOVATEFLATS,
        RENTALMARKET,
        CONSUMEANDSAVE,
        ENDOFPERIOD
    }

    public static Phase phase = Phase.SETUP;
    public static int nIterations;


    //I. Parameters from configuration file

    public static int nPeriods;
    public static int maxNPeriods;
    public static int nHistoryPeriods;
    public static int zeroPeriodYear;
    public static int zeroPeriodMonth;
    public static boolean simulationWithShock;
    public static boolean simulationWithCovid;
    public static int moratoryStartPeriodWithCovid;
    public static int moratoryEndPeriodWithCovid;
    public static double nMinMonthsForSavingsConsideringMoratory;

    public static boolean simulationWithSingleGeoLocation;
    public static int singleGeoLocationId;

    public static int nTypes;
    public static int lifespan;
    public static int retirementAgeInPeriods;
    public static double[] pensionReplacementRate;
    public static int[] minUnemploymentPeriods;
    public static int maxNChildren;
    public static int minAgeInPeriodsToBuyOrRentAFlatAsSingleHousehold;
    public static int nPeriodsToLookAheadToCalculateLifeTimeIncome;

    public static double savingsRateConstant;
    public static double savingsRateCoeff;
    public static double minConsumptionRate;
    public static int nPeriodsUntilMinConsumption;
    public static double minConsumptionPerCapitaLower;
    public static double minConsumptionPerCapitaUpper;
    public static double minConsumptionPerCapitaLowerThreshold;
    public static double minConsumptionPerCapitaUpperThreshold;
    public static double weightOfAdditionalAdultsInMinConsumption;
    public static double weightOfChildrenInMinConsumption;
    public static int minAgeInPeriodsToCountAsAdditionalAdultInMinConsumnption;
    public static double rangeMinForHomeValueInheritance;
    public static double rangeMaxForHomeValueInheritance;
    public static double ratioOfZeroInheritanceWithLessThan20MonthInheritance;
    public static double ratioOfZeroInheritanceWithLessThan30MonthInheritance;

    public static boolean marketPriceOnRegression;

    public static double forcedSaleDiscount;
    public static double realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount;
    public static double additionalForcedSaleDiscount;
    public static double monthlyForSalePriceDecrease;
    public static double minForSalePriceForRegression;
    public static double minForSalePrice;
    public static double maxSizeValueForSize2;
    public static double minStateValueForSizeState;
    public static double priceRatioMinRatio;
    public static double priceRatioMaxRatio;
    public static int nFlatsToLookAt;
    public static int minNFlatsToLookAtToShiftFlatIndices;
    public static int maxShiftInFlatIndicesInChooseBestFlat;
    public static int maxNChecksFromBucketOfNewlyBuiltBestFlat;
    public static double mayRenovateWhenBuyingRatio;
    public static double renovationStateIncreaseWhenBuying;
    public static int maxNIterationsForHouseholdPurchases;
    public static double[] canChangeGeoLocationProbability;
    public static int nFictiveFlatsForSalePerBucket;

    public static double[] bucketSizeIntervals;
    public static double[] bucketStateIntervals;

    public static double sizeDistanceRatioThresholdForClosestNeighbourCalculation;
    public static double stateDistanceRatioThresholdForClosestNeighbourCalculation;
    public static double sizeWeightInClosestNeighbourCalculation;
    public static double stateWeightInClosestNeighbourCalculation;
    public static double maxSizeDifferenceRatio;
    public static double maxStateDifferenceRatio;

    public static int ageInYearsForMandatoryMoving;
    public static double probabilityOfMandatoryMoving;
    public static double probabilityOfAssessingPotentialNewHomes;
    public static double baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes;
    public static double yearlyDiscountForProbabilityOfAssessingPotentialNewHomes;
    public static double thresholdRatioForMoving;
    public static double thresholdPriceDifferenceForMoving;
    public static double lowQualityNeighbourhoodMovingProbabilityMultiplier;
    public static double lowQualityMax;
    public static double middleQualityNeighbourhoodMovingProbabilityMultiplier;
    public static double middleQualityMax;
    public static double topQualityNeighbourhoodMovingProbabilityMultiplier;
    public static double linearlyDecreasingCapitalProbabilityMultiplierInFirstNPeriods;
    public static int firstNPeriodsForLinearlyDecreasingCapitalProbabilityMultiplier;

    public static int nMonthsToWithdrawHomeFromMarket;
    public static int minNMonthsOfOwnHomeOnMarketToBuyFlat;
    public static int nPeriodsForAverageNForSaleToNSold;
    public static double targetNForSaleToNSold;
    public static double coeffProbabilityNForSaleToNSoldAdjustment;
    public static double minForSaleToNSoldProbabilityAdjustment;
    public static double maxForSaleToNSoldProbabilityAdjustment;

    public static double sensitivityInLifetimeIncomeMultiplier;
    public static double cyclicalAdjusterPriceIndexCoeff;
    public static int cyclicalAdjusterBasePeriod;
    public static double coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy;

    public static double reservationMarkup;
    public static double sellerReservationPriceDecrease;
    public static double adjusterProbabilityOfPlacingBid;
    public static double newlyBuiltUtilityAdjusterCoeff1;
    public static double newlyBuiltUtilityAdjusterCoeff2;
    public static double reservationPriceShare;
    public static double maxForSaleMultiplier;
    public static double maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat;
    public static double powerInAdjustmentInReservationPrice;
    public static int maxNBidsPlacedPerHousehold;

    public static double minYearlyInterestRate;
    public static double maxYearlyInterestRate;
    public static double minMarketPriceForBridgeLoan;
    public static int bridgeLoanDuration;
    public static int renovationLoanDuration;
    public static double durationIncreaseInIncreaseLoanForRenovation;
    public static int nNonPerformingPeriodsForRestructuring;
    public static int nNonPerformingPeriodsForForcedSale;
    public static int nNonPerformingPeriodsForOwnSale;
    public static int ageCategory1StartingAgeInPeriods;
    public static int ageCategory2StartingAgeInPeriods;
    public static int minDurationForRestructuring;
    public static int durationIncreaseInRestructuring;
    public static double incomeRatioForBankFromFlatForSalePrice;
    public static int nPeriodsInNegativeKHR;
    public static double additionalDSTI;
    public static double firstBuyerAdditionalDSTI;
    public static double firstBuyerAdditionalLTV;
    public static double firstBuyerAdditionalLTVForZOP;
    public static int startOfFirstBuyerZOP;
    public static boolean firstBuyerZOPSpecialCriteria;
    public static int firstBuyerZOPSpecialCriteriaMaxAge;
    public static double unemploymentRateAdjusterInConsumptionPrescription = 1;
    public static double yearlyInterestRateRegressionConstant;
    public static double yearlyInterestRateRegressionCoeffLnWageIncome;
    public static double yearlyInterestRateRegressionCoeffLTV;
    public static double yearlyInterestRateRegressionCoeffAgeCategory1;
    public static double yearlyInterestRateRegressionCoeffAgeCategory2;
    public static double bridgeLoanToValue;

    public static int nPeriodsForConstruction;
    public static double sizeMaxOfConstructionFlatsToSizeMinInLargestSizeBucket;
    public static double sizeMaxOfFictiveFlatsToSizeMinInLargestSizeBucket;
    public static double[] constructionUnitCostBase;
    public static double constructionMarkupRatio1;
    public static double constructionMarkupRatio1Level;
    public static double constructionMarkupRatio2;
    public static double constructionMarkupRatio2Level;
    public static double constructionMarkupInFirstPeriods;
    public static int firstNPeriodsForFixedMarkup;
    public static double maxConstructionPriceToMarketPriceWithProperConstructionCostCoverage;
    public static double constructionCostCoverageNeed;
    public static int nForSalePeriodsToStartAdjustingConstructionForSalePrice;
    public static double parameterForConstructionForSalePriceAdjustment;
    public static double maxFlatStateToLandPriceStateForConstructionPurchase;
    public static double landPriceAdjuster;
    public static int landPriceDecreaseAdjusterStartPeriod;
    public static double landPriceDecreaseAdjusterMaxValue;
    public static double landPriceDecreaseAdjusterBase;
    public static double ZOPMonthlyInteresRate;
    public static double ZOPConstructionCost;
    public static int ZOPStartingPeriod;
    public static int ZOPStartingPeriodForFictiveFlats;
    public static int introductionOfZOP;
    public static double ZOPLimit;
    public static double ZOPAdditionalUtility;
    public static int introductionOfBabyloan;
    public static int endOfBabyloan;
    public static int nOfMaxChildrenBeforeBabyloan;
    public static int maxAgeInYearsForBabyloan;
    public static int maxAgeInYearsForBabyloanIsMoving;
    public static int babyloanDuration;
    public static double babyloanAmount;
    public static int nPeriodBabyloanSuspension;
    public static double ratioOfDecreasePrincipalInCaseOfChildBirth;
    public static double babyloanDSTIPayment;
    public static double babyloanPayment;
    public static int maxCapacityOfNewBabyloan;

    public static int nPeriodsForAverageNewlyBuiltDemand;
    public static double targetNewlyBuiltBuffer;
    public static double maxNFlatsToBuildInBucketToAverageNewlyBuiltDemandRatio;
    public static double maxFlatSizeForLandPrice;
    public static double maxFlatStateForLandPrice;
    public static double constructionAreaNeedRatio;
    public static boolean monthlyConstructionAreaLimit;

    public static double renovationProbability;
    public static double renovationToConstructionUnitCostBase;
    public static double renovationUnitCostAverageWageCoeff;
    public static double coeffRenovationRatio;
    public static int firstNPeriodsForRenovationNormalQuantity;
    public static double stateDepreciation;
    public static int nPeriodsInHighQualityBucket;
    public static double renovationCostBuffer;
    public static double absCoeffStateAdjuster;

    public static int capitalId;
    public static int agglomerationId;

    //Price and price index regression
    public static int nPeriodsForFlatSaleRecords;
    public static int nPeriodsForFlatSaleRecordsToUseInPriceRegression;
    public static int nFlatsForPriceRegression;
    public static int nPeriodsUntilCopyingPriceRegressionCoeffients;
    public static double maxChangeInRegressionParameters;
    public static int minNObservationForPriceIndex;
    public static int baseMinPeriodForPriceIndexToBeginning;
    public static int baseMaxPeriodForPriceIndexToBeginning;
    public static boolean size2inPriceIndexRegression;
    public static boolean stateInDirectPriceIndexRegression;
    public static boolean useTransactionWeightsForPriceIndex;
    public static boolean useForcedSaleForPriceIndex;

    //Rent parameters
    public static double minRentRatio;
    public static double maxRentRatio;
    public static int ageInYearsToConsiderSomebodyOldEnoughToRentForSure;
    public static double coeffOfFirstWageRegardingRentalProbability;
    public static double rentToPrice;
    public static int nMaxPeriodsForRent;
    public static double rentMarkupPower;
    public static double rentMarkupCoeff;
    public static double rentMarkupUtilizationRatioCap;
    public static int nPeriodsForAverageNFictiveRentDemand;
    public static int nPeriodsForAverageReturn;
    public static double expectedReturnCapitalGainCoeff;
    public static int nPeriodsForUtilizationRatio;
    public static double targetUtilizationRatio;
    public static double minUtilizationRatioForTargetUtilizationRatioToUtilizationRatioInRentSaleProbability;
    public static double minTargetRatioForInvestmentProbability;
    public static double stepInSearchingTargetRatioForInvestmentProbability;
    public static double monthlyInvestmentRatioConstantCentralInvestor;
    public static double monthlyInvestmentRatioCoeffExpectedReturnCentralInvestor;
    public static double minDepositToInvest;
    public static double yearlyRentSaleProbabilityAtZeroExpectedReturnSpread;
    public static double minYearlyExpectedReturnSpreadForZeroRentSaleProbability;
    public static double targetUtilizationRatioAdjusterInHouseholdRentSaleProbability;
    public static double householdRentSaleProbabilityScaler;
    public static double householdInvestmentProbabilityPower;
    public static double householdInvestmentProbabilityCoeff;
    public static double maxHouseholdInvestmentProbability;
    public static double maxPlannedInvestmentValueToAggregateMarketValue;

    //Demographic parameters
    public static int[] ageInPeriodsForFirstWorkExperience;
    public static double inheritorDistanceTypeCoeff;
    public static double inheritorDistanceFirstWageRatioCoeff;
    public static double inheritorDistancePreferredGeoLocationCoeff;

    public static int maxAgeInPeriodsForChildForCSOK;
    public static double[] newlyBuiltCSOK;
    public static double[] usedCSOK;
    public static double[] falusiCSOK;
    public static int introductionOfFalusiCSOK;
    public static int endOfFalusiCSOK;
    public static double neighbourhoodQualityThresholdForFalusiCSOK;
    public static double ratioOfHouseholdsWith2ChildrenEligibleFor3childrenCSOK;
    public static double ratioOfHouseholdsWith1ChildEligibleFor3childrenCSOK;
    public static double ratioOfHouseholdsWith2ChildrenEligibleFor3childrenFalusiCSOK;
    public static double minFirstWageForCSOK;
    public static double[] familyBenefit;
    public static double minSizeForCSOK;
    public static double depositInheritanceRatio;

    //Parameters for DataLoader
    public static double DLrentalMarketBuffer;
    public static double DLinvestorShare;
    public static double DLminFirstWageForProperty;
    public static int DLnNewlyBuiltOneYearBeforeZeroPeriodYear;
    public static int DLnNewlyBuiltTwoYearsBeforeZeroPeriodYear;
    public static double DLyearlyForSaleRatio;
    public static double DLinheritedYearlyForSaleRatio;
    public static double DLsoldRatioOfNewlyBuiltFlats;
    public static double DLmaxAreaUnderConstructionRatio;
    public static double DLneighbourhoodAreaRatio;
    public static int DLnMonthsForNeighbourhoodAreaRatio;
    public static double DLmonthlyInvestmentRatio;
    public static int DLpriceIndexFirstObservationYear;
    public static int DLpriceIndexFirstObservationMonth;
    public static double DLyearlyInterestRateRegressionConstant;
    public static double DLyearlyInterestRateRegressionCoeffLnWageIncome;
    public static double DLyearlyInterestRateRegressionCoeffLTV;
    public static double DLyearlyInterestRateRegressionCoeffAgeCategory1;
    public static double DLyearlyInterestRateRegressionCoeffAgeCategory2;
    public static double DLbridgeLoanToValue;
    public static double DLthresholdRatioForRentalNeighbourhood;
    public static int DLminAgeForRentalProbability;
    public static double DLrentalProbabilityForMinAge;
    public static int DLmaxAgeForRentalProbability;
    public static double DLrentalProbabilityForMaxAge;
    public static double DLminFirstWage;
    public static int DLmaxItersToSwitchHomesToRent;
    public static double DLhistBucketMonthlyForSaleToSoldRatio;
    public static double[] DLrentalRatio;


    //I.A Parameters from CSV files
    public static double[][][] birthProbability; //type X lifespan X childrenNum
    public static double[][] marriageProbability; //type X lifespan
    public static double[][][] deathProbability; //gender X type X lifespan

    //I.B Data files from CSV files
    public static File marriageDataFile;


    //II. Derived parameters from configuration file

    public static double[][] wageRatio; //type X lifespan
    public static double[][] unemploymentRatesPath; //nPeriod X nTypes
    public static double[][] unemploymentProbabilitiesPath;  //nPeriod X nTypes

    public static double[] realGDPLevelPath;
    public static double[] priceLevelPath;
    public static double[] yearlyBaseRatePath;
    public static double[] yearlyInterestRateRegressionConstantDeviationPath;
    public static double[] LTVPath;
    public static double[] DSTIPath;
    public static int[] maxDurationPath;
    public static double[] taxRatePath;
    public static double[] constructionUnitCostIndexPath;

    public static double[] loanOneYearFixationSharePathPoints;
    public static double[] loanFiveYearFixationSharePathPoints;
    public static double[] loanFixedSharePathPoints;
    public static double[] loanOneYearFixationSpreadPathPoints;
    public static double[] loanFiveYearFixationSpreadPathPoints;
    public static double[] loanFixedSpreadPathPoints;

    public static double[][] unemploymentRatesShockPath; //nPeriod X nTypes
    public static int[][] unemploymentProbabilitiesShockPath;  //nPeriod X nTypes
    public static double[] realGDPLevelShockPath;
    public static double[] priceLevelShockPath;
    public static double[] yearlyBaseRateShockPath;

    public static double highQualityStateMin;
    public static double highQualityStateMax;

    //Parameters to set in setup
    public static GeoLocation capital;
    public static GeoLocation agglomeration;
    public static GeoLocation singleGeoLocation;
    public static double maxBucketSize;

    //III. Periodical values
    public static int period = 0;
    public static boolean iterateHouseholdPurchases = true;

    public static double[] unemploymentRates;
    public static double[] unemploymentProbabilities;
    public static double[] unemploymentEndingProbabilities;

    public static double realGDPLevel;
    public static double priceLevel;
    public static double yearlyBaseRate;
    public static double yearlyInterestRateRegressionConstantDeviation;
    public static double LTV;
    public static double DSTI;
    public static int maxDuration;
    public static double taxRate;
    public static double constructionUnitCostIndex;

    public static double loanOneYearFixationShare;
    public static double loanFiveYearFixationShare;
    public static double loanFixedShare;
    public static double loanOneYearFixationSpread;
    public static double loanFiveYearFixationSpread;
    public static double loanFixedSpread;
    public static boolean isMoratoryPeriod;
    public static boolean ZOPLimitReached = false;
    public static double cumulatedZOPAmount;

    public static double maxAreaUnderConstruction;

    public static double predictedRenovationUnitCost;

    public static double renovationDemand;
    public static double renovationQuantity;

    public static double sumOwnHomePrice;
    public static double sumOwnHomePurchaserFirstWage;
    public static int nBabyLoanApplications;


    public static MappingWithWeights<Neighbourhood> neighbourhoodInvestmentProbabilities = new MappingWithWeights<>();

    public static double[] nHouseholdsInAgeIntervalsBudapestBuying;
    public static double[] sumFlatValueInAgeIntervalsBudapest;
    public static double[] nHouseholdsInAgeIntervalsVidekBuying;
    public static double[] sumFlatValueInAgeIntervalsVidek;


    //IV. State variables (outside of configuration)

    public static Map<Bucket, int[]> externalDemand = new HashMap<>();
    public static double[] histAverageRealPriceToFirstWage;
    public static double[] histNominalGDPLevel;


    //V. Central Maps and Lists
    //Maps for objectd with an ID
    public static Map<Integer, Household> households = new HashMap<Integer, Household>();
    public static Map<Integer, Flat> flats = new HashMap<Integer, Flat>();
    public static Map<Integer, LoanContract> loanContracts = new HashMap<Integer, LoanContract>();
    public static Map<Integer, Individual> individuals = new HashMap<Integer, Individual>();
    public static Map<Integer, GeoLocation> geoLocations = new HashMap<Integer, GeoLocation>();
    public static Map<Integer, Neighbourhood> neighbourhoods = new HashMap<Integer, Neighbourhood>();
    public static Map<Integer, Bucket> buckets = new HashMap<Integer, Bucket>();
    public static Map<Integer, Bank> banks = new HashMap<Integer, Bank>();
    public static Map<Integer, UtilityFunctionCES> utilityFunctionsCES = new HashMap<Integer, UtilityFunctionCES>();
    public static Map<Integer, PriceRegressionFunctionLinear> priceRegressionFunctionsLinear = new HashMap<Integer, PriceRegressionFunctionLinear>();
    public static Map<Integer, Constructor> constructors = new HashMap<Integer, Constructor>();
    public static Map<Integer, FlatSaleRecord> flatSaleRecords = new HashMap<Integer, FlatSaleRecord>();
    public static Map<Integer, Investor> investors = new HashMap<Integer, Investor>();

    public static Map<GeoLocation, ArrayList<Flat>> flatsForSaleInGeoLocations = new HashMap<>();
    public static Map<Bucket, ArrayList<Flat>> nonNewlyBuiltFlatsForSaleInBuckets = new HashMap<>();
    public static Map<Neighbourhood, ArrayList<Flat>> flatsForSaleInNeighbourhoodsForConstructionPurchases = new HashMap<>();
    public static Map<Bucket, ArrayList<Flat>> flatsForSaleForInvestment = new HashMap<>();
    public static ArrayList<Flat> fictiveFlatsForSale = new ArrayList<>();
    public static ArrayList<Flat> fictiveNewlyBuiltFlatsForSale = new ArrayList<>();
    public static ArrayList<ForSaleRecordTemporary> forSaleRecords = new ArrayList<>();
    public static Map<GeoLocation, ArrayList<Flat>> fictiveFlatsForRentInGeoLocations = new HashMap<>();
    public static Map<GeoLocation, ArrayList<Flat>> flatsForRentInGeoLocations = new HashMap<>();


    public static ArrayList<Map<Integer, Household>> householdsForParallelComputing = new ArrayList<>();
    public static ArrayList<Map<Integer, Flat>> flatsForParallelComputing = new ArrayList<>();
    public static ArrayList<Map<Integer, Individual>> individualsForParallelComputing = new ArrayList<>();
    public static ArrayList<Map<Integer, GeoLocation>> geoLocationsForParallelComputing = new ArrayList<>();
    public static ArrayList<Map<Integer, LoanContract>> loanContractsForParallelComputing = new ArrayList<>();

    public static Map<GeoLocation, ArrayList<Flat>> sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes = new HashMap<>();
    public static Map<Bucket, ArrayList<Flat>> sampleFlatsForSaleInBucketsForAssessingPotentialNewHomes = new HashMap<>();


    public static Map<Integer,Individual> individualsToRemoveFromMap = new HashMap<>();
    public static ArrayList<Individual> individualsToAddtoMap = new ArrayList<>();
    public static ArrayList<Individual> individualsInArrayListAtBeginningOfPeriod = new ArrayList<>();

    public static ArrayList<Household> householdsInRandomOrder = new ArrayList<>();
    public static ArrayList<Flat> flatsInRandomOrder = new ArrayList<>();

    public static Map<GeoLocation, Double> maxAreaUnderConstructionInGeoLocations = new HashMap<>();

    //VI. Derived variables
    public static int highestIdForGeoLocations;
    public static int highestIdForNeighbourhoods;

    //VII. OutputData
    public static double[][] outputData;
    public static String runSpecification = "base specification";
    public static ModelGUI modelGUI;

    public static int nHouseholds;
    public static int nFlats;
    public static int nLoanContracts;
    public static int nIndividuals;
    public static int nGeoLocations;
    public static int nNeighbourhoods;
    public static int nBuckets;
    public static int nBanks;
    public static int nUtilityFunctionsCES;
    public static int nPriceRegressionFunctionsLinear;
    public static int nConstructors;
    public static int nFlatSaleRecords;
    public static int nInvestors;

    public static double elapsedTimePeriod;
    public static double elapsedTimeBeginningOfPeriod;
    public static double elapsedTimeRefreshPriceRegressions;
    public static double elapsedTimeInvestmentDecisions;
    public static double elapsedTimeHousingMarketSupply;
    public static double elapsedTimeFictiveDemandForNewlyBuiltFlats;
    public static double elapsedTimeConstructionPurchases;
    public static double elapsedTimeInvestmentPurchases;
    public static double elapsedTimeHouseholdPurchases;
    public static double elapsedTimeConstructFlats;
    public static double elapsedTimeRenovateFlats;
    public static double elapsedTimeFictiveDemandForRent;
    public static double elapsedTimeRentalMarket;
    public static double elapsedTimeConsumeAndSave;
    public static double elapsedTimeEndOfPeriod;
    public static double elapsedTimeInsertIntoDB;

    public static boolean CAHAIForFTB;
    public static double[] affordabilityAgeIntervals;
    public static double[] affordabilitySizeIntervalsBudapest;
    public static double[] affordabilitySizeIntervalsVidek;

    //helper variables:

    public static int soboli;
    public static int sobolj;


    private Model() {

    }


    public static void setup(String configFileName) {

        rnd.setSeed(106000*(numberOfRun+1));
        rndArray = new Random[nThreads];
        for (int i = 0; i < rndArray.length; i++) {
            rndArray[i] = new Random();
            rndArray[i].setSeed(Model.rnd.nextInt(9999999));
        }
        Configuration.loadConfigurationParameters(configFileName);

        if (absoluteMaxDeviationForParameters>0) randomizeParameters();

        if (parametersToOverride != null) parametersToOverride.overrideParameters();

        for (int i = 0; i < nThreads; i++) {
            householdsForParallelComputing.add(new HashMap<Integer, Household>());
            flatsForParallelComputing.add(new HashMap<Integer, Flat>());
            individualsForParallelComputing.add(new HashMap<Integer, Individual>());
            geoLocationsForParallelComputing.add(new HashMap<Integer, GeoLocation>());
            loanContractsForParallelComputing.add(new HashMap<Integer, LoanContract>());
        }

        if (unemploymentRates == null) unemploymentRates = new double[nTypes];
        if (unemploymentProbabilities == null) unemploymentProbabilities = new double[nTypes];
        if (unemploymentEndingProbabilities == null) unemploymentEndingProbabilities = new double[nTypes];

        if (nHouseholdsInAgeIntervalsBudapestBuying == null) nHouseholdsInAgeIntervalsBudapestBuying = new double[affordabilityAgeIntervals.length/2];
        if (sumFlatValueInAgeIntervalsBudapest == null) sumFlatValueInAgeIntervalsBudapest = new double[affordabilityAgeIntervals.length/2];
        if (nHouseholdsInAgeIntervalsVidekBuying == null) nHouseholdsInAgeIntervalsVidekBuying = new double[affordabilityAgeIntervals.length/2];
        if (sumFlatValueInAgeIntervalsVidek == null) sumFlatValueInAgeIntervalsVidek = new double[affordabilityAgeIntervals.length/2];


        histNominalGDPLevel = new double[nHistoryPeriods + nPeriods];
        for (int i = 0; i < nHistoryPeriods; i++) {
            histNominalGDPLevel[i] = 1;
        }

        setPeriodicalValues();

        if (MainRun.runMode==1) DataLoader.setup();

        capital = geoLocations.get(capitalId);
        agglomeration = geoLocations.get(agglomerationId);
        singleGeoLocation = geoLocations.get(singleGeoLocationId);
        maxBucketSize = getMaxBucketSize();

        generateFictiveFlatsForSale();

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetRenovationUnitCost();
            geoLocation.calculateAndSetConstructionUnitCost();
        }

        MarriageWomenBucket.setup();

        for (Household household : households.values()) {
            household.refreshSize();
        }

        for (GeoLocation geoLocation : geoLocations.values()) {
            flatsForSaleInGeoLocations.put(geoLocation, new ArrayList<Flat>());
            sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.put(geoLocation, new ArrayList<>());
            flatsForRentInGeoLocations.put(geoLocation, new ArrayList<Flat>());
            fictiveFlatsForRentInGeoLocations.put(geoLocation, new ArrayList<Flat>());
        }
        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            flatsForSaleInNeighbourhoodsForConstructionPurchases.put(neighbourhood, new ArrayList<Flat>());
        }
        for (Bucket bucket : Model.buckets.values()) {
            nonNewlyBuiltFlatsForSaleInBuckets.put(bucket, new ArrayList<>());
            sampleFlatsForSaleInBucketsForAssessingPotentialNewHomes.put(bucket, new ArrayList<>());
        }

        buckets.values().forEach(Bucket::createSampleFlat);

        for (Household household: Model.households.values()) {
            double ZOParbitrageProbability = 0.3;
            if (rnd.nextDouble()<ZOParbitrageProbability) household.ZOParbitrage = true;
        }

        makeOutputDataArray();
        if (Model.geoLocations.size()>0) OutputDataNames.updateOutputDataNames();

    }

    public static void runPeriods() {
        for (int i = Model.period; i < nPeriods; i++) {
            Model.logger.info("PERIOD " + period);
            runPeriod();
            period++;
            if (period==Model.nPeriods-1) writeOutputDataCsv();

        }

        if (sobolRowIndex>0) writeSensitivityCsv();
    }

    public static void runPeriod() {

        OwnStopper stopperPeriod = new OwnStopper();

        beginningOfPeriod();
        investmentDecisions();
        housingMarketSupply();
        CAHAIcalculations();
        fictiveChoiceOfHouseholds();
        constructionPurchases();
        investmentPurchases();
        householdPurchases();
        constructFlats();
        renovateFlats();
        fictiveDemandForRent();
        rentalMarket();
        consumeAndSave();
        endOfPeriod();

        elapsedTimePeriod = stopperPeriod.getElapsedTimeInMilliseconds();
        writeOutputDataValues();
    }



    public static void beginningOfPeriod() {

        phase = Phase.BEGINNINGOFPERIOD;

        OwnStopper stopperBeginningOfPeriod = new OwnStopper();

        setPeriodicalValues();
        nullMiscDerivedVariables();
        refreshAbsCoeffState();
        individualsInArrayListAtBeginningOfPeriod.clear();
        for (Individual individual: Model.individuals.values()) {
            individualsInArrayListAtBeginningOfPeriod.add(individual);
        }

        for (Bank bank : banks.values()) {
            bank.setBankStrategyParameters();
            bank.nullMiscDerivedVariables();
        }

        individualsToRemoveFromMap.clear();

        for (Individual individual : individuals.values()) {
            individual.aging();
        }
        for (Individual individual:individualsToRemoveFromMap.values()) {
            individual.deleteIndividual();
        }


        for (Individual individual : Model.individuals.values()) {
            individual.refreshLabourMarketStatus();
        }

        List<Runnable> tasksToComputeLifeTimeIncomeAndEarned = individualsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (Individual individual : a.values()) {
                    calculateLifeTimeIncomeEarnedAndLifeTimeIncomeRunnable(individual);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksToComputeLifeTimeIncomeAndEarned);

        //marriage

            OwnStopper stopperBOPRefreshMenBuckets = new OwnStopper();

            List<Runnable> tasksBucketCodeForManAndWomanMarrying = individualsForParallelComputing.stream().map(a -> {
                Runnable task = () -> {

                    for (Individual individual : a.values()) {
                        calculateAndSetBucketCodeForManAndWomanMarryingRunnable(individual);
                    }

                };
                return task;
            }).collect(Collectors.toList());

            ParallelComputer.compute(tasksBucketCodeForManAndWomanMarrying);


            MarriageWomenBucket.refreshMenBuckets();


            for (Individual individual : individuals.values()) {
                if (individual.womanMarrying) individual.marry();
            }

        //birth

        individualsToAddtoMap.clear();
        for (Individual individual : individuals.values()) {
            individual.tryToGiveBirth();
        }
        for (Individual individual : individualsToAddtoMap) {
            addIndividualToMap(individual);
        }


        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetCyclicalAdjuster();
        }

        refreshPriceRegressions();

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            neighbourhood.flatSaleRecordsForPrice.clear();
            neighbourhood.forSaleRecordsForPrice.clear();
        }

        for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
            if (flatSaleRecord.recordEligibleForPrice()) {
                flatSaleRecord.bucket.getNeighbourhood().flatSaleRecordsForPrice.add(flatSaleRecord);
            }
        }
        for (ForSaleRecordTemporary forSaleRecord : Model.forSaleRecords) {
            if (forSaleRecord.recordEligibleForPrice()) {
                forSaleRecord.bucket.getNeighbourhood().forSaleRecordsForPrice.add(forSaleRecord);
            }
        }

        for (Flat flat : flats.values()) {
            flat.nullMiscDerivedVariables();
            if (flat.isForSale==false && flat.isForcedSale==false) flat.forSalePrice = 0;

        }

        for (Flat flat : fictiveFlatsForSale) {
            flat.marketPriceCalculated = false;
        }
        for (Flat flat : fictiveNewlyBuiltFlatsForSale) {
            flat.marketPriceCalculated = false;
        }


        List<Runnable> tasksForMarketPrice = flatsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (Flat flat : a.values()) {
                    calculateAndSetMarketPriceRunnable(flat);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksForMarketPrice);


        for (Bucket bucket : buckets.values()) {
            bucket.calculateAndSetAverageNForSaleToNSold();
            bucket.calculateAndSetNForSaleToNSoldProbabilityAdjustment();
        }
        generateSampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes();

        double[] neighbourhoodSumSizeState = new double[highestIdForNeighbourhoods + 1];
        double[] neighbourhoodSumSize = new double[highestIdForNeighbourhoods + 1];

        for (Flat flat : flats.values()) {
            neighbourhoodSumSizeState[flat.getNeighbourhood().getId()] += flat.size*flat.state;
            neighbourhoodSumSize[flat.getNeighbourhood().getId()] += flat.size;
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.averageState = Math.max(neighbourhoodSumSizeState[neighbourhood.getId()]/neighbourhoodSumSize[neighbourhood.getId()],neighbourhood.averageState);
        }

        List<Runnable> tasksToNullAndRefreshSizeAndIncomeLikeThings = householdsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (Household household : a.values()) {
                    nullMiscVariablesAndRefreshSizeAndIncomeLikeThingsRunnable(household);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksToNullAndRefreshSizeAndIncomeLikeThings);

        calculateHouseholdDecilesRanking();

        List<Runnable> tasksPriceIncomeToAverageWageIncomeCalculations = geoLocationsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (GeoLocation geoLocation : a.values()) {
                    priceIncomeToAverageWageIncomeCalculationsRunnable(geoLocation);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksPriceIncomeToAverageWageIncomeCalculations);

        refreshFictiveFlatsForSale();
        generateFlatsForSaleInGeoLocations();

        List<Runnable> tasksForSomeHouseholdVariablesAndDecisionOnMoving = householdsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                ArrayList<Household> parallelHouseholdsInRandomOrder = new ArrayList<>();
                for (Household household : a.values()) {
                    parallelHouseholdsInRandomOrder.add(household);
                }
                Collections.shuffle(parallelHouseholdsInRandomOrder, Model.rndArray[parallelHouseholdsInRandomOrder.get(0).getId() % nThreads]);

                for (Household household : parallelHouseholdsInRandomOrder) {
                    calculateAndSetSomeVariablesAndDecideOnMovingRunnable(household);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksForSomeHouseholdVariablesAndDecisionOnMoving);


        for (Household household : householdsInRandomOrder) {
            household.applyForBabyloan();
        }

        calculateAverageWageInGeoLocations();
        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            if (geoLocation.histRenovationUnitCost[nHistoryPeriods + period - 1] == 0) {
                for (int i = 0; i < Model.nHistoryPeriods; i++) {
                    geoLocation.histRenovationUnitCost[i] = geoLocation.calculateRenovationUnitCostBase();
                    geoLocation.histConstructionUnitCost[i] = geoLocation.calculateConstructionUnitCostBase();
                }
            }


        }


        for (LoanContract loanContract : loanContracts.values()) {
            loanContract.setIssuedInThisPeriod(false);
            loanContract.nullMiscDerivedVariables();
        }

        for (Flat flat : flats.values()) {
            flat.stateDepreciation();
            flat.decreasePeriodsLeftForRent();
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.nullMiscDerivedVariables();
            neighbourhood.sortSimilarNeighbourhoods();
            neighbourhood.calculateAndSetAggregateFlatInfo();
            neighbourhood.calculateAndSetExpectedReturnAndSpread();
            neighbourhood.calculateAndSetRentMarkup();
        }


        for (Bucket bucket : buckets.values()) {
            bucket.nullMiscDerivedVariables();
        }
        for (Flat flat : flats.values()) {
            flat.bucket.nFlats++;
        }

        List<Runnable> tasks = flatsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (Flat flat : a.values()) {
                    calculateAndSetEstimatedMarketPriceRunnable(flat);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasks);

        List<Runnable> tasksForGeoLocations = geoLocations.values().stream().map(a -> {
            Runnable task = () -> {

                a.calculateHousePriceToIncome();

            };
            return task;
        }).collect(Collectors.toList());
        ParallelComputer.compute(tasksForGeoLocations);

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetConstructionMarkup();
        }

        householdsInRandomOrder.clear();
        for (Household household : Model.households.values()) {
            householdsInRandomOrder.add(household);
        }
        Collections.shuffle(householdsInRandomOrder,rnd);

        for (int i = 0; i < householdsInRandomOrder.size(); i++) {
            householdsInRandomOrder.get(i).rankNumber = i;
        }
        elapsedTimeBeginningOfPeriod = stopperBeginningOfPeriod.getElapsedTimeInMilliseconds();

    }

    public static void calculateAndSetEstimatedMarketPriceRunnable(Flat flat) {
        flat.calculateAndSetEstimatedMarketPrice();
    }

    public static void calculateAndSetMarketPriceRunnable(Flat flat) {
        flat.calculateAndSetMarketPrice();
    }


    public static void forSaleCalculationsRunnable(Flat flat) {
        flat.forSaleCalculations();
    }

    public static void calculateAndSetBucketCodeForManAndWomanMarryingRunnable(Individual individual) {
        individual.calculateAndSetBucketCodeForMan();
        individual.calculateAndSetWomanMarrying();
    }

    public static void calculateAndSetSomeVariablesAndDecideOnMovingRunnable(Household household) {
        household.calculateAndSetHasNonPerformingLoan();
        household.calculateVariablesForPurchase();
        household.calculateVariablesForRent();
        household.setMinConsumptionLevel(household.calculateMinConsumptionLevel());
        household.calculateAndSetCanGetBridgeLoanInPeriod();
        household.updateLetThisYoungOverTooYoungAgeRent();
        household.decidesOnMoving();
    }

    public static void nullMiscVariablesAndRefreshSizeAndIncomeLikeThingsRunnable(Household household) {
        household.nullMiscDerivedVariables();
        household.refreshSize();
        household.refreshWageIncomeAndPotentialWageIncomeAndPermanentIncomeAndLifeTimeIncome();
    }

    public static void calculateLifeTimeIncomeEarnedAndLifeTimeIncomeRunnable(Individual individual) {
        individual.calculateLifeTimeIncomeEarnedAndLifeTimeIncome();
    }

    public static void priceIncomeToAverageWageIncomeCalculationsRunnable(GeoLocation geoLocation) {
        geoLocation.calculateAndSetAverageWageIncome();
        geoLocation.calculateAndSetPriceIndexToAverageWageIncome();
    }


    public static void refreshPriceRegressions() {

        OwnStopper stopperRefreshPriceRegressions = new OwnStopper();

        for (Bucket bucket : buckets.values()) {
            bucket.flatSaleRecords.clear();
        }

        for (FlatSaleRecord flatSaleRecord : flatSaleRecords.values()) {
            if (flatSaleRecord.isNewlyBuilt==false && flatSaleRecord.getPeriodOfRecord() >= Model.period - Model.nPeriodsForFlatSaleRecordsToUseInPriceRegression) {
                if (flatSaleRecord.size>Model.maxSizeValueForSize2 || flatSaleRecord.state<Model.minStateValueForSizeState) continue;
                flatSaleRecord.getBucket().flatSaleRecords.add(flatSaleRecord);
            }

        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.refreshPriceRegression();
        }
        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            if (period>=Model.nPeriodsUntilCopyingPriceRegressionCoeffients && neighbourhood.priceRegressionRefreshed == false) {
                neighbourhood.getPriceRegressionCoeffOfSimilarNeighbourhoods();
            }
        }

        buckets.values().forEach(Bucket::calculateSampleFlatPrice);

        calculateAndSetAggregateMarketValueForNeighbourhoods();

        elapsedTimeRefreshPriceRegressions = stopperRefreshPriceRegressions.getElapsedTimeInMilliseconds();

    }

    private static void investmentDecisions() {
        phase = Phase.INVESTMENTDECISIONS;
        OwnStopper stopperInvestmentDecisions = new OwnStopper();

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.calculateAndSetPlannedInvestmentValue();
            neighbourhood.calculateAndSetInvestmentProbabilities();
        }

        elapsedTimeInvestmentDecisions = stopperInvestmentDecisions.getElapsedTimeInMilliseconds();
    }

    public static void housingMarketSupply() {
        phase = Phase.HOUSINGMARKETSUPPLY;
        OwnStopper stopperHousingMarketSupply = new OwnStopper();

        for (Bucket bucket : buckets.values()) {
            bucket.calculateAndSetUtilizationRatio();
            bucket.calculateAndSetRentSaleProbabilities();
        }

        List<Runnable> tasksForSaleCalculations = flatsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {

                for (Flat flat : a.values()) {
                    forSaleCalculationsRunnable(flat);
                }

            };
            return task;
        }).collect(Collectors.toList());

        ParallelComputer.compute(tasksForSaleCalculations);

        forSaleRecords.clear();
        for (Flat flat : flats.values()) {

            if (flat.isForSale || flat.isForcedSale) {

                ForSaleRecordTemporary forSaleRecord = new ForSaleRecordTemporary();
                forSaleRecord.periodOfRecord = period;
                forSaleRecord.bucket = flat.bucket;
                forSaleRecord.isNewlyBuilt = flat.isNewlyBuilt;
                forSaleRecord.size = flat.size;
                forSaleRecord.state = flat.state;
                forSaleRecord.neighbourhoodQuality = flat.getQuality();
                forSaleRecord.isForcedSale = flat.isForcedSale;
                forSaleRecords.add(forSaleRecord);

                flat.bucket.nForSale++;
                flat.bucket.getGeoLocation().outputData[5][period]++;

            }
        }

        elapsedTimeHousingMarketSupply = stopperHousingMarketSupply.getElapsedTimeInMilliseconds();

    }

    public static void CAHAIcalculations() {

        phase = Phase.CAHAICALCULATIONS;

        int nDeciles = 10;
        int nAgeGroups = 3;
        if (CAHAIForFTB) nAgeGroups = 2;

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            double[] sumFlatPriceInBucket = new double[nDeciles*nAgeGroups];
            double[] nFlatsInBucket = new double[nDeciles*nAgeGroups];



            for (Household household : Model.households.values()) {
                if (household.home!=null && household.home.getGeoLocation()==geoLocation) {
                    int bucketCode = getBucketCodeOfPeerFlat(household,true); //peer bucket and not the bucket in the bucket-neighbourhood-geolocation division
                    if (household.home!=null && bucketCode>=0) {
                        sumFlatPriceInBucket[bucketCode] += household.home.getMarketPrice();
                        nFlatsInBucket[bucketCode]++;
                    }
                }

            }

            geoLocation.sampleFlatsForCAHAI = new HashMap<>();
            for (int i = 0; i < sumFlatPriceInBucket.length; i++) {
                Flat flat = new Flat();
                flat.forSalePrice = sumFlatPriceInBucket[i]/nFlatsInBucket[i];
                flat.bucket = geoLocation.neighbourhoods.get(0).buckets.get(0);
                flat.marketPrice = flat.forSalePrice;
                flat.isNewlyBuilt = false;
                geoLocation.sampleFlatsForCAHAI.put(i,flat);
            }

            for (int i = sumFlatPriceInBucket.length-1; i >= 0 ; i--) {
                if (Double.isNaN(geoLocation.sampleFlatsForCAHAI.get(i).forSalePrice)) {
                    geoLocation.sampleFlatsForCAHAI.get(i).forSalePrice = geoLocation.sampleFlatsForCAHAI.get(i+1).forSalePrice;
                    geoLocation.sampleFlatsForCAHAI.get(i).marketPrice = geoLocation.sampleFlatsForCAHAI.get(i+1).marketPrice;
                }
            }

        }




        //CAHAI - CA (credit availability part)
        //we consider each household once per potential purchases (so if it does not purchase a flat when we first examine it, in the next period it would not be included in the CA statistics, for the bucket codes see getBucketCodeOfPeerFlat()
        for (Household household : Model.households.values()) {

            if (household.considerForCAHAI && household.maySelectHome() && getBucketCodeOfPeerFlat(household,false)>=0) {
                Flat peerFlat = household.preferredGeoLocation.sampleFlatsForCAHAI.get(getBucketCodeOfPeerFlat(household,false));
                int bucketCode = getBucketCodeOfPeerFlat(household,false);
                household.setConsideredForCAHAIInThisPeriod(true);
                household.setValueOfPeerFlat(peerFlat.forSalePrice);
                double depositAvailableForFlat = household.depositAvailableForFlat(peerFlat);
                double ownHomeValue = 0;
                if (household.home!=null) ownHomeValue = household.home.getMarketPrice();
                if (peerFlat.forSalePrice<depositAvailableForFlat+ownHomeValue) {
                    household.setConsiderCAHAICanBuyWithoutLoan(true);
                    household.preferredGeoLocation.outputData[2400 + bucketCode][period] ++ ;
                    household.preferredGeoLocation.outputData[2490][period] ++ ;
                } else if (household.canBuyFlat(peerFlat)) {
                    household.setConsiderCAHAICanBuyWithLoan(true);
                    household.preferredGeoLocation.outputData[2500 + bucketCode][period] ++ ;
                    household.preferredGeoLocation.outputData[2590][period] ++ ;
                } else if (household.getWageIncome()==0 || household.hasNonPerformingLoan || household.lastNonPerformingPeriod>Model.period-Model.nPeriodsInNegativeKHR) {
                    household.setConsiderCAHAICantBuyMisc(true);
                    household.valueOfPeerFlatIfCantBuy = household.valueOfPeerFlat;
                    household.preferredGeoLocation.outputData[2600 + bucketCode][period] ++ ;
                    household.preferredGeoLocation.outputData[2690][period] ++ ;
                } else {
                    household.valueOfPeerFlatIfCantBuy = household.valueOfPeerFlat;
                    double loanNeed = peerFlat.forSalePrice-depositAvailableForFlat;
                    household.considerCAHAILoanNeed = loanNeed;
                    LoanContractOffer loanContractOffer = Model.banks.get(0).loanContractOffer(household,peerFlat,loanNeed);
                    household.considerCAHAILTVLimit = loanContractOffer.maxLoanLTVConstraint;
                    household.considerCAHAIDSTILimit = loanContractOffer.maxLoanDSTIConstraint;
                    household.considerCAHAIConsumptionLimit = loanContractOffer.maxLoanConsumptionConstraint;

                    double financing = depositAvailableForFlat + loanContractOffer.bridgeLoanPrincipal;

                    if (loanContractOffer.maxLoanLTVConstraint<loanContractOffer.maxLoanDSTIConstraint && loanContractOffer.maxLoanLTVConstraint<loanContractOffer.maxLoanConsumptionConstraint) {
                        household.preferredGeoLocation.outputData[2700 + bucketCode][period] ++;
                        household.preferredGeoLocation.outputData[2790][period] ++ ;
                        financing += loanContractOffer.maxLoanLTVConstraint;
                    } else if (loanContractOffer.maxLoanDSTIConstraint <loanContractOffer.maxLoanConsumptionConstraint) {
                        household.preferredGeoLocation.outputData[2800 + bucketCode][period] ++;
                        household.preferredGeoLocation.outputData[2890][period] ++ ;
                        financing += loanContractOffer.maxLoanDSTIConstraint;
                    } else {
                        household.preferredGeoLocation.outputData[2900 + bucketCode][period] ++;
                        household.preferredGeoLocation.outputData[2990][period] ++ ;
                        financing += loanContractOffer.maxLoanConsumptionConstraint;
                    }

                }

                household.setConsiderForCAHAI(false);
                household.periodOfLastConsideration = Model.period;

            }

        }

    }

    public static int getBucketCodeOfPeerFlat(Household household, boolean useFTBAgeThreshold) {
        //code: 0-9 deciles of young population (<35), 10-19 deciles of middle population (35-45), 20-29 deciles of population above 45
        int ageOfOldestMember = household.calculateAgeInYearsOfOldestMember();
        int ageGroupIndex = 0;
        if (ageOfOldestMember>=35) ageGroupIndex = 1;
        if (ageOfOldestMember>=45) ageGroupIndex = 2;

        if (CAHAIForFTB) {

            if (useFTBAgeThreshold) {
                ageGroupIndex = 0;
                if (ageOfOldestMember>35) ageGroupIndex = 1;
            } else {
                ageGroupIndex = 0;
                if (household.firstBuyer==false) ageGroupIndex = 1;
            }

        }

        return ageGroupIndex*10 + household.rankDecilesFrom1 - 1;

    }

    public static void fictiveChoiceOfHouseholds() {

        phase = Phase.FICTIVECHOICEOFHOUSEHOLDS;
        OwnStopper stopperFictiveDemandForNewlyBuiltFlats = new OwnStopper();

        buckets.values().forEach(a -> a.setNewlyBuiltDemand(0));

        refreshFictiveFlatsForSale();
        generateFlatsForSaleInGeoLocations();



            List<Runnable> tasks = householdsForParallelComputing.stream().map(a -> {
                Runnable task = () -> {

                    for (Household household : a.values()) {
                        ModelRunnableFunctions.selectWithFictiveSupplyIncrementDemand(household);
                    }

                };
                return task;
            }).collect(Collectors.toList());

            ParallelComputer.compute(tasks);

        elapsedTimeFictiveDemandForNewlyBuiltFlats = stopperFictiveDemandForNewlyBuiltFlats.getElapsedTimeInMilliseconds();


    }

    public static void generateFictiveFlatsForSale() {
        fictiveFlatsForSale.clear();
        fictiveNewlyBuiltFlatsForSale.clear();

        for (Bucket bucket : Model.buckets.values()) {


            for (int i = 0; i < Model.nFictiveFlatsForSalePerBucket; i++) {
                Flat flat = new Flat();
                flat.bucket = bucket; //not using setBucket, because that would change nFlats in the given bucket
                flat.setSize(bucket.sizeMin + Model.rnd.nextDouble()*(Math.min(bucket.sizeMax,bucket.getSizeMaxForFictiveFlats())-bucket.sizeMin));
                flat.setState(bucket.stateMin + Model.rnd.nextDouble()*(bucket.stateMax-bucket.stateMin));
                flat.setNewlyBuilt(false);
                flat.setOwnerInvestor(investors.get(0));
                flat.setForSale(true);
                flat.setNForSalePeriods(1);
                flat.calculateAndSetForSalePrice();
                flat.lastMarketPrice = flat.forSalePrice;
                flat.isFictiveFlatForSale = true;
                flat.lastNaturallyUpdatedMarketPriceOfFictiveFlat = flat.forSalePrice;
                flat.periodOfLastNaturalMarketPriceUpdateOfFictiveFlat = 0;
                fictiveFlatsForSale.add(flat);

            }

            if (bucket.isHighQuality) {

                Flat flat = new Flat();
                flat.bucket = bucket; //not using setBucket, because that would change nFlats in the given bucket
                flat.setSize((bucket.sizeMin + bucket.getSizeMaxForConstructionFlats()) / 2);
                flat.setState(Model.highQualityStateMax);
                flat.setNewlyBuilt(true);
                flat.setOwnerConstructor(constructors.get(0));
                flat.setForSale(true);
                flat.setNForSalePeriods(1);
                flat.calculateAndSetForSalePrice();
                flat.lastMarketPrice = flat.forSalePrice;
                flat.isFictiveFlatForSale = true;
                flat.lastNaturallyUpdatedMarketPriceOfFictiveFlat = flat.forSalePrice;
                flat.periodOfLastNaturalMarketPriceUpdateOfFictiveFlat = 0;
                fictiveFlatsForSale.add(flat);
                fictiveNewlyBuiltFlatsForSale.add(flat);

            }
        }

        Collections.sort(fictiveFlatsForSale, Flat.comparatorForSalePrice);
        Collections.sort(fictiveNewlyBuiltFlatsForSale, Flat.comparatorForSalePrice);

    }

    public static void refreshFictiveFlatsForSale() {

        for (Flat flat : fictiveFlatsForSale) {
            flat.calculateAndSetForSalePrice();
        }
        for (Flat flat : fictiveNewlyBuiltFlatsForSale) {
            if (period>=ZOPStartingPeriodForFictiveFlats) flat.isZOP=true;
            flat.calculateAndSetForSalePrice();
        }
        Collections.sort(fictiveFlatsForSale, Flat.comparatorForSalePrice);
        Collections.sort(fictiveNewlyBuiltFlatsForSale, Flat.comparatorForSalePrice);
    }

    public static void constructionPurchases() {

        phase = Phase.CONSTRUCTIONPURCHASES;

        OwnStopper stopperConstructionPurchases = new OwnStopper();

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.areaUnderConstruction = 0;
            geoLocation.plannedAdditionalAreaUnderConstruction = 0;
        }

        for (Flat flat : constructors.get(0).flatsReady) {
            flat.bucket.nNewlyBuiltFlatsNotYetSold++;
        }
        for (Flat flat : constructors.get(0).flatsUnderConstruction) {
            if (flat.ownerConstructor != null) {
                flat.bucket.nNewlyBuiltFlatsNotYetSold++;
            }
            flat.getGeoLocation().areaUnderConstruction += flat.size;
        }


        for (Neighbourhood neighbourhood : flatsForSaleInNeighbourhoodsForConstructionPurchases.keySet()) {
            flatsForSaleInNeighbourhoodsForConstructionPurchases.get(neighbourhood).clear();
        }

        for (Flat flat : Model.flats.values()) {
            if (flat.ownerConstructor == null && flat.nPeriodsLeftForConstruction == 0 && (flat.isForSale || flat.isForcedSale) && flat.state<Model.maxFlatStateToLandPriceStateForConstructionPurchase * Model.maxFlatStateForLandPrice) {
                flatsForSaleInNeighbourhoodsForConstructionPurchases.get(flat.bucket.neighbourhood).add(flat);
            }
        }

        for (Neighbourhood neighbourhood : flatsForSaleInNeighbourhoodsForConstructionPurchases.keySet()) {
            Collections.sort(flatsForSaleInNeighbourhoodsForConstructionPurchases.get(neighbourhood), Flat.comparatorForSaleUnitPrice);
        }

        for (Bucket bucket : Model.buckets.values()) {
            int nFlatsToBuildInNeighbourhoodBucket = constructors.get(0).calculateNFlatsToBuildInBucket(bucket);
            bucket.getGeoLocation().plannedAdditionalAreaUnderConstruction += (bucket.sizeMin+bucket.getSizeMaxForConstructionFlats())/2*nFlatsToBuildInNeighbourhoodBucket;
        }

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.nFlatsToBuildAdjuster = 1;
            if (monthlyConstructionAreaLimit) {
                if (geoLocation.plannedAdditionalAreaUnderConstruction>maxAreaUnderConstructionInGeoLocations.get(geoLocation)/nPeriodsForConstruction) {
                    double additionalAreaUnderConstructionWhichCanBeUsed = maxAreaUnderConstructionInGeoLocations.get(geoLocation)/nPeriodsForConstruction;
                    geoLocation.nFlatsToBuildAdjuster = additionalAreaUnderConstructionWhichCanBeUsed/geoLocation.plannedAdditionalAreaUnderConstruction;

                }
            } else if (geoLocation.plannedAdditionalAreaUnderConstruction+geoLocation.areaUnderConstruction>maxAreaUnderConstructionInGeoLocations.get(geoLocation)) {
                double additionalAreaUnderConstructionWhichCanBeUsed = maxAreaUnderConstructionInGeoLocations.get(geoLocation)-geoLocation.areaUnderConstruction;
                geoLocation.nFlatsToBuildAdjuster = additionalAreaUnderConstructionWhichCanBeUsed/geoLocation.plannedAdditionalAreaUnderConstruction;
            }

        }


        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {

            ArrayList<Flat> flatsForSaleInNeighbourhood = flatsForSaleInNeighbourhoodsForConstructionPurchases.get(neighbourhood);

            Map<Bucket, Integer> nFlatsToBuildInNeighbourhoodBuckets = new HashMap<>();
            for (Bucket bucket : neighbourhood.getBuckets()) {
                int nFlatsToBuildInNeighbourhoodBucket = (int) Math.ceil(neighbourhood.getGeoLocation().nFlatsToBuildAdjuster * constructors.get(0).calculateNFlatsToBuildInBucket(bucket));
                nFlatsToBuildInNeighbourhoodBuckets.put(bucket, nFlatsToBuildInNeighbourhoodBucket);
            }

            while (Collections.max(nFlatsToBuildInNeighbourhoodBuckets.values()) > 0) {

                int maxValue = Collections.max(nFlatsToBuildInNeighbourhoodBuckets.values());
                Bucket bucketToBuildIn = null;
                for (Bucket bucket : nFlatsToBuildInNeighbourhoodBuckets.keySet()) {
                    if (nFlatsToBuildInNeighbourhoodBuckets.get(bucket) == maxValue) {
                        bucketToBuildIn = bucket;
                        break;
                    }
                }
                double newSize = bucketToBuildIn.getSizeMaxForConstructionFlats() - rnd.nextDouble() * (bucketToBuildIn.getSizeMaxForConstructionFlats() - bucketToBuildIn.sizeMin);


                if (flatsForSaleInNeighbourhood.size() == 0 && constructors.get(0).neighbourhoodArea.get(neighbourhood) < newSize)
                    break;

                if (constructors.get(0).neighbourhoodArea.get(neighbourhood) < newSize && flatsForSaleInNeighbourhood.size() > 0) {


                    Flat flatToBuy = flatsForSaleInNeighbourhood.get(0);
                    buyFlat(flatToBuy, constructors.get(0));

                    double newNeighbourhoodArea = constructors.get(0).neighbourhoodArea.get(neighbourhood) + flatToBuy.size;
                    constructors.get(0).neighbourhoodArea.replace(neighbourhood, newNeighbourhoodArea);

                    flatToBuy.deleteFlat();

                }


                if (constructors.get(0).neighbourhoodArea.get(neighbourhood) >= newSize*constructionAreaNeedRatio) {

                    Flat flat = Model.createFlat();
                    flat.size = newSize;
                    flat.state = bucketToBuildIn.stateMax;
                    flat.setNewlyBuilt(true);
                    flat.setOwnerConstructor(Model.constructors.get(0));
                    flat.setBucket(bucketToBuildIn);
                    flat.setNPeriodsLeftForConstruction(Model.nPeriodsForConstruction);
                    if (period>=ZOPStartingPeriod) flat.isZOP=true;
                    constructors.get(0).flatsUnderConstruction.add(flat);

                    int newValue = nFlatsToBuildInNeighbourhoodBuckets.get(bucketToBuildIn) - 1;
                    nFlatsToBuildInNeighbourhoodBuckets.replace(bucketToBuildIn, newValue);

                    double newNeighbourhoodArea = constructors.get(0).neighbourhoodArea.get(neighbourhood) - newSize*constructionAreaNeedRatio;
                    constructors.get(0).neighbourhoodArea.replace(neighbourhood,newNeighbourhoodArea);
                }

            }

        }

        Model.investors.get(0).removeSoldFlatsFromProperties();

        elapsedTimeConstructionPurchases = stopperConstructionPurchases.getElapsedTimeInMilliseconds();

    }

    public static void investmentPurchases() {
        phase = Phase.INVESTMENTPURCHASES;
        OwnStopper stopperInvestmentPurchases = new OwnStopper();

        investmentPurchasesScheme();

        Model.investors.get(0).removeSoldFlatsFromProperties();
        elapsedTimeInvestmentPurchases = stopperInvestmentPurchases.getElapsedTimeInMilliseconds();
    }

    public static void householdPurchases() {

        phase = Phase.HOUSEHOLDPURCHASES;
        OwnStopper stopperHouseholdPurchases = new OwnStopper();

        generateFlatsForSaleInGeoLocations();

        iterateHouseholdPurchases = true;
        nIterations = 0;
        while (iterateHouseholdPurchases && nIterations < maxNIterationsForHouseholdPurchases) {
            nIterations++;

            int nHouseholdsSelectingHome = 0;
            for (Household household : Model.households.values()) {
                if (household.maySelectHome()) nHouseholdsSelectingHome++;
            }

            List<Runnable> tasksFlatAccounting = flatsForParallelComputing.stream().map(a -> {
                Runnable task = () -> {
                    for (Flat flat : a.values()) {
                        ModelRunnableFunctions.flatAccounting(flat);
                    }
                };
                return task;
            }).collect(Collectors.toList());
            ParallelComputer.compute(tasksFlatAccounting);

            //placeBids
            List<Runnable> tasksPlaceBidsOnFlats = householdsForParallelComputing.stream().map(a -> {
                Runnable task = () -> {
                    for (Household household : a.values()) {
                        ModelRunnableFunctions.placeBidsOnFlats(household);
                    }
                };
                return task;
            }).collect(Collectors.toList());


            OwnStopper stopperAdditionalInfo3 = new OwnStopper();
            ParallelComputer.compute(tasksPlaceBidsOnFlats);

            List<Runnable> tasksBidSorting = flatsForParallelComputing.stream().map(a -> {
                Runnable task = () -> {
                    for (Flat flat : a.values()) {
                        ModelRunnableFunctions.flatBidSorting(flat);
                    }
                };
                return task;
            }).collect(Collectors.toList());
            ParallelComputer.compute(tasksBidSorting);

            flatsInRandomOrder.clear();
            for (Flat flat : Model.flats.values()) {
                if (flat.isForSale || flat.isForcedSale) {
                    flatsInRandomOrder.add(flat);
                }
            }
            Collections.shuffle(flatsInRandomOrder,rnd);

            for (Flat flat: flatsInRandomOrder) {
                double greatestBid = 0;
                Household greatestBidder = null;
                double secondGreatestBid = 0;
                if (flat.bids.size()>0) {
                    double baseForSalePrice = flat.forSalePrice;
                    for (Bid bid : flat.bids) {
                        if (bid.household.isMoving) {
                            if (greatestBid==0) {
                                greatestBid = bid.reservationPrice;
                                greatestBidder = bid.household;
                            } else {
                                secondGreatestBid = bid.reservationPrice;
                                break;
                            }
                        }

                    }

                    if (greatestBid>0) {


                        if (secondGreatestBid>0) {

                            flat.forSalePrice = secondGreatestBid;
                            if (Double.isNaN(flat.forSalePrice)) flat.forSalePrice = 0;

                        } else {
                            if (Double.isNaN(flat.forSalePrice)) flat.forSalePrice = 0;
                        }


                         if (greatestBidder.utilityFunctionCES.calculateAbsoluteReservationPriceForFlat(flat)<baseForSalePrice) {
                            double bidderReservationPrice = greatestBidder.utilityFunctionCES.calculateAbsoluteReservationPriceForFlat(flat);
                        }

                        if (Double.isNaN(flat.forSalePrice) ) {
                            flat.forSalePrice = 0;
                        }

                        buyFlat(flat,greatestBidder);

                    }

                }
            }


            regenerateFlatsForSaleInGeoLocations();

            iterateHouseholdPurchases = false;
            for (Household household : Model.households.values()) {
                if (household.isMoving) iterateHouseholdPurchases = true;
            }

        }

        for (Household household : Model.households.values()) {
            if (household.isMoving && (household.flatTooExpensiveToBuy==false || household.firstBuyer)) household.nPeriodsWaitingForFlatToBuy++;
        }


        for (Household household : Model.households.values()) {
            household.bids.clear();
            household.potentialBids.clear();
        }

        Model.investors.get(0).removeSoldFlatsFromProperties();

        elapsedTimeHouseholdPurchases = stopperHouseholdPurchases.getElapsedTimeInMilliseconds();
    }


    public static void generateFlatsForSaleInGeoLocations() {
        for (GeoLocation geoLocation : flatsForSaleInGeoLocations.keySet()) {
            flatsForSaleInGeoLocations.get(geoLocation).clear();
        }

        for (Bucket bucket : nonNewlyBuiltFlatsForSaleInBuckets.keySet()) {
            nonNewlyBuiltFlatsForSaleInBuckets.get(bucket).clear();
        }

        Collection<Flat> flatsToGoThrough;
        if (phase == Phase.FICTIVECHOICEOFHOUSEHOLDS || phase == Phase.BEGINNINGOFPERIOD) {
            flatsToGoThrough = fictiveFlatsForSale;
        } else {
            flatsToGoThrough = Model.flats.values();
        }

        for (Flat flat : flatsToGoThrough) {
            if (flat.isForSale || flat.isForcedSale) {
                if (phase==Phase.HOUSEHOLDPURCHASES && flat.isForcedSale) continue;
                GeoLocation geoLocation = flat.getBucket().getNeighbourhood().getGeoLocation();
                flatsForSaleInGeoLocations.get(geoLocation).add(flat);
                if (flat.isNewlyBuilt == false) {
                    Bucket bucket = flat.getBucket();
                    nonNewlyBuiltFlatsForSaleInBuckets.get(bucket).add(flat);
                }
            }
        }

        for (GeoLocation geoLocation : flatsForSaleInGeoLocations.keySet()) {
            Collections.sort(flatsForSaleInGeoLocations.get(geoLocation), Flat.comparatorForSalePrice);
        }
        for (Bucket bucket : nonNewlyBuiltFlatsForSaleInBuckets.keySet()) {
            Collections.sort(nonNewlyBuiltFlatsForSaleInBuckets.get(bucket), Flat.comparatorForSalePrice);
        }


    }

    public static void regenerateFlatsForSaleInGeoLocations() {

        for (GeoLocation geoLocation : flatsForSaleInGeoLocations.keySet()) {
            ArrayList<Flat> formerFlatsForSale = flatsForSaleInGeoLocations.get(geoLocation);
            ArrayList<Flat> newFlatsForSale = new ArrayList<>();
            for (Flat flat : formerFlatsForSale) {
                if (flat.isForSale || flat.isForcedSale) {
                    flat.chosenBy = null;
                    newFlatsForSale.add(flat);
                }
            };
            flatsForSaleInGeoLocations.replace(geoLocation,newFlatsForSale);
        }
    }

    public static void generateSampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes() {
        for (GeoLocation geoLocation : sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.keySet()) {
            sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(geoLocation).clear();
        }

        int nFlatsInBucket = 1;
        if (phase==Phase.SETUP) nFlatsInBucket = 1;
        for (Bucket bucket : Model.buckets.values()) {
            for (int i = 0; i < nFlatsInBucket; i++) {
                Flat flat = new Flat();
                flat.bucket = bucket; //not using setBucket, because that would change nFlats in the given bucket
                flat.setSize((bucket.sizeMin + bucket.getSizeMaxForConstructionFlats())/2);
                flat.setState((bucket.stateMin + bucket.stateMax)/2);
                if (i>0) {
                    flat.setSize(bucket.sizeMin + rnd.nextDouble()*(bucket.sizeMax-bucket.sizeMin));
                    flat.setState(bucket.stateMin + rnd.nextDouble()*(bucket.stateMax-bucket.stateMin));
                }
                flat.setForSalePrice(flat.calculateMarketPrice());
                sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(flat.bucket.neighbourhood.geoLocation).add(flat);
            }


            if (bucket.isHighQuality && phase != Phase.SETUP) {
                Flat newlyBuiltFlat = new Flat();
                newlyBuiltFlat.bucket = bucket; //not using setBucket, because that would change nFlats in the given bucket
                newlyBuiltFlat.setSize((bucket.sizeMin + bucket.getSizeMaxForConstructionFlats())/2);
                newlyBuiltFlat.setState(Model.highQualityStateMax);
                newlyBuiltFlat.ownerConstructor = Model.constructors.get(0);
                newlyBuiltFlat.setNewlyBuilt(true);
                newlyBuiltFlat.calculateAndSetForSalePrice();
                sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(bucket.neighbourhood.geoLocation).add(newlyBuiltFlat);
            }
        }


        for (GeoLocation geoLocation : sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.keySet()) {
            Collections.sort(sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes.get(geoLocation), Flat.comparatorForSalePrice);
        }
    }


    public static void constructFlats() {
        phase = Phase.CONSTRUCTFLATS;
        OwnStopper stopperConstructFlats = new OwnStopper();

        List<Flat> newFlatsUnderConstruction = new ArrayList<>();

        for (Flat flat : constructors.get(0).flatsUnderConstruction) {

            flat.nPeriodsLeftForConstruction--;
            if (flat.nPeriodsLeftForConstruction == 0) {
                if (flat.ownerConstructor != null)
                    constructors.get(0).flatsReady.add(flat);
                if (flat.ownerHousehold != null) {
                    Household household = flat.ownerHousehold;

                    if (flat == household.homeUnderConstruction) {
                        if (household.home != null) {
                            household.properties.add(household.home);
                            household.home.setForSale(true);
                        }
                        household.homeUnderConstruction = null;
                        household.home = flat;
                        household.setShouldNotRent(true);
                    }

                    if (flat.willBeForRent) {
                        flat.willBeForRent = false;
                        flat.isForRent = true;
                    }

                }
                if (flat.ownerInvestor != null) {
                    flat.willBeForRent = false;
                    flat.isForRent = true;
                }
            } else {
                newFlatsUnderConstruction.add(flat);
            }
        }

        constructors.get(0).flatsUnderConstruction.clear();
        for (Flat flat : newFlatsUnderConstruction) {
            constructors.get(0).flatsUnderConstruction.add(flat);

        }

        elapsedTimeConstructFlats = stopperConstructFlats.getElapsedTimeInMilliseconds();
    }

    public static void renovateFlats() {
        phase = Phase.RENOVATEFLATS;
        OwnStopper stopperRenovateFlats = new OwnStopper();

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.renovationQuantity = 0;
            geoLocation.renovationUnitCost = geoLocation.histRenovationUnitCost[nHistoryPeriods + period - 1];
        }

        for (Household household : households.values()) {
            household.renovationDemand();
        }


        for (Flat flat : flats.values()) {

            if (flat.isForRent) {
                if (flat.bucket.isHighQuality) continue;
                if (flat.investmentStateIncrease == 0) flat.investmentStateIncrease = stateDepreciation;
                if (flat.state + flat.investmentStateIncrease > flat.bucket.stateMax) flat.investmentStateIncrease = flat.bucket.stateMax - flat.state; //this way flats for rent will not jump between buckets in every period
                renovationDemand += flat.investmentStateIncrease * flat.size;
            }
        }

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetRenovationUnitCost();
            geoLocation.calculateAndSetConstructionUnitCost();
        }

        for (Household household : households.values()) {
            if (household.homeOptimalRenovation > 0) {
                household.renovateFlat(household.home);
            }
        }

        for (Flat flat : flats.values()) {
            if (flat.investmentStateIncrease > 0) {
                if (flat.ownerHousehold != null) flat.ownerHousehold.renovateFlat(flat);
                if (flat.ownerInvestor != null) {
                    flat.ownerInvestor.renovateFlat(flat);
                }
            }
        }

        elapsedTimeRenovateFlats = stopperRenovateFlats.getElapsedTimeInMilliseconds();
    }

    public static void fictiveDemandForRent() {
        phase = Phase.FICTIVEDEMANDFORRENT;
        OwnStopper stopperFictiveDemandForRent = new OwnStopper();

        for (Bucket bucket : buckets.values()) {
            int[] externalDemandForBucket = externalDemand.get(bucket);
            bucket.nFictiveRentDemand = externalDemandForBucket[period];
        }

        for (GeoLocation geoLocation : fictiveFlatsForRentInGeoLocations.keySet()) {
            fictiveFlatsForRentInGeoLocations.get(geoLocation).clear();
        }

        for (Bucket bucket : Model.buckets.values()) {
            Flat flat = new Flat();
            flat.bucket = bucket; //not using setBucket, because that would change nFlats in the given bucket
            if (flat.bucket.neighbourhood.rentalNeighbourhood==false) continue;
            flat.setSize(bucket.sizeMin + (bucket.getSizeMaxForConstructionFlats() - bucket.sizeMin) / 2);
            flat.setState(bucket.stateMin + (bucket.stateMax - bucket.stateMin) / 2);
            if (bucket.isHighQuality) {
                flat.setNewlyBuilt(true);
                flat.setState(bucket.stateMax);
            }

            flat.calculateAndSetRent();

            GeoLocation geoLocation = flat.getBucket().getNeighbourhood().getGeoLocation();
            fictiveFlatsForRentInGeoLocations.get(geoLocation).add(flat);
        }

        for (GeoLocation geoLocation : geoLocations.values()) {
            Collections.sort(fictiveFlatsForRentInGeoLocations.get(geoLocation), Flat.comparatorRent);
        }



            List<Runnable> tasks = householdsForParallelComputing.stream().map(a -> {
                Runnable task = () -> {

                    for (Household household : a.values()) {
                        ModelRunnableFunctions.selectFictiveRentAndIncrementDemand(household);
                    }

                };
                return task;
            }).collect(Collectors.toList());

            ParallelComputer.compute(tasks);



        elapsedTimeFictiveDemandForRent = stopperFictiveDemandForRent.getElapsedTimeInMilliseconds();

    }

    public static void rentalMarket() {
        phase = Phase.RENTALMARKET;
        OwnStopper stopperRentalMarket = new OwnStopper();

        for (Flat flat : flats.values()) {
            if (flat.isAvailableForRent()) {
                flat.calculateAndSetRent();
            }
        }

        //external demand
        Map<Bucket, ArrayList<Flat>> flatsForExternalRentInBuckets = new HashMap<>();
        for (Bucket bucket : buckets.values()) {
            flatsForExternalRentInBuckets.put(bucket, new ArrayList<Flat>());
        }
        for (Flat flat : flats.values()) {
            if (flat.isAvailableForRent()) {
                flatsForExternalRentInBuckets.get(flat.bucket).add(flat);
            }
        }


        for (Bucket bucket : buckets.values()) {
            int[] externalDemandInBucket = externalDemand.get(bucket);
            ArrayList<Flat> flatsInBucket = flatsForExternalRentInBuckets.get(bucket);
            for (int i = 0; i < externalDemandInBucket[period]; i++) {
                if (flatsInBucket.size() == 0) break;
                int indexOfFlatInList = flatsInBucket.size() - 1;
                Flat flat = flatsInBucket.get(indexOfFlatInList);
                flatsInBucket.remove(indexOfFlatInList);
                flat.nPeriodsLeftForRent = 1;
                flat.rentedByExternal = true;
            }
        }

        //household rents
        for (GeoLocation geoLocation : flatsForRentInGeoLocations.keySet()) {
            flatsForRentInGeoLocations.get(geoLocation).clear();
        }

        for (Flat flat : flats.values()) {
            if (flat.isAvailableForRent()) {
                flat.calculateAndSetRent();
                GeoLocation geoLocation = flat.bucket.neighbourhood.geoLocation;
                flatsForRentInGeoLocations.get(geoLocation).add(flat);
            }
        }

        for (ArrayList<Flat> listOfFlatsInGeoLocation: flatsForRentInGeoLocations.values()) {
            Collections.sort(listOfFlatsInGeoLocation, Flat.comparatorRent);
        }


        for (Household household : householdsInRandomOrder) {

            if (household.mayTryToRent()) {
                Flat flatToRent = household.selectRent();
                if (flatToRent != null) {
                    rentFlat(flatToRent, household);
                }
            }
        }

        elapsedTimeRentalMarket = stopperRentalMarket.getElapsedTimeInMilliseconds();

    }

    public static void consumeAndSave() {
        phase = Phase.CONSUMEANDSAVE;
        OwnStopper stopperConsumeAndSave = new OwnStopper();


        //if payment is greater than the outstanding principal + interest, payment and duration is adjusted
        for (LoanContract loanContract: Model.loanContracts.values()) {
            if (loanContract.payment > loanContract.principal*(1+loanContract.monthlyInterestRate)) {
                loanContract.payment = loanContract.principal*(1+loanContract.monthlyInterestRate);
                loanContract.duration = 1;
            } else if (loanContract.duration == 1 && loanContract.payment < loanContract.principal*(1+loanContract.monthlyInterestRate)) {
                loanContract.duration++;
            }
        }

        for (Household household : households.values()) {
            household.getPaid();
        }


        for (Household household : households.values()) {
            household.payRent();
        }

        for (Flat flat : flats.values()) {
            if (flat.rentedByExternal && flat.ownerHousehold!=null) flat.ownerHousehold.creditDeposit(flat.rent);
        }

        for (Household household : households.values()) {
            household.consumeAndSave();
        }


        List<Runnable> tasksChangeInterestRate = loanContractsForParallelComputing.stream().map(a -> {
            Runnable task = () -> {
                for (LoanContract loanContract : a.values()) {
                    ModelRunnableFunctions.changeInterestRateIfNeeded(loanContract);
                }
            };
            return task;
        }).collect(Collectors.toList());
        ParallelComputer.compute(tasksChangeInterestRate);

        elapsedTimeConsumeAndSave = stopperConsumeAndSave.getElapsedTimeInMilliseconds();

    }


    public static void endOfPeriod() {
        phase = Phase.ENDOFPERIOD;
        OwnStopper stopperEndOfPeriod = new OwnStopper();

        calculateLandPrices();

        //History

        for (Bucket bucket : buckets.values()) {
            bucket.nFlatsForRent = 0;
            bucket.nFlatsRented = 0;
        }

        for (Flat flat : flats.values()) {
            if (flat.isForRent) flat.bucket.nFlatsForRent++;
            if (flat.renter != null || flat.rentedByExternal) flat.bucket.nFlatsRented++;
            if (flat.ownerConstructor != null || flat.nPeriodsLeftForConstruction>0 && flat.nPeriodsLeftForConstruction<Model.nPeriodsForConstruction-1) {
                flat.getGeoLocation().histNNewlyBuiltFlats[nHistoryPeriods + period]++;
            }
            if (flat.nPeriodsLeftForConstruction>0 && flat.ownerConstructor == null) {
                flat.getGeoLocation().histNNewlyBuiltFlatsSold[nHistoryPeriods + period]++;
            }
        }

        for (Bucket bucket : buckets.values()) {
            bucket.histNewlyBuiltDemand[nHistoryPeriods + period] = Math.max(bucket.newlyBuiltDemand,(int) (bucket.nNewlyBuiltSold*0.5));
            bucket.histNFlatsForRent[nHistoryPeriods + period] = bucket.nFlatsForRent;
            bucket.histNFlatsRented[nHistoryPeriods + period] = bucket.nFlatsRented;
            bucket.histNFictiveRentDemand[nHistoryPeriods + period] = bucket.nFictiveRentDemand;
            bucket.histNForSale[nHistoryPeriods + period] = bucket.nForSale;
            bucket.histNSold[nHistoryPeriods + period] = bucket.nSold;
        }

        for (Flat flat: Model.flats.values()) {
            if (flat.isForRent) {
                flat.bucket.neighbourhood.forRentValue += flat.getMarketPrice();
                if (flat.renter != null || flat.rentedByExternal) flat.bucket.neighbourhood.rentIncome += flat.rent;
            }
        }

        calculatePriceIndicesForNeighbourhoods();

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.histPriceIndex[nHistoryPeriods + period] = neighbourhood.priceIndex;
            neighbourhood.histPriceIndexToBeginning[nHistoryPeriods + period] = neighbourhood.priceIndexToBeginning;
            neighbourhood.histReturn[nHistoryPeriods + period] = neighbourhood.rentIncome/neighbourhood.forRentValue;
            if (neighbourhood.forRentValue==0) neighbourhood.histReturn[nHistoryPeriods + period]=neighbourhood.histReturn[nHistoryPeriods + period-1];
        }

        //nFlats neighbourhoodonként
        for (Flat flat: Model.flats.values()) {
            flat.bucket.neighbourhood.nFlats++;
        }
        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.histForRentValue[nHistoryPeriods + period] = neighbourhood.forRentValue;
        }
        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetPriceIndex();
            geoLocation.calculateAndSetPriceIndexToBeginning();
            geoLocation.histPriceIndex[nHistoryPeriods + period] = geoLocation.priceIndex;
            geoLocation.histPriceIndexToBeginning[nHistoryPeriods + period] = geoLocation.priceIndexToBeginning;
            if (geoLocation.priceIndexToBeginning == 0 || Double.isNaN(geoLocation.priceIndexToBeginning)) geoLocation.histPriceIndexToBeginning[nHistoryPeriods + period] = geoLocation.histPriceIndexToBeginning[nHistoryPeriods + period - 1];
            geoLocation.histRenovationUnitCost[nHistoryPeriods + period] = geoLocation.renovationUnitCost;
            geoLocation.histConstructionUnitCost[nHistoryPeriods + period] = geoLocation.constructionUnitCost;
            geoLocation.histRenovationQuantity[nHistoryPeriods + period] = geoLocation.renovationQuantity;
        }

        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.calculateAndSetDirectPriceIndex(0); //sets histDirectPriceIndexWithNewlyBuilt
            geoLocation.calculateAndSetDirectPriceIndex(1); //sets histDirectPriceIndexUsed
            geoLocation.calculateAndSetDirectPriceIndex(2); //sets histDirectPriceIndexNewlyBuilt
        }


        flatSaleRecordsDataForBeginningCSV();

        deleteOutdatedFlatSaleRecords();

        histNominalGDPLevel[nHistoryPeriods + period] = realGDPLevel * priceLevel;

        if (cumulatedZOPAmount>=ZOPLimit) ZOPLimitReached=true;

        int nFlatsEmptyCentralInvestor = 0;
        int nFlatsEmptyHouseholds = 0;
        int nFlatsRentedCentralInvestor = 0;
        int nFlatsRentedHouseholds = 0;
        for (Flat flat : Model.flats.values()) {
            if (flat.isForRent) {
                if (flat.isForSale || flat.isForcedSale) {
                    if (flat.ownerInvestor!=null) nFlatsEmptyCentralInvestor++;
                    if (flat.ownerHousehold!=null) nFlatsEmptyHouseholds++;
                }
                if (flat.rentedByExternal || flat.renter != null) {
                    if (flat.ownerInvestor!=null) nFlatsRentedCentralInvestor++;
                    if (flat.ownerHousehold!=null) nFlatsRentedHouseholds++;
                }
            }

        }

        //priceIndexToBeginning
        if (period==baseMaxPeriodForPriceIndexToBeginning) {

            for (int i = baseMaxPeriodForPriceIndexToBeginning; i >= 0 ; i--) {
                period = i;

                for (Neighbourhood neighbourhood : neighbourhoods.values()) {
                    neighbourhood.flatSaleRecordsDataForPriceIndexToBeginning.clear();
                }

                try {
                    Scanner scannerForCSV = new Scanner(new File("src/main/java/resources/flatSaleRecordsDataForBeginning.csv"));

                    scannerForCSV.nextLine();

                    //0. periodSold, 1. bucketId, 2. size, 3. state, 4. price;
                    while (scannerForCSV.hasNextLine()) {
                        String dataLine = scannerForCSV.nextLine();
                        String[] dataLineArray = dataLine.split(",");

                        int bucketId = Integer.parseInt(dataLineArray[1]);
                        double periodSold = Double.parseDouble(dataLineArray[0]);
                        double size= Double.parseDouble(dataLineArray[2]);
                        double state= Double.parseDouble(dataLineArray[3]);
                        double price= Double.parseDouble(dataLineArray[4]);

                        Double[] infoArray = {periodSold,size,state,price};
                        Model.buckets.get(bucketId).getNeighbourhood().flatSaleRecordsDataForPriceIndexToBeginning.add(infoArray);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
                    if (flatSaleRecord.isNewlyBuilt) continue;
                    if (useForcedSaleForPriceIndex==false && flatSaleRecord.isForcedSale) continue;
                    Double[] infoArray = {(double) flatSaleRecord.periodOfRecord,flatSaleRecord.size,flatSaleRecord.state,flatSaleRecord.price};
                    flatSaleRecord.bucket.neighbourhood.flatSaleRecordsDataForPriceIndexToBeginning.add(infoArray);
                }

                for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
                    neighbourhood.calculateAndSetPriceIndexToBeginning();
                }
                for (GeoLocation geoLocation : Model.geoLocations.values()) {
                    geoLocation.calculateAndSetPriceIndexToBeginning();
                }

            }

            period=baseMaxPeriodForPriceIndexToBeginning;
        }


        //Housing affordability calculations

        if (CAHAIForFTB) {
            affordabilityAgeIntervals = new double[4];
            affordabilityAgeIntervals[0] = 25;
            affordabilityAgeIntervals[1] = 26;
            affordabilityAgeIntervals[2] = 27;
            affordabilityAgeIntervals[3] = 100;
        }

        double[] nFullEmployedHouseholdsBudapest = new double[affordabilityAgeIntervals.length/2];
        double[] sumHouseholdWageBudapest = new double[affordabilityAgeIntervals.length/2];
        double[] nFullEmployedHouseholdsVidek = new double[affordabilityAgeIntervals.length/2];
        double[] sumHouseholdWageVidek = new double[affordabilityAgeIntervals.length/2];

        for (Household household : Model.households.values()) {
            int ageGroupIndexForAffordability = household.ageGroupIndexForAffordability();
            if (ageGroupIndexForAffordability<0) continue;
             boolean everyMemberEmployed = true;
             for (Individual individual : household.members) {
                 if (individual.nPeriodsInUnemployment>0 || individual.ageInPeriods>=Model.retirementAgeInPeriods) everyMemberEmployed = false;
             }
             if (everyMemberEmployed) {
                 if (household.preferredGeoLocation==Model.capital) {
                     nFullEmployedHouseholdsBudapest[ageGroupIndexForAffordability]++;
                     sumHouseholdWageBudapest[ageGroupIndexForAffordability] += household.getWageIncome();
                 } else {
                     nFullEmployedHouseholdsVidek[ageGroupIndexForAffordability]++;
                     sumHouseholdWageVidek[ageGroupIndexForAffordability] += household.getWageIncome();
                 }
             }
        }

        double[] nIndividualsWorkingBudapest = new double[affordabilityAgeIntervals.length/2];
        double[] sumIndividualWageBudapest = new double[affordabilityAgeIntervals.length/2];
        double[] nIndividualsWorkingVidek = new double[affordabilityAgeIntervals.length/2];
        double[] sumIndividualWageVidek = new double[affordabilityAgeIntervals.length/2];

        for (Individual individual : Model.individuals.values()) {
            int ageGroupIndexForAffordability = individual.ageGroupIndexForAffordability();
            if (ageGroupIndexForAffordability<0 || individual.nPeriodsInUnemployment>0 || individual.ageInPeriods>=Model.retirementAgeInPeriods) continue;
            if (individual.getPreferredGeoLocation()==Model.capital) {
                nIndividualsWorkingBudapest[ageGroupIndexForAffordability]++;
                sumIndividualWageBudapest[ageGroupIndexForAffordability] += individual.getWage();
            } else {
                nIndividualsWorkingVidek[ageGroupIndexForAffordability]++;
                sumIndividualWageVidek[ageGroupIndexForAffordability] += individual.getWage();
            }
        }

        double[] averageWageBudapest = new double[affordabilityAgeIntervals.length/2];
        double[] averageWageVidek = new double[affordabilityAgeIntervals.length/2];

        for (int i = 0; i < Model.affordabilityAgeIntervals.length/2; i++) {
            averageWageBudapest[i] = sumIndividualWageBudapest[i]/nIndividualsWorkingBudapest[i];
            averageWageVidek[i] = sumIndividualWageVidek[i]/nIndividualsWorkingVidek[i];
        }

        //Budapest-Vidék X AgeIntervals
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < Model.affordabilityAgeIntervals.length/2; j++) {

                double loanToValue = 0.7;
                double pti = 0.3;

                double wageIncome = 0;
                double houseValue = 0;
                double individualWageIncome = 0;
                if (i == 0) {
                    wageIncome = sumHouseholdWageBudapest[j]/nFullEmployedHouseholdsBudapest[j];
                    houseValue = sumFlatValueInAgeIntervalsBudapest[j]/nHouseholdsInAgeIntervalsBudapestBuying[j];
                    individualWageIncome = averageWageBudapest[j];
                } else {
                    wageIncome = sumHouseholdWageVidek[j]/nFullEmployedHouseholdsVidek[j];
                    houseValue = sumFlatValueInAgeIntervalsVidek[j]/nHouseholdsInAgeIntervalsVidekBuying[j];
                    individualWageIncome = averageWageVidek[j];
                }
                double lnWageIncome = Math.log(wageIncome);
                double ageCategoryEffectOnInterestRate = Model.banks.get(0).yearlyInterestRateRegressionCoeffAgeCategory1;
                if (j>=1) ageCategoryEffectOnInterestRate = Model.banks.get(0).yearlyInterestRateRegressionCoeffAgeCategory2;
                double yearlyInterestRate = Model.yearlyBaseRate + Model.banks.get(0).yearlyInterestRateRegressionConstant + yearlyInterestRateRegressionConstantDeviation + Model.banks.get(0).yearlyInterestRateRegressionCoeffLnWageIncome * lnWageIncome + Model.banks.get(0).yearlyInterestRateRegressionCoeffLTV * loanToValue + Model.loanFixedSpread + ageCategoryEffectOnInterestRate;
                double monthlyInterestRate = yearlyInterestRate/12;
                double monthlyPayment = houseValue*loanToValue * (monthlyInterestRate / (1 - Math.pow(1 + monthlyInterestRate, -maxDuration)));

                double affordability = individualWageIncome*2/monthlyPayment*pti;

                if (i==0 && j==0) {
                    outputData[720][period] = affordability; //Budapest Young
                } else if (i==0 && j==1) {
                    outputData[721][period] = affordability; //Budapest Middle
                } else if (i==1 && j==0) {
                    outputData[722][period] = affordability; //Videk Young
                }  else if (i==1 && j==1) {
                    outputData[723][period] = affordability; //Videk Middle
                }

            }
        }

        //Income deciles X AgeIntervals
        double[][] sumHouseholdWageDeciles = new double[affordabilityAgeIntervals.length/2][10];
        int[][] nHouseholdsDeciles = new int[affordabilityAgeIntervals.length/2][10];

        for (Household household : Model.households.values()) {
            if (household.rankDecilesFrom1>0) {
                Individual oldestIndividualInHousehold = household.members.get(0);
                if (household.members.size()>1 && household.members.get(1).getAgeInPeriods() > household.members.get(0).getAgeInPeriods()) oldestIndividualInHousehold = household.members.get(0);
                int ageGroupIndexForAffordability = oldestIndividualInHousehold.ageGroupIndexForAffordability();
                if (ageGroupIndexForAffordability>=0) {
                    sumHouseholdWageDeciles[ageGroupIndexForAffordability][household.rankDecilesFrom1-1]+=household.wageIncome;
                    nHouseholdsDeciles[ageGroupIndexForAffordability][household.rankDecilesFrom1-1]++;
                }

            }
        }


        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < Model.affordabilityAgeIntervals.length/2; j++) {

                double loanToValue = 0.7;
                double pti = 0.3;

                double wageIncome = sumHouseholdWageDeciles[j][i]/nHouseholdsDeciles[j][i];
                double houseValue = sumFlatValueInAgeIntervalsBudapest[j]/(nHouseholdsInAgeIntervalsBudapestBuying[j] + nHouseholdsInAgeIntervalsVidekBuying[j]) + sumFlatValueInAgeIntervalsVidek[j]/(nHouseholdsInAgeIntervalsBudapestBuying[j] + nHouseholdsInAgeIntervalsVidekBuying[j]);


                double lnWageIncome = Math.log(wageIncome);
                double ageCategoryEffectOnInterestRate = Model.banks.get(0).yearlyInterestRateRegressionCoeffAgeCategory1;
                if (j>=1) ageCategoryEffectOnInterestRate = Model.banks.get(0).yearlyInterestRateRegressionCoeffAgeCategory2;
                double yearlyInterestRate = Model.yearlyBaseRate + Model.banks.get(0).yearlyInterestRateRegressionConstant + yearlyInterestRateRegressionConstantDeviation + Model.banks.get(0).yearlyInterestRateRegressionCoeffLnWageIncome * lnWageIncome + Model.banks.get(0).yearlyInterestRateRegressionCoeffLTV * loanToValue + Model.loanFixedSpread + ageCategoryEffectOnInterestRate;
                double monthlyInterestRate = yearlyInterestRate/12;
                double monthlyPayment = houseValue*loanToValue * (monthlyInterestRate / (1 - Math.pow(1 + monthlyInterestRate, -maxDuration)));

                double affordability = wageIncome/monthlyPayment*pti;

                outputData[1300 + (j * 10) + i + 1][period] = affordability;


            }

        }

        elapsedTimeEndOfPeriod = stopperEndOfPeriod.getElapsedTimeInMilliseconds();

    }

    public static void investmentPurchasesScheme() {

        refreshFlatsForSaleForInvestment();
        investorInvestmentPurchases();
        householdInvestmentPurchases();

    }

    public static void refreshFlatsForSaleForInvestment() {

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.sizeToFlatMapForInvestment.clear();
            for (Bucket bucket : neighbourhood.buckets) {
                ArrayList<Flat> potentialArrayList = new ArrayList<>();
                neighbourhood.sizeToFlatMapForInvestment.putIfAbsent(bucket.sizeMax,potentialArrayList);
            }
        }

        flatsForSaleForInvestment.clear();

        for (Bucket bucket : Model.buckets.values()) {
            flatsForSaleForInvestment.put(bucket,bucket.neighbourhood.sizeToFlatMapForInvestment.get(bucket.sizeMax));
        }

        for (Flat flat : flats.values()) {
            if ((flat.isForSale || flat.isForcedSale) && flat.forSalePrice<1.2*flat.getMarketPrice()) {
                 flatsForSaleForInvestment.get(flat.bucket).add(flat);
            }
        }

    }

    public static void investorInvestmentPurchases() {
        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            if (neighbourhood.cumulativeInvestmentProbabilities.getSize()==0) continue;
            double investmentValue = 0;

            while (investmentValue < neighbourhood.plannedCentralInvestmentValue && neighbourhood.cumulativeInvestmentProbabilities.getSize()>0) {

                Bucket bucket = neighbourhood.cumulativeInvestmentProbabilities.selectObjectAccordingToCumulativeProbability(rnd.nextDouble());
                Flat flat = getBestInvestmentFlatInBucket(bucket,null);
                if (flat == null) {
                    neighbourhood.cumulativeInvestmentProbabilities.remove(bucket);
                }

                if (flat != null) {
                    if (flat.bucket.isHighQuality ==false) {
                        flat.investmentStateIncrease = bucket.stateMax - flat.state;
                    } else flat.investmentStateIncrease = 0;
                    investmentValue += flat.forSalePrice + flat.investmentStateIncrease * flat.size * flat.getGeoLocation().predictedRenovationUnitCost;
                    if (phase == Phase.INVESTMENTPURCHASES) {
                        buyFlat(flat, investors.get(0));
                        neighbourhood.centralInvestmentValue += flat.forSalePrice;
                    } else if (phase == Phase.BIDSFORFLATS) {
                        flat.incrementNBids(null);
                    }
                }
            }
        }
    }

    public static void householdInvestmentPurchases() {
        neighbourhoodInvestmentProbabilities.clear();
        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            double weight = neighbourhood.calculateWeightForNeighbourhoodInvestmentProbabilites();
            if (weight>0) neighbourhoodInvestmentProbabilities.put(weight, neighbourhood);
        }

        for (Household household : households.values()) {
            household.investIfPossible();
        }

    }

    public static void calculateLandPrices() {


        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            neighbourhood.sumSoldPrice = 0;
            neighbourhood.sumSoldArea = 0;
            neighbourhood.mightNeedOutOfRangeFlatForLandPrice = true;
        }

        for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
            if (flatSaleRecord.periodOfRecord == period && flatSaleRecord.isNewlyBuilt==false) {
                if (flatSaleRecord.bucket.neighbourhood.mightNeedOutOfRangeFlatForLandPrice == false && (flatSaleRecord.size>maxFlatSizeForLandPrice || flatSaleRecord.state>maxFlatStateForLandPrice)) continue;

                if (flatSaleRecord.bucket.neighbourhood.mightNeedOutOfRangeFlatForLandPrice == true && flatSaleRecord.size<maxFlatSizeForLandPrice && flatSaleRecord.state<maxFlatStateForLandPrice) {
                    flatSaleRecord.bucket.neighbourhood.sumSoldPrice = 0;
                    flatSaleRecord.bucket.neighbourhood.sumSoldArea = 0;
                    flatSaleRecord.bucket.neighbourhood.mightNeedOutOfRangeFlatForLandPrice = false;
                }

                flatSaleRecord.bucket.neighbourhood.sumSoldPrice += flatSaleRecord.price;
                flatSaleRecord.bucket.neighbourhood.sumSoldArea += flatSaleRecord.size;

            }
        }

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            if (neighbourhood.sumSoldArea>0) {
                neighbourhood.landPrice = Model.landPriceAdjuster * neighbourhood.sumSoldPrice/neighbourhood.sumSoldArea;
                int landPriceDecreaseStartPeriod = Model.landPriceDecreaseAdjusterStartPeriod; //by gradually decreasing the land price (relative to the unit area price), we mimick the fact that many houses are built on smaller real estates (without it the drop in newly built demand would be much more significant than in reality)
                if (period>=landPriceDecreaseStartPeriod) {
                    neighbourhood.landPrice *= Math.max(Model.landPriceDecreaseAdjusterMaxValue,Math.pow(Model.landPriceDecreaseAdjusterBase,period-landPriceDecreaseStartPeriod-1));
                }
            }
        }

    }

    public static void calculatePriceIndicesForNeighbourhoods() {

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.flatSaleRecordsForPriceIndex.clear();
        }

        for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
            if (useForcedSaleForPriceIndex==false && flatSaleRecord.isForcedSale) continue;
            flatSaleRecord.bucket.neighbourhood.flatSaleRecordsForPriceIndex.add(flatSaleRecord);
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.calculateAndSetPriceIndex();
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            if (neighbourhood.priceIndexRefreshed == false) neighbourhood.getPriceIndexOfSimilarNeighbourhoods();
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.flatSaleRecordsDataForPriceIndexToBeginning.clear();
        }

        try {
            Scanner scannerForCSV = new Scanner(new File("src/main/java/resources/flatSaleRecordsDataForBeginning.csv"));

            scannerForCSV.nextLine();

            //0. periodSold, 1. bucketId, 2. size, 3. state, 4. price;
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int bucketId = Integer.parseInt(dataLineArray[1]);
                double periodSold = Double.parseDouble(dataLineArray[0]);
                double size= Double.parseDouble(dataLineArray[2]);
                double state= Double.parseDouble(dataLineArray[3]);
                double price= Double.parseDouble(dataLineArray[4]);

                Double[] infoArray = {periodSold,size,state,price};
                Model.buckets.get(bucketId).getNeighbourhood().flatSaleRecordsDataForPriceIndexToBeginning.add(infoArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
            if (flatSaleRecord.isNewlyBuilt) continue;
            if (useForcedSaleForPriceIndex==false && flatSaleRecord.isForcedSale) continue;
            Double[] infoArray = {(double) flatSaleRecord.periodOfRecord,flatSaleRecord.size,flatSaleRecord.state,flatSaleRecord.price};
            flatSaleRecord.bucket.neighbourhood.flatSaleRecordsDataForPriceIndexToBeginning.add(infoArray);
        }

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            neighbourhood.calculateAndSetPriceIndexToBeginning();
        }


    }

    static void flatSaleRecordsDataForBeginningCSV() {

        if (Model.period == 3) {
            PrintWriter printWriter;
            try {

                printWriter = new PrintWriter("src/main/java/resources/flatSaleRecordsDataForBeginning.csv", "UTF-8");

                for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
                    if (flatSaleRecord.isNewlyBuilt) continue;
                    if (useForcedSaleForPriceIndex==false && flatSaleRecord.isForcedSale) continue;
                    if (flatSaleRecord.periodOfRecord == 3) {
                        printWriter.print(Model.period + "," + flatSaleRecord.bucket.getId() + "," + flatSaleRecord.size + "," + flatSaleRecord.state + "," + flatSaleRecord.price + "\n");
                    }

                }


                printWriter.close();


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Model.period==4 || Model.period==5) {
            try {
                FileWriter writer = new FileWriter("src/main/java/resources/flatSaleRecordsDataForBeginning.csv",true);
                for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
                    if (flatSaleRecord.isNewlyBuilt) continue;
                    if (useForcedSaleForPriceIndex==false && flatSaleRecord.isForcedSale) continue;
                    if (flatSaleRecord.periodOfRecord == Model.period) {
                        writer.write(Model.period + "," + flatSaleRecord.bucket.getId() + "," + flatSaleRecord.size + "," + flatSaleRecord.state +  "," + flatSaleRecord.price + "\n");
                    }

                }
                writer.flush();
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    static void deleteOutdatedFlatSaleRecords() {

        Map<Integer, FlatSaleRecord> newFlatSaleRecords = new HashMap<>();
        for (FlatSaleRecord flatSaleRecord : flatSaleRecords.values()) {
            if (flatSaleRecord.periodOfRecord >= period - nPeriodsForFlatSaleRecords + 1) {
                newFlatSaleRecords.put(flatSaleRecord.getId(), flatSaleRecord);
            }
        }

        flatSaleRecords.clear();
        for (FlatSaleRecord flatSaleRecord : newFlatSaleRecords.values()) {
            flatSaleRecords.put(flatSaleRecord.getId(), flatSaleRecord);
        }
    }



    public static void setPeriodicalValues() {
        unemploymentRates = unemploymentRatesPath[period];
        unemploymentProbabilities = unemploymentProbabilitiesPath[period];
        if (phase == Phase.SETUP) unemploymentProbabilities = unemploymentRates;

        realGDPLevel = realGDPLevelPath[period];
        priceLevel = priceLevelPath[period];
        yearlyBaseRate = yearlyBaseRatePath[period];
        yearlyInterestRateRegressionConstantDeviation = yearlyInterestRateRegressionConstantDeviationPath[period];
        LTV = LTVPath[period];
        DSTI = DSTIPath[period];
        maxDuration = maxDurationPath[period];
        taxRate = taxRatePath[period];
        constructionUnitCostIndex = constructionUnitCostIndexPath[period];

        loanOneYearFixationShare = loanOneYearFixationSharePathPoints[period];
        loanFiveYearFixationShare = loanFiveYearFixationSharePathPoints[period];
        loanFixedShare = loanFixedSharePathPoints[period];
        loanOneYearFixationSpread = loanOneYearFixationSpreadPathPoints[period];
        loanFiveYearFixationSpread = loanFiveYearFixationSpreadPathPoints[period];
        loanFixedSpread = loanFixedSpreadPathPoints[period];

        isMoratoryPeriod = false;
        if (simulationWithCovid && period>=moratoryStartPeriodWithCovid && period<=moratoryEndPeriodWithCovid) isMoratoryPeriod = true;



        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocation.predictedRenovationUnitCost = geoLocation.histRenovationUnitCost[nHistoryPeriods + period - 1];
        }

        if (simulationWithShock) {
            realGDPLevel *= realGDPLevelShockPath[period];
            priceLevel *= priceLevelShockPath[period];
            yearlyBaseRate *= yearlyBaseRateShockPath[period];
            for (int i = 0; i < nTypes; i++) {
                unemploymentRates[i] *= unemploymentRatesShockPath[period][i];
                unemploymentProbabilities[i] *= unemploymentProbabilitiesShockPath[period][i];
            }

        }

        calculateUnemploymentEndingProbabilities();

        for (int i = 0; i < affordabilityAgeIntervals.length/2; i++) {
            nHouseholdsInAgeIntervalsBudapestBuying[i] = 0;
            sumFlatValueInAgeIntervalsBudapest[i] = 0;
            nHouseholdsInAgeIntervalsVidekBuying[i] = 0;
            sumFlatValueInAgeIntervalsVidek[i] = 0;
        }

    }

    public static void nullMiscDerivedVariables() {
        renovationDemand = 0;
        renovationQuantity = 0;
        sumOwnHomePrice = 0;
        sumOwnHomePurchaserFirstWage = 0;
        nBabyLoanApplications = 0;
    }

    public static void refreshAbsCoeffState() {
        //to endogenously reach equilibrium renovation, absCoeffStateHabit is gradually adjusted for every geoLocation
        for (GeoLocation geoLocation : Model.geoLocations.values()) {

            double equilibriumRenovation = 0;
            for (Flat flat : Model.flats.values()) {
                if (flat.getGeoLocation()==geoLocation) {
                    equilibriumRenovation += flat.size * flat.state * stateDepreciation;
                }
            }

            if (period<36 && geoLocation.histRenovationQuantity[nHistoryPeriods+period-1]<1*equilibriumRenovation) {
               geoLocation.absCoeffStateHabit += 0.001;
            }

        }
    }

    public static void calculateUnemploymentEndingProbabilities() {
        if (phase!=Phase.SETUP) {
            int[] nActive = new int[nTypes];
            int[] nUnemployed = new int[nTypes];
            int[] nUnemployedAboveMinPeriods = new int[nTypes];
            for (Individual individual : Model.individuals.values()) {
                if (individual.workExperience>0 && individual.ageInPeriods<retirementAgeInPeriods-1) {
                    nActive[individual.typeIndex]++;
                    if (individual.nPeriodsInUnemployment >=1) {
                        nUnemployed[individual.typeIndex]++;
                        if (individual.nPeriodsInUnemployment >=minUnemploymentPeriods[individual.typeIndex]) {
                            nUnemployedAboveMinPeriods[individual.typeIndex]++;
                        }
                    }
                }

            }

            for (int i = 0; i < nTypes; i++) {
                unemploymentEndingProbabilities[i] = 0;

                double nUnemployedIfNooneFinishesUnemployment = nUnemployed[i] + unemploymentProbabilities[i]*nActive[i];
                double nUnemployedPeopleWhoShouldLeaveUnemploymentToReachUnemploymentRate = nUnemployedIfNooneFinishesUnemployment - unemploymentRates[i] * nActive[i];
                unemploymentEndingProbabilities[i] = Math.min(1,(nUnemployedPeopleWhoShouldLeaveUnemploymentToReachUnemploymentRate) / (double) (nUnemployedAboveMinPeriods[i]));
            }

        }

    }



    public static void buyFlat(Flat flat, Constructor constructor) {

        ArrayList<Flat> forSaleFlats = Model.flatsForSaleInNeighbourhoodsForConstructionPurchases.get(flat.getBucket().getNeighbourhood());
        forSaleFlats.remove(0);
        payPriceForOwner(flat);
        flat.ownerConstructor = constructor;
            if (flat.isForRent && flat.nPeriodsLeftForRent>0) {
                flat.renter.rentHome = null;
                flat.renter = null;
            }
        flat.setForRent(false);
        flat.setWillBeForRent(false);
        buyFlatAccounting(flat);

    }

    public static void buyFlat(Flat flat, Investor investor) {

        ArrayList<Flat> forSaleFlats = flatsForSaleForInvestment.get(flat.bucket);
        forSaleFlats.remove(flat);
        payPriceForOwner(flat);
        flat.ownerInvestor = investor;
        investor.properties.add(flat);
        if (flat.nPeriodsLeftForConstruction == 0) {
            flat.isForRent = true;
        } else {
            flat.willBeForRent = true;
        }

        buyFlatAccounting(flat);

    }

    public static void buyFlatForInvestment(Flat flat, Household household) {

        ArrayList<Flat> forSaleFlats = flatsForSaleForInvestment.get(flat.bucket);
        forSaleFlats.remove(flat);
        payPriceForOwner(flat);

        if (household.getDeposit() - household.minDeposit < flat.forSalePrice) {
            household.makeLoanContractForFlat(flat);
            LoanContract loanContract = flat.loanContract;
            if (loanContract.principal>0) {
                loanContract.forSalePriceAtIssuance = flat.forSalePrice;
            }
        }
        household.chargeDeposit(flat.forSalePrice);

        flat.ownerHousehold = household;
        household.properties.add(flat);
        if (flat.nPeriodsLeftForConstruction == 0) {
            flat.isForRent = true;
        } else {
            flat.willBeForRent = true;
        }
        buyFlatAccounting(flat);

    }

    public static void buyFlat(Flat flat, Household household) {//purchasing own home, BTL purchases of households are handled in buyFlatForInvetsment

        household.periodOfLastConsideration = -1;
        household.valueOfPeerFlatIfCantBuy = 0;

        if (flat.isEligibleForCSOK()) {
            double CSOKTransfer = Math.min(household.newlyBuiltCSOK, flat.forSalePrice);
            household.creditDeposit(CSOKTransfer);
            household.utilizedCSOK += CSOKTransfer;
        }

        if (flat.isEligibleForFalusiCSOK()) {
            double CSOKTransfer = Math.min(household.falusiCSOK, flat.forSalePrice);
            household.creditDeposit(CSOKTransfer);
            household.utilizedCSOK += CSOKTransfer;
        }

        payPriceForOwner(flat);

        if (household.getDeposit() - household.minDeposit < flat.forSalePrice || Model.banks.get(0).flatEligibleForZOP(flat)) {

            household.makeLoanContractForFlat(flat);
            LoanContract loanContract = flat.loanContract;

            if (loanContract.principal>0) {
                loanContract.forSalePriceAtIssuance = flat.forSalePrice;
                if (Bank.flatEligibleForZOP(loanContract.collateral)) {
                    cumulatedZOPAmount += loanContract.principal;
                }
            }

        }


        household.chargeDeposit(flat.forSalePrice);

        if (flat.nPeriodsLeftForConstruction == 0) {
            if (household.home != null) {
                household.properties.add(household.home);
            }
            household.home = flat;
            household.setShouldNotRent(true);
        } else {
            household.homeUnderConstruction = flat;
        }


        household.setMoving(false);
        household.setNPeriodsWaitingForFlatToBuy(0);

        flat.ownerHousehold = household;
            if (flat.isForRent && flat.nPeriodsLeftForRent>0) {
                flat.renter.rentHome = null;
                flat.renter = null;
            }
        flat.setForRent(false);
        flat.setWillBeForRent(false);

        buyFlatAccounting(flat);

        if (household.firstBuyer) {
            household.setFirstBuyer(false);
            household.setFirstTimeBuyerPurchaseInThisPeriod(true);
        }

        household.setCanChangeGeoLocation(false);
        household.preferredGeoLocation = flat.getGeoLocation();

        sumOwnHomePrice += flat.forSalePrice / Model.priceLevel / Model.realGDPLevel;
        sumOwnHomePurchaserFirstWage += household.getSumFirstWage();



        int ageGroupIndexForAffordability = household.ageGroupIndexForAffordability();

        if (ageGroupIndexForAffordability>=0) {

            if (flat.getGeoLocation()==Model.capital) {
                if (OwnFunctions.isInRange(flat.size,affordabilitySizeIntervalsBudapest[ageGroupIndexForAffordability*2],affordabilitySizeIntervalsBudapest[ageGroupIndexForAffordability*2+1])) {
                    nHouseholdsInAgeIntervalsBudapestBuying[ageGroupIndexForAffordability]++;
                    sumFlatValueInAgeIntervalsBudapest[ageGroupIndexForAffordability]+=flat.forSalePrice;
                }

            } else {
                if (OwnFunctions.isInRange(flat.size,affordabilitySizeIntervalsVidek[ageGroupIndexForAffordability*2],affordabilitySizeIntervalsVidek[ageGroupIndexForAffordability*2+1])) {
                    nHouseholdsInAgeIntervalsVidekBuying[ageGroupIndexForAffordability]++;
                    sumFlatValueInAgeIntervalsVidek[ageGroupIndexForAffordability]+=flat.forSalePrice;
                }
            }

        }

    }

    public static void rentFlat(Flat flat, Household household) {
        household.rentHome = flat;
        flat.renter = household;

        flat.nPeriodsLeftForRent = nMaxPeriodsForRent;
        if (flat.isForSale || flat.isForcedSale) flat.nPeriodsLeftForRent = 1;
    }


    public static void payPriceForOwner(Flat flat) {

        if (flat.isForcedSale) {
            if (flat.ownerHousehold!=null) {
                flat.ownerHousehold.setShouldNotRent(false);
            }
        }

        if (flat.ownerHousehold != null) {

            if (flat.isForcedSale == false) {
                flat.ownerHousehold.creditDeposit(flat.forSalePrice);
            } else {
                flat.ownerHousehold.creditDeposit(flat.forSalePrice * incomeRatioForBankFromFlatForSalePrice);
            }

            if (flat.loanContract != null) {
                LoanContract loanContract = flat.loanContract;
                if (loanContract.bridgeLoanCollateral==flat) loanContract.endBridgeLoan();
                if (loanContract.collateral==flat) flat.loanContract.endLoanContract();
            }

            if (flat.ownerHousehold.home == flat) {
                flat.ownerHousehold.periodOfTakingUnsoldHomeToMarket = - 100;
                flat.ownerHousehold.home = null;
            } else flat.ownerHousehold.properties.remove(flat);

            flat.ownerHousehold = null;

        } else if (flat.ownerConstructor != null) {
            flat.ownerConstructor.flatsReady.remove(flat);
            flat.ownerConstructor = null;
        } else if (flat.ownerInvestor != null) {
            //to reduce runtime, sold flats of the ownerInvestor are removed from the ownerInvestor's arrayList only after all the purchases had been done
            flat.ownerInvestor = null;
        }



    }


    public static void buyFlatAccounting(Flat flat) {

        flat.getGeoLocation().outputData[1][period] ++;
        flat.getGeoLocation().outputData[2][period] += flat.forSalePrice;

         flat.bucket.nSold++;
        if (flat.isNewlyBuilt) flat.bucket.nNewlyBuiltSold++;
        flat.getNeighbourhood().nTransactions++;

        flat.lastMarketPrice = flat.forSalePrice;
        flat.setForSale(false);
        flat.setForcedSale(false);
        flat.setNForSalePeriods(0);


            FlatSaleRecord flatSaleRecord = Model.createFlatSaleRecord();
            flatSaleRecord.setPeriodOfRecord(period);
            flatSaleRecord.setPrice(flat.forSalePrice);
            flatSaleRecord.setBucket(flat.bucket);
            flatSaleRecord.setSize(flat.size);
            flatSaleRecord.setState(flat.state);
            flatSaleRecord.setNeighbourhoodQuality(flat.bucket.neighbourhood.quality);
            flatSaleRecord.setNewlyBuilt(flat.isNewlyBuilt);
            flatSaleRecord.setForcedSale(flat.isForcedSale);
            flatSaleRecord.flatId = flat.id;

        flat.setNewlyBuilt(false);
        flat.boughtNow = true;

    }

    public static Flat getBestInvestmentFlatInBucket(Bucket bucket, Household household) {

        ArrayList<Flat> forSale = flatsForSaleForInvestment.get(bucket);

        Flat bestInvestmentFlat = null;
        double bestUnitPrice = 0;
        for (Flat flat : forSale) {
            if (flat.state>bucket.stateMax) continue;
            if (bestInvestmentFlat == null) {
                    bestInvestmentFlat = flat;
                    bestUnitPrice = calculateUnitPriceForInvestmentFlat(bucket, flat, household);
            } else {
                double unitPrice = calculateUnitPriceForInvestmentFlat(bucket, flat, household);

                if (unitPrice < bestUnitPrice) {
                    bestInvestmentFlat = flat;
                    bestUnitPrice = unitPrice;
                }
            }
        }

        return bestInvestmentFlat;
    }

    private static double calculateUnitPriceForInvestmentFlat(Bucket bucket, Flat flat, Household household) {
        if (flat.isForcedSale && household!=null && household.deposit<household.minDeposit + flat.forSalePrice) return household.deposit;
        double unitPrice = flat.forSalePrice / flat.size + (bucket.stateMax - flat.state) * flat.getGeoLocation().predictedRenovationUnitCost * (1 + renovationCostBuffer);
        if (flat.nPeriodsLeftForConstruction > 0)
            unitPrice *= Math.pow(1 + Math.max(0,flat.bucket.neighbourhood.expectedReturn) / 12, flat.nPeriodsLeftForConstruction);
        return unitPrice;
    }

    public static Bank createBank() {
        Bank bank = new Bank();
        addBankToMap(bank);
        return bank;
    }

    public static Bank createBank(int id) {
        Bank bank = new Bank(id);
        addBankToMap(bank);
        return bank;
    }

    private static void addBankToMap(Bank bank) {
        Model.banks.put(bank.getId(), bank);
        Model.nBanks++;
    }

    public static Bucket createBucket() {
        Bucket bucket = new Bucket();
        addBucketToMap(bucket);
        return bucket;
    }

    public static Bucket createBucket(int id) {
        Bucket bucket = new Bucket(id);
        addBucketToMap(bucket);
        return bucket;
    }

    private static void addBucketToMap(Bucket bucket) {
        Model.buckets.put(bucket.getId(), bucket);
        Model.nBuckets++;
    }

    public static Flat createFlat() {
        Flat flat = new Flat();
        addFlatToMap(flat);
        return flat;
    }

    public static Flat createFlat(int id) {
        Flat flat = new Flat(id);
        addFlatToMap(flat);
        return flat;
    }

    private static void addFlatToMap(Flat flat) {
        Model.flats.put(flat.getId(), flat);
        Model.nFlats++;
        Model.flatsForParallelComputing.get(flat.getId() % nThreads).put(flat.getId(), flat);
    }

    public static FlatSaleRecord createFlatSaleRecord() {
        FlatSaleRecord flatSaleRecord = new FlatSaleRecord();
        addFlatSaleRecordToMap(flatSaleRecord);
        return flatSaleRecord;
    }

    public static FlatSaleRecord createFlatSaleRecord(int id) {
        FlatSaleRecord flatSaleRecord = new FlatSaleRecord(id);
        addFlatSaleRecordToMap(flatSaleRecord);
        return flatSaleRecord;
    }

    private static void addFlatSaleRecordToMap(FlatSaleRecord flatSaleRecord) {
        Model.flatSaleRecords.put(flatSaleRecord.getId(), flatSaleRecord);
        Model.nFlatSaleRecords++;
    }

    public static GeoLocation createGeoLocation() {
        GeoLocation geoLocation = new GeoLocation();
        addGeoLocationToMap(geoLocation);
        maxAreaUnderConstructionInGeoLocations.put(geoLocation,Double.POSITIVE_INFINITY);
        return geoLocation;
    }

    public static GeoLocation createGeoLocation(int id) {
        GeoLocation geoLocation = new GeoLocation(id);
        addGeoLocationToMap(geoLocation);
        maxAreaUnderConstructionInGeoLocations.put(geoLocation,Double.POSITIVE_INFINITY);
        return geoLocation;
    }

    private static void addGeoLocationToMap(GeoLocation geoLocation) {
        Model.geoLocations.put(geoLocation.getId(), geoLocation);
        Model.nGeoLocations++;
        Model.geoLocationsForParallelComputing.get(geoLocation.getId() % nThreads).put(geoLocation.getId(),geoLocation);
        if (geoLocation.getId()>highestIdForGeoLocations) highestIdForGeoLocations = geoLocation.getId();
    }

    public static Household createHousehold() {
        Household household = new Household();
        addHouseholdToMap(household);
        return household;
    }

    public static Household createHousehold(int id) {
        Household household = new Household(id);
        addHouseholdToMap(household);
        return household;
    }

    private static void addHouseholdToMap(Household household) {
        Model.households.put(household.getId(), household);
        Model.nHouseholds++;
        Model.householdsForParallelComputing.get(household.getId() % nThreads).put(household.getId(), household);
    }

    public static Individual createIndividual() {
        Individual individual = new Individual();
        if (phase != Phase.BEGINNINGOFPERIOD) {
            addIndividualToMap(individual);
        } else {
            individualsToAddtoMap.add(individual);
        }
        return individual;
    }

    public static Individual createIndividual(int id) {
        Individual individual = new Individual(id);
        addIndividualToMap(individual);
        return individual;
    }

    private static void addIndividualToMap(Individual individual) {
        Model.individuals.put(individual.getId(), individual);
        Model.nIndividuals++;
        Model.individualsForParallelComputing.get(individual.getId() % nThreads).put(individual.getId(), individual);
    }

    public static Investor createInvestor() {
        Investor investor = new Investor();
        addInvestorToMap(investor);
        return investor;
    }

    public static Investor createInvestor(int id) {
        Investor investor = new Investor(id);
        addInvestorToMap(investor);
        return investor;
    }

    private static void addInvestorToMap(Investor investor) {
        Model.investors.put(investor.getId(), investor);
        Model.nInvestors++;
    }

    public static LoanContract createLoanContract() {
        LoanContract loanContract = new LoanContract();
        addLoanContractToMap(loanContract);
        return loanContract;
    }

    public static LoanContract createLoanContract(int id) {
        LoanContract loanContract = new LoanContract(id);
        addLoanContractToMap(loanContract);
        return loanContract;
    }

    private static void addLoanContractToMap(LoanContract loanContract) {
        Model.loanContracts.put(loanContract.getId(), loanContract);
        Model.nLoanContracts++;
        Model.loanContractsForParallelComputing.get(loanContract.getId() % nThreads).put(loanContract.getId(), loanContract);
    }

    public static Neighbourhood createNeighbourhood() {
        Neighbourhood neighbourhood = new Neighbourhood();
        addNeighbourhoodToMap(neighbourhood);
        return neighbourhood;
    }

    public static Neighbourhood createNeighbourhood(int id) {
        Neighbourhood neighbourhood = new Neighbourhood(id);
        addNeighbourhoodToMap(neighbourhood);
        return neighbourhood;
    }

    private static void addNeighbourhoodToMap(Neighbourhood neighbourhood) {
        Model.neighbourhoods.put(neighbourhood.getId(), neighbourhood);
        Model.nNeighbourhoods++;
        if (neighbourhood.getId()>highestIdForNeighbourhoods) highestIdForNeighbourhoods = neighbourhood.getId();
    }

    public static PriceRegressionFunctionLinear createPriceRegressionFunctionLinear() {
        PriceRegressionFunctionLinear priceRegressionFunctionLinear = new PriceRegressionFunctionLinear();
        addPriceRegressionFunctionLinearToMap(priceRegressionFunctionLinear);
        return priceRegressionFunctionLinear;
    }

    public static PriceRegressionFunctionLinear createPriceRegressionFunctionLinear(int id) {
        PriceRegressionFunctionLinear priceRegressionFunctionLinear = new PriceRegressionFunctionLinear(id);
        addPriceRegressionFunctionLinearToMap(priceRegressionFunctionLinear);
        return priceRegressionFunctionLinear;
    }

    private static void addPriceRegressionFunctionLinearToMap(PriceRegressionFunctionLinear priceRegressionFunctionLinear) {
        Model.priceRegressionFunctionsLinear.put(priceRegressionFunctionLinear.getId(), priceRegressionFunctionLinear);
        Model.nPriceRegressionFunctionsLinear++;
    }

    public static UtilityFunctionCES createUtilityFunctionCES() {
        UtilityFunctionCES utilityFunctionCES = new UtilityFunctionCES();
        addUtilityFunctionCESToMap(utilityFunctionCES);
        return utilityFunctionCES;
    }

    public static UtilityFunctionCES createUtilityFunctionCES(int id) {
        UtilityFunctionCES utilityFunctionCES = new UtilityFunctionCES(id);
        addUtilityFunctionCESToMap(utilityFunctionCES);
        return utilityFunctionCES;
    }

    private static void addUtilityFunctionCESToMap(UtilityFunctionCES utilityFunctionCES) {
        Model.utilityFunctionsCES.put(utilityFunctionCES.getId(), utilityFunctionCES);
        Model.nUtilityFunctionsCES++;
    }

    public static Constructor createConstructor() {
        Constructor constructor = new Constructor();
        addConstructorToMap(constructor);
        return constructor;
    }

    public static Constructor createConstructor(int id) {
        Constructor constructor = new Constructor(id);
        addConstructorToMap(constructor);
        return constructor;
    }

    private static void addConstructorToMap(Constructor constructor) {
        Model.constructors.put(constructor.getId(), constructor);
        Model.nConstructors++;
    }

    public static void makeOutputDataArray() {
        outputData = new double[MainRun.nOutputDataSeriesModel][nPeriods];
        for (Bucket bucket : buckets.values()) {
            bucket.outputData = new double[MainRun.nOutputDataSeriesBucket][nPeriods];
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.outputData = new double[MainRun.nOutputDataSeriesNeighbourhood][nPeriods];
        }
        for (GeoLocation geoLocation : geoLocations.values()) {
            geoLocation.outputData = new double[MainRun.nOutputDataSeriesGeoLocation][nPeriods];
        }
        if (modelGUI == null) modelGUI = new ModelGUI();
    }

    public static void writeOutputDataValues() {


        outputData[10][period] = realGDPLevel * priceLevel;
        outputData[11][period] = realGDPLevel;
        outputData[12][period] = priceLevel;

        outputData[13][period] = households.size();
        outputData[14][period] = individuals.size();

        outputData[100 + 0][period] = elapsedTimePeriod;
        outputData[100 + 1][period] = elapsedTimeBeginningOfPeriod;
        outputData[100 + 2][period] = elapsedTimeFictiveDemandForNewlyBuiltFlats;
        outputData[100 + 3][period] = elapsedTimeHouseholdPurchases;
        outputData[100 + 4][period] = elapsedTimeRentalMarket;


        for (Flat flat : flats.values()) {
            flat.getNeighbourhood().outputData[0][period]++;
            if (flat.isForSale()) flat.getNeighbourhood().outputData[1][period]++;
            if (flat.renter != null) flat.getNeighbourhood().outputData[2][period]++;
            flat.getNeighbourhood().outputData[3][period]+=flat.size;
            flat.getNeighbourhood().outputData[4][period]+=flat.state;
            if (flat.nPeriodsLeftForConstruction == Model.nPeriodsForConstruction - 1) {
                flat.getNeighbourhood().outputData[5][period]++;
                flat.getNeighbourhood().outputData[6][period]+=flat.size;
            }
        }

        writeLoanOutputData();

    }

    public static void writeLoanOutputData() {


        double thresholdDSTI = 0.4;

        int[] decilesHHNum = new int[10+1];
        int[] decilesHHWithHousingLoanContractNum = new int[10+1];
        double[] decilesHHWithHousingLoanContractVolume = new double[10+1];
        int[] decilesHHWithHousingNPLNum = new int[10+1];
        double[] decilesHHWithHousingNPLVolume = new double[10+1];

        int[] decilesHHWithHousingLoanContractNewNum = new int[10+1];
        double[] decilesHHWithHousingLoanContractNewVolume = new double[10+1];
        int[] decilesHHWithHousingNPLNewNum = new int[10+1];
        double[] decilesHHWithHousingNPLNewVolume = new double[10+1];

        double[] decilesSumDSTI = new double[10+1];
        double[] decilesSumDSTIHousingNew = new double[10+1];
        double[] decilesSumCollateralValueHousingNew = new double[10+1];
        double[] decilesSumIncomeWithHousingLoan = new double[10+1];
        int[] decilesLoanContractAboveDSTIThresholdNum = new int[10+1];
        double[] decilesLoanContractAboveDSTIThresholdVolume = new double[10+1];

        for (Household household : Model.households.values()) {
            if (household.rankDecilesFrom1 >0) decilesHHNum[household.rankDecilesFrom1]++;

        }

        for (LoanContract loanContract : Model.loanContracts.values()) {
            if (loanContract.isHousingLoan) {
                int debtorRankDeciles = loanContract.debtor.rankDecilesFrom1;

                decilesHHWithHousingLoanContractNum[debtorRankDeciles]++;
                decilesHHWithHousingLoanContractVolume[debtorRankDeciles]+=loanContract.principal;
                decilesSumDSTI[debtorRankDeciles]+=loanContract.debtor.actualDSTI;
                decilesSumIncomeWithHousingLoan[debtorRankDeciles]+=loanContract.debtor.householdIncome;
                if (loanContract.debtor.actualDSTI >thresholdDSTI) {
                    decilesLoanContractAboveDSTIThresholdNum[debtorRankDeciles]++;
                    decilesLoanContractAboveDSTIThresholdVolume[debtorRankDeciles]+=loanContract.principal;
                }


                if (loanContract.periodOfIssuance==Model.period) {
                    decilesHHWithHousingLoanContractNewNum[debtorRankDeciles]++;
                    decilesHHWithHousingLoanContractNewVolume[debtorRankDeciles]+=loanContract.principal;

                    decilesSumDSTIHousingNew[debtorRankDeciles] += loanContract.payment/loanContract.debtor.householdIncome;
                    decilesSumCollateralValueHousingNew[debtorRankDeciles] += loanContract.collateral.lastMarketPrice;
                }

                if (loanContract.nNonPerformingPeriods>0) {
                    decilesHHWithHousingNPLNum[debtorRankDeciles]++;
                    decilesHHWithHousingNPLVolume[debtorRankDeciles]+=loanContract.principal;
                    if (loanContract.nNonPerformingPeriods==1) {
                        decilesHHWithHousingNPLNewNum[debtorRankDeciles]++;
                        decilesHHWithHousingNPLNewVolume[debtorRankDeciles]+=loanContract.principal;
                    }
                }

            }

        }

        for (int i = 1; i <= 10; i++) {
            outputData[2000+i][period] = decilesHHWithHousingLoanContractNum[i];
            outputData[2010+i][period] = decilesHHWithHousingLoanContractVolume[i];
            outputData[2020+i][period] = decilesHHWithHousingNPLNum[i];
            outputData[2030+i][period] = decilesHHWithHousingNPLVolume[i];
            outputData[2040+i][period] = decilesHHWithHousingNPLNum[i]/(double) decilesHHWithHousingLoanContractNum[i];
            outputData[2050+i][period] = decilesHHWithHousingNPLVolume[i]/(double) decilesHHWithHousingLoanContractVolume[i];
            outputData[2060+i][period] = decilesHHWithHousingLoanContractNum[i]/(double) OwnFunctions.sumIntArray(decilesHHWithHousingLoanContractNum);
            outputData[2070+i][period] = decilesHHWithHousingLoanContractVolume[i]/(double) OwnFunctions.sumDoubleArray(decilesHHWithHousingLoanContractVolume);
            outputData[2080+i][period] = decilesHHWithHousingNPLNum[i]/(double) OwnFunctions.sumIntArray(decilesHHWithHousingNPLNum);
            outputData[2090+i][period] = decilesHHWithHousingNPLVolume[i]/(double) OwnFunctions.sumDoubleArray(decilesHHWithHousingNPLVolume);
        }

        for (int i = 1; i <= 10; i++) {
            outputData[2100+i][period] = decilesHHWithHousingLoanContractNewNum[i];
            outputData[2110+i][period] = decilesHHWithHousingLoanContractNewVolume[i];
            outputData[2120+i][period] = decilesHHWithHousingNPLNewNum[i];
            outputData[2130+i][period] = decilesHHWithHousingNPLNewVolume[i];
            outputData[2140+i][period] = decilesHHWithHousingNPLNewNum[i]/(double) decilesHHWithHousingLoanContractNewNum[i];
            outputData[2150+i][period] = decilesHHWithHousingNPLNewVolume[i]/(double) decilesHHWithHousingLoanContractNewVolume[i];
            outputData[2160+i][period] = decilesHHWithHousingLoanContractNewNum[i]/(double) OwnFunctions.sumIntArray(decilesHHWithHousingLoanContractNewNum);
            outputData[2170+i][period] = decilesHHWithHousingLoanContractNewVolume[i]/(double) OwnFunctions.sumDoubleArray(decilesHHWithHousingLoanContractNewVolume);
            outputData[2180+i][period] = decilesHHWithHousingNPLNewNum[i]/(double) OwnFunctions.sumIntArray(decilesHHWithHousingNPLNewNum);
            outputData[2190+i][period] = decilesHHWithHousingNPLNewVolume[i]/(double) OwnFunctions.sumDoubleArray(decilesHHWithHousingNPLNewVolume);
        }

    }

    public static void calculateHouseholdDecilesRanking() {
        double[] values = new double[households.size()];
        int index = 0;
        for (Household household : households.values()) {
            if (household.getWageIncome()==0) {
                household.wageIncome = threadNextDouble(household);
            }
            values[index]=household.getPotentialWageIncome()/household.getMembers().size();

            index++;
        }
        Arrays.sort(values);


        int nQuantiles = 10;

        for (Household household : households.values()) {
            for (int i = 1; i < nQuantiles + 1; i++) {
                if (household.getPotentialWageIncome()/household.getMembers().size()<values[values.length/nQuantiles*i-1]) {
                    household.rankDecilesFrom1 =i;
                    if (household.wageIncome<1) household.wageIncome=0;
                    break;
                }
            }
        }
    }


    public static double threadNextInt(HasID object, int bound) {
        return rndArray[object.getId() % nThreads].nextInt(bound);
    }

    public static double threadNextDouble(HasID object) {
        return rndArray[object.getId() % nThreads].nextDouble();
    }

    public static double threadNextGaussian(HasID object) {
        return rndArray[object.getId() % nThreads].nextGaussian();
    }

    public static void writeOutputDataCsv() {
        PrintWriter printWriter;

        Date date = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("dd-hh-mm");
        String strDate = dateFormat.format(date);
        String fileName = "src/main/java/savedCSVs/" + strDate + ".csv";
        if (parametersToOverride != null && parametersToOverride.outputDataName != null) fileName = "src/main/java/savedCSVs/outputData" + parametersToOverride.outputDataName + ".csv";

        try {

            OwnStopper stopperWritOutputDataCsv = new OwnStopper();

            printWriter = new PrintWriter(fileName, "UTF-8");

            for (int i = 0; i < Model.outputData.length; i++) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("m 0 ");
                stringBuffer.append(i);
                for (int j = 0; j < Model.outputData[i].length; j++) {
                    stringBuffer.append(" " + Model.outputData[i][j]);
                }

                printWriter.print(stringBuffer + "\n");
            }

            if (MainRun.writeOutputDataWithBuckets) {
                for (Bucket bucket : Model.buckets.values()) {
                    for (int i = 0; i < bucket.outputData.length; i++) {
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("b ");
                        stringBuffer.append(bucket.getId() + " ");
                        stringBuffer.append(i);
                        for (int j = 0; j < bucket.outputData[i].length; j++) {
                            stringBuffer.append(" " + bucket.outputData[i][j]);
                        }
                        printWriter.print(stringBuffer + "\n");
                    }
                }
            }


            for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
                for (int i = 0; i < neighbourhood.outputData.length; i++) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("n ");
                    stringBuffer.append(neighbourhood.getId() + " ");
                    stringBuffer.append(i);
                    for (int j = 0; j < neighbourhood.outputData[i].length; j++) {
                        stringBuffer.append(" " + neighbourhood.outputData[i][j]);
                    }
                    printWriter.print(stringBuffer + "\n");
                }
            }

            for (GeoLocation geoLocation : Model.geoLocations.values()) {
                for (int i = 0; i < geoLocation.outputData.length; i++) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append("l ");
                    stringBuffer.append(geoLocation.getId() + " ");
                    stringBuffer.append(i);
                    for (int j = 0; j < geoLocation.outputData[i].length; j++) {
                        stringBuffer.append(" " + geoLocation.outputData[i][j]);
                    }
                    printWriter.print(stringBuffer + "\n");
                }
            }

            printWriter.close();
            stopperWritOutputDataCsv.printElapsedTimeInMilliseconds();

        } catch (Exception e) {

        }
    }

    public static void loadOutputDataCsv(String pathName) {

        OwnStopper stopperLoadOutputDataCsv = new OwnStopper();

        Model.buckets.clear();
        Model.neighbourhoods.clear();
        Model.geoLocations.clear();

        outputData = null;

        modelGUI.labelDataAboutRun.setText(pathName);

        //determine outputDataArraySizes
        int nOutputPeriods = 0;
        int nOutputDataSeriesModel = 0;
        int nOutputDataSeriesBucket = 0;
        int nOutputDataSeriesNeighbourhood = 0;
        int nOutputDataSeriesGeoLocation = 0;

        try{

            Scanner scanner = new Scanner(new File(pathName));
            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                String[] stringArray = nextLine.split(" ");

                if (nOutputPeriods == 0) nOutputPeriods = stringArray.length - 3;

                if (stringArray[0].toString().equals("n")) {
                    nOutputDataSeriesNeighbourhood = Integer.parseInt(stringArray[2]) + 1;
                } else if (stringArray[0].toString().equals("l")) {
                    nOutputDataSeriesGeoLocation = Integer.parseInt(stringArray[2]) + 1;
                } else if (stringArray[0].toString().equals("m")) {
                    nOutputDataSeriesModel = Integer.parseInt(stringArray[2]) + 1;
                } else if (stringArray[0].toString().equals("b")) {
                    nOutputDataSeriesBucket = Integer.parseInt(stringArray[2]) + 1;
                }

            }
        } catch (Exception e) {
            System.out.println("Problem while determining outputData size");
        }

        try{

        Scanner scanner = new Scanner(new File(pathName));
        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            String[] stringArray = nextLine.split(" ");

            if (outputData == null) outputData = new double[nOutputDataSeriesModel][nOutputPeriods];

            if (stringArray[0].toString().equals("m")) {
                int variableIndex = Integer.parseInt(stringArray[2]);
                for (int i = 3; i < stringArray.length; i++) {
                    outputData[variableIndex][i-3] = Double.parseDouble(stringArray[i]);
                }
            }

            if (stringArray[0].toString().equals("b")) {
                int variableIndex = Integer.parseInt(stringArray[2]);
                int id = Integer.parseInt(stringArray[1]);
                Bucket bucket = buckets.get(id);
                if (bucket == null) {
                    bucket = Model.createBucket(id);
                    bucket.createOutputDataArray(nOutputDataSeriesBucket, nOutputPeriods);
                }
                for (int i = 3; i < stringArray.length; i++) {
                    bucket.outputData[variableIndex][i-3] = Double.parseDouble(stringArray[i]);
                }
            }

            if (stringArray[0].toString().equals("n")) {
                int variableIndex = Integer.parseInt(stringArray[2]);
                int id = Integer.parseInt(stringArray[1]);
                Neighbourhood neighbourhood = neighbourhoods.get(id);
                if (neighbourhood == null) {
                    neighbourhood = Model.createNeighbourhood(id);
                    neighbourhood.createOutputDataArray(nOutputDataSeriesNeighbourhood, nOutputPeriods);
                }
                for (int i = 3; i < stringArray.length; i++) {
                    neighbourhood.outputData[variableIndex][i-3] = Double.parseDouble(stringArray[i]);
                }
            }

            if (stringArray[0].toString().equals("l")) {
                int variableIndex = Integer.parseInt(stringArray[2]);
                int id = Integer.parseInt(stringArray[1]);
                GeoLocation geoLocation = geoLocations.get(id);
                if (geoLocation == null) {
                    geoLocation = Model.createGeoLocation(id);
                    geoLocation.createOutputDataArray(nOutputDataSeriesGeoLocation, nOutputPeriods);
                }
                for (int i = 3; i < stringArray.length; i++) {
                    geoLocation.outputData[variableIndex][i-3] = Double.parseDouble(stringArray[i]);
                }
            }

        }
        } catch (Exception e) {
            System.out.println("Problem while loading " + pathName);
        }

        modelGUI.txtToPeriod.setText(Integer.toString((int) nOutputPeriods - 1));
        OutputDataNames.updateOutputDataNames();
        stopperLoadOutputDataCsv.printElapsedTimeInMilliseconds();
    }

    public static void calculateAndSetAggregateMarketValueForNeighbourhoods() {
        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            neighbourhood.aggregateEstimatedMarketValue = 0;
        }
        for (Flat flat : Model.flats.values()) {
            if (flat.nPeriodsLeftForConstruction==0) {
                flat.getNeighbourhood().aggregateEstimatedMarketValue += flat.getEstimatedMarketPrice();
            }
        }
    }

    public static void calculateAverageWageInGeoLocations() {
        for (GeoLocation geoLocation : geoLocations.values()) {
            geoLocation.nFullTimeWorkers = 0;
            geoLocation.sumFullTimeWage = 0;
        }
        for (Individual individual : individuals.values()) {
            if(individual.ageInPeriods<Model.retirementAgeInPeriods && individual.nPeriodsInUnemployment ==0 && individual.wage>=0.5*individual.firstWage) {
                Household household = individual.household==null ? individual.parentHousehold : individual.household;
                GeoLocation geoLocation = household.preferredGeoLocation;
                if (household.home != null) {
                    geoLocation = household.home.getGeoLocation();
                } else if (household.rentHome != null) {
                    geoLocation = household.rentHome.getGeoLocation();
                }
                geoLocation.sumFullTimeWage += individual.wage;
                geoLocation.nFullTimeWorkers += 1;
            }
        }
        for (GeoLocation geoLocation : geoLocations.values()) {
            geoLocation.averageWage =geoLocation.sumFullTimeWage /(double) geoLocation.nFullTimeWorkers;
        }
    }

    public static double getMaxBucketSize() {
        double maxBucketSize = 0;
        if (Model.neighbourhoods.get(1) != null) {
            for (Bucket bucket : Model.neighbourhoods.get(1).buckets) {
                if (bucket.sizeMax>maxBucketSize) maxBucketSize = bucket.sizeMax;
            }
        } else {
            for (Bucket bucket : Model.buckets.values()) {
                if (bucket.sizeMax>maxBucketSize) maxBucketSize = bucket.sizeMax;
            }
        }

        return maxBucketSize;
    }


    public static void increaseOutputData(int additionalPeriods) {

        outputData = increasOutputDataForObject(outputData);

        for (Bucket bucket : buckets.values()) {
            bucket.outputData = increasOutputDataForObject(bucket.outputData);
        }

        for (Neighbourhood neighbourhood : neighbourhoods.values()) {
            neighbourhood.outputData = increasOutputDataForObject(neighbourhood.outputData);
        }
        for (GeoLocation geoLocation : geoLocations.values()) {
            geoLocation.outputData = increasOutputDataForObject(geoLocation.outputData);
        }
    }

    public static double[][] increasOutputDataForObject(double[][] outputData) {

        double[][] newOutputData = new double[outputData.length][nPeriods];

        for (int i = 0; i < outputData.length; i++) {
            double[] outputDataSeries = outputData[i];
            for (int j = 0; j < outputDataSeries.length; j++) {
                newOutputData[i][j]=outputDataSeries[j];
            }
        }

        return newOutputData;

    }

    public static void clearModelVariables() {
        phase = Phase.SETUP;
        period = 0;
        iterateHouseholdPurchases = true;

        capital = null;
        agglomeration = null;

        Constructor.nextId = 0;
        Investor.nextId = 0;
        Bank.nextId = 0;
        Flat.nextId = 0;
        Household.nextId = 0;
        Individual.nextId = 0;
        FlatSaleRecord.nextId = 0;
        Bucket.nextId = 0;

        miscHH = new HashMap<>();
        neighbourhoodInvestmentProbabilities = new MappingWithWeights<>();
        externalDemand = new HashMap<>();

        households = new HashMap<Integer, Household>();
        flats = new HashMap<Integer, Flat>();
        loanContracts = new HashMap<Integer, LoanContract>();
        individuals = new HashMap<Integer, Individual>();
        geoLocations = new HashMap<Integer, GeoLocation>();
        neighbourhoods = new HashMap<Integer, Neighbourhood>();
        buckets = new HashMap<Integer, Bucket>();
        banks = new HashMap<Integer, Bank>();
        utilityFunctionsCES = new HashMap<Integer, UtilityFunctionCES>();
        priceRegressionFunctionsLinear = new HashMap<Integer, PriceRegressionFunctionLinear>();
        constructors = new HashMap<Integer, Constructor>();
        flatSaleRecords = new HashMap<Integer, FlatSaleRecord>();
        investors = new HashMap<Integer, Investor>();

        flatsForSaleInGeoLocations = new HashMap<>();
        nonNewlyBuiltFlatsForSaleInBuckets = new HashMap<>();
        flatsForSaleInNeighbourhoodsForConstructionPurchases = new HashMap<>();
        flatsForSaleForInvestment = new HashMap<>();
        fictiveFlatsForSale = new ArrayList<>();
        fictiveNewlyBuiltFlatsForSale = new ArrayList<>();
        fictiveFlatsForRentInGeoLocations = new HashMap<>();
        flatsForRentInGeoLocations = new HashMap<>();
        forSaleRecords = new ArrayList<>();

        householdsForParallelComputing = new ArrayList<>();
        flatsForParallelComputing = new ArrayList<>();
        individualsForParallelComputing = new ArrayList<>();
        geoLocationsForParallelComputing = new ArrayList<>();

        sampleFlatsForSaleInGeoLocationsForAssessingPotentialNewHomes = new HashMap<>();
        sampleFlatsForSaleInBucketsForAssessingPotentialNewHomes = new HashMap<>();

        individualsToRemoveFromMap = new HashMap<>();
        individualsToAddtoMap = new ArrayList<>();

        householdsInRandomOrder = new ArrayList<>();

        maxAreaUnderConstructionInGeoLocations = new HashMap<>();

        nHouseholds = 0;
        nFlats = 0;
        nLoanContracts = 0;
        nIndividuals = 0;
        nGeoLocations = 0;
        nNeighbourhoods = 0;
        nBuckets = 0;
        nBanks = 0;
        nUtilityFunctionsCES = 0;
        nPriceRegressionFunctionsLinear = 0;
        nConstructors = 0;
        nFlatSaleRecords = 0;
        nInvestors = 0;


        //clear other static classes
        GeneralBucket.geoLocations = new ArrayList<>();
        IndividualBucketMarriage.buckets = new ConcurrentHashMap<>();
        DataLoader.nFlatsRented =  new HashMap<>();
        DataLoader.sampleUtilityFunctions = new ArrayList<>();
        DataLoader.loanContractYear =  new HashMap<>();
        DataLoader.ageProbabilities =  new HashMap<>();
        DataLoader.rentalsNeededInGeoLocations =  new HashMap<>();
        DataLoader.probabilityOfRentAccordingToAge = new HashMap<>();

        singleGeoLocation = null;
        loanContractsForParallelComputing = new ArrayList<>();
        individualsInArrayListAtBeginningOfPeriod = new ArrayList<>();
        flatsInRandomOrder = new ArrayList<>();


    }

    public static void printGivenSeriesToCsv() {

        int beginPeriod = 0;
        int endPeriod = outputData[1].length - 1;
        ArrayList<String> names = new ArrayList<>();
        ArrayList<String> codes = new ArrayList<>();
        ArrayList<Double[]> results = new ArrayList<>();


        names.add("Number of transactions");
        codes.add("+l1");
        names.add("Average price of transactions");
        codes.add("+l2/l1");



        for (String string : codes) {
            StringBuilder stringBuilder = new StringBuilder(string);
            double[] doubleSeries = modelGUI.getEvaluatedTimeSeries(stringBuilder,beginPeriod,endPeriod);
            Double[] series = new Double[doubleSeries.length];
            for (int i = 0; i < doubleSeries.length; i++) {
                series[i] = doubleSeries[i];
            }
            results.add(series);
        }

        PrintWriter printWriter;
        String fileName = csvNameForPrintGivenSeriesToCsv;

        try {

            printWriter = new PrintWriter(fileName, "UTF-8");


            StringBuilder newRow = new StringBuilder();
            for (String name : names) {
                newRow.append(name + "\t");
            }
            newRow.deleteCharAt(newRow.length()-1);
            System.out.println(newRow);
            printWriter.println("\t" + newRow);

            for (int i = 0; i < endPeriod-beginPeriod+1; i++) {
                newRow = new StringBuilder();
                for (Double[] series : results) {
                    newRow.append(series[i] + "\t");
                }
                newRow.deleteCharAt(newRow.length()-1);
                System.out.println(newRow);
                int dateInt = 201800 + i/12 * 100 + i%12 + 1;
                printWriter.println(dateInt + "\t" + newRow);
            }


            printWriter.close();

        } catch (Exception e) {

        }

    }

    public static void randomizeParameters() {

        if (Model.sobolRowIndex>0) {
            randomizeParametersForSobolIndex(Model.sobolRowIndex,Model.absoluteMaxDeviationForParameters);
            return;
        }
        double absoluteMaxDeviationForParameters = Model.absoluteMaxDeviationForParameters;
        double randomForIntegers = 0;
        double changeForIntegers = 0;
        sizeDistanceRatioThresholdForClosestNeighbourCalculation *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        stateDistanceRatioThresholdForClosestNeighbourCalculation *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        sizeWeightInClosestNeighbourCalculation *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        stateWeightInClosestNeighbourCalculation *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        monthlyForSalePriceDecrease *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        additionalForcedSaleDiscount *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        maxFlatStateForLandPrice *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        constructionMarkupRatio1Level *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        constructionMarkupRatio2Level *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        parameterForConstructionForSalePriceAdjustment *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        randomForIntegers = rnd.nextDouble();
        if (randomForIntegers<0.33333) {
            changeForIntegers = -1;
        } else if (randomForIntegers<0.66666) {
            changeForIntegers = 0;
        } else changeForIntegers = 1;
        nForSalePeriodsToStartAdjustingConstructionForSalePrice += changeForIntegers;

        randomForIntegers = rnd.nextDouble();
        if (randomForIntegers<0.33333) {
            changeForIntegers = -1;
        } else if (randomForIntegers<0.66666) {
            changeForIntegers = 0;
        } else changeForIntegers = 1;
        nFictiveFlatsForSalePerBucket += changeForIntegers;
        renovationProbability *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        coeffRenovationRatio *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        depositInheritanceRatio *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        minConsumptionRate *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        nPeriodsUntilMinConsumption *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        nPeriodsUntilMinConsumption = Math.max(1,nPeriodsUntilMinConsumption);
        minConsumptionPerCapitaLower *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        minConsumptionPerCapitaLowerThreshold *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        minConsumptionPerCapitaUpper *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        minConsumptionPerCapitaUpperThreshold *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        newlyBuiltUtilityAdjusterCoeff1 *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        newlyBuiltUtilityAdjusterCoeff2 *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        mayRenovateWhenBuyingRatio *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        renovationStateIncreaseWhenBuying *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        renovationCostBuffer *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        canChangeGeoLocationProbability[0] *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        canChangeGeoLocationProbability[1] *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        for (int i = 2; i < 8; i++) {
            canChangeGeoLocationProbability[i] = canChangeGeoLocationProbability[1];
        }
        probabilityOfMandatoryMoving *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        ageInYearsForMandatoryMoving *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        probabilityOfAssessingPotentialNewHomes *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        thresholdRatioForMoving *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        randomForIntegers = rnd.nextDouble();
        if (randomForIntegers<0.33333) {
            changeForIntegers = -1;
        } else if (randomForIntegers<0.66666) {
            changeForIntegers = 0;
        } else changeForIntegers = 1;
        nPeriodsForAverageNewlyBuiltDemand += changeForIntegers;
        targetNewlyBuiltBuffer *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        nPeriodsForConstruction *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        maxNFlatsToBuildInBucketToAverageNewlyBuiltDemandRatio *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        constructionAreaNeedRatio *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        monthlyInvestmentRatioConstantCentralInvestor *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        monthlyInvestmentRatioCoeffExpectedReturnCentralInvestor *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        maxPlannedInvestmentValueToAggregateMarketValue *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        maxHouseholdInvestmentProbability *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        householdInvestmentProbabilityCoeff *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        householdInvestmentProbabilityPower *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        targetUtilizationRatio *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        randomForIntegers = rnd.nextDouble();
        if (randomForIntegers<0.33333) {
            changeForIntegers = -1;
        } else if (randomForIntegers<0.66666) {
            changeForIntegers = 0;
        } else changeForIntegers = 1;
        maxNIterationsForHouseholdPurchases += changeForIntegers;
        coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        powerInAdjustmentInReservationPrice *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        adjusterProbabilityOfPlacingBid *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        reservationPriceShare *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        bridgeLoanDuration *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        maxDurationPath[0] *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        for (int i = 1; i < maxDurationPath.length; i++) {
            maxDurationPath[i] = maxDurationPath[1];
        }
        renovationLoanDuration *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        randomForIntegers = rnd.nextDouble();
        if (randomForIntegers<0.33333) {
            changeForIntegers = -1;
        } else if (randomForIntegers<0.66666) {
            changeForIntegers = 0;
        } else changeForIntegers = 1;
        nNonPerformingPeriodsForForcedSale += changeForIntegers;
        rentMarkupCoeff *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        rentMarkupPower *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        yearlyRentSaleProbabilityAtZeroExpectedReturnSpread *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;
        minYearlyExpectedReturnSpreadForZeroRentSaleProbability *= 1 + (rnd.nextDouble() - 0.5) * absoluteMaxDeviationForParameters * 2;

    }

    public static void randomizeParametersForSobolIndex(int sobolRowIndex, double absoluteMaxDeviationForParameters) {

        try {
            Scanner scannerForCSV = new Scanner(new File("src/main/java/resources/sobolpoints.csv"));


            int i = 1;

            while (i<sobolRowIndex) {
                String dataLine = scannerForCSV.nextLine();
                i++;
            }
            String dataLine = scannerForCSV.nextLine();
            String[] dataLineArray = dataLine.split(",");

            double random0 = Double.parseDouble(dataLineArray[0]);
            double random1 = Double.parseDouble(dataLineArray[1]);
            double random2 = Double.parseDouble(dataLineArray[2]);
            double random3 = Double.parseDouble(dataLineArray[3]);

            soboli = Integer.parseInt(dataLineArray[4]);
            sobolj = Integer.parseInt(dataLineArray[5]);

            minConsumptionPerCapitaUpper *= 1 + (random0 - 0.5) * absoluteMaxDeviationForParameters * 2;
            minConsumptionRate *= 1 + (random0 - 0.5) * absoluteMaxDeviationForParameters * 2;
            minConsumptionPerCapitaLowerThreshold *= 1 + (random0 - 0.5) * absoluteMaxDeviationForParameters * 2;
            minConsumptionPerCapitaLower *= 1 + (random0 - 0.5) * absoluteMaxDeviationForParameters * 2;
            minConsumptionPerCapitaUpperThreshold *= 1 + (random0 - 0.5) * absoluteMaxDeviationForParameters * 2;

            probabilityOfMandatoryMoving *= 1 + (random1 - 0.5) * absoluteMaxDeviationForParameters * 2;
            ageInYearsForMandatoryMoving *= 1 + (random1 - 0.5) * absoluteMaxDeviationForParameters * 2;

            int changeForIntegers = 0;
            if (random2<0.33333) {
                changeForIntegers = -1;
            } else if (random2<0.66666) {
                changeForIntegers = 0;
            } else changeForIntegers = 1;
            nNonPerformingPeriodsForForcedSale += changeForIntegers;
            realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount*= 1 + (random2 - 0.5) * absoluteMaxDeviationForParameters * 2;

            targetUtilizationRatio *= 1 + (random3 - 0.5) * absoluteMaxDeviationForParameters * 2;
            householdInvestmentProbabilityPower *= 1 + (random3 - 0.5) * absoluteMaxDeviationForParameters * 2;
            minYearlyExpectedReturnSpreadForZeroRentSaleProbability *= 1 + (random3 - 0.5) * absoluteMaxDeviationForParameters * 2;


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeSensitivityCsv() {
        PrintWriter printWriter;

        Date date = new Date(System.currentTimeMillis());
        DateFormat dateFormat = new SimpleDateFormat("dd-hh-mm-ss");
        String strDate = dateFormat.format(date);
        String fileName = "src/outputs/sensitivity" + strDate + ".csv";

        try {

            OwnStopper stopperWriteInfoToCsv = new OwnStopper();

            printWriter = new PrintWriter(fileName, "UTF-8");

            StringBuffer stringBufferHeaderLine = new StringBuffer();
            stringBufferHeaderLine.append("Period");
            for (GeoLocation geoLocation : Model.geoLocations.values()) {
                stringBufferHeaderLine.append(",nTransactionsGL" + geoLocation.getId());
            }
            for (GeoLocation geoLocation : Model.geoLocations.values()) {
                stringBufferHeaderLine.append(",AveragePriceGL" + geoLocation.getId());
            }
            printWriter.print(stringBufferHeaderLine + "\n");

            System.out.println("PRINT: " + stringBufferHeaderLine);

            for (int i = 0; i < nPeriods; i++) {
                StringBuffer stringBuffer = new StringBuffer();

                int year = 2018 + (int) Math.floor(i/12.0);
                Integer month = Math.floorMod(i,12) + 1;
                String monthString = month.toString();
                if (month<10) monthString = "0" + month;

                stringBuffer.append(year  + monthString + ",");

                double nTransactions = 0;
                double sumSoldPrice = 0;
                for (GeoLocation geoLocation : Model.geoLocations.values()) {
                    nTransactions += geoLocation.outputData[1][i];
                    sumSoldPrice += geoLocation.outputData[2][i];
                }
                double averagePrice = sumSoldPrice/nTransactions;
                stringBuffer.append(nTransactions + ",");
                stringBuffer.append(averagePrice + ",");

                stringBuffer.deleteCharAt(stringBuffer.length()-1);
                printWriter.print(stringBuffer + "\n");


            }

            StringBuffer stringBufferParameter = new StringBuffer();

            stringBufferParameter.append("soboli," + soboli + "\n");
            stringBufferParameter.append("sobolj," + sobolj + "\n");


            stringBufferParameter.append("sizeDistanceRatioThresholdForClosestNeighbourCalculation," + sizeDistanceRatioThresholdForClosestNeighbourCalculation + "\n");
            stringBufferParameter.append("stateDistanceRatioThresholdForClosestNeighbourCalculation," + stateDistanceRatioThresholdForClosestNeighbourCalculation + "\n");
            stringBufferParameter.append("sizeWeightInClosestNeighbourCalculation," + sizeWeightInClosestNeighbourCalculation + "\n");
            stringBufferParameter.append("stateWeightInClosestNeighbourCalculation," + stateWeightInClosestNeighbourCalculation + "\n");
            stringBufferParameter.append("monthlyForSalePriceDecrease," + monthlyForSalePriceDecrease + "\n");
            stringBufferParameter.append("additionalForcedSaleDiscount," + additionalForcedSaleDiscount + "\n");
            stringBufferParameter.append("realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount," + realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount + "\n");
            stringBufferParameter.append("maxFlatStateForLandPrice," + maxFlatStateForLandPrice + "\n");
            stringBufferParameter.append("constructionMarkupRatio1Level," + constructionMarkupRatio1Level + "\n");
            stringBufferParameter.append("constructionMarkupRatio2Level," + constructionMarkupRatio2Level + "\n");
            stringBufferParameter.append("parameterForConstructionForSalePriceAdjustment," + parameterForConstructionForSalePriceAdjustment + "\n");
            stringBufferParameter.append("nForSalePeriodsToStartAdjustingConstructionForSalePrice," + nForSalePeriodsToStartAdjustingConstructionForSalePrice + "\n");
            stringBufferParameter.append("nFictiveFlatsForSalePerBucket," + nFictiveFlatsForSalePerBucket + "\n");
            stringBufferParameter.append("renovationProbability," + renovationProbability + "\n");
            stringBufferParameter.append("coeffRenovationRatio," + coeffRenovationRatio + "\n");
            stringBufferParameter.append("depositInheritanceRatio," + depositInheritanceRatio + "\n");
            stringBufferParameter.append("minConsumptionRate," + minConsumptionRate + "\n");
            stringBufferParameter.append("nPeriodsUntilMinConsumption," + nPeriodsUntilMinConsumption + "\n");
            stringBufferParameter.append("minConsumptionPerCapitaLower," + minConsumptionPerCapitaLower + "\n");
            stringBufferParameter.append("minConsumptionPerCapitaLowerThreshold," + minConsumptionPerCapitaLowerThreshold + "\n");
            stringBufferParameter.append("minConsumptionPerCapitaUpper," + minConsumptionPerCapitaUpper + "\n");
            stringBufferParameter.append("minConsumptionPerCapitaUpperThreshold," + minConsumptionPerCapitaUpperThreshold + "\n");
            stringBufferParameter.append("newlyBuiltUtilityAdjusterCoeff1," + newlyBuiltUtilityAdjusterCoeff1 + "\n");
            stringBufferParameter.append("newlyBuiltUtilityAdjusterCoeff2," + newlyBuiltUtilityAdjusterCoeff2 + "\n");
            stringBufferParameter.append("mayRenovateWhenBuyingRatio," + mayRenovateWhenBuyingRatio + "\n");
            stringBufferParameter.append("renovationStateIncreaseWhenBuying," + renovationStateIncreaseWhenBuying + "\n");
            stringBufferParameter.append("renovationCostBuffer," + renovationCostBuffer + "\n");
            stringBufferParameter.append("canChangeGeoLocationProbability[0]," + canChangeGeoLocationProbability[0] + "\n");
            stringBufferParameter.append("canChangeGeoLocationProbability[1]," + canChangeGeoLocationProbability[1] + "\n");
            stringBufferParameter.append("probabilityOfMandatoryMoving," + probabilityOfMandatoryMoving + "\n");
            stringBufferParameter.append("ageInYearsForMandatoryMoving," + ageInYearsForMandatoryMoving + "\n");
            stringBufferParameter.append("probabilityOfAssessingPotentialNewHomes," + probabilityOfAssessingPotentialNewHomes + "\n");
            stringBufferParameter.append("baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes," + baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes + "\n");
            stringBufferParameter.append("thresholdRatioForMoving," + thresholdRatioForMoving + "\n");
            stringBufferParameter.append("nPeriodsForAverageNewlyBuiltDemand," + nPeriodsForAverageNewlyBuiltDemand + "\n");
            stringBufferParameter.append("targetNewlyBuiltBuffer," + targetNewlyBuiltBuffer + "\n");
            stringBufferParameter.append("nPeriodsForConstruction," + nPeriodsForConstruction + "\n");
            stringBufferParameter.append("maxNFlatsToBuildInBucketToAverageNewlyBuiltDemandRatio," + maxNFlatsToBuildInBucketToAverageNewlyBuiltDemandRatio + "\n");
            stringBufferParameter.append("constructionAreaNeedRatio," + constructionAreaNeedRatio + "\n");
            stringBufferParameter.append("monthlyInvestmentRatioConstantCentralInvestor," + monthlyInvestmentRatioConstantCentralInvestor + "\n");
            stringBufferParameter.append("monthlyInvestmentRatioCoeffExpectedReturnCentralInvestor," + monthlyInvestmentRatioCoeffExpectedReturnCentralInvestor + "\n");
            stringBufferParameter.append("maxPlannedInvestmentValueToAggregateMarketValue," + maxPlannedInvestmentValueToAggregateMarketValue + "\n");
            stringBufferParameter.append("maxHouseholdInvestmentProbability," + maxHouseholdInvestmentProbability + "\n");
            stringBufferParameter.append("householdInvestmentProbabilityCoeff," + householdInvestmentProbabilityCoeff + "\n");
            stringBufferParameter.append("householdInvestmentProbabilityPower," + householdInvestmentProbabilityPower + "\n");
            stringBufferParameter.append("targetUtilizationRatio," + targetUtilizationRatio + "\n");
            stringBufferParameter.append("maxNIterationsForHouseholdPurchases," + maxNIterationsForHouseholdPurchases + "\n");
            stringBufferParameter.append("coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy," + coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy + "\n");
            stringBufferParameter.append("powerInAdjustmentInReservationPrice," + powerInAdjustmentInReservationPrice + "\n");
            stringBufferParameter.append("maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat," + maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat + "\n");
            stringBufferParameter.append("adjusterProbabilityOfPlacingBid," + adjusterProbabilityOfPlacingBid + "\n");
            stringBufferParameter.append("reservationPriceShare," + reservationPriceShare + "\n");
            stringBufferParameter.append("bridgeLoanDuration," + bridgeLoanDuration + "\n");
            stringBufferParameter.append("maxDuration," + maxDurationPath[0] + "\n");
            stringBufferParameter.append("renovationLoanDuration," + renovationLoanDuration + "\n");
            stringBufferParameter.append("nNonPerformingPeriodsForForcedSale," + nNonPerformingPeriodsForForcedSale + "\n");
            stringBufferParameter.append("rentMarkupCoeff," + rentMarkupCoeff + "\n");
            stringBufferParameter.append("rentMarkupPower," + rentMarkupPower + "\n");
            stringBufferParameter.append("yearlyRentSaleProbabilityAtZeroExpectedReturnSpread," + yearlyRentSaleProbabilityAtZeroExpectedReturnSpread + "\n");
            stringBufferParameter.append("minYearlyExpectedReturnSpreadForZeroRentSaleProbability," + minYearlyExpectedReturnSpreadForZeroRentSaleProbability + "\n");
            stringBufferParameter.append("nFlatsToLookAt," + nFlatsToLookAt + "\n");
            stringBufferParameter.append("maxNBidsPlacedPerHousehold," + maxNBidsPlacedPerHousehold + "\n");

            printWriter.print(stringBufferParameter);

            printWriter.close();
            stopperWriteInfoToCsv.printElapsedTimeInMilliseconds();

        } catch (Exception e) {
            System.out.println("Problem while writing sensitivity csv");
        }
    }




}
