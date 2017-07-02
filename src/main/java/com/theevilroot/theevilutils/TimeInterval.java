package com.theevilroot.theevilutils;

/**
 * Created by TheEvilRoot on 6/30/2017.
 */

public class TimeInterval {

    public int id;
    public long starts, ends;
    public IntervalType type;

    public enum IntervalType {
        LESSON("Урок"), REST("Перемена");
        String str;
        IntervalType(String str){this.str = str;}
    }

    public TimeInterval(int id, long starts, long ends, IntervalType type) {
        this.id = id;
        this.starts = starts;
        this.ends = ends;
        this.type = type;
    }

}
