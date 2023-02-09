package model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.*;

@Getter
@Setter
public class Bank implements HasID {

    public static int nextId;
    final int id;

    //state variables
    public double yearlyInterestRateRegressionConstant;
    public double yearlyInterestRateRegressionCoeffLnWageIncome;
    public double yearlyInterestRateRegressionCoeffLTV;
    public double yearlyInterestRateRegressionCoeffAgeCategory1;
    public double yearlyInterestRateRegressionCoeffAgeCategory2;
    public double bridgeLoanToValue;

    List<LoanContract> loanContractsList = new ArrayList<LoanContract>();
    double loanTotal;
    Map<Integer, LoanContract> loanContracts = new HashMap<>();

    //derived variables
    double DSTI;
    double LTV;

    double interestIncome;
    double loss;
    double lossOnHousingLoans;


    public Bank() {
        id = Bank.nextId;
        Bank.nextId++;


    }

    public Bank(int id) {
        this.id = id;
        if (Bank.nextId < id + 1) Bank.nextId = id + 1;
    }

    void setBankStrategyParameters() {
        DSTI = Model.DSTI;
        LTV = Model.LTV;
    }

    void nullMiscDerivedVariables() {
        interestIncome = 0;
        loss = 0;
        lossOnHousingLoans = 0;
    }


    double calculatePayment(double loan, double monthlyInterestRate, double duration) {
        double payment = loan * (monthlyInterestRate / (1 - Math.pow(1 + monthlyInterestRate, -duration)));
        return payment;
    }

    LoanContractOffer loanContractOffer(Household household, Flat flat, double loan) {

        LoanContractOffer loanContractOffer = new LoanContractOffer();

        if (household.getWageIncome()==0 || household.hasNonPerformingLoan || household.lastNonPerformingPeriod>Model.period-Model.nPeriodsInNegativeKHR || flat.isForcedSale) {
            loanContractOffer.invalid = true;
            return loanContractOffer;
        }



        double totalLoan = 0;
        double totalPayment = 0;

        for (LoanContract loanContract : household.getLoanContracts()) {
            totalLoan += loanContract.principal;
            totalPayment += loanContract.payment;
        }

        if (household.babyloan.isIssued) totalPayment += household.babyloan.payment;

        double maxLoanWithoutBridgeLoan=calculateMaxLoanForLoanContractOffer(household, flat, totalLoan, totalPayment, Model.maxDuration);

        double principal = 0;
        double bridgeLoanPrincipal = 0;

        if (Model.phase != Model.Phase.RENOVATEFLATS && household.canGetBridgeLoanInPeriod && household.home != null && flat != household.home && (Model.phase==Model.Phase.FICTIVECHOICEOFHOUSEHOLDS || Model.phase == Model.Phase.HOUSEHOLDPURCHASES || Model.phase == Model.Phase.CAHAICALCULATIONS) ) {
            Flat home = household.home;
            LoanContract homeLoanContract = home.loanContract;




            if (homeLoanContract == null) {
                bridgeLoanPrincipal = Math.min(loan, household.home.getMarketPrice() * bridgeLoanToValue);
                if (flatEligibleForZOP(flat)) {
                    bridgeLoanPrincipal = Math.max(0,loan-maxLoanWithoutBridgeLoan);
                }
            }

            if (homeLoanContract != null && homeLoanContract.bridgeLoanPrincipal == 0 && homeLoanContract.principal < home.getEstimatedMarketPrice() * bridgeLoanToValue) { //if the outstanding principal is greater than the maximum allowable bridge loan, then no bridge loan is issued
                double refinancingLoan = homeLoanContract.principal * (1 + homeLoanContract.monthlyInterestRate);
                loan += refinancingLoan;

                bridgeLoanPrincipal = Math.min(loan, household.home.getEstimatedMarketPrice() * bridgeLoanToValue);
                if (flatEligibleForZOP(flat)) {
                    bridgeLoanPrincipal = Math.max(0,loan-maxLoanWithoutBridgeLoan);
                }
                totalLoan -= refinancingLoan;
                totalPayment -= home.loanContract.payment;
            }

        }
        principal = loan - bridgeLoanPrincipal;
        double bridgeLoanMonthlyInterestRate = calculateBridgeLoanMonthlyInterestRate(bridgeLoanPrincipal, household, flat);
        int bridgeLoanDuration = Model.bridgeLoanDuration;


        int duration = Model.maxDuration;
        double monthlyInterestRate = calculateMonthlyInterestRate(principal, household, flat, totalLoan, totalPayment, household.nMonthsInterestPeriodIfAskingForLoan, false);
        double payment = calculatePayment(principal, monthlyInterestRate, duration);

        if (Model.phase==Model.Phase.CAHAICALCULATIONS) {
            loanContractOffer.maxLoanLTVConstraint = calculateMaxLoanLTVConstraint(household,flat);
            loanContractOffer.maxLoanDSTIConstraint = calculateMaxLoanDSTIConstraint(household,flat,totalLoan,totalPayment,duration);
            loanContractOffer.maxLoanConsumptionConstraint = calculateMaxLoanConsumptionConstraint(household,flat,totalLoan,totalPayment,duration);
        }


        if (appropriateDSTI(household,payment,totalPayment)==false) {
            loanContractOffer.invalid = true;
            return loanContractOffer;
        }

        if (Model.phase != Model.Phase.RENOVATEFLATS && principal > calculateMaxLoanLTVConstraint(household, flat)) {
            loanContractOffer.invalid = true;
            return loanContractOffer;
        }

        loanContractOffer.bridgeLoanPrincipal = bridgeLoanPrincipal;
        loanContractOffer.bridgeLoanMonthlyInterestRate = bridgeLoanMonthlyInterestRate;
        loanContractOffer.bridgeLoanDuration = bridgeLoanDuration;

        loanContractOffer.principal = principal;
        loanContractOffer.monthlyInterestRate = monthlyInterestRate;
        if (Model.phase != Model.Phase.RENOVATEFLATS) {
            loanContractOffer.duration = duration;
        } else {
            loanContractOffer.duration = Model.renovationLoanDuration;
        }
        loanContractOffer.payment = payment;

        loanContractOffer.nMonthsInterestPeriod = household.nMonthsInterestPeriodIfAskingForLoan;
        if (flatEligibleForZOP(flat)) loanContractOffer.nMonthsInterestPeriod = Model.maxDuration;
        loanContractOffer.yearlyInterestSpread = calculateYearlyInterestSpread(loanContractOffer.nMonthsInterestPeriod);
        loanContractOffer.periodOfLastInterestCalculation = Model.period;
        loanContractOffer.yearlyBaseRateAtLastInterestCalculation = Model.yearlyBaseRate;

        loanContractOffer.bank = this;

        loanContractOffer.maxLoan = calculateMaxLoanForLoanContractOffer(household, flat, totalLoan, totalPayment, duration);



        return loanContractOffer;
    }

