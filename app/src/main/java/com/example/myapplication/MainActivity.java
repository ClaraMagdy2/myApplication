package com.example.myapplication;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "MyPrefsFile";
    private static final String PDF_PATH_KEY = "pdfPath";
    // TODO : set the API_KEY variable to your api key
    private static String API_KEY = "8a433e978d63481c86990e3a1ee8cd79";

    // setting the TAG for debugging purposes
    private static String TAG = "MainActivity";

    // declaring the views
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;

    // declaring an ArrayList of articles
    private ArrayList<NewsArticle> mArticleList;

    private ArticleAdapter mArticleAdapter;
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                switch (intent.getAction()) {
                    case DownloadService.ACTION_DOWNLOAD_STARTED:
                        // Download started, show progress bar or other UI indication
                        mProgressBar.setVisibility(View.VISIBLE);
                        break;
                    case DownloadService.ACTION_DOWNLOAD_COMPLETE:
                        // Download complete, hide progress bar or update UI
                        mProgressBar.setVisibility(View.GONE);
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));
        // initializing the Fast Android Networking Library
        AndroidNetworking.initialize(getApplicationContext());

        // No need to set the parser factory; it will use the default (Gson)

        // assigning views to their ids
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_id);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_id);

        // setting the recyclerview layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initializing the ArrayList of articles
        mArticleList = new ArrayList<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_STARTED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadReceiver, intentFilter);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_search) {
                androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);
                searchView.requestFocus();
                // Handle the search icon click
                showSearchView();
                return true;
            }
            // Add other cases for additional bottom navigation items if needed
            return false;
        });

        // calling get_news_from_api()
        get_news_from_api();
    }

    public void get_news_from_api() {
        // clearing the articles list before adding news ones
        mArticleList.clear();

        // Making a GET Request using Fast
        // Android Networking Library
        // the request returns a JSONObject containing
        // news articles from the news api
        // or it will return an error
        AndroidNetworking.get("https://newsapi.org/v2/top-headlines")
                .addQueryParameter("country", "in")
                .addQueryParameter("apiKey", API_KEY)
                .addHeaders("token", "1234")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // disabling the progress bar
                        mProgressBar.setVisibility(View.GONE);

                        // handling the response
                        try {

                            // storing the response in a JSONArray
                            JSONArray articles = response.getJSONArray("articles");

                            // looping through all the articles
                            // to access them individually
                            for (int j = 0; j < articles.length(); j++) {
                                // accessing each article object in the JSONArray
                                JSONObject article = articles.getJSONObject(j);

                                // initializing an empty ArticleModel
                                NewsArticle currentArticle = new NewsArticle();

                                // storing values of the article object properties
                                String author = article.getString("author");
                                String title = article.getString("title");
                                String description = article.getString("description");
                                String url = article.getString("url");
                                String urlToImage = article.getString("urlToImage");
                                String publishedAt = article.getString("publishedAt");
                                String content = article.getString("content");

                                // setting the values of the ArticleModel
                                // using the set methods
                                currentArticle.setAuthor(author);
                                currentArticle.setTitle(title);
                                currentArticle.setDescription(description);
                                currentArticle.setUrl(url);
                                currentArticle.setUrlToImage(urlToImage);
                                currentArticle.setPublishedAt(publishedAt);
                                currentArticle.setContent(content);

                                // adding an article to the articles List
                                mArticleList.add(currentArticle);
                            }

                            // setting the adapter
                            mArticleAdapter = new ArticleAdapter(getApplicationContext(), mArticleList);
                            mArticleAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    NewsArticle clickedArticle = mArticleList.get(position);
                                    new DownloadTask().execute(clickedArticle.getUrl());
                                }
                            });
                            mRecyclerView.setAdapter(mArticleAdapter);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            // logging the JSONException LogCat
                            Log.d(TAG, "Error : " + e.getMessage());
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        // logging the error detail and response to LogCat
                        Log.d(TAG, "Error detail : " + error.getErrorDetail());
                        Log.d(TAG, "Error response : " + error.getResponse());
                    }
                });
    }

    public void performSearch(String query) {
        // Perform search logic here
        // You can modify the API request to include the search query
        // For example, you can add a query parameter like this:
        // .addQueryParameter("q", query)

        // Clear the existing articles list
        mArticleList.clear();

        // Make the API request with the search query
        AndroidNetworking.get("https://newsapi.org/v2/top-headlines")
                .addQueryParameter("country", "in")
                .addQueryParameter("apiKey", API_KEY)
                .addQueryParameter("q", query) // Add the search query
                .addHeaders("token", "1234")
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // disabling the progress bar
                        mProgressBar.setVisibility(View.GONE);

                        // handling the response
                        try {

                            // storing the response in a JSONArray
                            JSONArray articles = response.getJSONArray("articles");

                            // looping through all the articles
                            // to access them individually
                            for (int j = 0; j < articles.length(); j++) {
                                // accessing each article object in the JSONArray
                                JSONObject article = articles.getJSONObject(j);

                                // initializing an empty ArticleModel
                                NewsArticle currentArticle = new NewsArticle();

                                // storing values of the article object properties
                                String author = article.getString("author");
                                String title = article.getString("title");
                                String description = article.getString("description");
                                String url = article.getString("url");
                                String urlToImage = article.getString("urlToImage");
                                String publishedAt = article.getString("publishedAt");
                                String content = article.getString("content");

                                // setting the values of the ArticleModel
                                // using the set methods
                                currentArticle.setAuthor(author);
                                currentArticle.setTitle(title);
                                currentArticle.setDescription(description);
                                currentArticle.setUrl(url);
                                currentArticle.setUrlToImage(urlToImage);
                                currentArticle.setPublishedAt(publishedAt);
                                currentArticle.setContent(content);

                                // adding an article to the articles List
                                mArticleList.add(currentArticle);
                            }

                            // setting the adapter
                            mArticleAdapter = new ArticleAdapter(getApplicationContext(), mArticleList);
                            mArticleAdapter.setOnItemClickListener(new ArticleAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    NewsArticle clickedArticle = mArticleList.get(position);
                                    new DownloadTask().execute(clickedArticle.getUrl());
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            // logging the JSONException LogCat
                            Log.d(TAG, "Error : " + e.getMessage());
                        }

                    }

                    @Override
                    public void onError(ANError error) {
                        // logging the error detail and response to LogCat
                        Log.d(TAG, "Error detail : " + error.getErrorDetail());
                        Log.d(TAG, "Error response : " + error.getResponse());
                    }
                });
    }

    private void showSearchView() {
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchView);

        // Set a listener to perform the search when the user submits the query
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // You can perform incremental search as the user types if needed
                return false;
            }
        });

        // Show the search view in the toolbar
        searchView.setVisibility(View.VISIBLE);

        // You can also set other configurations for the search view if needed
        // For example, you can set it to be iconified by default:
        // searchView.setIconifiedByDefault(true);
    }
    private class DownloadTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            // Show a progress bar or any UI indication
            mProgressBar.setVisibility(View.VISIBLE);
        }
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                String url = urls[0];
                // Create a URL object from the provided URL string
                URL fileUrl = new URL(url);

                // Open a connection to the URL
                HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
                connection.setRequestMethod("GET");

                // Get the InputStream from the connection
                InputStream inputStream = connection.getInputStream();

                // Create a File object for the "Download" directory in internal storage
                File downloadDir = new File(getFilesDir(), "storage");
                if (!downloadDir.exists()) {
                    downloadDir.mkdirs(); // Create the directory if it doesn't exist
                }

                // Create a File object for the downloaded file in the "Download" directory
                File outputFile = new File(downloadDir, "downloaded_file.txt");

                // Create a FileOutputStream for the downloaded file
                FileOutputStream outputStream = new FileOutputStream(outputFile);

                // Read from the InputStream and write to the FileOutputStream
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }

                // Close the streams and disconnect the connection
                inputStream.close();
                outputStream.close();
                connection.disconnect();
                SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PDF_PATH_KEY, outputFile.getAbsolutePath());
                editor.apply();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update the progress bar or any other UI indication
            mProgressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // Hide the progress bar or update UI
            mProgressBar.setVisibility(View.GONE);

            // Display a message or handle the result accordingly
            if (result) {
                Toast.makeText(MainActivity.this, "Download completed", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(MainActivity.this, "Download failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onDestroy() {
        // Unregister the BroadcastReceiver when the activity is destroyed
        unregisterReceiver(downloadReceiver);
        super.onDestroy();
    }
}