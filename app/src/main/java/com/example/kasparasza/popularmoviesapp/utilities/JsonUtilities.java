package com.example.kasparasza.popularmoviesapp.utilities;

import android.util.Log;
import android.util.Pair;

import com.example.kasparasza.popularmoviesapp.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that is used to parse raw JSON response to a necessary format
 */

public class JsonUtilities {

    private static final String TAG = JsonUtilities.class.getSimpleName();

    /*
    * Integer constants to be used as JSON response code
    * */
    public final static Integer JSON_RESPONSE_ERROR = 0;
    public final static Integer JSON_RESPONSE_WITH_RESULTS = 1; // results Array is not empty and the results String displays not the last String
    public final static Integer JSON_RESPONSE_NO_RESULTS = 2; // e.g. {"page":2,"total_results":0,"total_pages":1,"results":[]}
    public final static Integer JSON_RESPONSE_PAGE_LIMIT_REACHED = 3; // e.g. {"page":2,"total_results":10,"total_pages":1,"results":[]}

    /*
     * JSON KEYS - used in retrieving data from the raw JSON
     */
    private final static String PAGE = "page";
    private final static String TOTAL_RESULTS = "total_results";
    private final static String TOTAL_PAGES = "total_pages";
    private final static String RESULTS = "results";
    private final static String VOTE_AVERAGE = "vote_average";
    private final static String VOTE_COUNT = "vote_count";
    private final static String ID = "id";
    private final static String TITLE = "title";
    private final static String ORIGINAL_TITLE = "original_title";
    private final static String POSTER_PATH = "poster_path";
    private final static String OVERVIEW = "overview";
    private final static String RELEASE_DATE = "release_date";
    private final static String GENRE = "genre_ids";
    private final static String CAST = "cast";
    private final static String NAME = "name";
    private final static String CREW = "crew";
    private final static String JOB = "job";
    private final static String DIRECTOR = "Director";
    private final static String NOT_AVAILABLE = "not available";

    /*
    * Class constructor set as private so that the utility class would not be available to instantiate
    */
    private JsonUtilities() {
        super();
    }

    // Todo (100) check for error codes and interpret them

    /**
     * Reads JSONString and extracts relevant data from it
     *
     * @param JSONString - result of the previous http query parsed into String format
     * @return Pair of two objects: {@link List} a list of Movie objects
     * & {@link Integer} json response code
     */
    public static Pair<List<Movie>, Integer> extractFromJSONString(String JSONString) {

        List<Movie> movieList = new ArrayList<>();
        Integer jsonResponseCode;

        try {
            // convert String to a JSONObject
            JSONObject jsonObject = new JSONObject(JSONString);

            // set local variables to be used in determining JSON response code
            int currentPageOfResults = 0;
            int totalNumberOfPages = 0;
            int totalResults = 0;
            if (jsonObject.has(PAGE)) {
                currentPageOfResults = jsonObject.getInt(PAGE);
            }
            if (jsonObject.has(TOTAL_PAGES)) {
                totalNumberOfPages = jsonObject.getInt(TOTAL_PAGES);
            }
            if (jsonObject.has(TOTAL_RESULTS)) {
                totalResults = jsonObject.getInt(TOTAL_RESULTS);
            }

            // check whether we have "results" JSONArray available at all,
            // if true - extract the "results" JSONArray
            // if true - the parsing continues, else - we return an empty ArrayList, as there actually is no data to display
            if (jsonObject.has(RESULTS) && totalResults > 0) {

                if (currentPageOfResults >= totalNumberOfPages) {
                    jsonResponseCode = JSON_RESPONSE_PAGE_LIMIT_REACHED;
                } else {
                    jsonResponseCode = JSON_RESPONSE_WITH_RESULTS;
                }

                JSONArray resultsArray = jsonObject.getJSONArray(RESULTS);

                // Loop through each item in the array
                // Get Movie JSONObject at position i, and parse through its attributes
                int item;
                for (item = 0; item < resultsArray.length(); item++) {
                    JSONObject oneMovie = resultsArray.getJSONObject(item);

                    // extract "id" for the genuine id of a Movie in the Db
                    int id;
                    if (oneMovie.has(ID)) {
                        id = oneMovie.getInt(ID);
                    } else {
                        id = Movie.NO_ID_AVAILABLE;
                    }

                    // extract "title" for the title of a Movie in the Db
                    String title;
                    if (oneMovie.has(TITLE)) {
                        title = oneMovie.getString(TITLE);
                    } else {
                        title = Movie.NO_TITLE_AVAILABLE;
                    }

                    // extract "original_title" for the original title of a Movie in the Db
                    String originalTitle;
                    if (oneMovie.has(ORIGINAL_TITLE)) {
                        originalTitle = oneMovie.getString(ORIGINAL_TITLE);
                    } else {
                        originalTitle = title;
                    }

                    // extract "vote_average" for the average vote of a Movie in the Db
                    String userRating;
                    if (oneMovie.has(VOTE_AVERAGE)) {
                        userRating = oneMovie.getString(VOTE_AVERAGE);
                    } else {
                        userRating = Movie.NO_USER_RATING_AVAILABLE;
                    }

                    // extract "vote_count" for the number of votes of a Movie in the Db
                    int voteCount;
                    if (oneMovie.has(VOTE_COUNT)) {
                        voteCount = oneMovie.getInt(VOTE_COUNT);
                    } else {
                        voteCount = Movie.NO_VOTE_COUNT_AVAILABLE;
                    }

                    // extract "poster_path" for the link to the poster of a Movie in the Db
                    String posterLink;
                    if (oneMovie.has(POSTER_PATH)) {
                        String partOfPosterLink = oneMovie.getString(POSTER_PATH);
                        posterLink = AppUtilities.buildPosterUrl(partOfPosterLink).toString();
                    } else {
                        posterLink = Movie.NO_POSTER_AVAILABLE;
                    }

                    // extract "overview" for the plot synopsis of a Movie in the Db
                    String plotSynopsis;
                    if (oneMovie.has(OVERVIEW)) {
                        plotSynopsis = oneMovie.getString(OVERVIEW);
                    } else {
                        plotSynopsis = Movie.NO_SYNOPSIS_AVAILABLE;
                    }

                    // Todo (102) should date be stored in another form?
                    // extract "release_date" for the release date of a Movie in the Db
                    String releaseDate;
                    if (oneMovie.has(RELEASE_DATE)) {
                        releaseDate = oneMovie.getString(RELEASE_DATE);
                    } else {
                        releaseDate = Movie.NO_RELEASE_DATE_AVAILABLE;
                    }

                    // extract "genre_ids" for the List of genre id's of a Movie
                    JSONArray jsonArrayOfIds;
                    int[] arrayOfIds;
                    if (oneMovie.has(GENRE)) {
                        jsonArrayOfIds = oneMovie.getJSONArray(GENRE);
                        arrayOfIds = new int[jsonArrayOfIds.length()];
                        for (int i = 0; i < jsonArrayOfIds.length(); i++) {
                            arrayOfIds[i] = jsonArrayOfIds.getInt(i);
                        }
                    } else {
                        // add an arbitrary int id. This way a default id String will be assigned afterwards.
                        arrayOfIds = new int[]{-1};
                    }

                    // create Movie object from the extracted data
                    Movie movie = new Movie(id, title, originalTitle, posterLink, plotSynopsis,
                            userRating, voteCount, releaseDate, arrayOfIds);

                    // add the object to List
                    movieList.add(movie);
                }
            } else if (jsonObject.has(RESULTS) && totalResults == 0) {
                jsonResponseCode = JSON_RESPONSE_NO_RESULTS;
            } else {
                jsonResponseCode = JSON_RESPONSE_ERROR;
            }
        } catch (JSONException json_exception) {
            jsonResponseCode = JSON_RESPONSE_ERROR;
            json_exception.printStackTrace();
            Log.e(TAG, "An exception was encountered while trying to read JSONString " + json_exception);
        }
        // return result of the method
        Pair<List<Movie>, Integer> pair = Pair.create(movieList, jsonResponseCode);
        return pair;
    }

