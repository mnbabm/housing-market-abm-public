package model;

import util.MappingWithWeights;
import util.OwnFunctions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class MarriageWomenBucket extends GeneralBucket {

    public static double[] firstWageIntervalsWomen = {0, 100000, 200000, 300000, 400000, 500000, 2000000000}; //!!Ha 10-nél több interval lesz, akkor a code-okat érintő részben át kell írni a helyiértékeket
    public static double[] firstWageIntervalsMen = {0, 100000, 200000, 300000, 400000, 500000, 600000, 800000, 1000000, 2000000000};  //!!Ha 10-nél több interval lesz, akkor a code-okat érintő részben át kell írni a helyiértékeket
    public static int[] type = {0, 1, 2};
    public static int[] ageDifference = {-5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

    long code;
    public MappingWithWeights<Long> menSampleBucketWeight = new MappingWithWeights<>();
    public HashMap<Long,double[]> characteristics = new HashMap<>();


    public static ConcurrentHashMap<Long,MarriageWomenBucket> marriageWomenBuckets = new ConcurrentHashMap<>();
    public static HashMap<Long, LinkedList<Individual>> menBuckets = new HashMap<>();

    MarriageWomenBucket(long code) {
        this.code = code;
    }

    public static void setup() {

        marriageWomenBuckets = new ConcurrentHashMap<>();
        menBuckets = new HashMap<>();

        setupGeoLocations();

        try {
            Scanner marriageDataScanner = new Scanner(Model.marriageDataFile);
            //1 (in csv) refers to 0 (in code type), 2 refers to 1, and 3 and 0 refer to 2
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.marriageDataFile))) marriageDataScanner.nextLine();
            while (marriageDataScanner.hasNextLine()) {
                String dataLine = marriageDataScanner.nextLine();
                String[] dataLineArray = dataLine.split(",");
                int typeWoman = Integer.parseInt(dataLineArray[1])-1;
                if (typeWoman==-1) typeWoman=2;
                int typeMan = Integer.parseInt(dataLineArray[4])-1;
                if (typeMan==-1) typeMan=2;
                int ageDifference = Integer.parseInt(dataLineArray[0])-Integer.parseInt(dataLineArray[3]);
                double firstWageWoman = Double.parseDouble(dataLineArray[2]);
                double firstWageMan = Double.parseDouble(dataLineArray[5]);


                long codeForWoman = getCodeForWoman(firstWageWoman,typeWoman);
                long codeForMan = getCodeForManSample(firstWageMan,typeMan,ageDifference);

                if (codeForMan==-1) continue;

                marriageWomenBuckets.putIfAbsent(codeForWoman, new MarriageWomenBucket(codeForWoman));
                marriageWomenBuckets.get(codeForWoman).menSampleBucketWeight.putIfAbsent(0,codeForMan);
                double[] characteristicsValues = {getCodeForInterval(firstWageIntervalsMen,firstWageMan),getCodeForDiscreteValue(type,typeMan),ageDifference};
                marriageWomenBuckets.get(codeForWoman).characteristics.putIfAbsent(codeForMan,characteristicsValues);

                marriageWomenBuckets.get(codeForWoman).menSampleBucketWeight.increaseWeightBy(1,codeForMan);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void refreshMenBuckets() {
        menBuckets.clear();
        for (Individual individual: Model.individuals.values()) {
            if (individual.manBucketCode != -1) {
                menBuckets.putIfAbsent(individual.manBucketCode,new LinkedList<>());
                menBuckets.get(individual.manBucketCode).add(individual);
            }
        }

    }

    public static long getCodeForWoman(Individual woman) {
        return getCodeForWoman(woman.firstWage,woman.typeIndex);
    }

    public static long getCodeForWoman(double firstWage, int type) {
        long code = 0;
        long actCode;
        int codeMultiplier = 1;

        actCode = getCodeForInterval(firstWageIntervalsWomen,firstWage);
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;


        actCode = getCodeForDiscreteValue(MarriageWomenBucket.type,type);
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;

        marriageWomenBuckets.putIfAbsent(code, new MarriageWomenBucket(code));


        return code;

    }

    public static long getCodeForManSample(double firstWage, int type, int ageDifference) {
        long code = 0;
        long actCode;
        int codeMultiplier = 1;

        actCode = getCodeForInterval(firstWageIntervalsMen,firstWage);
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else {
            return -1;
        }
        codeMultiplier *= 10;


        actCode = getCodeForDiscreteValue(MarriageWomenBucket.type,type);
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else {
            return -1;
        }
        codeMultiplier *= 100;

        actCode = getCodeForDiscreteValue(MarriageWomenBucket.ageDifference,ageDifference);
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else {
            return -1;
        }

        return code;
    }

    public static Individual findHusband(Individual woman) {

        Individual husband = null;

        long code = getCodeForWoman(woman);

        long selectedCode;
        long codeForSelectedBucket=-10;

        int maxDraws=110;
        int nDraws=0;

        while (menBuckets.get(codeForSelectedBucket)==null) {
            nDraws++;
            selectedCode = marriageWomenBuckets.get(code).menSampleBucketWeight.selectObjectAccordingToCumulativeProbability(Model.rnd.nextDouble());
            codeForSelectedBucket=getCodeForSelectedManBucket(selectedCode,woman);

            if (nDraws==maxDraws) return null;
        }

        if (menBuckets.get(codeForSelectedBucket).size()>0) {
            husband = menBuckets.get(codeForSelectedBucket).get(0);
            menBuckets.get(codeForSelectedBucket).remove(0);
            if (menBuckets.get(codeForSelectedBucket).size() == 0) {
                menBuckets.remove(codeForSelectedBucket);
            }
        } else {
            return null;
        }

        return husband;

    }




    public static long getCodeForMan(Individual man) {
        long code = 0;
        long actCode;
        int codeMultiplier = 1;

        actCode = getCodeForInterval(firstWageIntervalsMen,man.getFirstWage());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;


        actCode = getCodeForDiscreteValue(MarriageWomenBucket.type,man.getTypeIndex());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;

        actCode = getCodeForDiscreteValue(MarriageWomenBucket.geoLocations,man.getPreferredGeoLocation());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;

        int birthYear = man.calculateBirthYear();

        code += birthYear;

        return code;
    }

    public static long getCodeForSelectedManBucket(long selectedCode, Individual woman) {
        MarriageWomenBucket womanBucket = marriageWomenBuckets.get(getCodeForWoman(woman));
        double[] characteristicsValues = womanBucket.characteristics.get(selectedCode); //firstWageCode

        long code = 0;
        long actCode;
        int codeMultiplier = 1;

        actCode = (long) characteristicsValues[0]; //firstWage
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;


        actCode = (long) characteristicsValues[1]; //type
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;
        codeMultiplier *= 10;

        actCode = getCodeForDiscreteValue(MarriageWomenBucket.geoLocations,woman.getPreferredGeoLocation());
        if (actCode != -1) {
            code += actCode * codeMultiplier;
        } else return -1;

        int birthYear = woman.calculateBirthYear();

        int birthYearOfMan = birthYear-(int) characteristicsValues[2];

        code += birthYearOfMan;

        return code;
    }

}
