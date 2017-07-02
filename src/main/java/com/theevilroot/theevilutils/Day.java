package com.theevilroot.theevilutils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by TheEvilRoot on 6/30/2017.
 */

public class Day {

    public int id;
    public String unlocalizedName, name;

    public HashMap<MainActivity.Profile, List<String>> lessons;

    public Day(int id, String unlocalizedName,String name) {
        this.id = id;
        this.unlocalizedName = unlocalizedName;
        this.name = name;
        this.lessons = new HashMap<>();
    }

    public Day initProfile(MainActivity.Profile prof, List<String> lessons) {
        this.lessons.put(prof, lessons);
        return this;
    }

}
