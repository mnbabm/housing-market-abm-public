package model;

import util.DataLoader;
import util.InputDataGenerator;
import util.OwnStopper;
import util.ScaleCsvGenerator;

import java.io.File;

public class MainRun {

    public static int runMode = 1; //1-multipleRuns, 2-results, 3-printGivenSeries for outputDataFiles in a given directory, 4-inputDataGeneration, 5-generateScaledCsvs
    public static String configFileName = "src/main/java/sampleResources/configInternational.properties";
    public static String pathNameForOutputDataCsv = "src/main/java/savedCSVs/outputDataFTBLTV85_v1.csv";

    public static String householdsFileName = "src/main/java/sampleResources/households.csv";
    public static String individualsFileName = "src/main/java/sampleResources/individuals.csv";
    public static String flatsFileName = "src/main/java/sampleResources/flats.csv";
    public static String loanContractsFileName = "src/main/java/sampleResources/loanContracts.csv";
    public static String externalDemandFileName = "src/main/java/sampleResources/externalDemand.csv";
    public static String priceRegressionFileName = "src/main/java/sampleResources/priceRegression.csv";
    public static String newlyBuiltFileName = "src/main/java/sampleResources/newlyBuilt.csv";
    public static String priceIndexFileName = "src/main/java/sampleResources/priceIndex.csv";
    public static String transactionsFileName = "src/main/java/sampleResources/transactions.csv";
    public static String absoluteUtilityParametersFileName = "src/main/java/sampleResources/absoluteUtilityParameters.csv";

    public static String marriageDataFile = "src/main/java/sampleResources/marriageData.csv";
    public static String birthProbabilityFile = "src/main/java/sampleResources/birthProbability.csv";
    public static String marriageProbabilityFile = "src/main/java/sampleResources/marriageProbability.csv";
    public static String deathProbabilityFile = "src/main/java/sampleResources/deathProbability.csv";
    public static String macroPathFile = "src/main/java/sampleResources/macroPath.csv";

    public static int nOutputDataSeriesModel = 3000;
    public static int nOutputDataSeriesBucket = 100;
    public static int nOutputDataSeriesNeighbourhood = 100;
    public static int nOutputDataSeriesGeoLocation = 3000;

    public static boolean writeOutputDataWithBuckets = false;

    public static void main(String[] args) {

        System.out.println("START");

        ParametersToOverride parametersToOverride = new ParametersToOverride();

        if (runMode==1) {
            multipleRuns();
        } else if (runMode==2) {
            Model.parametersToOverride = parametersToOverride;
            Model.setup(configFileName);
            Model.loadOutputDataCsv(pathNameForOutputDataCsv);
            Model.printGivenSeriesToCsv();
        } else if (runMode==3) {

            Model.parametersToOverride = parametersToOverride;
            String dirForCsvs = "C:/idea-projects/outputfiles/";
            File[] outputDataFilesForPrintGivenSeries = new File("src/main/java/savedCSVs").listFiles();
            for (File file : outputDataFilesForPrintGivenSeries) {

                pathNameForOutputDataCsv = file.toString();

                if (file.getName().length()>=10 && file.getName().regionMatches(false,0,"outputData",0,9)) {
                    System.out.println(file.toString());
                    Model.csvNameForPrintGivenSeriesToCsv = dirForCsvs + file.getName();
                    Model.setup(configFileName);
                    Model.loadOutputDataCsv(pathNameForOutputDataCsv);
                    Model.printGivenSeriesToCsv();
                } else {

                }

            }

            System.out.println("DONE");

        } else if (runMode == 4) {
            InputDataGenerator.inputDataGeneration();
        } else if (runMode == 5) {
            OwnStopper scaleStopper = new OwnStopper();
            ScaleCsvGenerator scaleCsvGenerator = new ScaleCsvGenerator();
            scaleCsvGenerator.createCsvsFrom("","src/main/java/resources4000000",(int) (3876966.0*1.0));
            scaleStopper.printElapsedTimeInMilliseconds("scaling: ");
        }


    }

    public static void multipleRuns() {


        int nRuns = 1;
        int nFirstRun = 1;
        for (int i = nFirstRun; i < nFirstRun + nRuns; i++) {
            if (i>nFirstRun) Model.clearModelVariables();

            System.gc();
            Model.period = 0;

            ParametersToOverride parametersToOverride = new ParametersToOverride();
            Model.parametersToOverride = parametersToOverride;

            parametersToOverride.LTVPath = Configuration.deriveStaticPath(0.8);

            int v = (i-1)%10 + 1;


            if (i>0 && i<=10) {
                parametersToOverride.firstBuyerAdditionalLTV = 0.05;
                parametersToOverride.outputDataName = "FTBLTV85_v" + v;
            } else if (i<=20) {
                 parametersToOverride.firstBuyerAdditionalLTV = 0.1;
                 parametersToOverride.outputDataName = "FTBLTV90_v" + v;
            }


            System.out.println(parametersToOverride.outputDataName);

            Model.numberOfRun = i;
            Model.parametersToOverride = parametersToOverride;

            Model.setup(configFileName);
            Model.runPeriods();

            Model.writeOutputDataCsv();
        }

    }


}
