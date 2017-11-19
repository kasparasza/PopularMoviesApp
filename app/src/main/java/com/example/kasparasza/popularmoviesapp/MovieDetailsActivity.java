package com.example.kasparasza.popularmoviesapp;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kasparasza.popularmoviesapp.utilities.AppUtilities;
import com.example.kasparasza.popularmoviesapp.utilities.JsonUtilities;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

public class MovieDetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Pair<String, String>> {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    // constants used as ID's, key names, etc.:
    private static int LOADER_ID_01 = 1;
    private static String MOVIE_CAST_STRING_KEY = "MOVIE_CAST_STRING_KEY";
    private static String MOVIE_DIRECTOR_STRING_KEY = "MOVIE_DIRECTOR_STRING_KEY";

    // class variables, widgets used, etc.:
    private TextView movieOriginalTitle;
    private TextView moviePlotSynopsis;
    private TextView movieUserRating;
    private TextView movieVoteCount;
    private TextView movieReleaseDate;
    private TextView movieGenre;
    private TextView movieDirector;
    private TextView movieCast;
    private ImageView moviePoster;
    private ActionBar actionBar;
    private Integer movieId = null;
    private String movieMainCastString;
    private String movieDirectorString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        // initialisation of objects
        movieOriginalTitle = (TextView) findViewById(R.id.tv_movie_original_title);
        moviePlotSynopsis = (TextView) findViewById(R.id.tv_movie_plot_synopsis);
        movieUserRating = (TextView) findViewById(R.id.tv_user_rating);
        movieVoteCount = (TextView) findViewById(R.id.tv_vote_count);
        movieReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        movieGenre = (TextView) findViewById(R.id.tv_genres);
        movieDirector = (TextView) findViewById(R.id.tv_movie_director);
        movieCast = (TextView) findViewById(R.id.tv_movie_cast);
        moviePoster = (ImageView) findViewById(R.id.iv_movie_poster_in_detailed_activity);
        actionBar = getSupportActionBar();

