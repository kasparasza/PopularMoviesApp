package com.example.kasparasza.popularmoviesapp.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.kasparasza.popularmoviesapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

/**
 * Class that contains static methods to construct url queries and to query the network
 * The implementation of the class has been in a major part based on Udacity.com application:
 * SunshineApp: https://github.com/udacity/ud851-Sunshine
 */

public class AppUtilities {

    private static final String TAG = AppUtilities.class.getSimpleName();

    /*
     * HTTP QUERY PARAMETERS - used in constructing general query url
     */
    private static final String BASE_MOVIE_DB_URL = "https://api.themoviedb.org/3/movie";
    private static final String SEARCH_MOVIE_DB_URL = "https://api.themoviedb.org/3/search/movie";

    private final static String API_KEY_PARAM = "api_key";
    private final static String PAGE_PARAM = "page";
    private final static String SEARCH_PARAM = "query";


    /* Personal API KEY to be included into query */
    private static final String API_KEY = "c75a692f7ec1120be7a6029a67f5c0c3";

    // https://api.themoviedb.org/3/popular?api_key=c75a692f7ec1120be7a6029a67f5c0c3&page=1
    // https://api.themoviedb.org/3/movie/popular?api_key=c75a692f7ec1120be7a6029a67f5c0c3&page=1

    // https://api.themoviedb.org/3/discover/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&query=Jack+Reacher&with_genres=18&primary_release_year=2014
    // https://api.themoviedb.org/3/search/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&query=Jack+Reacher
    // https://api.themoviedb.org/3/search/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&query=Jack+Reacher&year=2016

    /* Parts of the path of the query */
    public static final String QUERY_PATH_POPULAR = "popular"; // queries for the most popular movies
    public static final String QUERY_PATH_TOP_RATED = "top_rated"; // queries for the movies top rated

    /*
     * HTTP QUERY PARAMETERS - used in constructing movie poster query url
     */
    private static final String BASE_MOVIE_POSTER_URL = "https://image.tmdb.org/t/p";
    private final static String[] POSTER_SIZE = {"w92", "w154","w185", "w342", "w500", "w780"};

    /*
    * Class constructor set as private so that the utility class would not be available to instantiate
    */
    private AppUtilities(){
        super();
    }

    // Todo(700) afterwards all methods can take the same name, as their parameters are different

    /**
     * Builds the general URL used to query www.themoviedb.org API
     *
     * @return The URL to use to query the server.
     */
    public static URL buildGeneralUrl(int pageNumber, String queryPath) {
        Uri builtUri = Uri.parse(BASE_MOVIE_DB_URL).buildUpon()
                .appendEncodedPath(queryPath)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(PAGE_PARAM, Integer.toString(pageNumber))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Built URI " + url); //<-----------------------------------------------------------------------

        return url;
    }

    /**
     * Builds the URL used to search for Movies in the www.themoviedb.org API
     *
     * @return The URL to use to query the server.
     */
    public static URL buildSearchUrl(int pageNumber, String searchQueryString) {

        // format search query String for Url
        searchQueryString = formatStringForUrl(searchQueryString);

        Uri builtUri = Uri.parse(SEARCH_MOVIE_DB_URL).buildUpon()
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(PAGE_PARAM, Integer.toString(pageNumber))
                .appendQueryParameter(SEARCH_PARAM, searchQueryString)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Built URI " + url); //<-----------------------------------------------------------------------

        return url;
    }

    /**
     * Builds the URL used to query www.themoviedb.org API for movie poster
     *
     * @return The URL to use to query the server.
     */
    public static URL buildPosterUrl(String posterStringUrl){
        Uri builtUrl = Uri.parse(BASE_MOVIE_POSTER_URL).buildUpon()
                .appendPath(POSTER_SIZE[2]) // choose one from the available file sizes
                .appendEncodedPath(posterStringUrl)
                .build();

        URL url = null;
        try {
            url = new URL(builtUrl.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }


    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        String jsonResponse = null;

        // check if there is a non null url
        if (url == null) {
            return jsonResponse;
        }

        // proceed with connection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        try {
            Log.v(TAG, "ESTABLISHING HTTP CONNECTION"); //<-----------------------------------------------------------------------

            // check whether the connection response code is appropriate (in this case == 200)
            if (urlConnection.getResponseCode() == 200){
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    jsonResponse = scanner.next();
                    return jsonResponse;
                } else {
                    Log.e(TAG, "Bad response from the server was received - response code: " +
                            urlConnection.getResponseCode());
                    return jsonResponse;
                }
            }
        } finally {
            urlConnection.disconnect();
        }
        return jsonResponse;
    }

    /**
     * Transforms user's search query parameters into a String formatted for URL
     * @param query - user's search query String
     * @return queryFormattedForUrl - String formatted for URL
     */
    static String formatStringForUrl(String query) {
        String stringFormattedForUrl = null;
        try {
            stringFormattedForUrl = URLEncoder.encode(query, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "An exception was encountered while trying to convert the search query into URL " + e);
        }
        return stringFormattedForUrl;
    }


    /**
     * Method that checks whether there is a network connection
     *
     * @return boolean that is true is there is a connection
     */
    public static boolean checkNetworkConnection(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        return isConnected;
    }

    /**
     * Method that returns the number of columns (spanSize) to be displayed by GridLayoutManager in a RecyclerView
     * The result depends on displayMetrics of the device and targetColumnWidth, which was set in resources.
     *
     * @param context
     * @return noOfColumns - the number of columns to be displayed by GridLayoutManager in a RecyclerView
     */
    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        float targetColumnWidth = context.getResources().getDimension(R.dimen.target_width_of_poster) / displayMetrics.density;
        int noOfColumns = Math.round(dpWidth / targetColumnWidth);
        return noOfColumns;
    }

    /**
     * Method that returns the width of the display (depending on the current orientation of the screen).
     *
     * @param context
     * @return noOfColumns - the number of columns to be displayed by GridLayoutManager in a RecyclerView
     */
    public static int getDisplayWidth(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels;
    }

}
