package begnardi.luca.utils;

/**
 * Created by begno on 11/02/15.
 */

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public abstract class Utils {

    /**
	 * abstract class with static methods and fields
	 */

    public static final String APIKey = "AIzaSyAuqYwPH4dE4_JaE3oKTeTUihAeR7nBS3E";
    //modena pubblico
    public static final String IP = "2.224.243.45";
    //finale_pub
    //public static final String IP = "82.58.143.249";
    //locale_loc
    //public static final String IP = "192.168.1.57";

    public static double average(ArrayList<Double> values) {
        double average = 0;
        for(int i = 0; i < values.size(); i++)
            average += values.get(i);
        return average / values.size();
    }

    public static double milliFromNano(double nano) {
        return nano / 1000000;
    }

    public static double secondFromNano(double nano) {
        return nano / 1000000000;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}