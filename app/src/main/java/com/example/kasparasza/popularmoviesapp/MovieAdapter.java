package com.example.kasparasza.popularmoviesapp;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * An adapter class that will populate Recycler view of the main activity {@link AllMoviesActivity}
 * The implementation of the class has been in a major part based on Udacity.com application:
 * SunshineApp: https://github.com/udacity/ud851-Sunshine
 */

/*
* Brief explanation of the contents:
*
* @MovieAdapter - main class which extents RecyclerView.Adapter<ViewHolder> abstract class.
* The RecyclerView.Adapter class requires to implement 3 abstract methods: @onCreateViewHolder,
* @onBindViewHolder, @getItemCount - these methods populate the Views of the UI and track their IDs.
* The Views themselves are initialised by @MovieAdapterViewHolder class.
*
* @MovieAdapterViewHolder - an inner class (basically an independent class, that was documented as an
* inner class to keep the documentation more compact).
* It extends RecyclerView.ViewHolder and implements OnClickListener. The class: initialises the Views
 * of the UI (but does not populate them with info); tracks a position of each RecyclerView item
 * ViewHolder); if a RecyclerView item is clicked - it retrieves a related Movie Object and
 * provides it to the adapter (a class that implements @MovieAdapterOnClickHandler interface)
 *
 * @MovieAdapterOnClickHandler - a simple interface that defines onClick method
* */



public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder>{

    private static final String TAG = MovieAdapter.class.getSimpleName();

    // class variables
    private List<Movie> movieList; // it is not initialised in the constructor, instead setAdapterData()
                                    // method is used for that
    private MovieAdapterOnClickHandler adapterOnClickHandler; // interface that was defined in this
                                    // file below
    private Context context;        // Context will be passed to Picasso for ImageView inflation


    // constructor
    public MovieAdapter(MovieAdapterOnClickHandler adapterOnClickHandler){
        this.adapterOnClickHandler = adapterOnClickHandler;
    }


    ///// class methods (mostly those, which were required to be implemented by RecyclerView.Adapter
    ///// abstract class ///////////////////////////////////////////////////////////////////////////

    /*
    * Method that is called when the Adapter needs an additional ViewHolder to be made;
    * a new ViewHolder object is inflated
    * */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // parameters & objects that are necessary for the inflate() method
        context = parent.getContext();
        int layoutId = R.layout.all_movies_list_one_item_view;
        boolean attachToRoot = false;

        // LayoutInflater class object will be responsible for the inflation of our views
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate a View
        // method definition: inflate(int resource, ViewGroup root, boolean attachToRoot)
        View view = inflater.inflate(layoutId, parent, attachToRoot);

        // return new ViewHolder object
        return new MovieAdapterViewHolder(view);
    }

    /**
     * Method that is called by the RecyclerView to display the data at the specified
     * position. Basically, it displays Movie details in each ViewHolder object
     *
     * @param holder - ViewHolder that will be populated with Movie information.
     * @param position - position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MovieAdapterViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        String imageUrlLink = movie.getSmallPosterLink();

        // use of Picasso library to set ImageView
        // at first we check, whether the String with image link is not empty
        if (!imageUrlLink.matches(Movie.NO_POSTER_AVAILABLE)) {
            Picasso.with(context)
                    .load(movie.getSmallPosterLink())
                    .resize((int) context.getResources().getDimension(R.dimen.iv_movie_poster_width),
                            (int) context.getResources().getDimension(R.dimen.iv_movie_poster_height))
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.no_image_available)
                    .centerCrop()
                    .into(holder.moviePoster);
        } else {
            Picasso.with(context)
                    .load(R.drawable.no_image_available)
                    .resize((int) context.getResources().getDimension(R.dimen.iv_movie_poster_width),
                            (int) context.getResources().getDimension(R.dimen.iv_movie_poster_height))
                    .centerCrop()
                    .into(holder.moviePoster);
        }
    }

    /*
    * Method that provides the Adapter with information on the number of Movie objects that are ready
    * to be displayed in the UI
    * */
    @Override
    public int getItemCount() {
        if(movieList != null){
            return movieList.size();
        }
        return 0;
    }

    // Todo(600) metodas pakeistas, bet neaisku, ar ok

    /**
     * Method that provides the Adapter access to the data (a List of Movie items), without which
     * it would have no data with which to populate our UI
     *
     * @param newMovieList The new data to be displayed.
     */
    public void setAdapterData(List<Movie> newMovieList) {
        // this method is called from our main activity  - @AllMoviesActivity. Every time we call this method,
        // its parameter - newMovieList - contains a full set of data. Therefore, we simply assign all the
        // list to the variable @movieList.
        movieList = newMovieList;
        notifyDataSetChanged();
    }


    ///// inner class @MovieAdapterViewHolder //////////////////////////////////////////////////////

    /**
     * Inner class that defines a ViewHolder which caches the children views for a the Movie list item.
     * Also it acts as an onClick listener for each of the itemViews
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements OnClickListener{

        // class variables
        private ImageView moviePoster;

        // constructor
        public MovieAdapterViewHolder(View itemView) {
            super(itemView);

            // initialise child views
            moviePoster = (ImageView) itemView.findViewById(R.id.iv_movie_poster_view);

            // set OnClick listener for the itemView as a whole
            itemView.setOnClickListener(this);
        }


        /*
        * Method that defines OnClick functionality of each of the itemViews
        *
        * @param viewThatWasClicked - view that was clicked
        * */
        @Override
        public void onClick(View viewThatWasClicked) {
            int positionOfAdapter = getAdapterPosition();
            Movie movieRelatedToClickedView = movieList.get(positionOfAdapter);

            // actually, we pass the implementation of the OnClick functionality to the activity
            // which implements @MovieAdapterOnClickHandler interface - in this case: @AllMoviesActivity
            adapterOnClickHandler.onClick(movieRelatedToClickedView);
        }
    }

    ///// interface @MovieAdapterOnClickHandler  ///////////////////////////////////////////////////

    /**
     * The interface that receives onClick messages.
     *
     * This interface will be implemented by the main activity {@link AllMoviesActivity} and, thus,
     * through the onClick method click functionality will be automatically implemented by the
     * main activity. Main activity will define the actual onClick functionality
     */
    public interface MovieAdapterOnClickHandler{
        void onClick(Movie movieThatWasClicked);
    }

    ///// other helper methods of the Adapter  ///////////////////////////////////////////////////

}
