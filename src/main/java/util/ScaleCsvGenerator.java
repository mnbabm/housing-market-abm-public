package util;

import model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ScaleCsvGenerator {

    String inputDirName;
    String outputDirName;

    Map<Integer,Household> originalHouseholds = new HashMap<>();
    Map<Integer,Individual> originalIndividuals = new HashMap<>();
    Map<Integer,Flat> originalFLats = new HashMap<>();
    Map<Integer,LoanContract> originalLoanContracts = new HashMap<>();
    Map<Integer, UtilityParameterSet> originalUtilityParameterSets = new HashMap<>();

    Map<Integer,Household> newHouseholds = new HashMap<>();
    Map<Integer,Individual> newIndividuals = new HashMap<>();
    Map<Integer,Flat> newFLats = new HashMap<>();
    Map<Integer,LoanContract> newLoanContracts = new HashMap<>();
    Map<Integer,UtilityParameterSet> newUtilityParameterSets = new HashMap<>();

    int nextHouseholdId = 0;
    int nextIndividualId = 0;
    int nextFlatId = 0;
    int nextLoanContractId = 0;

    int nHouseholds;



    public void createCsvsFrom(String inputDirName, String outputDirName, int nHouseholds) {

        this.inputDirName = inputDirName;
        this.outputDirName = outputDirName;
        this.nHouseholds = nHouseholds;

        importOriginalHouseholds();
        importOriginalIndividuals();
        importOriginalFlats();
        importOriginalLoanContracts();
        importOriginalAbsoluteUtilityParameters();

        addHouseholdToParentIndividualsOriginal();
        addChildrenToHouseholdsOriginal();
        addUtilityParameterSetsToHouseholdsOriginal();

        createNewHouseholdsAndIndividualsAndFlatsAndLoanContracts();

        createHouseholdsCsv();
        createIndividualsCsv();
        createFlatsCsv();
        createLoanContractsCsv();
        createAbsoluteUtilityParametersCsv();

        createTransactionsCsv();
        createExternalDemandCsv();
    }

    public void importOriginalHouseholds() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.householdsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.householdsFileName))) scannerForCSV.nextLine();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");
                Household household = new Household();
                household.id = Integer.parseInt(dataLineArray[0]);
                household.individual1Id = Integer.parseInt(dataLineArray[1]);
                household.individual2Id = Integer.parseInt(dataLineArray[2]);
                household.flatId = Integer.parseInt(dataLineArray[3]);
                household.loanContractId = Integer.parseInt(dataLineArray[4]);
                household.parameterId = Integer.parseInt(dataLineArray[5]);

                if (household.individual1Id == 1) System.out.println("ennél: " + household.id);

                originalHouseholds.put(household.id,household);


            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("nOrigH: " + originalHouseholds.size());
    }

    public void importOriginalIndividuals() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.individualsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.individualsFileName))) scannerForCSV.nextLine();
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                Individual individual = new Individual();
                individual.id = Integer.parseInt(dataLineArray[0]);
                individual.gender = Integer.parseInt(dataLineArray[1]);
                individual.birthYear = Integer.parseInt(dataLineArray[2]);
                individual.region = Integer.parseInt(dataLineArray[3]);
                individual.county = Integer.parseInt(dataLineArray[4]);
                individual.jaras = Integer.parseInt(dataLineArray[5]);
                individual.type = Integer.parseInt(dataLineArray[6]);
                individual.firstWage = Double.parseDouble(dataLineArray[7]);
                individual.loanContractId = Integer.parseInt(dataLineArray[8]);
                individual.id2husband = Integer.parseInt(dataLineArray[9]);
                individual.id2wife = Integer.parseInt(dataLineArray[10]);
                individual.id2parent = Integer.parseInt(dataLineArray[11]);

                originalIndividuals.put(individual.id,individual);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void importOriginalFlats() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.flatsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.flatsFileName))) scannerForCSV.nextLine();
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                Flat flat = new Flat();
                flat.id = Integer.parseInt(dataLineArray[0]);
                flat.region = Integer.parseInt(dataLineArray[1]);
                flat.municipality_type = Integer.parseInt(dataLineArray[2]);
                flat.neighbourhoodQuality = Double.parseDouble(dataLineArray[3]);
                flat.size = Double.parseDouble(dataLineArray[4]);
                flat.state = Double.parseDouble(dataLineArray[5]);
                flat.stateCategory = Integer.parseInt(dataLineArray[6]);
                flat.neighbourhoodId = Integer.parseInt(dataLineArray[7]);
                flat.priceIndexedFor2014 = Double.parseDouble(dataLineArray[8]);
                flat.priceIndexedFor2015 = Double.parseDouble(dataLineArray[9]);
                flat.priceIndexedFor2018 = Double.parseDouble(dataLineArray[10]);
                flat.yearOfSale = Integer.parseInt(dataLineArray[11]);
                flat.yearOfConstruction = Integer.parseInt(dataLineArray[12]);
                flat.rented = Integer.parseInt(dataLineArray[13]);

                originalFLats.put(flat.id,flat);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void importOriginalLoanContracts() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.loanContractsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.loanContractsFileName))) scannerForCSV.nextLine();
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                LoanContract loanContract = new LoanContract();
                loanContract.id = Integer.parseInt(dataLineArray[0]);
                loanContract.principal = Double.parseDouble(dataLineArray[1]);
                loanContract.startingPrincipal = Double.parseDouble(dataLineArray[2]);
                loanContract.duration = Integer.parseInt(dataLineArray[3]);
                loanContract.payment = Double.parseDouble(dataLineArray[4]);
                loanContract.yearlyInterestRatePercent = Double.parseDouble(dataLineArray[5]);
                loanContract.isNonPerforming = Integer.parseInt(dataLineArray[6]);
                loanContract.nNonPerformingPeriods = Integer.parseInt(dataLineArray[7]);
                loanContract.adjustedFirstWageForFirstMember = Double.parseDouble(dataLineArray[8]);
                loanContract.yearOfIssuance = Integer.parseInt(dataLineArray[9]);
                loanContract.khr_id = Long.parseLong(dataLineArray[10]);
                loanContract.nMonthsInterestPeriod = Integer.parseInt(dataLineArray[11]);
                loanContract.periodOfLastInterestCalculation = Integer.parseInt(dataLineArray[12]);

                originalLoanContracts.put(loanContract.id,loanContract);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void importOriginalAbsoluteUtilityParameters() {
        try {
            Scanner scannerForCSV = new Scanner(new File(MainRun.absoluteUtilityParametersFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.absoluteUtilityParametersFileName))) scannerForCSV.nextLine();
            int originalId = 0;
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");


                UtilityParameterSet utilityParameterSet = new UtilityParameterSet();
                utilityParameterSet.id = ++originalId;

                utilityParameterSet.householdId = Integer.parseInt(dataLineArray[0]);
                utilityParameterSet.absCoeffSize = Double.parseDouble(dataLineArray[1]);
                utilityParameterSet.absExponentSize = Double.parseDouble(dataLineArray[2]);
                utilityParameterSet.absCoeffState = Double.parseDouble(dataLineArray[3]);
                utilityParameterSet.absExponentState = Double.parseDouble(dataLineArray[4]);
                utilityParameterSet.absSigmoid1 = Double.parseDouble(dataLineArray[5]);
                utilityParameterSet.absSigmoid2 = Double.parseDouble(dataLineArray[6]);

                originalUtilityParameterSets.put(utilityParameterSet.id,utilityParameterSet);

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addHouseholdToParentIndividualsOriginal() {
        for (Household household : originalHouseholds.values()) {
            originalIndividuals.get(household.individual1Id).household = household;
            if (originalIndividuals.get(household.individual2Id) != null) originalIndividuals.get(household.individual2Id).household = household;
        }
    }

    public void addChildrenToHouseholdsOriginal() {
        for (Individual individual : originalIndividuals.values()) {
            if (individual.id2parent > 0) {
                originalIndividuals.get(individual.id2parent).household.children.add(individual);
            }
        }
    }

    public void addUtilityParameterSetsToHouseholdsOriginal() {
        for (UtilityParameterSet utilityParameterSet : originalUtilityParameterSets.values()) {
            Household household = originalHouseholds.get(utilityParameterSet.householdId);
            household.utilityParameterSet = utilityParameterSet;
        }
    }

    public void createNewHouseholdsAndIndividualsAndFlatsAndLoanContracts() {
        double nNewEntriesToOriginal = (double) nHouseholds / (double) originalHouseholds.size();

        for (Household household : originalHouseholds.values()) {
            int nNewEntries = (int) Math.floor(nNewEntriesToOriginal);
            if (Model.rnd.nextDouble()<nNewEntriesToOriginal - Math.floor(nNewEntriesToOriginal)) nNewEntries++;
            for (int i = 0; i < nNewEntries; i++) {
                Household newHousehold = new Household();
                newHousehold.id = ++nextHouseholdId;

                Individual individual1 = new Individual();
                Individual individual2 = new Individual();

                individual1.id = ++nextIndividualId;
                newIndividuals.put(individual1.id,individual1);
                copyIndividualData(originalIndividuals.get(household.individual1Id),individual1);


                if (household.individual2Id>0) {
                    individual2.id = ++nextIndividualId;
                    newIndividuals.put(individual2.id,individual2);
                    copyIndividualData(originalIndividuals.get(household.individual2Id),individual2);
                }

                for (Individual child : household.children) {
                    Individual newChild = new Individual();
                    newChild.id = ++nextIndividualId;
                    newIndividuals.put(newChild.id,newChild);
                    copyIndividualData(originalIndividuals.get(child.id),newChild);
                    newChild.id2parent = individual1.id;
                }

                newHousehold.individual1Id = individual1.id;
                newHousehold.individual2Id = individual2.id;

                if (originalIndividuals.get(household.individual1Id).id2husband > 0) {
                    individual1.id2husband = individual2.id;
                    individual2.id2wife = individual1.id;
                } else if (originalIndividuals.get(household.individual1Id).id2wife > 0) {
                    individual1.id2wife = individual2.id;
                    individual2.id2husband= individual1.id;
                    System.out.println("AZT HITTEM, ELVBEN A FELESÉG VAN ELÖL");
                }

                Flat newFlat = new Flat();
                newFlat.id = ++nextFlatId;
                copyFlatData(originalFLats.get(household.flatId),newFlat);
                newFLats.put(newFlat.id,newFlat);

                newHousehold.flatId = newFlat.id;

                if (household.loanContractId >= 0) {
                    LoanContract newLoanContract = new LoanContract();
                    newLoanContract.id = ++nextLoanContractId;
                    copyLoanContractData(originalLoanContracts.get(household.loanContractId),newLoanContract);
                    newLoanContracts.put(newLoanContract.id,newLoanContract);
                    newHousehold.loanContractId = newLoanContract.id;
                }

                UtilityParameterSet newUtilityParameterSet = new UtilityParameterSet();
                newUtilityParameterSet.householdId = newHousehold.id;
                copyUtilityParameterSet(household.utilityParameterSet,newUtilityParameterSet);
                newUtilityParameterSets.put(newUtilityParameterSet.householdId,newUtilityParameterSet);

                newHouseholds.put(newHousehold.id,newHousehold);



            }
        }
    }

    public void createHouseholdsCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(outputDirName + "/households.csv", "UTF-8");

            for (Household household : newHouseholds.values()) {
                StringBuffer newRow = new StringBuffer();
                newRow.append(household.id + "," + household.individual1Id + "," + household.individual2Id + "," + household.flatId + "," + household.loanContractId);
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating householdsCsv");
        }
    }

    public void createIndividualsCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(outputDirName + "/individuals.csv", "UTF-8");

            for (Individual individual : newIndividuals.values()) {
                StringBuffer newRow = new StringBuffer();
                newRow.append(individual.id + "," + individual.gender + "," + individual.birthYear + "," + individual.region + "," + individual.county + "," + individual.jaras + "," + individual.type + "," + individual.firstWage + "," + individual.loanContractId + "," + individual.id2husband + "," + individual.id2wife + "," + individual.id2parent);
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating individualsCsv");
        }
    }

    public void createFlatsCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(outputDirName + "/flats.csv", "UTF-8");

            for (Flat flat : newFLats.values()) {
                StringBuffer newRow = new StringBuffer();
                newRow.append(flat.id + "," + flat.region + "," + flat.municipality_type + "," + flat.neighbourhoodQuality + "," + flat.size + "," + flat.state + "," + flat.stateCategory + "," + flat.neighbourhoodId + "," + flat.priceIndexedFor2014 + "," + flat.priceIndexedFor2015 + "," + flat.priceIndexedFor2018 + "," + flat.yearOfSale + "," + flat.yearOfConstruction + "," + flat.rented);
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating flatsCsv");
        }
    }

    public void createLoanContractsCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(outputDirName + "/loanContracts.csv", "UTF-8");

            for (LoanContract loanContract: newLoanContracts.values()) {
                StringBuffer newRow = new StringBuffer();
                newRow.append(loanContract.id + "," + loanContract.principal + "," + loanContract.startingPrincipal + "," + loanContract.duration + "," + loanContract.payment + "," + loanContract.yearlyInterestRatePercent + "," + loanContract.isNonPerforming + "," + loanContract.nNonPerformingPeriods + "," + loanContract.adjustedFirstWageForFirstMember + "," + loanContract.yearOfIssuance + "," + loanContract.khr_id + "," + loanContract.nMonthsInterestPeriod + "," + loanContract.periodOfLastInterestCalculation);
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating loanContractsCsv");
        }
    }

    public void createAbsoluteUtilityParametersCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(outputDirName + "/absoluteUtilityParameters.csv", "UTF-8");

            for (UtilityParameterSet utilityParameterSet: newUtilityParameterSets.values()) {
                StringBuffer newRow = new StringBuffer();
                newRow.append(utilityParameterSet.householdId + "," + utilityParameterSet.absCoeffSize + "," + utilityParameterSet.absExponentSize + "," + utilityParameterSet.absCoeffState + "," + utilityParameterSet.absExponentState + "," + utilityParameterSet.absSigmoid1 + "," + utilityParameterSet.absSigmoid2);
                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (Exception e) {
            System.out.println("Problem while generating absoluteUtilityParametersCsv");
        }
    }

    public void createTransactionsCsv() {
        try {

            PrintWriter printWriter = new PrintWriter(outputDirName + "/transactions.csv", "UTF-8");
            Scanner scannerForCSV = new Scanner(new File(MainRun.transactionsFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.transactionsFileName))) scannerForCSV.nextLine();

            double nNewEntriesToOriginal = (double) nHouseholds / (double) originalHouseholds.size();
            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();

                    int nNewEntries = (int) Math.floor(nNewEntriesToOriginal);
                    if (Model.rnd.nextDouble()<nNewEntriesToOriginal - Math.floor(nNewEntriesToOriginal)) nNewEntries++;
                    for (int i = 0; i < nNewEntries; i++) {
                        printWriter.println(dataLine);
                    }



            }

            printWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println("Problem while reading transactionsCsv");
        } catch (Exception e) {
            System.out.println("Problem while generating transactionCsv");
        }
    }

    public void createExternalDemandCsv() {
        try {
            PrintWriter printWriter = new PrintWriter(outputDirName + "/externalDemand.csv", "UTF-8");
            Scanner scannerForCSV = new Scanner(new File(MainRun.externalDemandFileName));
            if (OwnFunctions.csvHasHeaderLineWithNonNumericDataWithCommaSeparator(new File(MainRun.externalDemandFileName))) scannerForCSV.nextLine();

            double nNewEntriesToOriginal = (double) nHouseholds / (double) originalHouseholds.size();

            while (scannerForCSV.hasNextLine()) {
                String dataLine = scannerForCSV.nextLine();
                String[] dataLineArray = dataLine.split(",");

                StringBuffer newRow = new StringBuffer();
                newRow.append(dataLineArray[0] + "," + dataLineArray[1] + "," + dataLineArray[2]);
                for (int i = 3; i < dataLineArray.length; i++) {
                    int numberToAppend = (int) Math.round(Double.parseDouble(dataLineArray[i]) * nNewEntriesToOriginal);
                    newRow.append( "," + numberToAppend);
                }

                printWriter.println(newRow);
            }

            printWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println("Problem while reading externalDemandCsv");
        } catch (Exception e) {
            System.out.println("Problem while generating externalDemandCsv");
        }
    }


    class Household {

        int id = -1;
        int individual1Id;
        int individual2Id = -1;
        int flatId;
        int loanContractId = -1;
        int parameterId; //from absoluteUtilityParameters.csv
        ArrayList<Individual> children = new ArrayList<>();
        UtilityParameterSet utilityParameterSet;
    }

    class Individual {
        int id = -1;
        int gender;
        int birthYear;
        int region;
        int county;
        int jaras;
        int type;
        double firstWage;
        int loanContractId; //not used in DataLoader
        int id2husband;
        int id2wife;
        int id2parent;

        Household household; //used for parent individuals

    }

    class LoanContract {

        int id = -1;
        double principal;
        double startingPrincipal;
        int duration;
        double payment;
        double yearlyInterestRatePercent;
        int isNonPerforming;
        int nNonPerformingPeriods;
        double adjustedFirstWageForFirstMember;
        int yearOfIssuance;
        long khr_id;
        int nMonthsInterestPeriod;
        int periodOfLastInterestCalculation;
    }

    class Flat {
        int id = -1;
        int region;
        int municipality_type;
        double neighbourhoodQuality;
        double size;
        double state;
        int stateCategory;
        int neighbourhoodId;
        double priceIndexedFor2014;
        double priceIndexedFor2015;
        double priceIndexedFor2018;
        int yearOfSale;
        int yearOfConstruction;
        int rented;
    }

    class UtilityParameterSet {
        int id = -1;
        int householdId;
        double absCoeffSize;
        double absExponentSize;
        double absCoeffState;
        double absExponentState;
        double absSigmoid1;
        double absSigmoid2;
    }

    public void copyIndividualData(Individual originalIndividual, Individual newIndividual) {
        newIndividual.gender = originalIndividual.gender;
        newIndividual.birthYear = originalIndividual.birthYear;
        newIndividual.region = originalIndividual.region;
        newIndividual.county = originalIndividual.county;
        newIndividual.jaras = originalIndividual.jaras;
        newIndividual.type = originalIndividual.type;
        newIndividual.firstWage = originalIndividual.firstWage;
    }

    public void copyFlatData(Flat originalFlat, Flat newFlat) {
        newFlat.region = originalFlat.region;
        newFlat.municipality_type = originalFlat.municipality_type;
        newFlat.neighbourhoodQuality = originalFlat.neighbourhoodQuality;
        newFlat.size = originalFlat.size;
        newFlat.state = originalFlat.state;
        newFlat.stateCategory = originalFlat.stateCategory;
        newFlat.neighbourhoodId = originalFlat.neighbourhoodId;
        newFlat.priceIndexedFor2014 = originalFlat.priceIndexedFor2014;
        newFlat.priceIndexedFor2015 = originalFlat.priceIndexedFor2015;
        newFlat.priceIndexedFor2018 = originalFlat.priceIndexedFor2018;
        newFlat.yearOfSale = originalFlat.yearOfSale;
        newFlat.yearOfConstruction = originalFlat.yearOfConstruction;
        newFlat.rented = originalFlat.rented;
    }

    public void copyLoanContractData(LoanContract originalLoanContract, LoanContract newLoanContract) {
        newLoanContract.principal = originalLoanContract.principal;
        newLoanContract.startingPrincipal = originalLoanContract.startingPrincipal;
        newLoanContract.duration = originalLoanContract.duration;
        newLoanContract.payment = originalLoanContract. payment;
        newLoanContract.yearlyInterestRatePercent = originalLoanContract.yearlyInterestRatePercent;
        newLoanContract.isNonPerforming = originalLoanContract.isNonPerforming;
        newLoanContract.nNonPerformingPeriods = originalLoanContract.nNonPerformingPeriods;
        newLoanContract.adjustedFirstWageForFirstMember = originalLoanContract.adjustedFirstWageForFirstMember;
        newLoanContract.yearOfIssuance = originalLoanContract.yearOfIssuance;
        newLoanContract.khr_id = originalLoanContract.khr_id;
        newLoanContract.nMonthsInterestPeriod = originalLoanContract.nMonthsInterestPeriod;
        newLoanContract.periodOfLastInterestCalculation = originalLoanContract.periodOfLastInterestCalculation;
    }

    public void copyUtilityParameterSet(UtilityParameterSet originalUtilityParameterSet, UtilityParameterSet newUtilityParameterSet) {
        newUtilityParameterSet.absCoeffSize = originalUtilityParameterSet.absCoeffSize;
        newUtilityParameterSet.absExponentSize = originalUtilityParameterSet.absExponentSize;
        newUtilityParameterSet.absCoeffState = originalUtilityParameterSet.absCoeffState;
        newUtilityParameterSet.absExponentState = originalUtilityParameterSet.absExponentState;
        newUtilityParameterSet.absSigmoid1 = originalUtilityParameterSet.absSigmoid1;
        newUtilityParameterSet.absSigmoid2 = originalUtilityParameterSet.absSigmoid2;
    }
}