    double calculateMaxLoanForLoanContractOffer(Household household, Flat flat, double totalLoan, double totalPayment, int duration) {

        double maxLoanDSTIConstraint = calculateMaxLoanDSTIConstraint(household,flat,totalLoan,totalPayment,duration);
        double maxLoanLTVConstraint = calculateMaxLoanLTVConstraint(household,flat);
        double maxLoanConsumptionConstraint = calculateMaxLoanConsumptionConstraint(household,flat,totalLoan,totalPayment,duration);


        return Math.min(maxLoanConsumptionConstraint,Math.min(maxLoanDSTIConstraint,maxLoanLTVConstraint));
    }
    double calculateMaxLoanLTVConstraint(Household household, Flat flat) {
        if (Model.phase==Model.Phase.CAHAICALCULATIONS) {
            double maxLoan = flat.getMarketPrice() * LTVForHousehold(household,flat);
            if (flat.isNewlyBuilt) maxLoan = flat.forSalePrice * discountFactorForFlatUnderConstruction(flat) * LTVForHousehold(household,flat);
            return Math.min(maxLoan,household.depositAvailableForFlat(flat)/(1-LTVForHousehold(household)));
        }
        if (flat.isNewlyBuilt) {
            return flat.forSalePrice * discountFactorForFlatUnderConstruction(flat) * LTVForHousehold(household,flat);
        } else return flat.getMarketPrice() * LTVForHousehold(household,flat);
    }
    double calculateMaxLoanDSTIConstraint(Household household, Flat flat, double totalLoan, double totalPayment, int duration) {
        double interestRateForMaxLoanCalculation = calculateMonthlyInterestRate(0, household, flat, totalLoan, totalPayment, household.nMonthsInterestPeriodIfAskingForLoan, true);
        double paymentForUnitLoan = calculatePayment(1, interestRateForMaxLoanCalculation, duration);
        double maxPayment = household.householdIncome * calculateDSTILimit(household);

        double maxPaymentPrecision = 100;
        double range = maxPayment + 1;
        while (range>maxPaymentPrecision) {
            range /= 2;
            if (DSTIConditonBreached(household,maxPayment,totalPayment)) {
                maxPayment -= range;
            } else {
                maxPayment += range;
            }

        }
        maxPayment -= maxPaymentPrecision;
        return maxPayment / paymentForUnitLoan;
    }

