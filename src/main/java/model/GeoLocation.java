package model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import util.OwnFunctions;

import java.util.*;

@Getter
@Setter
public class GeoLocation implements HasID {

    private static int nextId;
    final int id;

    //state variables
    List<Neighbourhood> neighbourhoods = new ArrayList<Neighbourhood>();
    public double[] histPriceIndex;
    public double[] histPriceIndexToBeginning;
    public double basePriceIndexToAverageWageIncome;
    public int[] histNNewlyBuiltFlats; //underConstruction or ready but not sold, excluding flats the construction of which has just started and so are not for sale
    public int[] histNNewlyBuiltFlatsSold; //just underConstruction since what has been sold will not count as newlyBuilt from then on
    public double[] histRenovationUnitCost;
    public double[] histConstructionUnitCost;
    public double[] histRenovationQuantity;

    public double[] histDirectPriceIndexUsed; //just for queries (the history is 1.0 for all history periods)
    public double[] histDirectPriceIndexWithNewlyBuilt; //just for queries (the history is 1.0 for all history periods)
    public double[] histDirectPriceIndexNewlyBuilt; //just for queries (the history is 1.0 for all history periods)

    double absCoeffStateHabit = 0.00;
    //derived variables
    double priceIndex;
    double priceIndexToBeginning;
    double cyclicalAdjuster;
    double housePriceToIncome;
    double averageWageIncome;
    double priceIndexToAverageWageIncome;
    double constructionMarkup;
    double areaUnderConstruction;
    double plannedAdditionalAreaUnderConstruction;
    double nFlatsToBuildAdjuster;
    double renovationUnitCost;
    double constructionUnitCost;
    double predictedRenovationUnitCost;
    int nFullTimeWorkers;
    double sumFullTimeWage;
    double averageWage;
    double renovationQuantity;

    public static Map<Integer, Flat> sampleFlatsForCAHAI = new HashMap<>();

    //outputData
    public double[][] outputData;


    public GeoLocation() {
        id = GeoLocation.nextId;
        GeoLocation.nextId++;
        makeHistoryVariables();
    }

    public GeoLocation(int id) {
        this.id = id;
        if (GeoLocation.nextId<id+1) GeoLocation.nextId=id+1;
        makeHistoryVariables();
    }

    void makeHistoryVariables() {
        histPriceIndex = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histPriceIndexToBeginning = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histNNewlyBuiltFlats = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histNNewlyBuiltFlatsSold = new int[Model.nHistoryPeriods + Model.maxNPeriods];
        histRenovationUnitCost = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histConstructionUnitCost = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histRenovationQuantity = new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histDirectPriceIndexWithNewlyBuilt= new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histDirectPriceIndexUsed= new double[Model.nHistoryPeriods + Model.maxNPeriods];
        histDirectPriceIndexNewlyBuilt= new double[Model.nHistoryPeriods + Model.maxNPeriods];
    }


    public void calculateHousePriceToIncome() {
        double sumHousePrice = 0;
        double sumIncome = 0;
        for (Household household : Model.households.values()) {
            if (household.home != null) {
                sumHousePrice += household.home.getEstimatedMarketPrice();
                sumIncome += household.wageIncome;
            }
        }

        housePriceToIncome = sumHousePrice / sumIncome;

    }

    void calculateAndSetPriceIndex() {


        //if we modify this function, for consistency we should modify in DataLoader as well the part for histPriceIndex
        int baseMaxPeriod = Model.period - 3;

        double quarterlyPriceIndex = 0;
        int nFlats = 0;
        for (Neighbourhood neighbourhood: neighbourhoods) {
            nFlats += Model.useTransactionWeightsForPriceIndex ? neighbourhood.nTransactions : neighbourhood.nFlats;
            quarterlyPriceIndex += neighbourhood.quarterlyPriceIndex*(Model.useTransactionWeightsForPriceIndex ? neighbourhood.nTransactions : neighbourhood.nFlats);
        }
        quarterlyPriceIndex /= nFlats;

        priceIndex = histPriceIndex[Model.nHistoryPeriods+baseMaxPeriod]*quarterlyPriceIndex;
        if (Model.period==3) {
            priceIndex = quarterlyPriceIndex;
            for (int i = 0; i < Model.period; i++) {
                histPriceIndex[Model.nHistoryPeriods + i] = priceIndex;
            }
        }
    }

