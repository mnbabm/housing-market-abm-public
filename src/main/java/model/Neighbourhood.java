package model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.lang3.ArrayUtils;
import util.MappingWithWeights;
import util.OwnFunctions;
import util.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public class Neighbourhood implements HasID {
    private static int nextId;
    final int id;

    //state variables
    public double quality;
    double landPrice;
    List<Bucket> buckets = new ArrayList<Bucket>();
    GeoLocation geoLocation;
    public PriceRegressionFunctionLinear priceRegressionFunctionLinear;
    public double[] histPriceIndex;
    public double[] histPriceIndexToBeginning;
    double[] histCentralInvestmentValue;
    double[] histHouseholdInvestmentValue;
    public double[] histReturn;
    public double[] histForRentValue;

    public boolean rentalNeighbourhood = true;

    //derived variables
    double expectedReturn;
    double expectedReturnSpread;
    List<FlatSaleRecord> flatSaleRecordsForPriceIndex = new ArrayList<FlatSaleRecord>();
    List<FlatSaleRecord> flatSaleRecordsForPrice = new ArrayList<FlatSaleRecord>();
    List<ForSaleRecordTemporary> forSaleRecordsForPrice = new ArrayList<ForSaleRecordTemporary>();
    double quarterlyPriceIndex;
    double priceIndex;
    double priceIndexToBeginning;
    double rentMarkup;
    double householdInvestmentValue;
    double centralInvestmentValue;
    double plannedCentralInvestmentValue;
    MappingWithWeights<Bucket> cumulativeInvestmentProbabilities = new MappingWithWeights<>();
    int nFlatsForRent;
    double nFictiveRentDemand;
    boolean priceIndexRefreshed = false;
    boolean priceRegressionRefreshed = false;
    List<Neighbourhood> similarNeighbourhoods = new ArrayList<>();
    public int nFlats;
    public int nTransactions; //purchases
    ArrayList<Double[]> flatSaleRecordsDataForPriceIndexToBeginning = new ArrayList<>();
    public double averageState;

    public double rentIncome;
    public double forRentValue;
    public double aggregateEstimatedMarketValue;

    public double sumSoldPrice;
    public double sumSoldArea;
    public boolean mightNeedOutOfRangeFlatForLandPrice;

    Map<Double, ArrayList<Flat>> sizeToFlatMapForInvestment = new HashMap<>();

    //outputData
    public double[][] outputData;

    //variablesForDataGeneration
    public int nFlatsInTowns;
    public int nFlatsInVillages;


    public Neighbourhood() {
        id = Neighbourhood.nextId;
        Neighbourhood.nextId++;
        makeHistoryVariables();
    }

    public Neighbourhood(int id) {
        this.id = id;
        if (Neighbourhood.nextId < id + 1) Neighbourhood.nextId = id + 1;
        makeHistoryVariables();

    }

    public void makeHistoryVariables() {
        histPriceIndex = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histPriceIndexToBeginning = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histCentralInvestmentValue = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histHouseholdInvestmentValue = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histReturn = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histForRentValue = new double[Model.nHistoryPeriods + Model.maxNPeriods];
    }

    public void refreshPriceRegression() {
        List<FlatSaleRecord> flatSaleRecords = new ArrayList<>();
        for (Bucket bucket: buckets) {
            for (FlatSaleRecord flatSaleRecord : bucket.flatSaleRecords) {
                flatSaleRecords.add(flatSaleRecord);
            }
        }

        priceRegressionRefreshed = priceRegressionFunctionLinear.priceRegressionRefreshed(flatSaleRecords);

    }

    public void getPriceRegressionCoeffOfSimilarNeighbourhoods() {
        for (Neighbourhood neighbourhood : similarNeighbourhoods) {
            if (neighbourhood.priceRegressionRefreshed) {
                priceRegressionFunctionLinear.copyRegressionParameters(neighbourhood.priceRegressionFunctionLinear);
                break;
            }
        }
    }

    void calculateAndSetPriceIndex() {

        priceIndexRefreshed = false;

        int baseMinPeriod = Model.period - 5;
        int baseMaxPeriod = Model.period - 3;
        int actualMinPeriod = Model.period - 2;
        int actualMaxPeriod = Model.period - 0;

        ArrayList<Double> flatPrice = new ArrayList<Double>();
        ArrayList<Double[]> flatInfo = new ArrayList<Double[]>();

        int nObservationFromBaseInterval = 0;
        int nObservationFromActualInterval = 0;

        for (FlatSaleRecord flatSaleRecord : flatSaleRecordsForPriceIndex) {
            if ((flatSaleRecord.isNewlyBuilt) || flatSaleRecord.periodOfRecord <0) continue;
            if ((flatSaleRecord.periodOfRecord >= baseMinPeriod && flatSaleRecord.periodOfRecord <= baseMaxPeriod) ||
                    (flatSaleRecord.periodOfRecord >= actualMinPeriod && flatSaleRecord.periodOfRecord <= actualMaxPeriod)) {
                double price = Math.log(flatSaleRecord.price);
                Double[] info = new Double[2 + (Model.size2inPriceIndexRegression ? 1 : 0)];

                info[0] = Math.log(flatSaleRecord.size);
                if (Model.size2inPriceIndexRegression) info[1] = Math.log(flatSaleRecord.size)*Math.log(flatSaleRecord.size);

                if ((flatSaleRecord.periodOfRecord >= actualMinPeriod && flatSaleRecord.periodOfRecord <= actualMaxPeriod)) {
                    info[1 + (Model.size2inPriceIndexRegression ? 1 : 0)] = 1.0;
                    nObservationFromActualInterval++;
                } else {
                    info[1 + (Model.size2inPriceIndexRegression ? 1 : 0)] = 0.0;
                    nObservationFromBaseInterval++;
                }

                flatPrice.add(price);
                flatInfo.add(info);

            }
        }

        double[] y = new double[flatPrice.size()];
        double[][] x = new double[flatPrice.size()][];

        if (nObservationFromActualInterval < Model.minNObservationForPriceIndex || nObservationFromBaseInterval < Model.minNObservationForPriceIndex) {
            quarterlyPriceIndex = 1;
            priceIndex = histPriceIndex[Model.nHistoryPeriods+Model.period-1];
            return;
        }


        for (int i = 0; i < y.length; i++) {
            y[i] = flatPrice.get(i);
            x[i] = ArrayUtils.toPrimitive(flatInfo.get(i));
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);

        double[] priceIndex_regressionCoeff = new double[5];

        try {
            priceIndex_regressionCoeff = regression.estimateRegressionParameters();

            quarterlyPriceIndex = Math.exp(priceIndex_regressionCoeff[2 + (Model.size2inPriceIndexRegression ? 1 : 0)]);
            priceIndex = histPriceIndex[Model.nHistoryPeriods + baseMaxPeriod]* quarterlyPriceIndex; //constant + 3rd explanatory variable
            priceIndexRefreshed = true;
            if (histPriceIndex[Model.nHistoryPeriods + Model.period - 1]==1.0) {
                priceIndex = quarterlyPriceIndex;
                for (int i = 0; i < Model.period; i++) {
                    histPriceIndex[Model.nHistoryPeriods + i] = priceIndex;
                }
            }

        } catch (SingularMatrixException e) {
            e.printStackTrace();
            quarterlyPriceIndex = 1;
            priceIndex = histPriceIndex[Model.nHistoryPeriods+Model.period-1];
        }

    }

    public void calculateAndSetPriceIndexToBeginning() {

        int actualMinPeriod = Model.period - 2;
        int actualMaxPeriod = Model.period - 0;

        ArrayList<Double> flatPrice = new ArrayList<Double>();
        ArrayList<Double[]> flatInfo = new ArrayList<Double[]>();

        int nObservationFromBaseInterval = 0;
        int nObservationFromActualInterval = 0;

        for (Double[] infoArray : flatSaleRecordsDataForPriceIndexToBeginning) {
            double periodSold = infoArray[0];
            double soldSize = infoArray[1];
            double soldState = infoArray[2];
            double soldPrice = infoArray[3];
            if ((periodSold >= Model.baseMinPeriodForPriceIndexToBeginning && periodSold <= Model.baseMaxPeriodForPriceIndexToBeginning) ||
                    (periodSold >= actualMinPeriod && periodSold <= actualMaxPeriod)) {
                double price = Math.log(soldPrice);
                Double[] info = new Double[2 + (Model.size2inPriceIndexRegression ? 1 : 0)];

                info[0] = Math.log(soldSize);
                if (Model.size2inPriceIndexRegression) info[1] = Math.log(soldSize)*Math.log(soldSize);
                if ((periodSold >= actualMinPeriod && periodSold <= actualMaxPeriod)) {
                    info[1 + (Model.size2inPriceIndexRegression ? 1 : 0)] = 1.0;
                    nObservationFromActualInterval++;
                } else {
                    info[1 + (Model.size2inPriceIndexRegression ? 1 : 0)] = 0.0;
                    nObservationFromBaseInterval++;
                }

                flatPrice.add(price);
                flatInfo.add(info);

            }
        }

        double[] y = new double[flatPrice.size()];
        double[][] x = new double[flatPrice.size()][];

        if (nObservationFromActualInterval < Model.minNObservationForPriceIndex || nObservationFromBaseInterval < Model.minNObservationForPriceIndex) {
            priceIndexToBeginning = histPriceIndexToBeginning[Model.nHistoryPeriods+Model.period-1];
            if (Model.period>=Model.baseMinPeriodForPriceIndexToBeginning && Model.period<=Model.baseMaxPeriodForPriceIndexToBeginning) priceIndexToBeginning = 1;
            return;
        }


        for (int i = 0; i < y.length; i++) {
            y[i] = flatPrice.get(i);
            x[i] = ArrayUtils.toPrimitive(flatInfo.get(i));
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);

        double[] priceIndex_regressionCoeff = new double[5];

        try {
            priceIndex_regressionCoeff = regression.estimateRegressionParameters();
            priceIndexToBeginning = Math.exp(priceIndex_regressionCoeff[2 + (Model.size2inPriceIndexRegression ? 1 : 0)]);
        } catch (SingularMatrixException e) {
            e.printStackTrace();
            priceIndexToBeginning = histPriceIndexToBeginning[Model.nHistoryPeriods+Model.period-1];
        }
    }

    public double getPriceIndexOfSimilarNeighbourhoods() {
        int maxNSimilarNeighbourhoods=2;
        int nSimilarNeighbourhoods = 0;
        double sumPriceIndex = 0;

        for (Neighbourhood neighbourhood : similarNeighbourhoods) {
            if (neighbourhood.priceIndexRefreshed) {
                nSimilarNeighbourhoods++;
                sumPriceIndex += neighbourhood.priceIndex/neighbourhood.histPriceIndex[Model.nHistoryPeriods+Model.period-1];

                if (nSimilarNeighbourhoods==maxNSimilarNeighbourhoods) {
                    priceIndex = sumPriceIndex/nSimilarNeighbourhoods;
                    return priceIndex;
                }

            }
        }
        return -1;
    }


    public void calculateAndSetAggregateFlatInfo() {
        nFlatsForRent = 0;
        nFictiveRentDemand = 0;


        for (Bucket bucket : buckets) {
            nFlatsForRent += bucket.histNFlatsForRent[Model.nHistoryPeriods + Model.period - 1];
            nFictiveRentDemand += OwnFunctions.average(bucket.histNFictiveRentDemand,
                    Model.nHistoryPeriods + Model.period - Model.nPeriodsForAverageNFictiveRentDemand,
                    Model.nHistoryPeriods + Model.period - 1);
        }

        if (rentalNeighbourhood==false) {
            nFlatsForRent=0;
            nFictiveRentDemand=0;
        }

    }

    public void calculateAndSetRentMarkup() {


        double empiricalNFlatsRented = 0;
        double fictiveNFlatsRented = 0;
        double nFlatsForRent = 0;
        for (int i = 0; i < Model.nPeriodsForUtilizationRatio; i++) {
            for (Bucket bucket: buckets) {
                empiricalNFlatsRented += bucket.histNFlatsRented[Model.nHistoryPeriods + Model.period - 1 - i];
                fictiveNFlatsRented += bucket.histNFictiveRentDemand[Model.nHistoryPeriods + Model.period - 1 - i];
                nFlatsForRent += bucket.histNFlatsForRent[Model.nHistoryPeriods + Model.period - 1 - i];
            }
        }

        double fictiveUtilizationRatio = 0;
        double utilizationRatio = 0;

        if (nFlatsForRent > 0) {
            utilizationRatio = empiricalNFlatsRented/nFlatsForRent;
            fictiveUtilizationRatio = Math.min(fictiveNFlatsRented/nFlatsForRent,Model.rentMarkupUtilizationRatioCap);

            if (utilizationRatio>Model.targetUtilizationRatio) {
                utilizationRatio = Math.max(utilizationRatio,fictiveUtilizationRatio);
            }
        } else if (nFlatsForRent == 0 && fictiveNFlatsRented > 0) {
            utilizationRatio = Model.rentMarkupUtilizationRatioCap;
        }



        rentMarkup = Model.rentMarkupCoeff * Math.pow(utilizationRatio, Model.rentMarkupPower);
    }

    void calculateAndSetExpectedReturnAndSpread() {

        //yearly
        expectedReturn = 12 * OwnFunctions.average(histReturn,
                Model.nHistoryPeriods + Model.period - Model.nPeriodsForAverageReturn,
                Model.nHistoryPeriods + Model.period - 1)
                + Model.expectedReturnCapitalGainCoeff * (histPriceIndex[Model.nHistoryPeriods + Model.period - 1] / histPriceIndex[Model.nHistoryPeriods + Model.period - 13] - 1);


        expectedReturnSpread = expectedReturn - Model.yearlyBaseRate;

    }

    void calculateAndSetPlannedInvestmentValue() {
        double histForrentValueRatio = 1.0;
        plannedCentralInvestmentValue = Model.monthlyInvestmentRatioConstantCentralInvestor * Model.priceLevel * Model.realGDPLevel * histForrentValueRatio * histForRentValue[Model.nHistoryPeriods - 1];
    }

    void calculateAndSetInvestmentProbabilities() {

        double expectedInvestmentValue = plannedCentralInvestmentValue + histHouseholdInvestmentValue[Model.nHistoryPeriods + Model.nPeriods - 1];

        double value = 0;
        double targetRatioForInvestmentProbability = Model.targetUtilizationRatio + Model.stepInSearchingTargetRatioForInvestmentProbability;

        //this while block helps calculate the targetRatioForInvestmentProbability at which the expectedInvestmentValue would be invested
        while (value < expectedInvestmentValue && targetRatioForInvestmentProbability>Model.minTargetRatioForInvestmentProbability) {

            value = 0;
            targetRatioForInvestmentProbability -= Model.stepInSearchingTargetRatioForInvestmentProbability;
            for (Bucket bucket : buckets) {
                int nFlatsForRent = bucket.histNFlatsForRent[Model.nHistoryPeriods + Model.period - 1];
                int nFictiveRentDemand = bucket.histNFictiveRentDemand[Model.nHistoryPeriods + Model.period - 1];
                int nMissingFlats = (int) Math.ceil(nFictiveRentDemand / targetRatioForInvestmentProbability) - nFlatsForRent;
                if (nMissingFlats <= 0) continue;
                value += bucket.sampleFlatPrice * nMissingFlats;
            }

        }

        cumulativeInvestmentProbabilities.clear();

        for (Bucket bucket : buckets) {
            int nFlatsForRent = bucket.histNFlatsForRent[Model.nHistoryPeriods + Model.period - 1];
            int nFictiveRentDemand = bucket.histNFictiveRentDemand[Model.nHistoryPeriods + Model.period - 1];
            int nMissingFlats = (int) Math.ceil(nFictiveRentDemand / targetRatioForInvestmentProbability) - nFlatsForRent;

            if (nMissingFlats>0) {
                cumulativeInvestmentProbabilities.put(nMissingFlats, bucket);
            }

        }

    }

    public double calculateWeightForNeighbourhoodInvestmentProbabilites() {
        int missingFlats = (int) Math.ceil(nFictiveRentDemand / Model.targetUtilizationRatio) - nFlatsForRent;
        if (missingFlats <= 0) return 0;
        return Math.max(0,expectedReturnSpread) * nFictiveRentDemand;
    }

    public void nullMiscDerivedVariables() {
        centralInvestmentValue = 0;
        householdInvestmentValue = 0;
        rentIncome = 0;
        forRentValue = 0;
        nFlats = 0;
        nTransactions = 0;

    }

    void sortSimilarNeighbourhoods() {

        similarNeighbourhoods.clear();

        List<Pair<Double, Neighbourhood>> distance = new ArrayList<>();
        for (Neighbourhood neighbourhood : geoLocation.getNeighbourhoods()) {

                if (neighbourhood != this) distance.add(new Pair<>(Math.abs(quality-neighbourhood.quality), neighbourhood));

        }

        distance.sort(comparatorPairForDistance);
        similarNeighbourhoods = distance.stream().map(Pair::getValue).collect(Collectors.toList());

    }


    public static Comparator<Pair<Double, Neighbourhood>> comparatorPairForDistance = new Comparator<Pair<Double, Neighbourhood>>() {

        //@Override
        public int compare(Pair<Double, Neighbourhood> o1, Pair<Double, Neighbourhood> o2) {
            return Double.compare(o1.getKey(), o2.getKey());
        }
    };

    public void createOutputDataArray(int nVariables, int nPeriods) {
        outputData = new double[nVariables][nPeriods];
    }


}
