package com.theevilroot.theevilutils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by TheEvilRoot on 6/30/2017.
 */

public class ConfigDownloadTask extends AsyncTask<Void, Void, Void> {

    private Activity activity;
    private PowerManager.WakeLock mWakeLock;
    private Dialog dialog;

    public ConfigDownloadTask(Activity activity, Dialog dialog) {
        this.activity = activity;
        this.dialog = dialog;
    }
    @Override
    protected Void doInBackground(Void... voids) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {

            URL url = new URL("https://raw.githubusercontent.com/TheEvilRoot/TheEvilUtils/master/theevilutils-config.json");
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("error");
            }
            int fileLength = connection.getContentLength();
            new File("/sdcard/theevilutils/").mkdir();
            input = connection.getInputStream();
            output = new FileOutputStream("/sdcard/theevilutils/theevilutils-config.json");
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                if (isCancelled()) {
                    input.close();
                    return null;
                }
                total += count;
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity.runOnUiThread(() -> Toast.makeText(activity, "An exception occurred while downloading config : "+e.getClass(), Toast.LENGTH_SHORT).show());
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        activity.runOnUiThread(() -> dialog.dismiss());
        MainActivity.config.init();
    }
}
