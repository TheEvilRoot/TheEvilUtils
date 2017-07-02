package com.theevilroot.theevilutils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by TheEvilRoot on 6/30/2017.
 */

public class MiscUtils {

    public static List<String> jsonArrayToStringList(JsonArray arr){
        List<String> ret = new ArrayList<>();
        for(JsonElement e : arr)  ret.add(e.getAsString());
        return ret;
    }

    public static String formatLessons(List<String> lessons) {
        String ret = "";
        for(int i = 0; i < lessons.size(); i++) {
            ret += String.format("\t\t• %s : %s\n", i+1, lessons.get(i));
        }
        return ret;
    }

    public static long deformetTime(String str){
        try{
            int hours = Integer.parseInt(str.split(":")[0]);
            int mins =  Integer.parseInt(str.split(":")[1]);
            Date d = new Date();
            d.setHours(hours);
            d.setMinutes(mins);
            return d.getTime();
        }catch (Exception e){
            e.printStackTrace();
            return 0l;
        }
    }

    public static TimeInterval.IntervalType parseIntervalType(String str){
        if(str.equals("lesson")){
            return TimeInterval.IntervalType.LESSON;
        }else if(str.equals("rest")){
            return TimeInterval.IntervalType.REST;
        }else{
            return TimeInterval.IntervalType.LESSON;
        }
    }

}
