package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecordForPrice {

    //state variables in case of FlatSaleRecord
    public int periodOfRecord; //DB
    public Bucket bucket;
    public double size;
    public double state;
    public double neighbourhoodQuality;
    public boolean isNewlyBuilt;
    public boolean isForcedSale;

    public boolean recordEligibleForPrice() {
        if (periodOfRecord==Model.period-1 && isNewlyBuilt==false && isForcedSale==false) {
            return true;
        } else return false;
    }

    public boolean isSimilarRecordForPrice(Flat flat) {

        if (Math.abs(size-flat.size)/flat.size<Model.maxSizeDifferenceRatio && Math.abs(state-flat.state)/flat.state<Model.maxStateDifferenceRatio) {
            return true;
        } else return false;
    }
}
