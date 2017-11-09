package com.example.kasparasza.popularmoviesapp;

import android.content.Intent;
import android.icu.text.NumberFormat;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MovieDetailsActivity extends AppCompatActivity {

    // class variables, widgets used, etc.:
    private TextView movieOriginalTitle;
    private TextView moviePlotSynopsis;
    private TextView movieUserRating;
    private TextView movieVoteCount;
    private TextView movieReleaseDate;
    private ImageView moviePoster;
    private ActionBar actionBar;

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
        moviePoster = (ImageView) findViewById(R.id.iv_movie_poster_in_detailed_activity);
        actionBar = getSupportActionBar();


        // checking whether we have a non empty data set provided by the Intent that started
        // the Activity. If that is the case - we extract the data from it
        if(getIntent() != null){
            Intent intent = getIntent();
            if(intent.hasExtra(Movie.MOVIE_OBJECT)){
                Movie movie = intent.getParcelableExtra(Movie.MOVIE_OBJECT);
                setUpViews(movie);
            }
        }
        //Todo (300) what home button and back button functionality should be?
    }

    /*
    * Links UI views with data to be displayed
    *
    * @parameters - Movie object that was received with a starting intent
    * */
    private void setUpViews(Movie movie){
        if(!movie.getTitle().equals(Movie.NO_TITLE_AVAILABLE)){
            actionBar.setTitle(movie.getTitle());
        }
        if(!movie.getTitle().equals(movie.getOriginalTitle())){
            String text = getString(R.string.movie_details_original_title, movie.getOriginalTitle());
            movieOriginalTitle.setText(getStyledText(text));
        } else {
            movieOriginalTitle.setVisibility(View.GONE);
        }
        if(!movie.getPlotSynopsis().equals(Movie.NO_SYNOPSIS_AVAILABLE)){
            moviePlotSynopsis.setText(movie.getPlotSynopsis());
        }
        if(!movie.getUserRating().equals(Movie.NO_USER_RATING_AVAILABLE)){
            String text = getString(R.string.movie_details_rating, movie.getUserRating());
            movieUserRating.setText(getStyledText(text));
        }
        if(!movie.getReleaseDate().equals(Movie.NO_RELEASE_DATE_AVAILABLE)){
            String text = getString(R.string.movie_details_release_date, movie.getReleaseDate());
            movieReleaseDate.setText(getStyledText(text));
        }
        // java.text.NumberFormat.getNumberInstance().format() is used to apply thousands separator to numbers
        String text = getString(R.string.movie_details_vote_count, java.text.NumberFormat.getNumberInstance().format(movie.getVoteCount()));
        movieVoteCount.setText(getStyledText(text));

        // Todo(302) Visa Picaso darba geriau iskelti i utilities
        // Todo(303) Image size in dp should be probably higher
        // use of Picasso library to set ImageView
        // at first we check, whether the String with image link is not empty
        String imageUrlLink = movie.getPosterLink();
        if (!imageUrlLink.matches(Movie.NO_POSTER_AVAILABLE)) {
            Picasso.with(this)
                    .load(movie.getPosterLink())
                    .resize((int) getResources().getDimension(R.dimen.iv_movie_poster_width),
                            (int) getResources().getDimension(R.dimen.iv_movie_poster_height))
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.no_image_to_download)
                    .centerInside()
                    .into(moviePoster);
        } else {
            Picasso.with(this)
                    .load(R.drawable.no_image_to_download)
                    .resize((int) getResources().getDimension(R.dimen.iv_movie_poster_width),
                            (int) getResources().getDimension(R.dimen.iv_movie_poster_height))
                    .centerInside()
                    .into(moviePoster);
        }
    }

    /*
    * Returns text with formatting parameters applied (as was defined in String resources)
    *
    * @parameters textWithoutStyling
    * */
    private CharSequence getStyledText(String textWithoutStyling){
        CharSequence styledText;
        if(Build.VERSION.SDK_INT >= 24){
            styledText = Html.fromHtml(textWithoutStyling, Html.FROM_HTML_MODE_COMPACT);
        } else {
            styledText = Html.fromHtml(textWithoutStyling);
        }
        return styledText;
    }
}
