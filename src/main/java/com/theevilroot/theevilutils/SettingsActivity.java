package com.theevilroot.theevilutils;

import android.app.Dialog;
import android.content.Context;
import android.media.audiofx.BassBoost;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by TheEvilRoot on 7/2/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    ListView list;
    Button save_button;
    Toolbar toolbar;

    Config _config;

    List<SettingsEntry> entries;

    SettingsArrayAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_layout);
        entries = new ArrayList<>();
        _config = MainActivity.config;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setDisplayUseLogoEnabled(false);
        this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        list = (ListView) findViewById(R.id.settings_list);
        entries.addAll(Arrays.asList(
                new SettingsEntry(getString(R.string.settings_lang_btn), _config.getFromCategory(Config.Category.SETTINGS, "lang").getAsString(), android.R.drawable.ic_dialog_info, (adapterView, view, i, l) -> {
                    if(_config.getFromCategory(Config.Category.SETTINGS, "lang").getAsString().equals("ru")) {
                        _config.setValue(Config.Category.SETTINGS, "lang", new JsonPrimitive("en"));
                    }else{
                        _config.setValue(Config.Category.SETTINGS, "lang", new JsonPrimitive("ru"));
                    }
                }),
                new SettingsEntry(getString(R.string.settings_saturday), Boolean.toString(_config.getFromCategory(Config.Category.SETTINGS, "enableSaturday").getAsBoolean()), android.R.drawable.btn_dialog, (adapterView, view, i, l) -> {

                }),
                new SettingsEntry(getString(R.string.settings_lock_screen_timer), Boolean.toString(_config.getFromCategory(Config.Category.SETTINGS, "lockScreenTimer").getAsBoolean()), android.R.drawable.ic_dialog_info, (adapterView, view, i, l) -> {

                })
        ));
        adapter = new SettingsArrayAdapter(this, R.layout.setting_entry, entries);
        list.setAdapter(adapter);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            try {
                entries.get(i).listener.onItemClick(adapterView, view, i, l);
                adapter.setNotifyOnChange(true);
                adapter.notifyDataSetChanged();
            }catch (IndexOutOfBoundsException e){
                // TODO
            }
        });
    }

    public class SettingsArrayAdapter extends ArrayAdapter<SettingsEntry> {

        public final Context context;
        public final List<SettingsEntry> objects;

        public SettingsArrayAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<SettingsEntry> objects) {
            super(context, resource, objects);
            this.context = context;
            this.objects = objects;
            this.setNotifyOnChange(true);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.setting_entry, parent, false);
            TextView label = (TextView) rowView.findViewById(R.id.settings_enrty_label);
            TextView value = (TextView) rowView.findViewById(R.id.settings_entry_value);
            ImageView icon = (ImageView) rowView.findViewById(R.id.settings_enrty_icon);
            label.setText(objects.get(position).label);
            value.setText(""+objects.get(position).value);
            icon.setImageDrawable(SettingsActivity.this.getDrawable(objects.get(position).resource));
            return rowView;
        }
    }

    public static class SettingsEntry {
        public String label, value;
        public AdapterView.OnItemClickListener listener;
        @DrawableRes
        public int resource;
        public SettingsEntry(String label, String value, int resource, AdapterView.OnItemClickListener onclick) {
            this.label = label;
            this.value = value;
            this.resource = resource;
            this.listener = onclick;
        }

        public SettingsEntry setValue(String value) {
            this.value = value;
            return this;
        }

        public SettingsEntry setLabel(String label) {
            this.label = label;
            return this;
        }

        public SettingsEntry setIcon(@DrawableRes int resource){
            this.resource = resource;
            return this;
        }
    }

}
