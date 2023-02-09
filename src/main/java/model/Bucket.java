package model;


import lombok.Getter;
import lombok.Setter;

import util.*;
import java.util.*;


@Getter
@Setter
public class Bucket implements HasID {


    public static int nextId;
    final int id;

    //state variables
    public double sizeMin;
    public double sizeMax;
    public double stateMin;
    public double stateMax;
    boolean isHighQuality = false;

    public Neighbourhood neighbourhood;
    public int sizeIndex; //for outputData, starts with 1!
    public int stateIndex; //starts with 1, not just for outputData

    public int[] histNewlyBuiltDemand;
    public int[] histNFictiveRentDemand;
    public int[] histNFlatsForRent;
    public int[] histNFlatsRented;
    public int[] histNForSale;
    public int[] histNSold;

    //derived variables
    int nFlats;
    int newlyBuiltDemand;
    List<FlatSaleRecord> flatSaleRecords = new ArrayList<>();
    double sampleFlatPrice;
    Flat sampleFlat;
    int nFictiveRentDemand;
    int nFlatsForRent;
    int nFlatsRented;
    double utilizationRatio;
    double rentSaleProbabilityCentralInvestor;
    double rentSaleProbabilityHouseholds;
    int nNewlyBuiltFlatsNotYetSold; //including flats under construction
    int nForSale;
    int nSold;
    double averageNForSaleToNSold;
    double nForSaleToNSoldProbabilityAdjustment;
    int nNewlyBuiltSold;

    //outputData
    public double[][] outputData;
    double sampleFlatPrice0;


    public Bucket() {
        id = Bucket.nextId;
        Bucket.nextId++;
        makeHistoryVariables();
    }

    public Bucket(int id) {
        this.id = id;
        if (Bucket.nextId < id + 1) Bucket.nextId = id + 1;
        makeHistoryVariables();
    }

    public void makeHistoryVariables() {
        histNewlyBuiltDemand = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histNFictiveRentDemand = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histNFlatsForRent = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histNFlatsRented = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histNForSale = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histNSold = new int[Model.nHistoryPeriods + Model.maxNPeriods];
    }

    public void createSampleFlat() {
        sampleFlat = new Flat();
        sampleFlat.bucket = this;
        sampleFlat.size = Math.min((sizeMin + sizeMax) / 2, getSizeMaxForConstructionFlats());
        sampleFlat.state = (stateMin + stateMax) / 2;
    }

    public void calculateSampleFlatPrice() {

        sampleFlatPrice = sampleFlat.calculateMarketPrice();
        if (Model.period==0) sampleFlatPrice0 = sampleFlatPrice;
    }

    public void calculateAndSetAverageNForSaleToNSold() {
        double averageNForSale= OwnFunctions.average(histNForSale,
                Model.nHistoryPeriods + Model.period - Model.nPeriodsForAverageNForSaleToNSold,
                Model.nHistoryPeriods + Model.period - 1);
        double averageNSold= OwnFunctions.average(histNSold,
                Model.nHistoryPeriods + Model.period - Model.nPeriodsForAverageNForSaleToNSold,
                Model.nHistoryPeriods + Model.period - 1);
        if (averageNSold==0) {
         averageNForSaleToNSold=1000;
         if (averageNForSale==0) averageNForSaleToNSold = Model.targetNForSaleToNSold;
         return;
        }
        averageNForSaleToNSold = averageNForSale/averageNSold;

    }

    public void calculateAndSetNForSaleToNSoldProbabilityAdjustment() {

        nForSaleToNSoldProbabilityAdjustment = (1/averageNForSaleToNSold-1/Model.targetNForSaleToNSold)*Model.coeffProbabilityNForSaleToNSoldAdjustment;
        nForSaleToNSoldProbabilityAdjustment = OwnFunctions.doubleInRange(nForSaleToNSoldProbabilityAdjustment,Model.minForSaleToNSoldProbabilityAdjustment,Model.maxForSaleToNSoldProbabilityAdjustment);

    }



    public static Comparator<Pair<Double, Bucket>> comparatorPairForDistance = new Comparator<Pair<Double, Bucket>>() {

        //@Override
        public int compare(Pair<Double, Bucket> o1, Pair<Double, Bucket> o2) {
            return Double.compare(o1.getKey(), o2.getKey());
        }
    };

    public void calculateAndSetUtilizationRatio() {
        int nFlatsForRent = histNFlatsForRent[Model.nHistoryPeriods + Model.period - 1];
        double nFictiveRentDemand = OwnFunctions.average(histNFictiveRentDemand,
                Model.nHistoryPeriods + Model.period - Model.nPeriodsForAverageNFictiveRentDemand,
                Model.nHistoryPeriods + Model.period - 1);
        utilizationRatio = nFictiveRentDemand / (double) nFlatsForRent;
        if (nFlatsForRent == 0 || utilizationRatio > 1) utilizationRatio = 1; //utilizationRatio is basically used to determine rentSaleProbability and so if nFlatsForRent == 0, utilizationRatio does not matter since there is no flat to be sold
    }

    public void calculateAndSetRentSaleProbabilities() {

        rentSaleProbabilityCentralInvestor = 3*Math.max(0,Model.yearlyRentSaleProbabilityAtZeroExpectedReturnSpread/12 - Model.yearlyRentSaleProbabilityAtZeroExpectedReturnSpread/12/Model.minYearlyExpectedReturnSpreadForZeroRentSaleProbability * neighbourhood.expectedReturnSpread)*Model.targetUtilizationRatio/Math.max(Model.minUtilizationRatioForTargetUtilizationRatioToUtilizationRatioInRentSaleProbability,utilizationRatio);
        rentSaleProbabilityHouseholds = (Model.targetUtilizationRatio-Model.targetUtilizationRatioAdjusterInHouseholdRentSaleProbability-utilizationRatio)*Model.householdRentSaleProbabilityScaler;

    }

    public void nullMiscDerivedVariables() {
        nNewlyBuiltFlatsNotYetSold = 0;
        nFlats = 0;
        nForSale = 0;
        nSold = 0;
        nNewlyBuiltSold = 0;
    }

    public double getSizeMaxForConstructionFlats() {
        if (sizeMax==Model.maxBucketSize) {
            return sizeMin*Model.sizeMaxOfConstructionFlatsToSizeMinInLargestSizeBucket;
        } else {
            return sizeMax;
        }
    }

    public double getSizeMaxForFictiveFlats() {
        if (sizeMax==Model.maxBucketSize) {
            return sizeMin*Model.sizeMaxOfFictiveFlatsToSizeMinInLargestSizeBucket;
        } else {
            return sizeMax;
        }
    }

    public void incrementNFictiveRentDemand() {
        synchronized(this) {
            nFictiveRentDemand++;
        }
    }

    public void incrementNewlyBuiltDemand() {
        synchronized(this) {
            if (Model.period==0) return;
            newlyBuiltDemand++;
        }
    }

    public void incrementNewlyBuiltDemandCSOK(Household household) {
        synchronized(this) {
            if (Model.period==0) return;
        }
    }

    public GeoLocation getGeoLocation() {
        return neighbourhood.getGeoLocation();
    }

    public void createOutputDataArray(int nVariables, int nPeriods) {
        outputData = new double[nVariables][nPeriods];
    }


}