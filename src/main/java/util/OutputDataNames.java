package util;

import model.Bucket;
import model.GeoLocation;
import model.Model;
import model.Neighbourhood;

public class OutputDataNames {

    static String[] l = new String[1];
    static String[] m = new String[1];
    static String[] n = new String[1];
    static String[] b = new String[1];


    public static void updateOutputDataNames() {

        m = new String[Model.outputData.length];
        for (int i = 0; i < m.length; i++) {
            m[i] = Integer.toString(i);
        }

        for (Neighbourhood neighbourhood : Model.neighbourhoods.values()) {
            n = new String[neighbourhood.outputData.length];
            for (int i = 0; i < n.length; i++) {
                n[i] = Integer.toString(i);;
            }
            break;
        }
        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            l = new String[geoLocation.outputData.length];
            for (int i = 0; i < l.length; i++) {
                l[i] = Integer.toString(i);;
            }
            break;
        }
        for (Bucket bucket : Model.buckets.values()) {
            b = new String[bucket.outputData.length];
            for (int i = 0; i < b.length; i++) {
                b[i] = Integer.toString(i);;
            }
            break;
        }

        m[10] = "nominalGDPLevel";
        m[11] = "realGDPLevel";
        m[12] = "priceLevel";
        m[13] = "nHouseholds";
        m[14] = "nIndividuals";


        m[100] = "elapsedTimePeriod";
        m[101] = "elapsedTimeBeginningOfPeriod";
        m[102] = "elapsedTimeFictiveDemandForNewlyBuiltFlats";
        m[103] = "elapsedTimeHouseholdPurchases";
        m[104] = "elapsedTimeRentalMarket";

        m[720] = "affordability - MR Young"; //Main region
        m[721] = "affordability - MR Middle";
        m[722] = "affordability - RC Young"; //Rest of the country
        m[723] = "affordability - RC Middle";



        for (int i = 1301; i <= 1300 + 10 * Model.affordabilityAgeIntervals.length/2; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            int categoryIndex = (int) Math.floor((i/10)) + 1;
            int decile = i % 10;
            if (decile == 0) decile = 10;
            stringBuilder.append("affordabilityC" + categoryIndex + "D" + decile);
            m[i] = stringBuilder.toString();
        }

        for (int i = 2001; i <= 2200; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            if (i<=2010) {
                stringBuilder.append("HLCNum"); //HLC stands for Housing loan contract
            } else if (i<=2020) {
                stringBuilder.append("HLCVolume");
            } else if (i<=2030) {
                stringBuilder.append("HNPLNum");
            } else if (i<=2040) {
                stringBuilder.append("HNPLVolume");
            } else if (i<=2050) {
                stringBuilder.append("HNPLRatioNum");
            } else if (i<=2060) {
                stringBuilder.append("HNPLRatioVolume");
            } else if (i<=2070) {
                stringBuilder.append("HLCNumToAllLCNum");
            } else if (i<=2080) {
                stringBuilder.append("HLCVolumeToAllLCVolume");
            } else if (i<=2090) {
                stringBuilder.append("HNPLNumToAllNPLNum");
            } else if (i<=2100) {
                stringBuilder.append("HNPLVolumeToAllNPLVolume");
            } else if (i<=2110) {
                stringBuilder.append("NewHLCNum");
            } else if (i<=2120) {
                stringBuilder.append("NewHLCVolume");
            } else if (i<=2130) {
                stringBuilder.append("NewHNPLNum");
            } else if (i<=2140) {
                stringBuilder.append("NewHNPLVolume");
            } else if (i<=2150) {
                stringBuilder.append("NewHNPLRatioNum");
            } else if (i<=2160) {
                stringBuilder.append("NewHNPLRatioVolume");
            } else if (i<=2170) {
                stringBuilder.append("NewHLCNumToAllNewLCNum");
            } else if (i<=2180) {
                stringBuilder.append("NewHLCVolumeToAllNewLCVolume");
            } else if (i<=2190) {
                stringBuilder.append("NewHNPLNumToAllNewNPLNum");
            } else if (i<=2200) {
                stringBuilder.append("NewHNPLVolumeToAllNewNPLVolume");
            }

            int decile = i % 10;
            if (decile == 0) decile = 10;
            stringBuilder.append("D" + decile);
            m[i] = stringBuilder.toString();
        }

        l[1] = "#transactions";
        l[2] = "sumSoldPrice";
        l[3] = "priceIndexToBeginning";
        l[5] = "nFlatsForSale";

        for (int i = 2400; i < 3000; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            int categoryIndex = (int) Math.floor((i/10)) + 1;
            int bucketCode = i % 100;
            StringBuilder bucketCodeStringBuilder = new StringBuilder(bucketCode);
            if (bucketCode<10) bucketCodeStringBuilder = new StringBuilder("0" + bucketCode);

            //CA stands for credit availability, for interpretation look for "CAHAI - CA (credit availability part)" in Model.java
            //WOL stands for "without loan" - so number of households in different buckets which can purchase the peer flat without loan
            //MISC stand for miscellaneous - so number of households in different buckets which would not loan to purchase the peer flat but are not granted a loan for miscellaneous reasons
            //LTV - LTV constraint is binding
            //DSTI - DSTI constraint is binding
            //CONS - prescribed consumption criterion is binding
            if (i<2500) {
                stringBuilder.append("CA_WOL_" + categoryIndex + "BC" + bucketCodeStringBuilder);
                if (i==2490) stringBuilder = new StringBuilder("CA_WOL_All");
            } else if (i<2600) {
                stringBuilder.append("CA_WL_" + categoryIndex + "BC" + bucketCodeStringBuilder);
                if (i==2590) stringBuilder = new StringBuilder("CA_WL_All");
            } else if (i<2700) {
                stringBuilder.append("CA_MISC_" + categoryIndex + "BC" + bucketCodeStringBuilder);
                if (i==2690) stringBuilder = new StringBuilder("CA_MISC_All");
            } else if (i<2800) {
                stringBuilder.append("CA_LTV_" + categoryIndex + "BC" + bucketCodeStringBuilder);
                if (i==2790) stringBuilder = new StringBuilder("CA_LTV_All");
            } else if (i<2900) {
                stringBuilder.append("CA_DSTI_" + categoryIndex + "BC" + bucketCodeStringBuilder);
                if (i==2890) stringBuilder = new StringBuilder("CA_DSTI_All");
            } else if (i<3000) {
                stringBuilder.append("CA_CONS_" + categoryIndex + "BC" + bucketCodeStringBuilder);
                if (i==2990) stringBuilder = new StringBuilder("CA_CONS_All");
            } else {

            }
            l[i] = stringBuilder.toString();
        }

        n[0] = "nFlats";
        n[1] = "nFlatsForSaleAtEndOfPeriod";
        n[2] = "nFlatsRentedByHouseholds";
        n[3] = "sumFlatSize";
        n[4] = "sumFlatState";

    }
}
