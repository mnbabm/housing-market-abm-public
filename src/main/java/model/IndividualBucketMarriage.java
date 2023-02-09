package model;

import util.OwnFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class IndividualBucketMarriage extends GeneralBucket {

    public static double[] firstWageIntervals = {0, 100000, 200000, 300000, 400000, 600000, 800000, 2000000};
    public static int[] type = {0, 1, 2};
    public static int[] ageInPeriods = {0, 12, 24, 36, 48, 60, 72, 84, 96, 108, 120,
            132, 144, 156, 168, 180, 192, 204, 216, 228, 240,
            252, 264, 276, 288, 300, 312, 324, 336, 348, 360,
            372, 384, 396, 408, 420, 432, 444, 456, 468, 480,
            492, 504, 516, 528, 540, 552, 564, 576, 588, 600,
            612, 624, 636, 648, 660, 672, 684, 696, 708, 72000};

    long code;
    List<Individual> individualsInBucket = new ArrayList<>();

    public static ConcurrentHashMap<Long, IndividualBucketMarriage> buckets = new ConcurrentHashMap<>();



    IndividualBucketMarriage(long code) {
        this.code = code;
    }


    public static long getCodeForIndividual(Individual individual) {
        long code = 0;
        long actCode;
        int codeMultiplier = 1;

        actCode = getCodeForInterval(firstWageIntervals, individual.getFirstWage());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;

        actCode = getCodeForInterval(ageInPeriods, individual.ageInPeriods);
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 100;

        actCode = getCodeForDiscreteValue(type, individual.getTypeIndex());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;


        actCode = getCodeForDiscreteValue(geoLocations, individual.getPreferredGeoLocation());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;

        actCode = 0;
        if (individual.isMale == false) {
            actCode = 1;
        }
        code += actCode * codeMultiplier;
        codeMultiplier *= 10;

        buckets.putIfAbsent(code, new IndividualBucketMarriage(code));
        return code;

    }

    public static void addIndividualToProperBucket(Individual individual) {
        if (individual.isMale) return;
        if (individual.household != null && individual.household.children.size() != 0) return;
        if (individual.household != null && individual.household.members.size() == 2) return;

        long code = getCodeForIndividual(individual);
        buckets.get(code).individualsInBucket.add(individual);
    }

}

