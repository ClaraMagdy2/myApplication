package com.example.myapplication;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;


import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadService extends IntentService {

    public static final String ACTION_DOWNLOAD_STARTED = "com.example.myapplication.DOWNLOAD_STARTED";
    public static final String ACTION_DOWNLOAD_COMPLETE = "com.example.myapplication.DOWNLOAD_COMPLETE";

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String newsUrl = intent.getStringExtra("news_url");
            if (newsUrl != null) {
                // Broadcast that the download has started
                sendBroadcast(new Intent(ACTION_DOWNLOAD_STARTED));

                // Download content
                downloadContent(newsUrl);

                // Broadcast that the download is complete
                sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE));
            }
        }
    }

    private void downloadContent(String newsUrl) {
        try {
            // Create a URL object from the provided URL string
            URL url = new URL(newsUrl);

            // Open a connection to the URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Get the InputStream from the connection
            InputStream inputStream = connection.getInputStream();

            // Read the content from the InputStream
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            // Log the downloaded content
            Log.d("DownloadService", "Downloaded content:\n" + content.toString());

            // Close the BufferedReader, InputStream, and disconnect the connection
            reader.close();
            inputStream.close();
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e("DownloadService", "Error during download: " + e.getMessage());
        }
    }
}
