package com.example.finkel.bikecycle;

/**
 * Created by Finkel on 02/03/2016.
 */
public class LiveLogger {
    private static String log;
    private static boolean readFlag;

    public static void setLog(String l){
        log += l;
        readFlag = false;
    }

    public static String getLog(){
        readFlag = true;
        String toReturn = log;
        log = "";
        return toReturn;
    }

    public static boolean isNewLog(){
        return !readFlag;
    }
}
