package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanContract implements HasID {

    private static int nextId;
    int id;

    //state variables

    Household debtor;
    Flat collateral;
    Bank bank;

    public double principal;
    public double renovationPrincipal;
    public int duration;
    public double monthlyInterestRate;
    public double payment;

    double bridgeLoanPrincipal;
    double bridgeLoanPrincipalOriginal;
    int bridgeLoanDuration;
    double bridgeLoanMonthlyInterestRate;
    Flat bridgeLoanCollateral;

    public boolean isNonPerforming = false;
    public int nNonPerformingPeriods;
    public boolean issuedInThisPeriod = false;

    double loss; //without bridgeLoan, principal when collateral becomes forcedSale

    boolean isHousingLoan = true;

    int nMonthsInterestPeriod = 12;
    int periodOfLastInterestCalculation = 0;
    double yearlyInterestSpread = 0.02;
    double yearlyBaseRateAtLastInterestCalculation = 0.009 + 0.005;

    int periodOfIssuance = -1;


    //derived variables

    double interestIncome;
    double lossIfBecomesForcedSale;
    double additionalLossOfPrincipalIncrease;
    double bankRevenueWhenForcedSaleSold;
    double healingPrincipal;
    double partialHealingPayment;
    double bankRevenueToPrincipalWhenForcedSaleSold;
    double lossWhenSellingCollateralOfPerformingLoan;

    double principalWhichShouldNotBeAccountedAsGrossFlow;
    double forSalePriceAtIssuance;




    public LoanContract() {
        id = LoanContract.nextId;
        LoanContract.nextId++;
    }

    public LoanContract(int id) {
        this.id = id;
        if (LoanContract.nextId < id + 1) LoanContract.nextId = id + 1;
    }


    public void nullMiscDerivedVariables() {
        interestIncome=0;
        lossIfBecomesForcedSale=0;
        additionalLossOfPrincipalIncrease=0;
        bankRevenueWhenForcedSaleSold=0;
        healingPrincipal=0;
        partialHealingPayment=0;
        bankRevenueToPrincipalWhenForcedSaleSold=0;
        lossWhenSellingCollateralOfPerformingLoan = 0;
        principalWhichShouldNotBeAccountedAsGrossFlow = 0;
    }

    public void endBridgeLoan() {
        bridgeLoanPrincipal *= 1 + bridgeLoanMonthlyInterestRate;
        if (bridgeLoanPrincipal>debtor.deposit-debtor.minDeposit) {
            double missingDeposit = bridgeLoanPrincipal-(debtor.deposit-debtor.minDeposit);
            principal += missingDeposit;
            restructureLoan();
            debtor.creditDeposit(missingDeposit);

        }

        debtor.chargeDeposit(bridgeLoanPrincipal);
        bank.addInterestIncome(Math.max(0,bridgeLoanPrincipal-bridgeLoanPrincipalOriginal));
        bridgeLoanPrincipal = 0;
        bridgeLoanMonthlyInterestRate = 0;
        bridgeLoanDuration = 0;
        bridgeLoanCollateral.setLoanContract(null);
        bridgeLoanCollateral = null;

    }

    public void endLoanContract() {

        if (bridgeLoanPrincipal > 0) {
            endBridgeLoan();
        }



        double interestPayment = principal * monthlyInterestRate;
        if (isHousingLoan && collateral.isForcedSale) {
            bankRevenueWhenForcedSaleSold = Math.min(debtor.deposit,principal);
            bankRevenueToPrincipalWhenForcedSaleSold = bankRevenueWhenForcedSaleSold/principal;
            bank.loss -= bankRevenueWhenForcedSaleSold;
            bank.lossOnHousingLoans -= bankRevenueWhenForcedSaleSold;
            debtor.chargeDeposit(Math.min(debtor.deposit,principal));

        } else {
            if (debtor.deposit<principal) {
                lossWhenSellingCollateralOfPerformingLoan = principal - debtor.deposit;
                bank.loss += principal - debtor.deposit;
                if (isHousingLoan) {
                    bank.lossOnHousingLoans += principal - debtor.deposit;
                }
                debtor.deposit = 0;
            } else {
                debtor.chargeDeposit(principal);
            }
        }
        bank.addInterestIncome(Math.min(debtor.deposit,interestPayment));
        if (debtor.deposit > interestPayment) {
            debtor.chargeDeposit(interestPayment);
        } else {
            debtor.deposit = 0;
        }


        if (isHousingLoan) {
            collateral.setLoanContract(null);
            collateral.setForcedSale(false);
            if (collateral.isForSale == false) collateral.nForSalePeriods = 0;
        }

        bank.getLoanContracts().remove(id);
        debtor.getLoanContracts().remove(this);
        bank.loanTotal -= principal;

        if (debtor.deposit<0) debtor.deposit = 0;

        deleteLoanContract();

    }

    public void deleteLoanContract() {
        Model.loanContracts.remove(id);
        Model.nLoanContracts--;
        Model.loanContractsForParallelComputing.get(id % Model.nThreads).remove(id);
    }

    public void monthlyPayment() {

        if (bridgeLoanPrincipal>0) bridgeLoanPrincipal *= 1 + bridgeLoanMonthlyInterestRate;
        if (bridgeLoanDuration >= 1) {
            bridgeLoanDuration--;
            if (bridgeLoanDuration == 0) restructureLoanContractWithBridgeLoanPrincipal();
        }

        if (debtor.isInMoratory && issuedInThisPeriod==false) {
            principal *= 1 + monthlyInterestRate;
            bridgeLoanPrincipal *= 1 + bridgeLoanMonthlyInterestRate;
            return;
        }

        if (debtor.deposit >= payment && isNonPerforming == false) {
            normalPayment();

        } else {
            isNonPerforming = true;
            nonPerformingPayment();
        }


    }

    private void normalPayment() {
        debtor.chargeDeposit(payment);
        double decreaseInPrincipal = payment - principal * monthlyInterestRate;
        if (renovationPrincipal>0) renovationPrincipal -= renovationPrincipal/principal * decreaseInPrincipal;
        principal -= decreaseInPrincipal;

        bank.loanTotal -= decreaseInPrincipal;
        duration--;

        interestIncome = principal * monthlyInterestRate;
        bank.addInterestIncome(principal * monthlyInterestRate);

        if (duration == 0) payment = 0;
        if (duration == 0 && bridgeLoanDuration == 0) endLoanContract();

    }

    private void nonPerformingPayment() {

        if (debtor.deposit > payment && (isHousingLoan==false || collateral.isForcedSale == false)) {
            isNonPerforming = false;
            nNonPerformingPeriods = 0;
            if (isHousingLoan) {
                collateral.setForcedSale(false);
                if (collateral.isForSale == false) collateral.nForSalePeriods = 0;
            }

            if (loss>0) {
                healingPrincipal = principal;
                bank.loss -= principal;
                if (isHousingLoan) bank.lossOnHousingLoans -= principal;
            }
            loss = 0;

            normalPayment();


        } else {

            if (nNonPerformingPeriods>3) debtor.lastNonPerformingPeriod = Model.period;

            double toPay = Math.min(debtor.deposit, payment);
            double interestPayment = principal * monthlyInterestRate;
            interestIncome = Math.min(toPay,interestPayment);
            bank.addInterestIncome(Math.min(toPay,interestPayment));
            double decreaseInPrincipal = toPay - interestPayment;
            if (renovationPrincipal>0) renovationPrincipal -= renovationPrincipal/principal * decreaseInPrincipal;
            principal -= decreaseInPrincipal;

            if (decreaseInPrincipal<0) {
                bank.loss += -decreaseInPrincipal;
                additionalLossOfPrincipalIncrease = -decreaseInPrincipal;
                if (isHousingLoan) bank.lossOnHousingLoans += -decreaseInPrincipal;
            } else {
                bank.loss -= decreaseInPrincipal;
                if (isHousingLoan) bank.lossOnHousingLoans -= decreaseInPrincipal;
                partialHealingPayment = decreaseInPrincipal;
            }
            bank.loanTotal -= decreaseInPrincipal;
            debtor.chargeDeposit(toPay);

            duration--;
            payment = bank.calculatePayment(principal, monthlyInterestRate, duration);

            if (principal<=0) {
                duration=0;
                isNonPerforming = false;
                nNonPerformingPeriods = 0;
                if (isHousingLoan) {
                    collateral.setForcedSale(false);
                    if (collateral.isForSale == false) collateral.nForSalePeriods = 0;
                }


                if (bridgeLoanPrincipal<= 0) {
                    endLoanContract();
                }
                return;
            }

            if (duration == 0) duration = 1; //missing principal is to be paid in the next period
            payment = bank.calculatePayment(principal, monthlyInterestRate,duration);


            nNonPerformingPeriods++;

            if (nNonPerformingPeriods==Model.nNonPerformingPeriodsForOwnSale && collateral!=null && isHousingLoan) {
                collateral.isForSale = true;
            }

            if (nNonPerformingPeriods >= Model.nNonPerformingPeriodsForRestructuring && nNonPerformingPeriods<Model.nNonPerformingPeriodsForForcedSale && debtor.getWageIncome()>0) {
                restructureLoan();
            }

            if (nNonPerformingPeriods == Model.nNonPerformingPeriodsForForcedSale) {
                if (isHousingLoan) {
                    collateral.setForcedSale(true);
                    collateral.setForSale(false);

                    if (collateral.nPeriodsLeftForRent>1) collateral.setNPeriodsLeftForRent(1);
                    lossIfBecomesForcedSale += principal;

                }
                loss = principal;
                bank.loss += principal;
                if (isHousingLoan) bank.lossOnHousingLoans += principal;

            }

        }

    }

    void restructureLoanContractWithBridgeLoanPrincipal() {
        principal += bridgeLoanPrincipal;
        bridgeLoanPrincipal = 0;
        endBridgeLoan();
        restructureLoan();

    }

    void restructureLoan() {

        boolean PTIbinding = true;
        if (duration==0) duration = Model.minDurationForRestructuring;

        while (PTIbinding) {

            double totalLoan = 0;
            double totalPayment = 0;

            for (LoanContract loanContract : debtor.getLoanContracts()) {
                if (loanContract != this) {
                    totalLoan += loanContract.principal;
                    totalPayment += loanContract.payment;
                }
            }

            monthlyInterestRate = bank.calculateMonthlyInterestRate(principal, debtor, collateral, totalLoan, totalPayment, nMonthsInterestPeriod, false);
            payment = bank.calculatePayment(principal, monthlyInterestRate, duration);
            if ((payment + totalPayment) / debtor.potentialWageIncome <= bank.calculateDSTILimit(debtor)) {
                PTIbinding = false;
            } else {
                duration += Model.durationIncreaseInRestructuring;
                if (duration>=Model.maxDuration) {
                    duration=Model.maxDuration;
                    payment = bank.calculatePayment(principal, monthlyInterestRate, duration);
                    return;
                }
            }
        }

    }

    public static synchronized void changeDebtor(LoanContract loanContract, Household household) {
            loanContract.debtor.loanContracts.remove(loanContract);
            loanContract.debtor = household;
            household.loanContracts.add(loanContract);
    }

    public void changeInterestRateIfNeeded() {

        if (Model.period == nMonthsInterestPeriod + periodOfLastInterestCalculation) {

            monthlyInterestRate = monthlyInterestRate + (Model.yearlyBaseRate - yearlyBaseRateAtLastInterestCalculation)/12;
            periodOfLastInterestCalculation = Model.period;
            yearlyBaseRateAtLastInterestCalculation = Model.yearlyBaseRate;
            payment = bank.calculatePayment(principal,monthlyInterestRate,duration);

        }
    }

    public boolean isNewlyIssuedZOP() {
        return collateral.isZOP && Model.period>=Model.introductionOfZOP && issuedInThisPeriod && Model.ZOPLimitReached==false;
    }

}
