package com.example.kasparasza.popularmoviesapp.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.kasparasza.popularmoviesapp.AllMoviesActivity;
import com.example.kasparasza.popularmoviesapp.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
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
    private static final String DISCOVER_MOVIE_DB_URL = "https://api.themoviedb.org/3/discover/movie";

    private final static String API_KEY_PARAM = "api_key";
    private final static String PAGE_PARAM = "page";
    private final static String SEARCH_PARAM = "query";

    private final static String SORT_PARAMETER = "sort_by";
    private final static String SORT_PARAM_VALUE_BY_POPULARITY = "popularity.desc";
    private final static String SORT_PARAM_VALUE_BY_AVERAGE_VOTES = "vote_average.desc";
    private final static String START_YEAR_PARAM = "primary_release_date.gte";
    private final static String END_YEAR_PARAM = "primary_release_date.lte";
    private final static String GENRE_PARAM = "with_genres";

    /* Personal API KEY to be included into query */
    private static final String API_KEY = "c75a692f7ec1120be7a6029a67f5c0c3";

    // https://api.themoviedb.org/3/popular?api_key=c75a692f7ec1120be7a6029a67f5c0c3&page=1
    // https://api.themoviedb.org/3/movie/popular?api_key=c75a692f7ec1120be7a6029a67f5c0c3&page=1

    // https://api.themoviedb.org/3/discover/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&query=Jack+Reacher&with_genres=18&primary_release_year=2014
    // https://api.themoviedb.org/3/search/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&query=Jack+Reacher
    // https://api.themoviedb.org/3/search/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&query=Jack+Reacher&year=2016
    // https://api.themoviedb.org/3/movie/5/credits?api_key=c75a692f7ec1120be7a6029a67f5c0c3

    // https://api.themoviedb.org/3/discover/movie?api_key=c75a692f7ec1120be7a6029a67f5c0c3&sort_by=popularity.desc&page=1&primary_release_date.lte=2016-12-31&primary_release_date.gte=2014-01-01&with_genres=53

    /* Parts of the path of the query */
    public static final String QUERY_PATH_POPULAR = "popular"; // queries for the most popular movies
    public static final String QUERY_PATH_TOP_RATED = "top_rated"; // queries for the movies top rated
    public static final String QUERY_PATH_CREDITS = "credits"; // queries for the credits of a particular Movie

    /*
     * HTTP QUERY PARAMETERS - used in constructing movie poster query url
     */
    private static final String BASE_MOVIE_POSTER_URL = "https://image.tmdb.org/t/p";

    /*
    * Class constructor set as private so that the utility class would not be available to instantiate
    */
    private AppUtilities(){
        super();
    }

    // Todo(700) afterwards all methods can take the same name, as their parameters are different

    /**
     * Builds the URL used to query www.themoviedb.org API
     * @param pageNumber - page number of json response
     * @param queryPath - part of the query path (null in case search/movie endpoint is used)
     * @param searchQueryString - query parameter (null in case discover/movie endpoint is used)
     * @return The URL to use to query the server.
     */
    public static URL buildUrl(int pageNumber, String queryPath, String searchQueryString,
                               String genreAsString, String startYear, String endYear, boolean userPreferences){
        // start with building Uri
        Uri builtUri;
        // Case #1: no search String & default user preferences
        if(searchQueryString.equals("") && userPreferences){
            builtUri = Uri.parse(BASE_MOVIE_DB_URL).buildUpon()
                    .appendEncodedPath(queryPath)
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(pageNumber))
                    .build();
        } // Case #2 there is a search String
        else if (!searchQueryString.equals("")) {
            searchQueryString = formatStringForUrl(searchQueryString);

            builtUri = Uri.parse(SEARCH_MOVIE_DB_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(pageNumber))
                    .appendQueryParameter(SEARCH_PARAM, searchQueryString)
                    .build();
        } // Case #3 no search String & other than default user preferences
        else {
            // prepare query parameters
            String sortParameterValue;
            if(queryPath.equals(AppUtilities.QUERY_PATH_POPULAR)){
                sortParameterValue = SORT_PARAM_VALUE_BY_POPULARITY;
                // note: we will call the same sort parameter twice, as this is easier
                // than to discern between two different sort types while building Uri
            } else {
                sortParameterValue = SORT_PARAM_VALUE_BY_AVERAGE_VOTES;
            }
            String startYearParameterValue = prepareStartYear(startYear);
            String endYearParameterValue = prepareEndYear(endYear);
            String genreAsInt = getMovieGenre(genreAsString);

            builtUri = Uri.parse(DISCOVER_MOVIE_DB_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .appendQueryParameter(PAGE_PARAM, Integer.toString(pageNumber))
                    .appendQueryParameter(SORT_PARAMETER, sortParameterValue)
                    .appendQueryParameter(START_YEAR_PARAM, startYearParameterValue)
                    .appendQueryParameter(END_YEAR_PARAM, endYearParameterValue)
                    .appendQueryParameter(GENRE_PARAM, genreAsInt)
                    .build();
        }

        // having the Uri, convert it to URL
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
     * @param posterStringUrl part of the url that refers to a particular Movie
     * @param size enumerator value denoting size
     * @return The URL to use to query the server.
     */
    public static URL buildPosterUrl(String posterStringUrl, PosterSizes size){
        Uri builtUrl = Uri.parse(BASE_MOVIE_POSTER_URL).buildUpon()
                .appendPath(String.valueOf(size))
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
     * Builds the URL used to query www.themoviedb.org API for movie credits
     *
     * @return The URL to use to query the server.
     */
    public static URL buildCreditsUrl(int movieId){
        Uri builtUrl = Uri.parse(BASE_MOVIE_DB_URL).buildUpon()
                .appendPath(String.valueOf(movieId))
                .appendPath(QUERY_PATH_CREDITS)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
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
            Log.v(TAG, "ESTABLISHING HTTP CONNECTION with URL " + url); //<-----------------------------------------------------------------------

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
        return activeNetwork != null && activeNetwork.isConnected();
    }


    private static String prepareStartYear(String startYear){
        return startYear + "-01-01";
    }

    private static String prepareEndYear(String endYear){
        return endYear + "-12-31";
    }

    /**
    * Returns genre id of the movie
    * @parameter String with genre name (available String values are set in R.string)
    * @return String with genre id (predefined in TheMovieDb)
    * */
    private static String getMovieGenre(String genreString) {
        // create the map of Movies genres
        Map<String, Integer> genresMap = new HashMap<>();
        genresMap.put("action", 28);
        genresMap.put("adventure", 12);
        genresMap.put("animation", 16);
        genresMap.put("comedy", 35);
        genresMap.put("crime", 80);
        genresMap.put("documentary", 99);
        genresMap.put("drama", 18);
        genresMap.put("family", 10751);
        genresMap.put("fantasy", 14);
        genresMap.put("history", 36);
        genresMap.put("horror", 27);
        genresMap.put("music", 10402);
        genresMap.put("mystery", 9648);
        genresMap.put("romance", 10749);
        genresMap.put("science_fiction", 878);
        genresMap.put("tv_movie", 10770);
        genresMap.put("thriller", 53);
        genresMap.put("war", 10752);
        genresMap.put("western", 37);
        genresMap.put("any", -1);

        String result = String.valueOf(genresMap.get(genreString));
        // if preference is set to "any" genre, this in TheMovieDb is equivalent to an empty String
        if(result.equals("-1")){
            result = "";
        }
        return result;
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
