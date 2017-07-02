package com.theevilroot.theevilutils;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import static com.theevilroot.theevilutils.MainActivity.config;

/**
 * Created by TheEvilRoot on 7/2/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    Button lang_settings, saturday_settings, interval_notif_settings, lockscreen_timer_settings, lessons_settings, intervals_settings, days_settings, close_btn;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_main_layout);
        lang_settings = (Button) findViewById(R.id.lang_btn);
        saturday_settings = (Button) findViewById(R.id.saturday_btn);
        interval_notif_settings = (Button)findViewById(R.id.interval_notifications_btn);
        lockscreen_timer_settings = (Button) findViewById(R.id.lockscreen_timer_btn);
        lessons_settings = (Button)findViewById(R.id.lessons_settings_btn);
        intervals_settings = (Button) findViewById(R.id.intervals_settings_btn);
        days_settings = (Button) findViewById(R.id.days_settings_btn);
        close_btn = (Button) findViewById(R.id.close_settings);

        lang_settings.setText(getString(R.string.settings_lang_btn, config.getFromCategory(Config.Category.SETTINGS, "lang")));
        saturday_settings.setText(getString(R.string.settings_saturday, config.getFromCategory(Config.Category.SETTINGS, "enableSaturday")));
        interval_notif_settings.setText(getString(R.string.settings_interval_notifications, config.getFromCategory(Config.Category.SETTINGS, "intervalsNotification")));
        lockscreen_timer_settings.setText(getString(R.string.settings_lock_screen_timer, config.getFromCategory(Config.Category.SETTINGS, "lockScreenTimer")));
    }
}
