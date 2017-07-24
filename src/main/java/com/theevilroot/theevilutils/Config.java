package com.theevilroot.theevilutils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

/**
 * Created by TheEvilRoot on 6/29/2017.
 */

public class Config {

    public enum Category {
        SETTINGS("settings"), LESSONS("lessons");
        public String key;
        Category(String key){
            this.key = key;
        }
    }

    public File configfile;
    private JsonObject config_obj;
    public Activity activity;
    private String path;
    public Config(Activity activity, String path) {
        this.activity = activity;
        this.path = path;
        configfile = new File(path);
        if(configfile.exists()) {
            init();
        }else{
            download();
        }
    }

    public void download() {
        try {
            new AlertDialog.Builder(activity).
                    setTitle("Config").
                    setMessage("I can't find config file on your device. Do you want to download it from my server?").
                    setPositiveButton("Yeah, sure", (dialogInterface, i) -> {
                        Dialog dialog = new Dialog(activity);
                        dialog.setContentView(R.layout.download_layout);
                        dialog.show();
                        new ConfigDownloadTask(activity, dialog).execute();
                    }).
                    setNegativeButton("No, of course not", (dialogInterface, i) -> System.exit(0)).create().show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void init() {
        try(FileReader reader = new FileReader(configfile)) {
            config_obj = new JsonParser().parse(reader).getAsJsonObject();
            Toast.makeText(activity, "Config successfully loaded", Toast.LENGTH_SHORT).show();
            MainActivity.config_loaded = true;
            MainActivity.lock.countDown();
        } catch (Throwable e) {
            Toast.makeText(activity, "An exception occurred while reading config. Exiting..", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public JsonElement getFromCategory(Category cat, String key) {
        return config_obj.get(cat.key).getAsJsonObject().get(key);
    }

    public JsonElement get(String key) {
        return config_obj.get(key);
    }

    protected void setValue(Category cat, String key, JsonElement value) {
        if(config_obj.get(cat.key).getAsJsonObject().has(key)) {
            config_obj.get(cat.key).getAsJsonObject().add(key, value);
        }
    }

    public JsonObject getConfigObject(){
        return config_obj;
    }

}
