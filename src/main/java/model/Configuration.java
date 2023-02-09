package model;


import util.OwnFunctions;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Configuration {

    static Properties prop;

    public static void loadConfigurationParameters(String configFileName) {


        // Try-with-resources statement
        try (FileReader fileReader = new FileReader(configFileName)) {
            prop = new Properties();
            prop.load(fileReader);


            Model.nPeriods = Integer.parseInt(prop.getProperty("nPeriods"));
            if (Model.parametersToOverride.nPeriods != 0) Model.nPeriods = Model.parametersToOverride.nPeriods;
            Model.maxNPeriods = Integer.parseInt(prop.getProperty("maxNPeriods"));
            Model.nHistoryPeriods = Integer.parseInt(prop.getProperty("nHistoryPeriods"));
            Model.zeroPeriodYear = Integer.parseInt(prop.getProperty("zeroPeriodYear"));
            Model.zeroPeriodMonth = Integer.parseInt(prop.getProperty("zeroPeriodMonth"));

            Model.simulationWithShock = Boolean.parseBoolean(prop.getProperty("simulationWithShock"));
            Model.simulationWithCovid = Boolean.parseBoolean(prop.getProperty("simulationWithCovid"));
            Model.moratoryStartPeriodWithCovid = Integer.parseInt(prop.getProperty("moratoryStartPeriodWithCovid"));
            Model.moratoryEndPeriodWithCovid = Integer.parseInt(prop.getProperty("moratoryEndPeriodWithCovid"));
            Model.nMinMonthsForSavingsConsideringMoratory = Double.parseDouble(prop.getProperty("nMinMonthsForSavingsConsideringMoratory"));
            if (Model.parametersToOverride.simulationWithShock != null) {
                if (Model.parametersToOverride.simulationWithCovid != null) {
                    Model.simulationWithCovid = Boolean.parseBoolean(Model.parametersToOverride.simulationWithCovid);
                }
            }
            Model.simulationWithSingleGeoLocation = Boolean.parseBoolean(prop.getProperty("simulationWithSingleGeoLocation"));
            Model.singleGeoLocationId = Integer.parseInt(prop.getProperty("singleGeoLocationId"));

            Model.nTypes = Integer.parseInt(prop.getProperty("nTypes"));
            Model.lifespan = Integer.parseInt(prop.getProperty("lifespan"));
            Model.retirementAgeInPeriods = Integer.parseInt(prop.getProperty("retirementAgeInPeriods"));
            Model.pensionReplacementRate = PropertyToDoubleArray("pensionReplacementRate");
            Model.minUnemploymentPeriods = PropertyToIntArray("minUnemploymentPeriods");
            Model.maxNChildren = Integer.parseInt(prop.getProperty("maxNChildren"));
            Model.minAgeInPeriodsToBuyOrRentAFlatAsSingleHousehold = Integer.parseInt(prop.getProperty("minAgeInPeriodsToBuyOrRentAFlatAsSingleHousehold"));
            Model.nPeriodsToLookAheadToCalculateLifeTimeIncome = Integer.parseInt(prop.getProperty("nPeriodsToLookAheadToCalculateLifeTimeIncome"));


            Model.savingsRateConstant = Double.parseDouble(prop.getProperty("savingsRateConstant"));
            Model.savingsRateCoeff = Double.parseDouble(prop.getProperty("savingsRateCoeff"));
            Model.minConsumptionRate = Double.parseDouble(prop.getProperty("minConsumptionRate"));
            Model.nPeriodsUntilMinConsumption = Integer.parseInt(prop.getProperty("nPeriodsUntilMinConsumption"));
            Model.minConsumptionPerCapitaLower = Double.parseDouble(prop.getProperty("minConsumptionPerCapitaLower"));
            Model.minConsumptionPerCapitaUpper = Double.parseDouble(prop.getProperty("minConsumptionPerCapitaUpper"));
            Model.minConsumptionPerCapitaLowerThreshold = Double.parseDouble(prop.getProperty("minConsumptionPerCapitaLowerThreshold"));
            Model.minConsumptionPerCapitaUpperThreshold = Double.parseDouble(prop.getProperty("minConsumptionPerCapitaUpperThreshold"));
            Model.weightOfAdditionalAdultsInMinConsumption = Double.parseDouble(prop.getProperty("weightOfAdditionalAdultsInMinConsumption"));
            Model.weightOfChildrenInMinConsumption = Double.parseDouble(prop.getProperty("weightOfChildrenInMinConsumption"));
            Model.minAgeInPeriodsToCountAsAdditionalAdultInMinConsumnption = Integer.parseInt(prop.getProperty("minAgeInPeriodsToCountAsAdditionalAdultInMinConsumnption"));
            Model.rangeMinForHomeValueInheritance = Double.parseDouble(prop.getProperty("rangeMinForHomeValueInheritance"));
            Model.rangeMaxForHomeValueInheritance = Double.parseDouble(prop.getProperty("rangeMaxForHomeValueInheritance"));
            Model.ratioOfZeroInheritanceWithLessThan20MonthInheritance = Double.parseDouble(prop.getProperty("ratioOfZeroInheritanceWithLessThan20MonthInheritance"));
            Model.ratioOfZeroInheritanceWithLessThan30MonthInheritance = Double.parseDouble(prop.getProperty("ratioOfZeroInheritanceWithLessThan30MonthInheritance"));

            Model.marketPriceOnRegression = Boolean.parseBoolean(prop.getProperty("marketPriceOnRegression"));

            Model.forcedSaleDiscount = Double.parseDouble(prop.getProperty("forcedSaleDiscount"));
            Model.realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount = Double.parseDouble(prop.getProperty("realGDPLevelShockTriggerValueForAdditionalForcedSaleDiscount"));
            Model.additionalForcedSaleDiscount = Double.parseDouble(prop.getProperty("additionalForcedSaleDiscount"));
            Model.monthlyForSalePriceDecrease = Double.parseDouble(prop.getProperty("monthlyForSalePriceDecrease"));
            Model.minForSalePriceForRegression = Double.parseDouble(prop.getProperty("minForSalePriceForRegression"));
            Model.minForSalePrice = Double.parseDouble(prop.getProperty("minForSalePrice"));
            Model.maxSizeValueForSize2 = Double.parseDouble(prop.getProperty("maxSizeValueForSize2"));
            Model.minStateValueForSizeState = Double.parseDouble(prop.getProperty("minStateValueForSizeState"));
            Model.priceRatioMinRatio = Double.parseDouble(prop.getProperty("priceRatioMinRatio"));
            Model.priceRatioMaxRatio = Double.parseDouble(prop.getProperty("priceRatioMaxRatio"));
            Model.nFlatsToLookAt = Integer.parseInt(prop.getProperty("nFlatsToLookAt"));
            Model.minNFlatsToLookAtToShiftFlatIndices = Integer.parseInt(prop.getProperty("minNFlatsToLookAtToShiftFlatIndices"));
            Model.maxShiftInFlatIndicesInChooseBestFlat = Integer.parseInt(prop.getProperty("maxShiftInFlatIndicesInChooseBestFlat"));
            Model.maxNChecksFromBucketOfNewlyBuiltBestFlat = Integer.parseInt(prop.getProperty("maxNChecksFromBucketOfNewlyBuiltBestFlat"));
            Model.mayRenovateWhenBuyingRatio= Double.parseDouble(prop.getProperty("mayRenovateWhenBuyingRatio"));
            Model.renovationStateIncreaseWhenBuying = Double.parseDouble(prop.getProperty("renovationStateIncreaseWhenBuying"));
            Model.maxNIterationsForHouseholdPurchases = Integer.parseInt(prop.getProperty("maxNIterationsForHouseholdPurchases"));
            Model.canChangeGeoLocationProbability = PropertyToDoubleArray("canChangeGeoLocationProbability");
            Model.nFictiveFlatsForSalePerBucket = Integer.parseInt(prop.getProperty("nFictiveFlatsForSalePerBucket"));

            Model.bucketSizeIntervals = PropertyToDoubleArray("bucketSizeIntervals");
            Model.bucketStateIntervals = PropertyToDoubleArray("bucketStateIntervals");

            Model.sizeDistanceRatioThresholdForClosestNeighbourCalculation = Double.parseDouble(prop.getProperty("sizeDistanceRatioThresholdForClosestNeighbourCalculation"));
            Model.stateDistanceRatioThresholdForClosestNeighbourCalculation = Double.parseDouble(prop.getProperty("stateDistanceRatioThresholdForClosestNeighbourCalculation"));
            Model.sizeWeightInClosestNeighbourCalculation = Double.parseDouble(prop.getProperty("sizeWeightInClosestNeighbourCalculation"));
            Model.stateWeightInClosestNeighbourCalculation = Double.parseDouble(prop.getProperty("stateWeightInClosestNeighbourCalculation"));
            Model.maxSizeDifferenceRatio = Double.parseDouble(prop.getProperty("maxSizeDifferenceRatio"));
            Model.maxStateDifferenceRatio = Double.parseDouble(prop.getProperty("maxStateDifferenceRatio"));

            Model.ageInYearsForMandatoryMoving = Integer.parseInt(prop.getProperty("ageInYearsForMandatoryMoving"));
            Model.probabilityOfMandatoryMoving = Double.parseDouble(prop.getProperty("probabilityOfMandatoryMoving"));
            Model.probabilityOfAssessingPotentialNewHomes = Double.parseDouble(prop.getProperty("probabilityOfAssessingPotentialNewHomes"));
            Model.baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes = Double.parseDouble(prop.getProperty("baseAgeInYearsForProbabilityOfAssessingPotentialNewHomes"));
            Model.yearlyDiscountForProbabilityOfAssessingPotentialNewHomes = Double.parseDouble(prop.getProperty("yearlyDiscountForProbabilityOfAssessingPotentialNewHomes"));
            Model.thresholdRatioForMoving = Double.parseDouble(prop.getProperty("thresholdRatioForMoving"));
            Model.thresholdPriceDifferenceForMoving = Double.parseDouble(prop.getProperty("thresholdPriceDifferenceForMoving"));
            Model.lowQualityNeighbourhoodMovingProbabilityMultiplier = Double.parseDouble(prop.getProperty("lowQualityNeighbourhoodMovingProbabilityMultiplier"));
            Model.lowQualityMax = Double.parseDouble(prop.getProperty("lowQualityMax"));
            Model.middleQualityNeighbourhoodMovingProbabilityMultiplier = Double.parseDouble(prop.getProperty("middleQualityNeighbourhoodMovingProbabilityMultiplier"));
            Model.middleQualityMax = Double.parseDouble(prop.getProperty("middleQualityMax"));
            Model.topQualityNeighbourhoodMovingProbabilityMultiplier = Double.parseDouble(prop.getProperty("topQualityNeighbourhoodMovingProbabilityMultiplier"));
            Model.linearlyDecreasingCapitalProbabilityMultiplierInFirstNPeriods = Double.parseDouble(prop.getProperty("linearlyDecreasingCapitalProbabilityMultiplierInFirstNPeriods"));
            Model.firstNPeriodsForLinearlyDecreasingCapitalProbabilityMultiplier = Integer.parseInt(prop.getProperty("firstNPeriodsForLinearlyDecreasingCapitalProbabilityMultiplier"));
            Model.nMonthsToWithdrawHomeFromMarket = Integer.parseInt(prop.getProperty("nMonthsToWithdrawHomeFromMarket"));
            Model.minNMonthsOfOwnHomeOnMarketToBuyFlat = Integer.parseInt(prop.getProperty("minNMonthsOfOwnHomeOnMarketToBuyFlat"));
            Model.nPeriodsForAverageNForSaleToNSold = Integer.parseInt(prop.getProperty("nPeriodsForAverageNForSaleToNSold"));
            Model.targetNForSaleToNSold = Double.parseDouble(prop.getProperty("targetNForSaleToNSold"));
            Model.coeffProbabilityNForSaleToNSoldAdjustment = Double.parseDouble(prop.getProperty("coeffProbabilityNForSaleToNSoldAdjustment"));
            Model.minForSaleToNSoldProbabilityAdjustment = Double.parseDouble(prop.getProperty("minForSaleToNSoldProbabilityAdjustment"));
            Model.maxForSaleToNSoldProbabilityAdjustment = Double.parseDouble(prop.getProperty("maxForSaleToNSoldProbabilityAdjustment"));
            Model.sensitivityInLifetimeIncomeMultiplier = Double.parseDouble(prop.getProperty("sensitivityInLifetimeIncomeMultiplier"));
            Model.coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy = Double.parseDouble(prop.getProperty("coeffInReservationPriceAdjusterAccordingToNPeriodsWaitingForFlatToBuy"));

            Model.reservationMarkup = Double.parseDouble(prop.getProperty("reservationMarkup"));
            Model.sellerReservationPriceDecrease = Double.parseDouble(prop.getProperty("sellerReservationPriceDecrease"));
            Model.adjusterProbabilityOfPlacingBid = Double.parseDouble(prop.getProperty("adjusterProbabilityOfPlacingBid"));
            Model.newlyBuiltUtilityAdjusterCoeff1 = Double.parseDouble(prop.getProperty("newlyBuiltUtilityAdjusterCoeff1"));
            Model.newlyBuiltUtilityAdjusterCoeff2 = Double.parseDouble(prop.getProperty("newlyBuiltUtilityAdjusterCoeff2"));
            Model.reservationPriceShare = Double.parseDouble(prop.getProperty("reservationPriceShare"));
            Model.maxForSaleMultiplier = Double.parseDouble(prop.getProperty("maxForSaleMultiplier"));
            Model.maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat = Double.parseDouble(prop.getProperty("maxIncreaseInReservationPriceAsARatioOfTheSurplusDifferenceToTheFictiveFlat"));
            Model.powerInAdjustmentInReservationPrice = Double.parseDouble(prop.getProperty("powerInAdjustmentInReservationPrice"));
            Model.maxNBidsPlacedPerHousehold = Integer.parseInt(prop.getProperty("maxNBidsPlacedPerHousehold"));

            Model.minYearlyInterestRate = Double.parseDouble(prop.getProperty("minYearlyInterestRate"));
            Model.maxYearlyInterestRate = Double.parseDouble(prop.getProperty("maxYearlyInterestRate"));
            Model.minMarketPriceForBridgeLoan = Double.parseDouble(prop.getProperty("minMarketPriceForBridgeLoan"));
            Model.bridgeLoanDuration = Integer.parseInt(prop.getProperty("bridgeLoanDuration"));
            Model.renovationLoanDuration = Integer.parseInt(prop.getProperty("renovationLoanDuration"));
            Model.durationIncreaseInIncreaseLoanForRenovation = Double.parseDouble(prop.getProperty("durationIncreaseInIncreaseLoanForRenovation"));
            Model.nNonPerformingPeriodsForRestructuring = Integer.parseInt(prop.getProperty("nNonPerformingPeriodsForRestructuring"));
            Model.nNonPerformingPeriodsForForcedSale = Integer.parseInt(prop.getProperty("nNonPerformingPeriodsForForcedSale"));
            Model.nNonPerformingPeriodsForOwnSale = Integer.parseInt(prop.getProperty("nNonPerformingPeriodsForOwnSale"));
            Model.ageCategory1StartingAgeInPeriods = Integer.parseInt(prop.getProperty("ageCategory1StartingAgeInPeriods"));
            Model.ageCategory2StartingAgeInPeriods = Integer.parseInt(prop.getProperty("ageCategory2StartingAgeInPeriods"));
            Model.minDurationForRestructuring = Integer.parseInt(prop.getProperty("minDurationForRestructuring"));
            Model.durationIncreaseInRestructuring = Integer.parseInt(prop.getProperty("durationIncreaseInRestructuring"));
            Model.incomeRatioForBankFromFlatForSalePrice = Double.parseDouble(prop.getProperty("incomeRatioForBankFromFlatForSalePrice"));
            Model.nPeriodsInNegativeKHR = Integer.parseInt(prop.getProperty("nPeriodsInNegativeKHR"));
            Model.additionalDSTI = Double.parseDouble(prop.getProperty("additionalDSTI"));
            Model.firstBuyerAdditionalDSTI = Double.parseDouble(prop.getProperty("firstBuyerAdditionalDSTI"));
            Model.firstBuyerAdditionalLTV = Double.parseDouble(prop.getProperty("firstBuyerAdditionalLTV"));
            Model.firstBuyerAdditionalLTVForZOP = Double.parseDouble(prop.getProperty("firstBuyerAdditionalLTVForZOP"));
            Model.startOfFirstBuyerZOP = Integer.parseInt(prop.getProperty("startOfFirstBuyerZOP"));
            Model.firstBuyerZOPSpecialCriteria = Boolean.parseBoolean(prop.getProperty("firstBuyerZOPSpecialCriteria"));
            Model.firstBuyerZOPSpecialCriteriaMaxAge = Integer.parseInt(prop.getProperty("firstBuyerZOPSpecialCriteriaMaxAge"));
            Model.unemploymentRateAdjusterInConsumptionPrescription = Double.parseDouble(prop.getProperty("unemploymentRateAdjusterInConsumptionPrescription"));
            Model.yearlyInterestRateRegressionConstant = Double.parseDouble(prop.getProperty("yearlyInterestRateRegressionConstant"));
            Model.yearlyInterestRateRegressionCoeffLnWageIncome = Double.parseDouble(prop.getProperty("yearlyInterestRateRegressionCoeffLnWageIncome"));
            Model.yearlyInterestRateRegressionCoeffLTV = Double.parseDouble(prop.getProperty("yearlyInterestRateRegressionCoeffLTV"));
            Model.yearlyInterestRateRegressionCoeffAgeCategory1 = Double.parseDouble(prop.getProperty("yearlyInterestRateRegressionCoeffAgeCategory1"));
            Model.yearlyInterestRateRegressionCoeffAgeCategory2 = Double.parseDouble(prop.getProperty("yearlyInterestRateRegressionCoeffAgeCategory2"));
            Model.bridgeLoanToValue = Double.parseDouble(prop.getProperty("bridgeLoanToValue"));

            Model.nPeriodsForConstruction = Integer.parseInt(prop.getProperty("nPeriodsForConstruction"));
            Model.sizeMaxOfConstructionFlatsToSizeMinInLargestSizeBucket = Double.parseDouble(prop.getProperty("sizeMaxOfConstructionFlatsToSizeMinInLargestSizeBucket"));
            Model.sizeMaxOfFictiveFlatsToSizeMinInLargestSizeBucket = Double.parseDouble(prop.getProperty("sizeMaxOfFictiveFlatsToSizeMinInLargestSizeBucket"));
            Model.constructionUnitCostBase = PropertyToDoubleArray("constructionUnitCostBase");

            Model.constructionMarkupRatio1 = Double.parseDouble(prop.getProperty("constructionMarkupRatio1"));
            Model.constructionMarkupRatio1Level = Double.parseDouble(prop.getProperty("constructionMarkupRatio1Level"));
            Model.constructionMarkupRatio2 = Double.parseDouble(prop.getProperty("constructionMarkupRatio2"));
            Model.constructionMarkupRatio2Level = Double.parseDouble(prop.getProperty("constructionMarkupRatio2Level"));
            Model.constructionMarkupInFirstPeriods = Double.parseDouble(prop.getProperty("constructionMarkupInFirstPeriods"));
            Model.firstNPeriodsForFixedMarkup = Integer.parseInt(prop.getProperty("firstNPeriodsForFixedMarkup"));
            Model.maxConstructionPriceToMarketPriceWithProperConstructionCostCoverage = Double.parseDouble(prop.getProperty("maxConstructionPriceToMarketPriceWithProperConstructionCostCoverage"));
            Model.constructionCostCoverageNeed = Double.parseDouble(prop.getProperty("constructionCostCoverageNeed"));
            Model.nForSalePeriodsToStartAdjustingConstructionForSalePrice =  Integer.parseInt(prop.getProperty("nForSalePeriodsToStartAdjustingConstructionForSalePrice"));
            Model.parameterForConstructionForSalePriceAdjustment = Double.parseDouble(prop.getProperty("parameterForConstructionForSalePriceAdjustment"));
            Model.maxFlatStateToLandPriceStateForConstructionPurchase = Double.parseDouble(prop.getProperty("maxFlatStateToLandPriceStateForConstructionPurchase"));
            Model.landPriceAdjuster = Double.parseDouble(prop.getProperty("landPriceAdjuster"));
            Model.landPriceDecreaseAdjusterStartPeriod = Integer.parseInt(prop.getProperty("landPriceDecreaseAdjusterStartPeriod"));
            Model.landPriceDecreaseAdjusterMaxValue = Double.parseDouble(prop.getProperty("landPriceDecreaseAdjusterMaxValue"));
            Model.landPriceDecreaseAdjusterBase = Double.parseDouble(prop.getProperty("landPriceDecreaseAdjusterBase"));
            Model.ZOPMonthlyInteresRate = Double.parseDouble(prop.getProperty("ZOPMonthlyInterestRate"));
            Model.ZOPConstructionCost = Double.parseDouble(prop.getProperty("ZOPConstructionCost"));
            Model.ZOPStartingPeriod = Integer.parseInt(prop.getProperty("ZOPStartingPeriod"));
            Model.ZOPStartingPeriodForFictiveFlats = Integer.parseInt(prop.getProperty("ZOPStartingPeriodForFictiveFlats"));
            Model.introductionOfZOP = Integer.parseInt(prop.getProperty("introductionOfZOP"));
            Model.ZOPLimit = Double.parseDouble(prop.getProperty("ZOPLimit"));
            Model.ZOPAdditionalUtility = Double.parseDouble(prop.getProperty("ZOPAdditionalUtility"));
            Model.introductionOfBabyloan = Integer.parseInt(prop.getProperty("introductionOfBabyloan"));
            Model.endOfBabyloan = Integer.parseInt(prop.getProperty("endOfBabyloan"));
            Model.nOfMaxChildrenBeforeBabyloan = Integer.parseInt(prop.getProperty("nOfMaxChildrenBeforeBabyloan"));
            Model.maxAgeInYearsForBabyloan = Integer.parseInt(prop.getProperty("maxAgeInYearsForBabyloan"));
            Model.maxAgeInYearsForBabyloanIsMoving = Integer.parseInt(prop.getProperty("maxAgeInYearsForBabyloanIsMoving"));
            Model.babyloanDuration = Integer.parseInt(prop.getProperty("babyloanDuration"));
            Model.babyloanAmount = Double.parseDouble(prop.getProperty("babyloanAmount"));
            Model.nPeriodBabyloanSuspension = Integer.parseInt(prop.getProperty("nPeriodBabyloanSuspension"));
            Model.ratioOfDecreasePrincipalInCaseOfChildBirth = Double.parseDouble(prop.getProperty("ratioOfDecreasePrincipalInCaseOfChildBirth"));
            Model.babyloanDSTIPayment = Double.parseDouble(prop.getProperty("babyloanDSTIPayment"));
            Model.babyloanPayment = Double.parseDouble(prop.getProperty("babyloanPayment"));
            Model.maxCapacityOfNewBabyloan = Integer.parseInt(prop.getProperty("maxCapacityOfNewBabyloan"));
            Model.nPeriodsForAverageNewlyBuiltDemand =  Integer.parseInt(prop.getProperty("nPeriodsForAverageNewlyBuiltDemand"));
            Model.targetNewlyBuiltBuffer = Double.parseDouble(prop.getProperty("targetNewlyBuiltBuffer"));
            Model.maxNFlatsToBuildInBucketToAverageNewlyBuiltDemandRatio = Double.parseDouble(prop.getProperty("maxNFlatsToBuildInBucketToAverageNewlyBuiltDemandRatio"));
            Model.maxFlatSizeForLandPrice = Double.parseDouble(prop.getProperty("maxFlatSizeForLandPrice"));
            Model.maxFlatStateForLandPrice = Double.parseDouble(prop.getProperty("maxFlatStateForLandPrice"));
            Model.constructionAreaNeedRatio = Double.parseDouble(prop.getProperty("constructionAreaNeedRatio"));
            Model.monthlyConstructionAreaLimit = Boolean.parseBoolean(prop.getProperty("monthlyConstructionAreaLimit"));

            Model.renovationProbability = Double.parseDouble(prop.getProperty("renovationProbability"));
            Model.renovationToConstructionUnitCostBase = Double.parseDouble(prop.getProperty("renovationToConstructionUnitCostBase"))/PropertyToDoubleArray("bucketStateIntervals")[PropertyToDoubleArray("bucketStateIntervals").length-2];
            Model.coeffRenovationRatio = Double.parseDouble(prop.getProperty("coeffRenovationRatio"));
            Model.firstNPeriodsForRenovationNormalQuantity = Integer.parseInt(prop.getProperty("firstNPeriodsForRenovationNormalQuantity"));
            Model.stateDepreciation = Double.parseDouble(prop.getProperty("stateDepreciation"));
            Model.nPeriodsInHighQualityBucket = Integer.parseInt(prop.getProperty("nPeriodsInHighQualityBucket"));
            Model.renovationCostBuffer = Double.parseDouble(prop.getProperty("renovationCostBuffer"));
            Model.absCoeffStateAdjuster = Double.parseDouble(prop.getProperty("absCoeffStateAdjuster"));

            Model.capitalId = Integer.parseInt(prop.getProperty("capitalId"));
            Model.agglomerationId = Integer.parseInt(prop.getProperty("agglomerationId"));

            Model.nPeriodsForFlatSaleRecords = Integer.parseInt(prop.getProperty("nPeriodsForFlatSaleRecords"));
            Model.nPeriodsForFlatSaleRecordsToUseInPriceRegression = Integer.parseInt(prop.getProperty("nPeriodsForFlatSaleRecordsToUseInPriceRegression"));
            Model.nFlatsForPriceRegression = Integer.parseInt(prop.getProperty("nFlatsForPriceRegression"));
            Model.nPeriodsUntilCopyingPriceRegressionCoeffients = Integer.parseInt(prop.getProperty("nPeriodsUntilCopyingPriceRegressionCoeffients"));
            Model.maxChangeInRegressionParameters = Double.parseDouble(prop.getProperty("maxChangeInRegressionParameters"));
            Model.minNObservationForPriceIndex = Integer.parseInt(prop.getProperty("minNObservationForPriceIndex"));
            Model.baseMinPeriodForPriceIndexToBeginning = Integer.parseInt(prop.getProperty("baseMinPeriodForPriceIndexToBeginning"));
            Model.baseMaxPeriodForPriceIndexToBeginning = Integer.parseInt(prop.getProperty("baseMaxPeriodForPriceIndexToBeginning"));
            Model.size2inPriceIndexRegression = Boolean.parseBoolean(prop.getProperty("size2inPriceIndexRegression"));
            Model.stateInDirectPriceIndexRegression = Boolean.parseBoolean(prop.getProperty("stateInDirectPriceIndexRegression"));
            Model.useTransactionWeightsForPriceIndex = Boolean.parseBoolean(prop.getProperty("useTransactionWeightsForPriceIndex"));
            Model.useForcedSaleForPriceIndex = Boolean.parseBoolean(prop.getProperty("useForcedSaleForPriceIndex"));

            Model.minRentRatio = Double.parseDouble(prop.getProperty("minRentRatio"));
            Model.maxRentRatio = Double.parseDouble(prop.getProperty("maxRentRatio"));
            Model.ageInYearsToConsiderSomebodyOldEnoughToRentForSure = Integer.parseInt(prop.getProperty("ageInYearsToConsiderSomebodyOldEnoughToRentForSure"));
            Model.coeffOfFirstWageRegardingRentalProbability = Double.parseDouble(prop.getProperty("coeffOfFirstWageRegardingRentalProbability"));
            Model.rentToPrice = Double.parseDouble(prop.getProperty("rentToPrice"));
            Model.nMaxPeriodsForRent = Integer.parseInt(prop.getProperty("nMaxPeriodsForRent"));
            Model.rentMarkupPower = Double.parseDouble(prop.getProperty("rentMarkupPower"));
            Model.rentMarkupCoeff = Double.parseDouble(prop.getProperty("rentMarkupCoeff"));
            Model.rentMarkupUtilizationRatioCap = Double.parseDouble(prop.getProperty("rentMarkupUtilizationRatioCap"));
            Model.nPeriodsForAverageNFictiveRentDemand = Integer.parseInt(prop.getProperty("nPeriodsForAverageNFictiveRentDemand"));
            Model.nPeriodsForAverageReturn = Integer.parseInt(prop.getProperty("nPeriodsForAverageReturn"));
            Model.expectedReturnCapitalGainCoeff = Double.parseDouble(prop.getProperty("expectedReturnCapitalGainCoeff"));
            Model.nPeriodsForUtilizationRatio = Integer.parseInt(prop.getProperty("nPeriodsForUtilizationRatio"));
            Model.targetUtilizationRatio = Double.parseDouble(prop.getProperty("targetUtilizationRatio"));
            Model.minUtilizationRatioForTargetUtilizationRatioToUtilizationRatioInRentSaleProbability = Double.parseDouble(prop.getProperty("minUtilizationRatioForTargetUtilizationRatioToUtilizationRatioInRentSaleProbability"));
            Model.minTargetRatioForInvestmentProbability = Double.parseDouble(prop.getProperty("minTargetRatioForInvestmentProbability"));
            Model.stepInSearchingTargetRatioForInvestmentProbability = Double.parseDouble(prop.getProperty("stepInSearchingTargetRatioForInvestmentProbability"));
            Model.monthlyInvestmentRatioConstantCentralInvestor = Double.parseDouble(prop.getProperty("monthlyInvestmentRatioConstantCentralInvestor"));
            Model.monthlyInvestmentRatioCoeffExpectedReturnCentralInvestor = Double.parseDouble(prop.getProperty("monthlyInvestmentRatioCoeffExpectedReturnCentralInvestor"));
            Model.minDepositToInvest = Double.parseDouble(prop.getProperty("minDepositToInvest"));

            Model.yearlyRentSaleProbabilityAtZeroExpectedReturnSpread = Double.parseDouble(prop.getProperty("yearlyRentSaleProbabilityAtZeroExpectedReturnSpread"));
            Model.minYearlyExpectedReturnSpreadForZeroRentSaleProbability = Double.parseDouble(prop.getProperty("minYearlyExpectedReturnSpreadForZeroRentSaleProbability"));
            Model.targetUtilizationRatioAdjusterInHouseholdRentSaleProbability = Double.parseDouble(prop.getProperty("targetUtilizationRatioAdjusterInHouseholdRentSaleProbability"));
            Model.householdRentSaleProbabilityScaler = Double.parseDouble(prop.getProperty("householdRentSaleProbabilityScaler"));
            Model.householdInvestmentProbabilityPower = Double.parseDouble(prop.getProperty("householdInvestmentProbabilityPower"));
            Model.householdInvestmentProbabilityCoeff = Double.parseDouble(prop.getProperty("householdInvestmentProbabilityCoeff"));
            Model.maxHouseholdInvestmentProbability = Double.parseDouble(prop.getProperty("maxHouseholdInvestmentProbability"));
            Model.maxPlannedInvestmentValueToAggregateMarketValue = Double.parseDouble(prop.getProperty("maxPlannedInvestmentValueToAggregateMarketValue"));

            Model.ageInPeriodsForFirstWorkExperience = PropertyToIntArray("ageInPeriodsForFirstWorkExperience");
            Model.inheritorDistanceTypeCoeff = Double.parseDouble(prop.getProperty("inheritorDistanceTypeCoeff"));
            Model.inheritorDistanceFirstWageRatioCoeff = Double.parseDouble(prop.getProperty("inheritorDistanceFirstWageRatioCoeff"));
            Model.inheritorDistancePreferredGeoLocationCoeff = Double.parseDouble(prop.getProperty("inheritorDistancePreferredGeoLocationCoeff"));

            Model.maxAgeInPeriodsForChildForCSOK = Integer.parseInt(prop.getProperty("maxAgeInPeriodsForChildForCSOK"));
            Model.newlyBuiltCSOK = PropertyToDoubleArray("newlyBuiltCSOK");
            Model.usedCSOK = PropertyToDoubleArray("usedCSOK");
            Model.falusiCSOK = PropertyToDoubleArray("falusiCSOK");
            Model.introductionOfFalusiCSOK = Integer.parseInt(prop.getProperty("introductionOfFalusiCSOK"));
            Model.endOfFalusiCSOK = Integer.parseInt(prop.getProperty("endOfFalusiCSOK"));
            Model.neighbourhoodQualityThresholdForFalusiCSOK = Double.parseDouble(prop.getProperty("neighbourhoodQualityThresholdForFalusiCSOK"));
            Model.ratioOfHouseholdsWith2ChildrenEligibleFor3childrenCSOK = Double.parseDouble(prop.getProperty("ratioOfHouseholdsWith2ChildrenEligibleFor3childrenCSOK"));
            Model.ratioOfHouseholdsWith1ChildEligibleFor3childrenCSOK = Double.parseDouble(prop.getProperty("ratioOfHouseholdsWith1ChildEligibleFor3childrenCSOK"));
            Model.ratioOfHouseholdsWith2ChildrenEligibleFor3childrenFalusiCSOK = Double.parseDouble(prop.getProperty("ratioOfHouseholdsWith2ChildrenEligibleFor3childrenFalusiCSOK"));
            Model.minFirstWageForCSOK = Double.parseDouble(prop.getProperty("minFirstWageForCSOK"));
            Model.familyBenefit = PropertyToDoubleArray("familyBenefit");
            Model.minSizeForCSOK = Double.parseDouble(prop.getProperty("minSizeForCSOK"));
            Model.depositInheritanceRatio = Double.parseDouble(prop.getProperty("depositInheritanceRatio"));
            Model.DLrentalMarketBuffer = Double.parseDouble(prop.getProperty("DLrentalMarketBuffer"));
            Model.DLinvestorShare = Double.parseDouble(prop.getProperty("DLinvestorShare"));
            Model.DLminFirstWageForProperty = Double.parseDouble(prop.getProperty("DLminFirstWageForProperty"));
            Model.DLnNewlyBuiltOneYearBeforeZeroPeriodYear = Integer.parseInt(prop.getProperty("DLnNewlyBuiltOneYearBeforeZeroPeriodYear"));
            Model.DLnNewlyBuiltTwoYearsBeforeZeroPeriodYear = Integer.parseInt(prop.getProperty("DLnNewlyBuiltTwoYearsBeforeZeroPeriodYear"));
            Model.DLyearlyForSaleRatio = Double.parseDouble(prop.getProperty("DLyearlyForSaleRatio"));
            Model.DLinheritedYearlyForSaleRatio = Double.parseDouble(prop.getProperty("DLinheritedYearlyForSaleRatio"));
            Model.DLsoldRatioOfNewlyBuiltFlats = Double.parseDouble(prop.getProperty("DLsoldRatioOfNewlyBuiltFlats"));
            Model.DLmaxAreaUnderConstructionRatio = Double.parseDouble(prop.getProperty("DLmaxAreaUnderConstructionRatio"));
            Model.DLneighbourhoodAreaRatio = Double.parseDouble(prop.getProperty("DLneighbourhoodAreaRatio"));
            Model.DLnMonthsForNeighbourhoodAreaRatio = Integer.parseInt(prop.getProperty("DLnMonthsForNeighbourhoodAreaRatio"));
            Model.DLmonthlyInvestmentRatio = Double.parseDouble(prop.getProperty("DLmonthlyInvestmentRatio"));
            Model.DLpriceIndexFirstObservationYear = Integer.parseInt(prop.getProperty("DLpriceIndexFirstObservationYear"));
            Model.DLpriceIndexFirstObservationMonth = Integer.parseInt(prop.getProperty("DLpriceIndexFirstObservationMonth"));
            Model.DLyearlyInterestRateRegressionConstant = Double.parseDouble(prop.getProperty("DLyearlyInterestRateRegressionConstant"));
            Model.DLyearlyInterestRateRegressionCoeffLnWageIncome = Double.parseDouble(prop.getProperty("DLyearlyInterestRateRegressionCoeffLnWageIncome"));
            Model.DLyearlyInterestRateRegressionCoeffLTV = Double.parseDouble(prop.getProperty("DLyearlyInterestRateRegressionCoeffLTV"));
            Model.DLyearlyInterestRateRegressionCoeffAgeCategory1 = Double.parseDouble(prop.getProperty("DLyearlyInterestRateRegressionCoeffAgeCategory1"));
            Model.DLyearlyInterestRateRegressionCoeffAgeCategory2 = Double.parseDouble(prop.getProperty("DLyearlyInterestRateRegressionCoeffAgeCategory2"));
            Model.DLbridgeLoanToValue = Double.parseDouble(prop.getProperty("DLbridgeLoanToValue"));
            Model.DLthresholdRatioForRentalNeighbourhood = Double.parseDouble(prop.getProperty("DLthresholdRatioForRentalNeighbourhood"));
            Model.DLminAgeForRentalProbability = Integer.parseInt(prop.getProperty("DLminAgeForRentalProbability"));
            Model.DLrentalProbabilityForMinAge = Double.parseDouble(prop.getProperty("DLrentalProbabilityForMinAge"));
            Model.DLmaxAgeForRentalProbability = Integer.parseInt(prop.getProperty("DLmaxAgeForRentalProbability"));
            Model.DLrentalProbabilityForMaxAge = Double.parseDouble(prop.getProperty("DLrentalProbabilityForMaxAge"));
            Model.DLminFirstWage = Double.parseDouble(prop.getProperty("DLminFirstWage"));
            Model.DLmaxItersToSwitchHomesToRent = Integer.parseInt(prop.getProperty("DLmaxItersToSwitchHomesToRent"));
            Model.DLhistBucketMonthlyForSaleToSoldRatio = Double.parseDouble(prop.getProperty("DLhistBucketMonthlyForSaleToSoldRatio"));
            Model.DLrentalRatio = PropertyToDoubleArray("DLrentalRatio");



            Model.CAHAIForFTB = Boolean.parseBoolean(prop.getProperty("CAHAIForFTB"));
            Model.affordabilityAgeIntervals = PropertyToDoubleArray("affordabilityAgeIntervals");
            Model.affordabilitySizeIntervalsBudapest = PropertyToDoubleArray("affordabilitySizeIntervalsBudapest");
            Model.affordabilitySizeIntervalsVidek = PropertyToDoubleArray("affordabilitySizeIntervalsVidek");

            //derive Parameters
            Model.wageRatio = deriveMonthlyWageRatio();
            Model.unemploymentRatesPath = deriveStaticPathsForTypes(PropertyToDoubleArray("unemploymentRatesBase"));
            Model.unemploymentProbabilitiesPath = deriveStaticPathsForTypes(PropertyToDoubleArray("unemploymentProbabilitiesBase"));
            Model.priceLevelPath = derivePathWithFixedGrowthRate(Double.parseDouble(prop.getProperty("monthlyInflationRateBase")));
            Model.realGDPLevelPath = derivePathWithFixedGrowthRate(Double.parseDouble(prop.getProperty("monthlyGDPGrowthRateBase")));
            Model.yearlyBaseRatePath = deriveStaticPath(Double.parseDouble(prop.getProperty("yearlyBaseRateBase")));
            Model.LTVPath = deriveStaticPath(Double.parseDouble(prop.getProperty("LTVBase")));
            Model.DSTIPath = deriveStaticPath(Double.parseDouble(prop.getProperty("DSTIBase")));
            Model.maxDurationPath = deriveStaticPath(Integer.parseInt(prop.getProperty("maxDurationBase")));
            Model.taxRatePath = deriveStaticPath(Double.parseDouble(prop.getProperty("taxRateBase")));
            Model.constructionUnitCostIndexPath = deriveStaticPath(1.0);

            Model.highQualityStateMin = Model.bucketStateIntervals[Model.bucketStateIntervals.length-2];
            Model.highQualityStateMax = Model.bucketStateIntervals[Model.bucketStateIntervals.length-1];

            //birthProbability

            Scanner birthProbabilityScanner = new Scanner(new File(MainRun.birthProbabilityFile));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.birthProbabilityFile))) birthProbabilityScanner.nextLine();

            Model.birthProbability = new double[Model.nTypes][Model.lifespan][Model.maxNChildren];
            while (birthProbabilityScanner.hasNextLine()) {
                String dataLine = birthProbabilityScanner.nextLine();
                String[] dataLineArray = dataLine.split(",");

                int typeIndex = Integer.parseInt(dataLineArray[0])-1;
                int ageInPeriods = Integer.parseInt(dataLineArray[1]);
                int childrenIndex = Integer.parseInt(dataLineArray[2])-1;
                double probability = Double.parseDouble(dataLineArray[3]);
                Model.birthProbability[typeIndex][ageInPeriods][childrenIndex] = 1.35 * probability;
            }
            for (int i = 0; i < Model.nTypes; i++) {
                for (int j = 0; j < Model.lifespan; j++) {
                    for (int k = 1; k < Model.maxNChildren; k++) {
                        if (Model.birthProbability[i][j][k]==0) Model.birthProbability[i][j][k]=Model.birthProbability[i][j][k-1];
                    }
                }
            }

            //marriageProbability
            Scanner marriageProbabilityScanner = new Scanner(new File(MainRun.marriageProbabilityFile));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.marriageProbabilityFile))) marriageProbabilityScanner.nextLine();

            Model.marriageProbability = new double[Model.nTypes][Model.lifespan];
            for (int i = 0; i < Model.nTypes; i++) {
                String dataLine = marriageProbabilityScanner.nextLine();
                String[] dataLineArray = dataLine.split(",");
                for (int j = 0; j < Math.min(dataLineArray.length,Model.lifespan); j++) {
                    Model.marriageProbability[i][j] = Double.parseDouble(dataLineArray[j]);
                }
            }

            //deathProbability
            Scanner deathProbabilityScanner = new Scanner(new File(MainRun.deathProbabilityFile));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.deathProbabilityFile))) deathProbabilityScanner.nextLine();

            Model.deathProbability = new double[2][Model.nTypes][Model.lifespan];
            for (int i = 0; i < Model.nTypes; i++) {
                String dataLine = deathProbabilityScanner.nextLine();
                String[] dataLineArray = dataLine.split(",");
                for (int j = 0; j < Math.min(dataLineArray.length,Model.lifespan); j++) {
                    if (i<3) { //deathProbability for men
                        Model.deathProbability[0][i][j] = Double.parseDouble(dataLineArray[j]);
                    } else { //deathProbability for women
                        Model.deathProbability[1][i-3][j] = Double.parseDouble(dataLineArray[j]);
                    }


                }
            }

            //realGDPLevelPath, priceLevelPath
            int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();

            double[] realGDPLevelPath = new double[Model.nPeriods + nAdditionalPeriodsForPath];
            double[] priceLevelPath = new double[Model.nPeriods + nAdditionalPeriodsForPath];



            Scanner macroPathScanner = new Scanner(new File(MainRun.macroPathFile));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.macroPathFile))) macroPathScanner.nextLine();
            int index = 0;
            while (macroPathScanner.hasNextLine() && index<realGDPLevelPath.length) {
                String dataLine = macroPathScanner.nextLine();
                String[] dataLineArray = dataLine.split(",");
                realGDPLevelPath[index] = Double.parseDouble(dataLineArray[1]);
                priceLevelPath[index] = Double.parseDouble(dataLineArray[2]);
                Model.yearlyBaseRatePath[index] = Double.parseDouble(dataLineArray[3]);
                Model.constructionUnitCostIndexPath[index] = Double.parseDouble(dataLineArray[4]);
                index++;

            }

            double realGDPLevelPath0 = realGDPLevelPath[0];
            double priceLevelPath0 = priceLevelPath[0];
            double constructionUnitCostIndexPath0 = Model.constructionUnitCostIndexPath[0];
            for (int i = 0; i < realGDPLevelPath.length; i++) {
                realGDPLevelPath[i] /= realGDPLevelPath0;
                priceLevelPath[i] /= priceLevelPath0;
                Model.constructionUnitCostIndexPath[i] /= constructionUnitCostIndexPath0;
            }

            //realGDPLevelPath = derivePathWithFixedGrowthRate(Double.parseDouble(prop.getProperty("monthlyGDPGrowthRateBase")));
            //priceLevelPath = derivePathWithFixedGrowthRate(Double.parseDouble(prop.getProperty("monthlyInflationRateBase")));

            Model.realGDPLevelPath = realGDPLevelPath;
            Model.priceLevelPath = priceLevelPath;


        } catch (IOException ioe) {
            System.out.println("Exception " + ioe + " while trying to read file '" + configFileName + "'");
            ioe.printStackTrace();
        }

        Model.marriageDataFile = new File(MainRun.marriageDataFile);

        //Shock paths

        Model.realGDPLevelShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("realGDPShockPoints"));
        Model.priceLevelShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("priceLevelShockPoints"));
        Model.yearlyBaseRateShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("yearlyBaseRateShockPoints"));

        double[] unemploymentRate0ShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("unemploymentRate0ShockPoints"));
        double[] unemploymentRate1ShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("unemploymentRate1ShockPoints"));
        double[] unemploymentRate2ShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("unemploymentRate2ShockPoints"));
        double[] unemploymentProbability0ShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("unemploymentProbability0ShockPoints"));
        double[] unemploymentProbability1ShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("unemploymentProbability1ShockPoints"));
        double[] unemploymentProbability2ShockPath = deriveShockPath(Integer.parseInt(prop.getProperty("shockStartPeriod")),Integer.parseInt(prop.getProperty("shockEndPeriod")),PropertyToDoubleArray("unemploymentProbability2ShockPoints"));

        Model.unemploymentRatesShockPath = new double[Model.nPeriods][];
        Model.unemploymentProbabilitiesShockPath = new int[Model.nPeriods][];
        for (int i = 0; i < Model.nPeriods; i++) {
            Model.unemploymentRatesShockPath[i] = new double[Model.nTypes];
            Model.unemploymentProbabilitiesShockPath[i] = new int[Model.nTypes];

            Model.unemploymentRatesShockPath[i][0] = unemploymentRate0ShockPath[i];
            Model.unemploymentRatesShockPath[i][1] = unemploymentRate1ShockPath[i];
            Model.unemploymentRatesShockPath[i][2] = unemploymentRate2ShockPath[i];
            Model.unemploymentProbabilitiesShockPath[i][0] = (int) Math.ceil(unemploymentProbability0ShockPath[i]);
            Model.unemploymentProbabilitiesShockPath[i][1] = (int) Math.ceil(unemploymentProbability1ShockPath[i]);
            Model.unemploymentProbabilitiesShockPath[i][2] = (int) Math.ceil(unemploymentProbability2ShockPath[i]);

        }

        Model.loanOneYearFixationSharePathPoints=derivePathWithPoints(PropertyToDoubleArray("loanOneYearFixationSharePathPoints"));
        Model.loanFiveYearFixationSharePathPoints=derivePathWithPoints(PropertyToDoubleArray("loanFiveYearFixationSharePathPoints"));
        Model.loanFixedSharePathPoints = new double[Model.loanFiveYearFixationSharePathPoints.length];
        for (int i = 0; i < Model.loanFiveYearFixationSharePathPoints.length; i++) {
            Model.loanFixedSharePathPoints[i] = 1 - Model.loanOneYearFixationSharePathPoints[i] - Model.loanFiveYearFixationSharePathPoints[i];
        }
        Model.loanOneYearFixationSpreadPathPoints = derivePathWithPoints(PropertyToDoubleArray("loanOneYearFixationSpreadPathPoints"));
        Model.loanFiveYearFixationSpreadPathPoints = derivePathWithPoints(PropertyToDoubleArray("loanFiveYearFixationSpreadPathPoints"));
        Model.loanFixedSpreadPathPoints = derivePathWithPoints(PropertyToDoubleArray("loanFixedSpreadPathPoints"));
        Model.loanFixedSpreadPathPoints = derivePathWithPoints(PropertyToDoubleArray("loanFixedSpreadPathPoints"));
        Model.yearlyInterestRateRegressionConstantDeviationPath = derivePathWithPoints(PropertyToDoubleArray("yearlyInterestRateRegressionConstantDeviationPathPoints"));

    }

    public static void deriveParameters(String configFileName) {

    }

    public static double[] PropertyToDoubleArray(String propertyName) {
        String string = prop.getProperty(propertyName);
        if (string.equals("{}")) return new double[0];
        string = string.replace("{","");
        string = string.replace("}","");
        string = string.replace(", ",",");
        String[] values = string.split(",");
        double[] result = new double[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Double.parseDouble(values[i]);
        }

        return result;

    }

    public static int[] PropertyToIntArray(String propertyName) {

        String string = prop.getProperty(propertyName);
        if (string.equals("{}")) return new int[0];
        string = string.replace("{","");
        string = string.replace("}","");
        string = string.replace(", ",",");
        String[] values = string.split(",");

        int[] result = new int[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = Integer.parseInt(values[i]);
        }

        return result;

    }

    public static double[][] deriveMonthlyWageRatio() {

        double[][] wageRatio = new double[Model.nTypes][];

        wageRatio[0] = PropertyToDoubleArray("wageRatio0");
        wageRatio[1] = PropertyToDoubleArray("wageRatio1");
        wageRatio[2] = PropertyToDoubleArray("wageRatio2");

        double[][] calculatedWageRatio = new double[Model.nTypes][Model.lifespan];

        for (int i = 0; i < Model.nTypes; i++) { //wageRatio[][] lifespan*typeNum
            for (int j = 0; j < Model.lifespan; j++) {
                if (j < wageRatio[i].length * 12) {
                    calculatedWageRatio[i][j] = wageRatio[i][(int) Math.floor(j / 12.0)];
                } else {
                    calculatedWageRatio[i][j] = calculatedWageRatio[i][j-1];
                }

            }
        }

    return calculatedWageRatio;

    }

    public static double[][] deriveStaticPathsForTypes(double[] array) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();

        double[][] paths = new double[getModelNPeriods() + nAdditionalPeriodsForPath][array.length];
        for (int i = 0; i < getModelNPeriods() + nAdditionalPeriodsForPath; i++) {
            for (int j = 0; j < array.length; j++) {
                paths[i][j] = array[j];
            }
        }

        return paths;
    }

    public static int[][] deriveStaticPathsForTypes(int[] array) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();

        int[][] paths = new int[getModelNPeriods() + nAdditionalPeriodsForPath][array.length];
        for (int i = 0; i < getModelNPeriods() + nAdditionalPeriodsForPath; i++) {
            for (int j = 0; j < array.length; j++) {
                paths[i][j] = array[j];
            }
        }

        return paths;
    }

    public static double[] derivePathWithFixedGrowthRate(double growthRate) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();
        double[] path = new double[getModelNPeriods() + nAdditionalPeriodsForPath];
        path[0] = 1;
        for (int i = 1; i < getModelNPeriods() + nAdditionalPeriodsForPath; i++) {
            path[i] = path[i-1]*(1 + growthRate);
        }

        return path;
    }

    public static double[] derivePathWithFixedGrowthRate(double initialValue, double growthRate) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();
        double[] path = new double[getModelNPeriods() + nAdditionalPeriodsForPath];
        path[0] = initialValue;
        for (int i = 1; i < getModelNPeriods() + nAdditionalPeriodsForPath; i++) {
            path[i] = path[i-1]*(1 + growthRate);
        }

        return path;
    }

    public static double[] deriveStaticPath(double base) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();
        double[] path = new double[getModelNPeriods() + nAdditionalPeriodsForPath];
        for (int i = 0; i < getModelNPeriods() + nAdditionalPeriodsForPath; i++) {
            path[i] = base;
        }
        return path;
    }

    public static int[] deriveStaticPath(int base) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();
        int[] path = new int[getModelNPeriods() + nAdditionalPeriodsForPath];
        for (int i = 0; i < getModelNPeriods() + nAdditionalPeriodsForPath; i++) {
            path[i] = base;
        }
        return path;
    }

    public static double[] deriveShockPath(int shockStartPeriod, int shockEndPeriod, double[] shockPoints) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();
        double[] path = new double[getModelNPeriods() + nAdditionalPeriodsForPath];
        for (int i = 0; i < path.length; i++) {
            path[i] = 1.0;
        }
        double[] shockPointsFull = new double[shockPoints.length + 4];
        shockPointsFull[0] = shockStartPeriod;
        shockPointsFull[1] = 1;
        shockPointsFull[shockPointsFull.length-2] = shockEndPeriod;
        shockPointsFull[shockPointsFull.length-1] = 1;
        for (int i = 2; i < shockPointsFull.length-2; i++) {
            shockPointsFull[i] = shockPoints[i-2];
        }

        for (int i = 1; i < shockPointsFull.length/2; i++) {
            int exclusiveStartDate = (int) shockPointsFull[(i-1)*2];
            double exclusiveStartValue = shockPointsFull[(i-1)*2 + 1];
            int inclusiveEndDate = (int) shockPointsFull[i*2];
            double inclusiveEndValue = shockPointsFull[i*2+1];

            for (int j = exclusiveStartDate + 1; j <= inclusiveEndDate ; j++) {
                path[j] = exclusiveStartValue + (inclusiveEndValue - exclusiveStartValue)/(inclusiveEndDate - exclusiveStartDate) * (j-exclusiveStartDate);
            }
        }

        return path;
    }

    public static double[] derivePathWithPoints(double[] points) {

        int nAdditionalPeriodsForPath = getNAdditionalPeriodsForPath();
        double[] path = new double[getModelNPeriods() + nAdditionalPeriodsForPath];
        for (int i = 0; i < path.length; i++) {
            path[i] = points[1];
        }
        double[] pointsFull = new double[points.length + 2];
        for (int i = 0; i < points.length; i++) {
            pointsFull[i] = points[i];
        }
        pointsFull[pointsFull.length-2] = getModelNPeriods() + nAdditionalPeriodsForPath - 1;
        pointsFull[pointsFull.length-1] = points[points.length - 1];

        for (int i = 0; i < pointsFull.length/2; i++) {
            int exclusiveStartDate = 0;
            double exclusiveStartValue = pointsFull[1];
            if (i>0) {
                exclusiveStartDate = (int) pointsFull[(i-1)*2];
                exclusiveStartValue = pointsFull[(i-1)*2 + 1];
            }

            int inclusiveEndDate = (int) pointsFull[i*2];
            double inclusiveEndValue = pointsFull[i*2+1];

            for (int j = exclusiveStartDate + 1; j <= inclusiveEndDate ; j++) {
                path[j] = exclusiveStartValue + (inclusiveEndValue - exclusiveStartValue)/(inclusiveEndDate - exclusiveStartDate) * (j-exclusiveStartDate);
            }
        }

        return path;
    }

    public static int getNAdditionalPeriodsForPath() {
        int nAdditionalPeriodsForPath = 0;
        if (prop==null) {
            try (FileReader fileReader = new FileReader(MainRun.configFileName)) {
                prop = new Properties();
                prop.load(fileReader);

            } catch (IOException ioe) {

            }
        }
        nAdditionalPeriodsForPath = Integer.parseInt(prop.getProperty("nAdditionalPeriodsForPath"));
        return nAdditionalPeriodsForPath;
    }

    public static int getModelNPeriods() {
        if (Model.nPeriods==0) {
            try (FileReader fileReader = new FileReader(MainRun.configFileName)) {
                prop = new Properties();
                prop.load(fileReader);

                Model.nPeriods = Integer.parseInt(prop.getProperty("nPeriods"));
                if (Model.parametersToOverride.nPeriods != 0) Model.nPeriods = Model.parametersToOverride.nPeriods;
            } catch (IOException ioe) {

            }
            return Model.nPeriods;
        } else {
            return Model.nPeriods;
        }
    }

}
