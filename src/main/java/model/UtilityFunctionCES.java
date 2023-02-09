package model;

import lombok.Getter;
import lombok.Setter;
import util.OwnFunctions;

import java.util.Arrays;

@Setter
@Getter
public class UtilityFunctionCES implements HasID, UtilityFunction {

    private static int nextId;
    final int id;

    //state variables
    public Household household;

    public double absExponentSize = 0.65;
    public double absExponentState = 0.7;
    public double absCoeffSize = 0.007;
    public double absCoeffState = 0.5;
    public double absSigmoid1 = 0.1;
    public double absSigmoid2 = 0.1;
    public double absCoeffStateHabit = 0;

    //for DataLoader
    public double sampleNeighbourhoodQuality;
    public double sampleLifeTimeIncome;

    //derived variables


    public UtilityFunctionCES() {
        id = UtilityFunctionCES.nextId;
        UtilityFunctionCES.nextId++;

        Model.utilityFunctionsCES.put(id, this);
        Model.nUtilityFunctionsCES++;
    }

    public UtilityFunctionCES(int id) {
        this.id = id;
        if (UtilityFunctionCES.nextId < id + 1) UtilityFunctionCES.nextId = id + 1;
        Model.utilityFunctionsCES.put(id, this);
        Model.nUtilityFunctionsCES++;
    }

    public void cloneParametersTo(UtilityFunctionCES utilityFunctionCES) {

        utilityFunctionCES.absCoeffSize = absCoeffSize;
        utilityFunctionCES.absCoeffState = absCoeffState;
        utilityFunctionCES.absExponentSize = absExponentSize;
        utilityFunctionCES.absExponentState = absExponentState;
        utilityFunctionCES.absSigmoid1 = absSigmoid1;
        utilityFunctionCES.absSigmoid2 = absSigmoid2;

    }

    public double calculateUtility(Flat flat) {

        double price = flat.forSalePrice;
        if (price == 0) price=flat.getMarketPrice();
        if (Model.phase == Model.Phase.FICTIVEDEMANDFORRENT || Model.phase == Model.Phase.RENTALMARKET) {
            price = flat.rent/Model.rentToPrice;
        }

        return calculateAbsoluteReservationPriceForFlat(flat) - price;

    }


    public double calculateAbsoluteReservationPriceForFlat(Flat flat) {

        if (household.canChangeGeoLocation==false && flat.getGeoLocation()!=household.preferredGeoLocation) return 0;
        if (flat.isForcedSale) return 0;

        double absoluteReservationPrice = (absCoeffSize * Math.pow(flat.size,absExponentSize) * (1 + absCoeffState * Math.pow(flat.state,absExponentState)) + flat.getGeoLocation().absCoeffStateHabit * Math.log((flat.state - flat.getNeighbourhood().averageState)/Model.highQualityStateMax+1)/Math.log(10) * flat.size + absSigmoid1/Math.pow(1+ Math.exp(-flat.bucket.neighbourhood.quality),1/absSigmoid2)) * household.lifeTimeIncome * Model.priceLevel * flat.getGeoLocation().cyclicalAdjuster;

        if (Model.phase == Model.Phase.RENTALMARKET || Model.phase == Model.Phase.FICTIVEDEMANDFORRENT) {
            return absoluteReservationPrice;
        }

        if (flat.isNewlyBuilt() && flat.isEligibleForCSOK()) absoluteReservationPrice += household.newlyBuiltCSOK;
        if (flat.isEligibleForFalusiCSOK()) absoluteReservationPrice += household.falusiCSOK;

        if (flat.isNewlyBuilt) {
                double newlyBuiltReservationPriceIncrease = (Model.newlyBuiltUtilityAdjusterCoeff1 + Model.newlyBuiltUtilityAdjusterCoeff2 * flat.getQuality()) * absCoeffSize * Math.pow(flat.size,absExponentSize) * household.lifeTimeIncome;
                if (flat.isZOP) {
                    newlyBuiltReservationPriceIncrease *= 1+Model.ZOPAdditionalUtility;
                    if (Model.banks.get(0).flatEligibleForZOP(flat)) {
                        absoluteReservationPrice += household.surplusIncreaseForZOPFlat(flat);

                    }
                }
                absoluteReservationPrice += newlyBuiltReservationPriceIncrease;
                if (absoluteReservationPrice<0) absoluteReservationPrice = 0;
        }

        //If the flat would partly be financed out of loan, the reservation price is decreased according to the loan and its interest rate
        double loanNeed = Math.max(0,flat.forSalePrice-household.depositAvailableForFlat(flat));
        if (loanNeed>0) {
            double monthlyInterestRate = Model.banks.get(0).calculateMonthlyInterestRate(loanNeed,household,flat,0,0,0,true);
            absoluteReservationPrice -= loanNeed*monthlyInterestRate*Model.maxDuration*0.5;
        }



        return absoluteReservationPrice;
    }

    public void deleteUtilityFunctionCES() {
        Model.utilityFunctionsCES.remove(id);
        Model.nUtilityFunctionsCES--;

    }

}