    double calculateMaxLoanConsumptionConstraint(Household household, Flat flat, double totalLoan, double totalPayment, int duration) {
        double interestRateForMaxLoanCalculation = calculateMonthlyInterestRate(0, household, flat, totalLoan, totalPayment, household.nMonthsInterestPeriodIfAskingForLoan, true);
        double paymentForUnitLoan = calculatePayment(1, interestRateForMaxLoanCalculation, duration);
        double maxPayment = household.householdIncome;

        double maxPaymentPrecision = 100;
        double range = maxPayment + 1;
        while (range>maxPaymentPrecision) {
            range /= 2;
            if (ConsumptionPrescriptionBreached(household,maxPayment,totalPayment)) {
                maxPayment -= range;
            } else {
                maxPayment += range;
            }

        }
        maxPayment -= maxPaymentPrecision;
        return maxPayment / paymentForUnitLoan;
    }


    double calculateBridgeLoanMonthlyInterestRate(double bridgeLoan, Household household, Flat flat) {
        return Model.yearlyBaseRate/12;
    }

    double calculateMonthlyInterestRate(double loan, Household household, Flat flat, double totalLoan, double totalPayment, double nMonthsFixation, boolean maxLTV) {

        double lnWageIncome = Math.log(household.getWageIncome());
        if (household.getWageIncome()==0) lnWageIncome = Math.log(household.getPotentialWageIncome());
        double loanToValue = 1;
        if (Model.phase != Model.Phase.RENOVATEFLATS && flat!=null && maxLTV == false) {
            loanToValue = loan / flat.getMarketPrice();
        } else loanToValue = LTV;
        int ageOfOldestMember = household.calculateAgeOfOldestMember();

        double yearlyInterestRate = Model.yearlyBaseRate + yearlyInterestRateRegressionConstant + Model.yearlyInterestRateRegressionConstantDeviation + yearlyInterestRateRegressionCoeffLnWageIncome * lnWageIncome + yearlyInterestRateRegressionCoeffLTV * loanToValue;

        yearlyInterestRate += calculateYearlyInterestSpread(household.nMonthsInterestPeriodIfAskingForLoan);

        if (ageOfOldestMember>=Model.ageCategory1StartingAgeInPeriods) {
            if (ageOfOldestMember<Model.ageCategory2StartingAgeInPeriods) {
                yearlyInterestRate += yearlyInterestRateRegressionCoeffAgeCategory1;
            } else {
                yearlyInterestRate += yearlyInterestRateRegressionCoeffAgeCategory2;
            }
        }



        if (yearlyInterestRate < Model.minYearlyInterestRate) yearlyInterestRate = Model.minYearlyInterestRate;
        if (yearlyInterestRate > Model.maxYearlyInterestRate) yearlyInterestRate = Model.maxYearlyInterestRate;

        if (flat!= null && flatEligibleForZOP(flat)) yearlyInterestRate = Math.min(Model.ZOPMonthlyInteresRate*12,yearlyInterestRate);

        return yearlyInterestRate / 12;


    }

    public boolean appropriateDSTI(Household household, double payment, double totalPayment) {

        if (DSTIConditonBreached(household,payment,totalPayment) || ConsumptionPrescriptionBreached(household,payment,totalPayment)) {
            return false;
        } else return true;
    }

    public boolean DSTIConditonBreached(Household household, double payment, double totalPayment){
        if ((payment + totalPayment) / household.householdIncome > calculateDSTILimit(household)) {
            return true;
        } else return false;
    }

    public boolean ConsumptionPrescriptionBreached(Household household, double payment, double totalPayment) {
        double unemploymentRateToUse = Model.unemploymentRates[household.getTypeOfMemberWihLowerEducationalLevel()];
        if (household.householdIncome * (1 - Model.unemploymentRateAdjusterInConsumptionPrescription*unemploymentRateToUse)<household.minConsumptionLevel+payment+totalPayment) {
            return true;
        } else return false;
    }

    public double LTVForHousehold(Household household) {

        if (!household.firstBuyer) {
            return LTV;
        } else {

            return LTV + Model.firstBuyerAdditionalLTV;
        }
    }

