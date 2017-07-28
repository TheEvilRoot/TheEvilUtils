package com.theevilroot.theevilutils;

import android.content.Context;
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

/**
 * Created by TheEvilRoot on 7/27/2017.
 */

public class EditLessonsActivity extends AppCompatActivity {
    ListView list;
    Button save_button;
    Toolbar toolbar;
    TextView toolbar_title, toolbar_subtitle;
    Button reset;
    ImageView toolbar_icon;

    Config _config;

    EditLessonsAdapter adapter;

    boolean changed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lessons_edit_layout);
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
        list = (ListView) findViewById(R.id.lessons_list);
        list.setDivider(null);
        list.setDividerHeight(0);
        toolbar_subtitle.setText(MainActivity.Profile.valueOf(getIntent().getStringExtra("profile")).getName(MainActivity.lang));
        adapter = new EditLessonsActivity.EditLessonsAdapter(this, R.layout.setting_entry,
                Arrays.asList(
                        MainActivity.activity.timeIntervals.stream().map(timeInterval -> timeInterval.type.str + " " + timeInterval.id + " " + timeInterval.starts + "/" + timeInterval.ends).toArray(String[]::new)
                ));
        list.setAdapter(adapter);
        list.setOnItemClickListener((adapterView, view, i, l) -> {

        });
    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    public class EditLessonsAdapter extends ArrayAdapter<String> {

        public final Context context;
        public final List<String> objects;

        public EditLessonsAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
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
            label.setText(objects.get(position));
            value.setText("" + objects.get(position));
      //      icon.setImageDrawable(SettingsActivity.this.getDrawable(objects.get(position).resource));
            return rowView;
        }
    }

}
