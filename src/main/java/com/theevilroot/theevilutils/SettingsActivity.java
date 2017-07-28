package com.theevilroot.theevilutils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


/**
 * Created by TheEvilRoot on 7/2/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    ListView list;
    Button save_button;
    Toolbar toolbar;
    TextView toolbar_title, toolbar_subtitle;
    Button reset;
    ImageView toolbar_icon;

    Config _config;

    SettingsArrayAdapter adapter;

    boolean changed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_layout);
        _config = MainActivity.config;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayShowTitleEnabled(false);
        this.getSupportActionBar().setDisplayUseLogoEnabled(false);
        this.getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_subtitle = (TextView) findViewById(R.id.toolbar_subtitle);
        toolbar_icon = (ImageView) findViewById(R.id.imageView);
        toolbar_icon.setOnClickListener(view -> {
            changed = false;
            this.finish();
        });
        list = (ListView) findViewById(R.id.settings_list);
        list.setDivider(null);
        list.setDividerHeight(0);
        adapter = new SettingsArrayAdapter(this, R.layout.setting_entry, Arrays.asList(
                new SettingsEntry<>(getString(R.string.settings_lang_btn), _config.getFromCategory(Config.Category.SETTINGS, "lang").getAsString(), _config.getFromCategory(Config.Category.SETTINGS, "lang").getAsString().equals("ru") ? R.drawable.ru : R.drawable.en, (a, index, entry) -> {
                    _config.setValue(Config.Category.SETTINGS, "lang", new JsonPrimitive(entry.value.equals("ru") ? "en" : "ru"));
                    a.objects.set(index, entry.setValue(entry.value.equals("ru") ? "en" : "ru").setIcon(entry.value.equals("ru") ? R.drawable.ru : R.drawable.en));
                }, true),
                new SettingsEntry<>(getString(R.string.settings_saturday), _config.getFromCategory(Config.Category.SETTINGS, "enableSaturday").getAsBoolean(), _config.getFromCategory(Config.Category.SETTINGS, "enableSaturday").getAsBoolean() ? R.drawable.calendar_on : R.drawable.calendar_off, (a, index, entry) -> {
                    _config.setValue(Config.Category.SETTINGS, "enableSaturday", new JsonPrimitive(!entry.value));
                    a.objects.set(index, entry.setValue(!entry.value).setIcon(entry.value ? R.drawable.calendar_on : R.drawable.calendar_off));
                }, true),
                new SettingsEntry<>(getString(R.string.settings_lock_screen_timer), _config.getFromCategory(Config.Category.SETTINGS, "lockScreenTimer").getAsBoolean(), _config.getFromCategory(Config.Category.SETTINGS, "lockScreenTimer").getAsBoolean() ? R.drawable.timer_on : R.drawable.timer_off, (a, index, entry) -> {
                    _config.setValue(Config.Category.SETTINGS, "lockScreenTimer", new JsonPrimitive(!entry.value));
                    a.objects.set(index, entry.setValue(!entry.value).setIcon(entry.value ? R.drawable.timer_on : R.drawable.timer_off));
                }, true),
                new SettingsEntry<String>(getString(R.string.settings_lessons_btn), "❯", R.drawable.lessons, (adapter1, index, entry) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(R.drawable.ic_launcher);
                    builder.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, Arrays.asList(MainActivity.Profile.BASE.getName(MainActivity.lang), MainActivity.Profile.PHYSMATH.getName(MainActivity.lang), MainActivity.Profile.RUSENG.getName(MainActivity.lang))), (dialogInterface, i) -> {
                        switch (i)  {
                            case 0: showEditActivity(MainActivity.Profile.BASE); break;
                            case 1: showEditActivity(MainActivity.Profile.PHYSMATH); break;
                            case 2: showEditActivity(MainActivity.Profile.RUSENG); break;
                            default: return;
                        }
                        changed = false;
                    });
                    builder.create().show();
                }, false)
        ));
        list.setAdapter(adapter);
        list.setOnItemClickListener((adapterView, view, i, l) -> {
            SettingsEntry entry = adapter.getItem(i);
            entry.action.action(adapter, i, entry);
            adapter.notifyDataSetChanged();
            if(entry.change) {
                toolbar_subtitle.setText(getString(R.string.settings_subtitle_changed) + " : " + entry.label);
                changed = true;
            }
        });
    }

    public void showEditActivity(MainActivity.Profile editfor) {
        Intent intent = new Intent(this, EditLessonsActivity.class);
        intent.putExtra("profile", editfor.name());
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (changed) {
            try (FileWriter writer = new FileWriter(_config.configfile, false)) {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(_config.getConfigObject()));
            } catch (Exception e) {
                Log.e(e.getClass().getName(), e.getLocalizedMessage());
            }
            MainActivity.activity.recreate();
        }
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
            TextView label = rowView.findViewById(R.id.settings_enrty_label);
            TextView value = rowView.findViewById(R.id.settings_entry_value);
            ImageView icon = rowView.findViewById(R.id.settings_enrty_icon);
            label.setText(objects.get(position).label);
            value.setText("" + objects.get(position).value);
            icon.setImageDrawable(SettingsActivity.this.getDrawable(objects.get(position).resource));
            return rowView;
        }
    }

    public static class SettingsEntry<T> {
        interface Action<T> {
            void action(SettingsArrayAdapter adapter, int index, SettingsEntry<T> entry);
        }

        public String label;
        public Action action;
        public T value;
        @DrawableRes
        public int resource;
        public boolean change;

        public SettingsEntry(String label, T value, int resource, Action<T> action, boolean change) {
            this.label = label;
            this.value = value;
            this.action = action;
            this.resource = resource;
            this.change = change;
        }

        public SettingsEntry setValue(T value) {
            this.value = value;
            return this;
        }

        public SettingsEntry setLabel(String label) {
            this.label = label;
            return this;
        }

        public SettingsEntry setIcon(@DrawableRes int resource) {
            this.resource = resource;
            return this;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