    /**
     * Reads JSONString and extracts relevant data from it
     *
     * @param JSONString - result of the previous http query parsed into String format
     * @return Pair of two objects: {@link String} string with names of main members of the cast
     * & {@link String} string with the name(s) of director(s)
     */
    public static Pair<String, String> extractPersonsFromJSONString(String JSONString) {

        String cast = "";
        String director = "";

        String separator = ", ";

        try {
            // convert String to a JSONObject
            JSONObject jsonObject = new JSONObject(JSONString);

            if (jsonObject.has(CAST)) {

                JSONArray resultsArray = jsonObject.getJSONArray(CAST);

                int maxNumberOfPersons = 4; // max number of cast members to be displayed
                if (resultsArray.length() < maxNumberOfPersons) {
                    maxNumberOfPersons = resultsArray.length();
                }
                // Loop through each item in the array
                // Get cast member at position i, and parse through its attributes
                for (int item = 0; item < maxNumberOfPersons; item++) {
                    JSONObject castMember = resultsArray.getJSONObject(item);

                    // extract "name"
                    String name;
                    if (castMember.has(NAME)) {
                        name = castMember.getString(NAME);
                    } else {
                        name = "";
                    }
                    cast += name;
                }
            }

            if (jsonObject.has(CREW)) {

                JSONArray resultsArray = jsonObject.getJSONArray(CREW);

                // Loop through each item in the array
                // Get crew member at position i, and parse through its attributes
                for (int item = 0; item < resultsArray.length(); item++) {
                    JSONObject crewMember = resultsArray.getJSONObject(item);

                    // extract "name"
                    String name = null;
                    if (crewMember.has(JOB) && crewMember.has(NAME)) {
                        if (crewMember.getString(JOB).equals(DIRECTOR)) {
                            name = crewMember.getString(NAME);
                        }
                    }
                    if (name != null) {
                        director += name + separator;
                    }
                }
            }

        } catch (JSONException json_exception) {
            json_exception.printStackTrace();
            Log.e(TAG, "An exception was encountered while trying to read JSONString " + json_exception);
        }

        cast = cast.substring(0, cast.lastIndexOf(separator));
        director = director.substring(0, director.lastIndexOf(separator));

        if(cast.equals("")){
            cast = NOT_AVAILABLE;
        }
        if(director.equals("")){
            director = NOT_AVAILABLE;
        }

        // return result of the method
        Pair<String, String> pair = Pair.create(cast, director);
        return pair;
    }

}