        // restore instance state of variables
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(MOVIE_CAST_STRING_KEY)){
                movieMainCastString = savedInstanceState.getString(MOVIE_CAST_STRING_KEY);
            }
            if(savedInstanceState.containsKey(MOVIE_DIRECTOR_STRING_KEY)){
                movieDirectorString = savedInstanceState.getString(MOVIE_DIRECTOR_STRING_KEY);
            }
        }

        // check whether we have a non empty data set provided by the Intent that started
        // the Activity. If that is the case - we extract the data from it
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.hasExtra(Movie.MOVIE_OBJECT)) {
                Movie movie = intent.getParcelableExtra(Movie.MOVIE_OBJECT);

                // Initiate LoaderManager and Loaders
                movieId = movie.getId();
                LoaderManager loaderManager = getSupportLoaderManager();
                if (loaderManager.getLoader(LOADER_ID_01) == null) {
                    loaderManager.initLoader(LOADER_ID_01, null, this);
                } else {
                    loaderManager.restartLoader(LOADER_ID_01, null, this);
                }

                // set up UI
                setUpViews(movie);
            }
        }

        //Todo (310) movie image has to be of a better quality?
    }


    /*****************************************************
     * Methods, responsible for UI implementation
     ******************************************************/

    /*
    * Links UI views with data to be displayed
    *
    * @parameters - Movie object that was received with a starting intent
    * */
    private void setUpViews(Movie movie) {
        if (!movie.getTitle().equals(Movie.NO_TITLE_AVAILABLE)) {
            actionBar.setTitle(movie.getTitle());
        }
        if (!movie.getTitle().equals(movie.getOriginalTitle())) {
            String text = getString(R.string.movie_details_original_title, movie.getOriginalTitle());
            movieOriginalTitle.setText(getStyledText(text));
        } else {
            movieOriginalTitle.setVisibility(View.GONE);
        }
        if (!movie.getPlotSynopsis().equals(Movie.NO_SYNOPSIS_AVAILABLE)) {
            String text = getString(R.string.movie_plot, movie.getPlotSynopsis());
            moviePlotSynopsis.setText(getStyledText(text));
        }
        if (!movie.getUserRating().equals(Movie.NO_USER_RATING_AVAILABLE)) {
            String text = getString(R.string.movie_details_rating, movie.getUserRating());
            movieUserRating.setText(getStyledText(text));
        }
        if (!movie.getReleaseDate().equals(Movie.NO_RELEASE_DATE_AVAILABLE)) {
            String text = getString(R.string.movie_details_release_date, movie.getReleaseDate());
            movieReleaseDate.setText(getStyledText(text));
        }
        // java.text.NumberFormat.getNumberInstance().format() is used to apply thousands separator to numbers
        String text = getString(R.string.movie_details_vote_count, java.text.NumberFormat.getNumberInstance().format(movie.getVoteCount()));
        movieVoteCount.setText(getStyledText(text));

        text = getString(R.string.movie_genre, getMovieGenres(movie.getGenreIdList()));
        movieGenre.setText(getStyledText(text));

        // Todo(302) Visa Picaso darba geriau iskelti i utilities
        // Todo(303) Image size in dp should be probably higher
        // use of Picasso library to set ImageView
        // at first we check, whether the String with image link is not empty
        String imageUrlLink = movie.getBigPosterLink();
        if (!imageUrlLink.matches(Movie.NO_POSTER_AVAILABLE)) {
            Picasso.with(this)
                    .load(imageUrlLink)
                    .resize((int) getResources().getDimension(R.dimen.iv_movie_poster_width),
                            (int) getResources().getDimension(R.dimen.iv_movie_poster_height))
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.no_image_available)
                    .centerInside()
                    .into(moviePoster);
        } else {
            Picasso.with(this)
                    .load(R.drawable.no_image_available)
                    .resize((int) getResources().getDimension(R.dimen.iv_movie_poster_width),
                            (int) getResources().getDimension(R.dimen.iv_movie_poster_height))
                    .centerCrop()
                    .into(moviePoster);
        }
        moviePoster.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBackgroundDark));
    }

    /*****************************************************
     * LoaderManager methods - with these our LoaderManager will have
     * a control upon all the Loaders used in the Activity
     ******************************************************/

    /**
    * Creates an instance of AsyncTaskLoader
    *
    * @param id - unique id for each loader
    * @param args - an optional set of additional parameters passed to a Loader
    * @return Loader that will load additional info about a particular Movie object
    * */
    @Override
    public Loader<Pair<String, String>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Pair<String, String>>(this) {

            // Control the start of Loading. Start only if the data is not already available
            @Override
            protected void onStartLoading() {
                if(movieMainCastString != null && movieMainCastString != null ){
                    deliverResult(new Pair<>(movieMainCastString, movieDirectorString));
                } else {
                    forceLoad();
                }

            }

            // Method that will perform the actual url http request in another thread
            @Override
            public Pair<String, String> loadInBackground() {

                // obtain the Url, used for the http request
                URL url = AppUtilities.buildCreditsUrl(movieId);

                // perform the url request
                String jsonResponseString = null;
                try {
                    jsonResponseString = AppUtilities.getResponseFromHttpUrl(url);
                } catch (IOException io_exception) {
                    io_exception.printStackTrace();
                }

                // initialise the return object
                Pair<String, String> result = null;

                // if the response String is not null - parse it
                if (jsonResponseString != null) {
                    // call helper method to parse JSON
                    result = JsonUtilities.extractPersonsFromJSONString(jsonResponseString);
                }
                return result;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Pair<String, String>> loader, Pair<String, String> data) {

        // update UI
        if(data != null){

            movieDirectorString = data.second;
            String text = getString(R.string.movie_directors, movieDirectorString);
            movieDirector.setText(getStyledText(text));

            movieMainCastString = data.first;
            text = getString(R.string.movie_cast, movieMainCastString);
            movieCast.setText(getStyledText(text));
        }
    }

    @Override
    public void onLoaderReset(Loader<Pair<String, String>> loader) {
        loader.reset();

        // set the Strings to null, otherwise forceLoad() will not be called
        movieDirectorString = null;
        movieMainCastString = null;
    }

    /*****************************************************
     * Methods responsible for saving variable values in case the Activity is destroyed
     ******************************************************
     * */

    /*
    * Saving the values to a Bundle
    * */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(MOVIE_CAST_STRING_KEY, movieMainCastString);
        outState.putString(MOVIE_DIRECTOR_STRING_KEY, movieDirectorString);
    }

    /*****************************************************
     * Other helper methods
     ******************************************************/

    /*
    * Returns text with formatting parameters applied (as was defined in String resources)
    *
    * @parameters textWithoutStyling
    * */
    private CharSequence getStyledText(String textWithoutStyling) {
        CharSequence styledText;
        if (Build.VERSION.SDK_INT >= 24) {
            styledText = Html.fromHtml(textWithoutStyling, Html.FROM_HTML_MODE_COMPACT);
        } else {
            styledText = Html.fromHtml(textWithoutStyling);
        }
        return styledText;
    }

    /*
    * Returns genre of the movie
    * @parameter idList - List with integers each denoting an id (predefined in TheMovieDb)
    * @return - String with genre name
    * */
    public String getMovieGenres(int[] idList) {
        String result = "";
        String separator = ", ";
        if(idList.length == 0){
            return result;
        }
        for (int id : idList) {
            switch (id) {
                case 28:
                    result += (this.getResources().getString(R.string.genre_action)) + separator;
                    break;
                case 12:
                    result += (this.getResources().getString(R.string.genre_adventure)) + separator;
                    break;
                case 16:
                    result += (this.getResources().getString(R.string.genre_animation)) + separator;
                    break;
                case 35:
                    result += (this.getResources().getString(R.string.genre_comedy)) + separator;
                    break;
                case 80:
                    result += (this.getResources().getString(R.string.genre_crime)) + separator;
                    break;
                case 99:
                    result += (this.getResources().getString(R.string.genre_documentary)) + separator;
                    break;
                case 18:
                    result += (this.getResources().getString(R.string.genre_drama)) + separator;
                    break;
                case 10751:
                    result += (this.getResources().getString(R.string.genre_family)) + separator;
                    break;
                case 14:
                    result += (this.getResources().getString(R.string.genre_fantasy)) + separator;
                    break;
                case 36:
                    result += (this.getResources().getString(R.string.genre_history)) + separator;
                    break;
                case 27:
                    result += (this.getResources().getString(R.string.genre_horror)) + separator;
                    break;
                case 10402:
                    result += (this.getResources().getString(R.string.genre_music)) + separator;
                    break;
                case 9648:
                    result += (this.getResources().getString(R.string.genre_mystery)) + separator;
                    break;
                case 10749:
                    result += (this.getResources().getString(R.string.genre_romance)) + separator;
                    break;
                case 878:
                    result += (this.getResources().getString(R.string.genre_science_fiction)) + separator;
                    break;
                case 10770:
                    result += (this.getResources().getString(R.string.genre_tv_movie)) + separator;
                    break;
                case 53:
                    result += (this.getResources().getString(R.string.genre_thriller)) + separator;
                    break;
                case 10752:
                    result += (this.getResources().getString(R.string.genre_war)) + separator;
                    break;
                case 37:
                    result += (this.getResources().getString(R.string.genre_western)) + separator;
                    break;
                default:
                    result += (this.getResources().getString(R.string.genre_unknown)) + separator;
                    break;
            }
        }
        result = result.substring(0, result.lastIndexOf(separator));
        return result;
    }

}