    void calculateAndSetPriceIndexToBeginning() {
        //if we modify this function, for consistency we should modify in DataLoader as well the part for histPriceIndex
        int baseMaxPeriod = Model.period - 3;

        priceIndexToBeginning = 0;
        int nFlats = 0;
        for (Neighbourhood neighbourhood: neighbourhoods) {
            nFlats += Model.useTransactionWeightsForPriceIndex ? neighbourhood.nTransactions : neighbourhood.nFlats;
            priceIndexToBeginning += neighbourhood.priceIndexToBeginning*(Model.useTransactionWeightsForPriceIndex ? neighbourhood.nTransactions : neighbourhood.nFlats);
        }
        priceIndexToBeginning /= nFlats;

        outputData[3][Model.period] = priceIndexToBeginning;

    }

    void calculateAndSetDirectPriceIndex(int mode) {
        //mode: 0-withNewlyBuilt 1-used, 2-newlyBuilt

        if (mode==0 || mode==1) {
            Model.stateInDirectPriceIndexRegression = true;
        } else {
            Model.stateInDirectPriceIndexRegression = false;
        }


        boolean monthly = true;
        boolean countryWidePriceIndexForLastGeoLocationExceptForGeoLocation0 = false;


        int baseMinPeriod = Model.period - 5;
        int baseMaxPeriod = Model.period - 3;
        int actualMinPeriod = Model.period - 2;
        int actualMaxPeriod = Model.period - 0;

        if (monthly) {
            baseMinPeriod = Model.period - 1;
            baseMaxPeriod = Model.period - 1;
            actualMinPeriod = Model.period - 0;
            actualMaxPeriod = Model.period - 0;
        }

        ArrayList<Double> flatPrice = new ArrayList<Double>();
        ArrayList<Double[]> flatInfo = new ArrayList<Double[]>();

        int nObservationFromBaseInterval = 0;
        int nObservationFromActualInterval = 0;

        ArrayList<GeoLocation> geoLocationsToUse = new ArrayList<>();

        double trimRatio = 0.0000;

        ArrayList<FlatSaleRecord> flatSaleRecordsForDirectPriceIndexTrimmed = new ArrayList<>();

        if (countryWidePriceIndexForLastGeoLocationExceptForGeoLocation0 && id==Model.highestIdForGeoLocations) {
            for (GeoLocation geoLocation : Model.geoLocations.values()) {
                if (geoLocation.id>0) {
                    geoLocationsToUse.add(geoLocation);
                }
            }
        } else {
            geoLocationsToUse.add(this);
        }

        for (GeoLocation geoLocation : geoLocationsToUse) {

            ArrayList<FlatSaleRecord> flatSaleRecordsForDirectPriceIndexBase = new ArrayList<>();
            ArrayList<FlatSaleRecord> flatSaleRecordsForDirectPriceIndexActual = new ArrayList<>();

            for (FlatSaleRecord flatSaleRecord : Model.flatSaleRecords.values()) {
                if (flatSaleRecord.bucket.getGeoLocation() != geoLocation || flatSaleRecord.periodOfRecord < 0 || flatSaleRecord.isForcedSale) continue;

                if (Model.useForcedSaleForPriceIndex == false && flatSaleRecord.isForcedSale) continue;
                if (mode == 1 && flatSaleRecord.isNewlyBuilt) continue;
                if (mode == 2 && !flatSaleRecord.isNewlyBuilt) continue;

                if (flatSaleRecord.periodOfRecord >= baseMinPeriod && flatSaleRecord.periodOfRecord <= baseMaxPeriod) {
                    flatSaleRecordsForDirectPriceIndexBase.add(flatSaleRecord);
                }
                if (flatSaleRecord.periodOfRecord >= actualMinPeriod && flatSaleRecord.periodOfRecord <= actualMaxPeriod) {
                    flatSaleRecordsForDirectPriceIndexActual.add(flatSaleRecord);
                }
            }

            Collections.sort(flatSaleRecordsForDirectPriceIndexBase,FlatSaleRecord.comparatorPriceIncrease);
            Collections.sort(flatSaleRecordsForDirectPriceIndexActual,FlatSaleRecord.comparatorPriceIncrease);


            for (int i = 0; i < flatSaleRecordsForDirectPriceIndexBase.size(); i++) {
                if (i>trimRatio*flatSaleRecordsForDirectPriceIndexBase.size() && i<(1-trimRatio)*flatSaleRecordsForDirectPriceIndexBase.size()) {
                    flatSaleRecordsForDirectPriceIndexTrimmed.add(flatSaleRecordsForDirectPriceIndexBase.get(i));
                }
            }
            for (int i = 0; i < flatSaleRecordsForDirectPriceIndexActual.size(); i++) {
                if (i>trimRatio*flatSaleRecordsForDirectPriceIndexActual.size() && i<(1-trimRatio)*flatSaleRecordsForDirectPriceIndexActual.size()) {
                    flatSaleRecordsForDirectPriceIndexTrimmed.add(flatSaleRecordsForDirectPriceIndexActual.get(i));
                }
            }
        }



        for (FlatSaleRecord flatSaleRecord : flatSaleRecordsForDirectPriceIndexTrimmed) {

                double price = Math.log(flatSaleRecord.price);
                Double[] info = new Double[3 + (Model.size2inPriceIndexRegression ? 1 : 0) + (useStateInDirectPriceIndexRegression(mode) ? 1 : 0)];

                info[0] = Math.log(flatSaleRecord.size);
                if (Model.size2inPriceIndexRegression) info[1] = Math.log(flatSaleRecord.size)*Math.log(flatSaleRecord.size);

                info[1 + (Model.size2inPriceIndexRegression ? 1 : 0)] = Math.log(flatSaleRecord.getNeighbourhoodQuality());

                if ((flatSaleRecord.periodOfRecord >= actualMinPeriod && flatSaleRecord.periodOfRecord <= actualMaxPeriod)) {
                    info[2 + (Model.size2inPriceIndexRegression ? 1 : 0)] = 1.0;
                    nObservationFromActualInterval++;
                } else {
                    info[2 + (Model.size2inPriceIndexRegression ? 1 : 0)] = 0.0;
                    nObservationFromBaseInterval++;
                }

                if (useStateInDirectPriceIndexRegression(mode)) {
                    info[3 + (Model.size2inPriceIndexRegression ? 1 : 0)] = flatSaleRecord.state;
                }

                flatPrice.add(price);
                flatInfo.add(info);


        }


        double[] y = new double[flatPrice.size()];
        double[][] x = new double[flatPrice.size()][];

        if (nObservationFromActualInterval < Model.minNObservationForPriceIndex || nObservationFromBaseInterval < Model.minNObservationForPriceIndex) {
            if (mode == 0) {
                histDirectPriceIndexWithNewlyBuilt[Model.nHistoryPeriods+Model.period]=histDirectPriceIndexWithNewlyBuilt[Model.nHistoryPeriods+Model.period-1];
            } else if (mode == 1) {
                histDirectPriceIndexUsed[Model.nHistoryPeriods+Model.period]=histDirectPriceIndexUsed[Model.nHistoryPeriods+Model.period-1];
            } else if (mode == 2) {
                histDirectPriceIndexNewlyBuilt[Model.nHistoryPeriods+Model.period]=histDirectPriceIndexNewlyBuilt[Model.nHistoryPeriods+Model.period-1];
            }
            return;
        }


        for (int i = 0; i < y.length; i++) {
            y[i] = flatPrice.get(i);
            x[i] = ArrayUtils.toPrimitive(flatInfo.get(i));
        }

        OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
        regression.newSampleData(y, x);

        double[] priceIndex_regressionCoeff = new double[6];

        try {
            priceIndex_regressionCoeff = regression.estimateRegressionParameters();
            double quarterlyPriceIndex = Math.exp(priceIndex_regressionCoeff[3 + (Model.size2inPriceIndexRegression ? 1 : 0)]);
            double directPriceIndex;
                    if (mode == 0) {
                        directPriceIndex = histDirectPriceIndexWithNewlyBuilt[Model.nHistoryPeriods + baseMaxPeriod]* quarterlyPriceIndex;
                        histDirectPriceIndexWithNewlyBuilt[Model.nHistoryPeriods + Model.period] = directPriceIndex;
                    } else if (mode == 1) {
                        directPriceIndex = histDirectPriceIndexUsed[Model.nHistoryPeriods + baseMaxPeriod]* quarterlyPriceIndex;
                        histDirectPriceIndexUsed[Model.nHistoryPeriods + Model.period] = directPriceIndex;
                    } else if (mode == 2) {
                        directPriceIndex = histDirectPriceIndexNewlyBuilt[Model.nHistoryPeriods + baseMaxPeriod]* quarterlyPriceIndex;
                        histDirectPriceIndexNewlyBuilt[Model.nHistoryPeriods + Model.period] = directPriceIndex;
                    }


            if (histPriceIndex[Model.nHistoryPeriods + Model.period - 1]==1.0 && mode==1) {
                directPriceIndex = quarterlyPriceIndex;
                for (int i = 0; i < Model.period; i++) {
                    histPriceIndex[Model.nHistoryPeriods + i] = directPriceIndex;
                }
            }

        } catch (SingularMatrixException e) {
            e.printStackTrace();
            if (mode==0) {
                histDirectPriceIndexUsed[Model.nHistoryPeriods+Model.period]=histDirectPriceIndexUsed[Model.nHistoryPeriods+Model.period-1];
            } else if (mode==1) {
                histDirectPriceIndexWithNewlyBuilt[Model.nHistoryPeriods+Model.period]=histDirectPriceIndexWithNewlyBuilt[Model.nHistoryPeriods+Model.period-1];
            } else if (mode==2) {
                histDirectPriceIndexNewlyBuilt[Model.nHistoryPeriods+Model.period]=histDirectPriceIndexNewlyBuilt[Model.nHistoryPeriods+Model.period-1];
            }
        }

    }

