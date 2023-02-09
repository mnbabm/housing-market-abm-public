package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class Babyloan {
    boolean isIssued=false;
    public int nChildrenSinceIssuance=0;
    public double principal;
    public int duration;
    public double payment;
    public double PTIPayment;
    public int nPeriodsSinceChildBirth = -1;

public boolean isSuspended() {
    if (nChildrenSinceIssuance>0 && nPeriodsSinceChildBirth<=Model.nPeriodBabyloanSuspension) return true;
    return false;
}



}


