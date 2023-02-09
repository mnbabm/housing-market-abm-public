package model;

import java.util.ArrayList;
import java.util.List;

public class GeneralBucket {

    public static List<GeoLocation> geoLocations = new ArrayList<>();
    long code;

    public static void setupGeoLocations() {
        for (GeoLocation geoLocation : Model.geoLocations.values()) {
            geoLocations.add(geoLocation);
        }
    }

    public static int getCodeForInterval(double[] intervals, double value) {
        int code = -1;
        if (value==intervals[0]) return 0;
        for (int i = 0; i < intervals.length-1; i++) {
            if (value>intervals[i]) {
                code++;
            } else {
                break;
            }
        }
        return code;
    }

    public static int getCodeForInterval(int[] intervals, int value) {
        int code = -1;
        if (value==intervals[0]) return 0;
        for (int i = 0; i < intervals.length-1; i++) {
            if (value>intervals[i]) {
                code++;
            } else {
                break;
            }
        }
        return code;
    }

    public static int getCodeForDiscreteValue(double[] doubles, double value) {
        int code = -1;
        for (int i = 0; i < doubles.length; i++) {
            if (value==doubles[i]) {
                code = i;
                break;
            }
        }
        return code;
    }

    public static int getCodeForDiscreteValue(int[] integers, int integer) {
        int code = -1;
        for (int i = 0; i < integers.length; i++) {
            if (integer==integers[i]) {
                code = i;
                break;
            }
        }
        return code;
    }

    public static <T> int getCodeForDiscreteValue(List<T> list, T object) {
        int code = -1;
        int i = -1;
        for (Object element : list) {
            i++;
            if (element == object) {
                code = i;
                break;
            }
        }
        return code;
    }
}