    void calculateAndSetAverageWageIncome() {
        double sumWage = 0;
        int nWorkers = 0;
        for (Household household : Model.households.values()) {
            if ((household.home != null && household.home.bucket.neighbourhood.geoLocation == this)
                    || (household.rentHome != null && household.rentHome.bucket.neighbourhood.geoLocation == this)) {
                sumWage += household.wageIncome;
                nWorkers += household.members.size();
            }
        }
        averageWageIncome = sumWage/nWorkers;
    }

    void calculateAndSetPriceIndexToAverageWageIncome() {
        priceIndexToAverageWageIncome = histPriceIndex[Model.nHistoryPeriods+Model.period-1]/averageWageIncome;
        if (basePriceIndexToAverageWageIncome == 0){
            //this state variable has not yet been set
            basePriceIndexToAverageWageIncome = priceIndexToAverageWageIncome;
        }
    }

    void calculateAndSetConstructionMarkup() {
        double soldRatio = histNNewlyBuiltFlatsSold[Model.nHistoryPeriods + Model.period -1] / (double) histNNewlyBuiltFlats[Model.nHistoryPeriods + Model.period -1];
        if (Double.isNaN(soldRatio)) soldRatio = 0;
        constructionMarkup = Model.constructionMarkupRatio1Level + (soldRatio - Model.constructionMarkupRatio1) * (Model.constructionMarkupRatio2Level-Model.constructionMarkupRatio1Level)/(Model.constructionMarkupRatio2-Model.constructionMarkupRatio1);
        if (Model.period<Model.firstNPeriodsForFixedMarkup) {
            constructionMarkup=Model.constructionMarkupInFirstPeriods;
        }
    }

