package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanContractOffer {

    Bank bank;

    double principal;
    int duration;
    double monthlyInterestRate;
    double payment;

    double bridgeLoanPrincipal;
    int bridgeLoanDuration;
    double bridgeLoanMonthlyInterestRate;

    int nMonthsInterestPeriod;
    int periodOfLastInterestCalculation;
    double yearlyInterestSpread;
    double yearlyBaseRateAtLastInterestCalculation;

    double maxLoan;

    boolean invalid = false;
    double maxLoanDSTIConstraint;
    double maxLoanLTVConstraint;
    double maxLoanConsumptionConstraint;


}
