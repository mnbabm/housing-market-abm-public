package model;

public class ParametersToOverride {

    String simulationWithShock;
    String simulationWithCovid;


    double[] LTVPath;
    int nPeriods;
    double additionalPTI = 0;
    double firstBuyerAdditionalPTI = 0;
    double firstBuyerAdditionalLTV = 0;

    public String outputDataName;

    public void overrideParameters() {

        if (LTVPath != null && Model.LTVPath != null) {
            if (Model.LTVPath.length > LTVPath.length) {
                for (int i = 0; i < Model.LTVPath.length; i++) {
                    if (i<LTVPath.length) {
                        Model.LTVPath[i] = LTVPath[i];
                    } else {
                        if (LTVPath[LTVPath.length-2]==LTVPath[LTVPath.length-1]) {
                            Model.LTVPath[i] = Model.LTVPath[i-1];
                        } else {
                            Model.LTVPath[i] = Model.LTVPath[i-1] * (LTVPath[LTVPath.length-1]/LTVPath[LTVPath.length-2]);
                        }
                    }
                }
            } else {
                Model.LTVPath = LTVPath;
            }

        }



        if (nPeriods != 0) {
            Model.nPeriods = nPeriods;
        }

        if (simulationWithShock != null) {
            if (simulationWithShock.equals("true")) Model.simulationWithShock = true;
            if (simulationWithShock.equals("false")) Model.simulationWithShock = false;
        }

        if (simulationWithCovid != null) {
            if (simulationWithCovid.equals("true")) Model.simulationWithCovid = true;
            if (simulationWithCovid.equals("false")) Model.simulationWithCovid = false;
        }

        if (additionalPTI != 0) {
            Model.additionalDSTI = additionalPTI;
        }

        if (firstBuyerAdditionalPTI != 0) {
            Model.firstBuyerAdditionalDSTI = firstBuyerAdditionalPTI;
        }

        if (firstBuyerAdditionalLTV != 0) {
            Model.firstBuyerAdditionalLTV = firstBuyerAdditionalLTV;
        }

    }
}
