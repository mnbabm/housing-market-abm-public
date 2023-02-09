package model;

import lombok.Getter;
import lombok.Setter;
import util.OwnFunctions;

import java.util.Comparator;

@Getter
@Setter
public class FlatSaleRecord extends RecordForPrice implements HasID {
    public static int nextId;
    final int id;

    //state variables + see other state variables at RecordForPrice
    public double price; //DB
    public int flatId;

    public FlatSaleRecord() {
        id = FlatSaleRecord.nextId;
        FlatSaleRecord.nextId++;

    }

    public FlatSaleRecord(int id) {
        this.id = id;
        if (FlatSaleRecord.nextId<id+1) FlatSaleRecord.nextId=id+1;
    }


    public void setSize(double value) {
        size = value;
    }

    public static Comparator<FlatSaleRecord> comparatorPriceIncrease = new Comparator<FlatSaleRecord>() {

        //@Override
        public int compare(FlatSaleRecord o1, FlatSaleRecord o2) {
            if (o1.getPrice()==o2.getPrice()) return Integer.compare(o1.getId(), o2.getId());
            return Double.compare(o1.getPrice(), o2.getPrice());
        }
    };


}
