package com.theevilroot.theevilutils;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public enum Profile {
        BASE("Base", "База"), PHYSMATH("Phys-Math", "Физ.Мат"), RUSENG("Rus-Eng", "Русск.Англ");
        private String ru, en;
        Profile(String en, String ru) {
            this.ru = ru;
            this.en = en;
        }
        public String getName(String lang){
            if(lang.equals("ru")) return ru;
            if(lang.equals("en")) return en;
            return name();
        }
    }

    FloatingActionButton fab;
    TextView time, status, lefttime, info, toolbar_title, toolbar_subtitle, debug_status;
    AlertDialog.Builder timetable_dialog;
    Toolbar toolbar;

    public static List<Day> days;
    public static List<TimeInterval> timeIntervals;

    public static Config config;

    public static final String config_path = "/sdcard/theevilutils/theevilutils-config.json";

    public static boolean config_loaded = false;

    public static String lang = "ru";

    public static Profile current_profile;

    public static final SimpleDateFormat hms = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat hm = new SimpleDateFormat("HH:mm");
    public static final SimpleDateFormat ms = new SimpleDateFormat("mm:ss");

    public static final CountDownLatch lock = new CountDownLatch(1);

    float x1,x2,y1,y2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setDisplayUseLogoEnabled(false);
        this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        try {
            fab = (FloatingActionButton) findViewById(R.id.fab);
            time = (TextView) findViewById(R.id.time);
            status = (TextView) findViewById(R.id.status);
            lefttime = (TextView) findViewById(R.id.lefttime);
            info = (TextView) findViewById(R.id.info);
            timetable_dialog = new AlertDialog.Builder(this);
            toolbar_title = (TextView) findViewById(R.id.toolbar_title);
            toolbar_subtitle = (TextView) findViewById(R.id.toolbar_subtitle);
            debug_status = (TextView) findViewById(R.id.debug_status);
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
            toolbar.setOnLongClickListener(view -> {
                if(current_profile == Profile.BASE)
                    initProfile(Profile.PHYSMATH);
                else if(current_profile == Profile.PHYSMATH)
                    initProfile(Profile.RUSENG);
                else initProfile(Profile.BASE);
                return false;
            });
           config = new Config(this, config_path);

            Runnable tick = () -> {
                try {
                    time.setText(hms.format(new Date()));
                    TimeInterval currentInterval = getCurrentInterval();
                    Day currentDay = getCurrentDay();
                    debug_status.setText(currentDay == null ? "No day" : currentDay.name + ". " + (currentInterval == null ? "No Interval." : currentDay.lessons.get(current_profile).get(currentInterval.id) + "[" + hm.format(new Date(currentInterval.starts)) + ":" + hm.format(new Date(currentInterval.ends)) + "] " + currentInterval.type.str));
                    if (currentDay == null) {
                        status.setText(getString(R.string.invalid_day));
                        lefttime.setText("00:00");
                    }else {
                        if (currentInterval == null) {
                            status.setText(R.string.invalid_interval);
                            lefttime.setText("00:00");
                        }else {
                            if (currentInterval.type == TimeInterval.IntervalType.LESSON) {
                                status.setText(getString(R.string.status_lesson, (currentInterval.id + 1), currentDay.lessons.get(current_profile).get(currentInterval.id)));
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
                    lock.await();
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
            runOnUiThread(initThread::start);

            Thread thr = new Thread(() -> {
                try {
                    initThread.join();
                    initProfile(Profile.BASE);
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
            if(d.unlocalizedName.equals(new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date()).toLowerCase())) return d;
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
        return ti.ends - new Date().getTime() ;
    }

    public void initProfile(Profile prof) {
        current_profile = prof;
        toolbar_subtitle.setText(getString(R.string.profile_subtitle) + ": " + current_profile.getName(lang));
    }

}