    public void calculateAndSetCyclicalAdjuster() {
        cyclicalAdjuster = 1;
    }


    public void calculateAndSetRenovationUnitCost() {
        renovationUnitCost = calculateRenovationUnitCostBase();
    }

    public double calculateRenovationUnitCostBase() {
        double renovationUnitCostBase = Model.constructionUnitCostIndex*Model.constructionUnitCostBase[id]*Model.renovationToConstructionUnitCostBase;

        double renovationRatio = 1;
        if (Model.period>=Model.firstNPeriodsForRenovationNormalQuantity) {
            double averageRenovation = OwnFunctions.average(histRenovationQuantity,Model.nHistoryPeriods,Model.nHistoryPeriods + Model.firstNPeriodsForRenovationNormalQuantity - 1);
            renovationRatio = histRenovationQuantity[Model.nHistoryPeriods + Model.period - 1]/averageRenovation;
        }

        return renovationUnitCostBase + Model.coeffRenovationRatio * Math.max(0,renovationRatio - 1) * averageWage;
    }
    public void calculateAndSetConstructionUnitCost() {
        constructionUnitCost = calculateConstructionUnitCostBase();
    }

    public double calculateConstructionUnitCostBase() {
        double constructionUnitCostBase = Model.constructionUnitCostIndex*Model.constructionUnitCostBase[id];

        return constructionUnitCostBase;
    }

    public void createOutputDataArray(int nVariables, int nPeriods) {
        outputData = new double[nVariables][nPeriods];
    }

    public boolean useStateInDirectPriceIndexRegression(int mode) {
        if (mode == 2) return false;
        return Model.stateInDirectPriceIndexRegression;
    }

}
