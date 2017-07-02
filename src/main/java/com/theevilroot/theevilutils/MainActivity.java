package com.theevilroot.theevilutils;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public enum Profile {
        BASE(), PHYSMATH(), RUSENG();
    }

    FloatingActionButton fab;
    TextView time, status, lefttime, info;
    Toolbar toolbar;
    AlertDialog.Builder timetable_dialog;

    public static List<Day> days;
    public static List<TimeInterval> timeIntervals;

    public static Config config;

    public static final String config_path = "/sdcard/theevilutils/theevilutils-config.json";

    public static boolean config_loaded = false;

    public static String lang = "ru";

    public static final Profile current_profile = Profile.BASE;

    public static final SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat ms = new SimpleDateFormat("mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            toolbar = (Toolbar) findViewById(R.id.appBarLayout);
            setSupportActionBar(toolbar);
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.setSubtitleTextColor(Color.GRAY);
            fab = (FloatingActionButton) findViewById(R.id.fab);
            time = (TextView) findViewById(R.id.time);
            status = (TextView) findViewById(R.id.status);
            lefttime = (TextView) findViewById(R.id.lefttime);
            info = (TextView) findViewById(R.id.info);
            timetable_dialog = new AlertDialog.Builder(this);
            fab.setOnClickListener((e) -> {
                String ret = getString(R.string.day_lessons_title) + ":\n";
                Day cur = getCurrentDay();
                if(cur == null)
                    return;
                ret += "\t" + cur.name + ":\n";
                ret += MiscUtils.formatLessons(cur.lessons.get(current_profile));
                timetable_dialog.setMessage(ret).create().show();
            });
            fab.setOnLongClickListener(view -> {
                String ret = getString(R.string.all_lessons_title) + ":\n";
                for (Day d : days){ ret += String.format("\t%s:\n %s", d.name, MiscUtils.formatLessons(d.lessons.get(current_profile))); }
                timetable_dialog.setMessage(ret).create().show();
                return true;
            });

            config = new Config(this, config_path);

            Runnable tick = () -> {
                try {
                    toolbar.setSubtitle(getString(R.string.profile_subtitle) + ":" + current_profile.name());
                    time.setText(hms.format(new Date()));
                    TimeInterval currentInterval = getCurrentInterval();
                    Day currentDay = getCurrentDay();
                    if (currentDay == null) {
                        status.setText(getString(R.string.invalid_day));
                        lefttime.setText("00:00");
                    }else {
                        if (currentInterval == null) {
                            status.setText(R.string.invalid_interval);
                            lefttime.setText("00:00");
                        }else {
                            if (currentInterval.type == TimeInterval.IntervalType.LESSON) {
                                status.setText(getString(R.string.status_lesson, currentInterval.id + 1,currentDay.lessons.get(current_profile).get(currentInterval.id)));
                                lefttime.setText(ms.format(getLeftTime(currentInterval)));
                            } else {
                                status.setText(getString(R.string.status_rest,currentInterval.id ,(currentDay.lessons.get(current_profile).size() + 1 < currentInterval.id + 1 ? currentDay.lessons.get(current_profile).get(currentInterval.id) : "Ничего =3")));
                                lefttime.setText(ms.format(getLeftTime(currentInterval)));
                            }
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            };

            Thread initThread = new Thread(() -> {
                try {
                    while (!config_loaded) {
                        Thread.sleep(100);
                    }
                    lang = config.getFromCategory(Config.Category.SETTINGS, "lang").getAsString();
                    Locale locale = new Locale(lang);
                    Locale.setDefault(locale);
                    Configuration conf = new Configuration();
                    conf.setLocale(locale);
                    getBaseContext().getResources().updateConfiguration(conf, getBaseContext().getResources().getDisplayMetrics());
                    days = new ArrayList<>();
                    JsonArray arr_days = config.get("days").getAsJsonArray();
                    JsonObject obj_lessons = config.get("lessons").getAsJsonObject();
                    for (JsonElement day : arr_days) {
                        JsonObject d = day.getAsJsonObject();
                        days.add(new Day(d.get("id").getAsInt(), d.get("unlocalized").getAsString(), d.get(lang).getAsString()).
                                initProfile(Profile.BASE, MiscUtils.jsonArrayToStringList(obj_lessons.get("base").getAsJsonObject().get(d.get("id").getAsString()).getAsJsonObject().get(lang).getAsJsonArray())).
                                initProfile(Profile.PHYSMATH, MiscUtils.jsonArrayToStringList(obj_lessons.get("physmath").getAsJsonObject().get(d.get("id").getAsString()).getAsJsonObject().get(lang).getAsJsonArray())).
                                initProfile(Profile.RUSENG, MiscUtils.jsonArrayToStringList(obj_lessons.get("rusEng").getAsJsonObject().get(d.get("id").getAsString()).getAsJsonObject().get(lang).getAsJsonArray())));
                    }
                    timeIntervals = new ArrayList<>();
                    JsonArray intervals = config.get("intervals").getAsJsonArray();
                    for (JsonElement e : intervals) {
                        JsonObject interval = e.getAsJsonObject();
                        timeIntervals.add(new TimeInterval(interval.get("id").getAsInt(), MiscUtils.deformetTime(interval.get("starts").getAsString()), MiscUtils.deformetTime(interval.get("ends").getAsString()), MiscUtils.parseIntervalType(interval.get("type").getAsString())));
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            });
            runOnUiThread(() -> initThread.start());

            Thread thr = new Thread(() -> {
                try {
                    initThread.join();
                    while(true){
                            runOnUiThread(tick);
                            TimeUnit.SECONDS.sleep(1);
                    }
                }catch (Throwable t){
                    t.printStackTrace();
                }
            });

            thr.start();

        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_settings) {
            try {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            }catch (Throwable t){
                t.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static Day getCurrentDay(){
        for(Day d : days) {
            if(d.unlocalizedName.equals(new SimpleDateFormat("EEEE").format(new Date()).toLowerCase()))
                return d;
        }
        return null;
    }

    public static TimeInterval getCurrentInterval(){
        long cur = new Date().getTime();
        for(TimeInterval i : timeIntervals) {
            if(cur >= i.starts && cur <= i.ends)
                return i;
        }
        return null;
    }

    public static long getLeftTime(TimeInterval ti) {
        return new Date().getTime() - ti.ends;
    }
}