    public double LTVForHousehold(Household household, Flat flat) {
        double ltv = LTVForHousehold(household);
        if (Model.period>=Model.startOfFirstBuyerZOP && Bank.flatEligibleForZOP(flat)) {

            if (household.consideredZOPFirstBuyer()) {
                ltv += Model.firstBuyerAdditionalLTVForZOP;
            }
        }

        return ltv;
    }

    public boolean increaseLoanForRenovation(LoanContract loanContract, double loan) {
        Household household = loanContract.debtor;

        double totalLoan = 0;
        double totalPayment = 0;

        for (LoanContract actLoanContract : household.getLoanContracts()) {
            if (actLoanContract == loanContract) continue;
            totalLoan += actLoanContract.principal;
            totalPayment += actLoanContract.payment;
        }

        double increasedLoan = loanContract.principal + loan;
        if (loanContract.issuedInThisPeriod == false) increasedLoan += loanContract.principal * loanContract.monthlyInterestRate;

        if (increasedLoan / loanContract.collateral.forSalePrice > LTV) return false;

        double monthlyInterestRate = calculateMonthlyInterestRate(increasedLoan, household, loanContract.collateral, totalLoan, totalPayment, loanContract.nMonthsInterestPeriod, false);
        double payment = calculatePayment(increasedLoan, monthlyInterestRate, loanContract.duration);
        int duration = loanContract.duration;

        if ((payment + totalPayment) / household.wageIncome > calculateDSTILimit(household)) {

            while (duration <= Model.maxDuration) {

                duration += Model.durationIncreaseInIncreaseLoanForRenovation;
                payment = calculatePayment(increasedLoan, monthlyInterestRate, duration);
                if ((payment + totalPayment) / household.wageIncome < calculateDSTILimit(household)) break;
            }

            if ((payment + totalPayment) / household.wageIncome > calculateDSTILimit(household)) return false;
        }

        household.creditDeposit(increasedLoan - loanContract.principal);

        if (loanContract.issuedInThisPeriod==false) loanContract.principalWhichShouldNotBeAccountedAsGrossFlow = loanContract.principal;
        loanContract.principal = increasedLoan;
        loanContract.renovationPrincipal += loan;
        loanContract.monthlyInterestRate = monthlyInterestRate;
        loanContract.payment = payment;
        loanContract.duration = duration;

        loanContract.issuedInThisPeriod = true;

        return true;


    }


    double discountFactorForFlatUnderConstruction(Flat flat) {
        return 1-flat.nPeriodsLeftForConstruction*Model.maxYearlyInterestRate/12;
    }

    double calculateYearlyInterestSpread(int nMonthsFixation) {
        if (nMonthsFixation<=12) {
            return Model.loanOneYearFixationSpread;
        } else if (nMonthsFixation<=60) {
            return Model.loanFiveYearFixationSpread;
        } else {
            return Model.loanFixedSpread;
        }
    }

    void addInterestIncome(double amount) {
        synchronized(this) {
            interestIncome += amount;
        }
    }

    public double calculateDSTILimit(Household household) {
        double firstBuyerAdditionalDSTI = 0;
        if (household.firstBuyer) firstBuyerAdditionalDSTI = Model.firstBuyerAdditionalDSTI;
        if (household.nMonthsInterestPeriodIfAskingForLoan>60) {
            if ((Model.period>=18 && household.wageIncome<500000) || (Model.period<18 && household.wageIncome<400000)) {
                return 0.5 + Model.additionalDSTI + firstBuyerAdditionalDSTI;
            } else {
                return 0.6 + Model.additionalDSTI + firstBuyerAdditionalDSTI;
            }
        } else if (household.nMonthsInterestPeriodIfAskingForLoan>12) {
            if ((Model.period>=18 && household.wageIncome<500000) || (Model.period<18 && household.wageIncome<400000)) {
                return 0.35 + Model.additionalDSTI + firstBuyerAdditionalDSTI;
            } else {
                return 0.4 + Model.additionalDSTI + firstBuyerAdditionalDSTI;
            }
        } else {
            if ((Model.period>=18 && household.wageIncome<500000) || (Model.period<18 && household.wageIncome<400000)) {
                return 0.25 + Model.additionalDSTI + firstBuyerAdditionalDSTI;
            } else {
                return 0.30 + Model.additionalDSTI + firstBuyerAdditionalDSTI;
            }
        }

    }

    public static boolean flatEligibleForZOP(Flat flat){
        return flat.isNewlyBuilt && flat.isZOP && Model.period>=Model.introductionOfZOP && Model.ZOPLimitReached==false;
    }


}
