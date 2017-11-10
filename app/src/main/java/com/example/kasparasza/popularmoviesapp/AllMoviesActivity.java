package com.example.kasparasza.popularmoviesapp;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kasparasza.popularmoviesapp.utilities.AppUtilities;
import com.example.kasparasza.popularmoviesapp.utilities.EndlessRecyclerViewScrollListener;
import com.example.kasparasza.popularmoviesapp.utilities.JsonUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// todo (10): Left to implement, compared to project rubric:
// Movie details activity

// todo (10): Left to implement, compared to the example:
// SharedPreferences
// Options menu
// More versatile loading options
// method: invalidateData()

/*
* LoaderManager.LoaderCallbacks<T> - interface provides ability to perform network requests in a separate
* thread;
* MovieAdapter.MovieAdapterOnClickHandler - interface sets OnClick functionality to the items displayed
* by the RecyclerView in the Activity UI
* */


public class AllMoviesActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<Movie>>,
        MovieAdapter.MovieAdapterOnClickHandler {

    private static final String TAG = AllMoviesActivity.class.getSimpleName();

    // constants used as ID's, key names, etc.:
    private static int LOADER_ID_01 = 1;
    private static final String PAGE_NUMBER_KEY = "PAGE_NUMBER_KEY";
    private static final String CACHED_MOVIE_LIST_KEY = "CACHED_MOVIE_LIST_KEY";
    private static final String LINEAR_LAYOUT_MANAGER_KEY = "LINEAR_LAYOUT_MANAGER_STATE_KEY";
    private static final String ACTIVITY_MODE_KEY = "ACTIVITY_MODE_KEY";
    private static final String SEARCH_VIEW_MODE_KEY = "SEARCH_VIEW_MODE_KEY";
    private static final String SUBMITTED_SEARCH_QUERY_KEY = "SUBMITTED_SEARCH_QUERY_KEY";
    private static final String DEFAULT_QUERY_PATH = "DEFAULT_QUERY_PATH";

    // class variables, widgets used, etc.:
    private TextView errorMessageDisplayView;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private MovieAdapter movieAdapter;
    private Integer pageNumberBeingQueried = 1; // each JSON response provides only one page of data (20 items),
    // thus we will often will need make multiple http queries. Page number will be a parameter used in the queries.
    private ArrayList<Movie> cachedMovieData = new ArrayList<>(); // This List will help cache our Movie data,
    // so that numerous http requests are avoided
    private EndlessRecyclerViewScrollListener recyclerViewScrollListener;
    private static Integer jsonResponseCode = null;
    // variables used for Navigation Drawer implementation
    private DrawerLayout navDrLayout;
    private ListView navDrListView;
    private ActionBarDrawerToggle navDrToggle;
    List<NavDrawerItem> navDrawerItems;
    // variables used for SharedPreferences:
    private String sortOrderOfResults; // default sort order of Movies
    private SharedPreferences sharedPreferences;
    private ActionBar actionBar;
    // variables used for SearchView functionality
    private SearchView searchView;
    private String searchQueryToRestore = "";
    private String searchQueryToListen = "";
    private String searchQuerySubmitted = "";
    private boolean searchModeIsOn;
    private boolean searchQueryIsSubmitted;
    private FrameLayout rootLayout;

    // todo(11): movieAdapter needs to implement further methods (e.g. invalidate(), etc)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_movies);

        // initialisation of objects
        errorMessageDisplayView = (TextView) findViewById(R.id.tv_display_http_error_message);
        progressBar = (ProgressBar) findViewById(R.id.pb_movie_db_query);
        recyclerView = (RecyclerView) findViewById(R.id.rv_all_movies_grid);
        navDrLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navDrListView = (ListView) findViewById(R.id.lv_navigation_drawer);
        actionBar = getSupportActionBar();
        rootLayout = (FrameLayout) findViewById(R.id.fl_root_layout);

        // restoring instance state data (in all cases where this is not an initial start of our Activity)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PAGE_NUMBER_KEY)) {
                // page number that was last used in http query
                pageNumberBeingQueried = savedInstanceState.getInt(PAGE_NUMBER_KEY);
            }
            if (savedInstanceState.containsKey(CACHED_MOVIE_LIST_KEY)) {
                // ArrayList of Movie objects that has been already loaded
                cachedMovieData = savedInstanceState.getParcelableArrayList(CACHED_MOVIE_LIST_KEY);
            }
            if (savedInstanceState.containsKey(ACTIVITY_MODE_KEY)){
                // activity mode (in search view or otherwise (default))
                searchModeIsOn = savedInstanceState.getBoolean(ACTIVITY_MODE_KEY, false);
            }
            if (savedInstanceState.containsKey(SEARCH_VIEW_MODE_KEY)){
                // searchView mode (search query is either submitted or not)
                searchQueryIsSubmitted = savedInstanceState.getBoolean(SEARCH_VIEW_MODE_KEY, false);
            }
            if (savedInstanceState.containsKey(SearchManager.QUERY)){
                searchQueryToListen = savedInstanceState.getString(SearchManager.QUERY);
                searchQueryToRestore = searchQueryToListen;
            }
            if(savedInstanceState.containsKey(SUBMITTED_SEARCH_QUERY_KEY)){
                searchQuerySubmitted = savedInstanceState.getString(SUBMITTED_SEARCH_QUERY_KEY);
            }
        }

        // Todo (13) Logging of our Parameters - to be removed

        Log.d(TAG, "SITUACIJA 1: searchMode = " + searchModeIsOn + " ar submitted = " + searchQueryIsSubmitted + " pati query = " + searchQueryToRestore);

        Log.d(TAG, "My parameters are: sizeOfList = " + cachedMovieData.size() +
                " / page number = " + pageNumberBeingQueried);
        String movieTitles = "";
        for (Movie m : cachedMovieData) {
            movieTitles += m.getTitle() + "\n";
        }
        Log.d(TAG, "My parameters are: Movies in list = " + movieTitles);


        // --------- set up of Shared Preferences (start) ----------

        // note: as there is only one SharedPreferences file for this activity, we use getPreferences()
        // instead of getSharedPreferences();
        sharedPreferences = getPreferences(MODE_PRIVATE);

        // Restore the preferences
        // note: if no key is found - then the following default value will be used: @AppUtilities.QUERY_PATH_POPULAR
        sortOrderOfResults = sharedPreferences.getString(DEFAULT_QUERY_PATH, AppUtilities.QUERY_PATH_POPULAR);

        // --------- set up of Shared Preferences (end) ----------

        // set the label for the Activity (depending on the @sortOrderOfResults)
        setLabelForActivity();

        // --------- set up of the recycler view (start) ----------

         /*
         * LayoutManager is responsible for measuring and positioning item views within a
         * RecyclerView. Here LayoutManager will be implemented as GridLayoutManager.
         *
         * Parameters used in GridLayoutManager constructor:
         * Context context;
         * int numberOfColumns (spanCount) - The number of columns (VERTICAL) or rows (HORIZONTAL) in the grid;
         * int orientation - HORIZONTAL or VERTICAL;
         * boolean reverseLayout - When set to true, layouts from end to start. Generally, this is only
         *              true with layouts that need to support a right-to-left layout.
         */

        // Todo(21): NavigationDrawer follows design guidelines

        // calculate the number of columns (span size) to be displayed
        int numberOfColumns = AppUtilities.calculateNoOfColumns(this);

        // Construct the LayoutManager(Context context, int numberOfColumns, int orientation, boolean reverseLayout)
        LinearLayoutManager layoutManager
                = new GridLayoutManager(this, numberOfColumns, LinearLayoutManager.VERTICAL, false);

        // Set the LayoutManager to our RecyclerView
        recyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        recyclerView.setHasFixedSize(true);

        /*
         * The MovieAdapter is responsible for linking our Movie data with the Views that
         * will end up displaying it.
         */
        movieAdapter = new MovieAdapter(this);

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(movieAdapter);

        // --------- set up of the recycler view (end) ----------

        // --------- set up of the endless scroll listener (start) ----------

        /*
        * EndlessRecyclerViewScrollListener is responsible for triggering an additional load  of
        * Movie objects for display in the UI
        * */
        recyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager, pageNumberBeingQueried) {

                    // implementation of the abstract method, the one that is actually responsible for
                    // triggering the additional data loading
                    @Override
                    public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        pageNumberBeingQueried = page;

                        // Todo (13) Logging of our Parameters - to be removed
                        Toast.makeText(getApplicationContext(), "onLoadMore was called; page: " + page, Toast.LENGTH_SHORT).show();

                        /*
                        * Loader call - case 2 of 3
                        * We call loader methods in case our recycler view is running out of items for display
                        * */
                        manageLoaders();
                    }

                    // Todo(35) is this still necessary, is it working as intended

                    // we override another method, which will be responsible to track situations where
                    // we have reached the bottom of the data list, however, there is no network connection
                    // for additional data to be loaded
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        // canScrollVertically(1) - checks that vertical scrolling is possible, where constant "int 1"
                        // denotes bottom direction
                        if (!recyclerView.canScrollVertically(1) &&
                                !AppUtilities.checkNetworkConnection(getApplicationContext()) &&
                                cachedMovieData.size() != 0) {
                            Toast.makeText(recyclerView.getContext(), getString(R.string.tv_no_network_connection), Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        recyclerView.addOnScrollListener(recyclerViewScrollListener);


        // --------- set up of the endless scroll listener (end) ----------

        // --------- set up of the Navigation Drawer (start) ----------

        // first prepare the List of NavDrawerItem objects to be used by the Navigation Drawer
        prepareNavDrawerContents();

        // Set the adapter for the list view of Navigation Drawer
        navDrListView.setAdapter(new NavigationDrawerAdapter(this,
                R.layout.all_movies_nav_drawer_list_one_item_view,
                navDrawerItems));

        // Set the click listener for the list view of Navigation Drawer
        navDrListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // obtain the name of the clicked list view item
               String name = navDrawerItems.get(position).getListItemName();
               // call a helper method to perform a corresponding action
               performActionOnNavDrawerItem(name);
            }
        });

        // this will allow users to open and close the Navigation Drawer by touching the app icon
        navDrToggle = new ActionBarDrawerToggle(
                this,                                       /* host Activity */
                navDrLayout,                                /* DrawerLayout object */
                R.string.nav_dr_accessibility_open_drawer,  /* "open drawer" description */
                R.string.nav_dr_accessibility_close_drawer  /* "close drawer" description */
        ){
            // Todo(26) add code if needed, or delete {...}
        };

        // Set the drawer toggle as the NavigationDrawerListener - it will allow to use an icon
        // in the ActionBar to open / close the NavigationDrawer
        navDrLayout.addDrawerListener(navDrToggle);

        // this tells to display the icon in the ActionBar
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);


        // --------- set up of the Navigation Drawer (end) ----------

        /*
        * Loader call - case 1 of 3
        * We call loader methods in case our recycler view has no data for display
        * */
        // if we have no data to display
        if (cachedMovieData.size() == 0) {
            // Call loader methods - if there is network connection
            if (AppUtilities.checkNetworkConnection(getApplicationContext())) {
                manageLoaders();
            } else {
                // If there is no network connection - display error message in TextView
                errorMessageDisplayView.setText(R.string.tv_no_network_connection);
                errorMessageDisplayView.setVisibility(View.VISIBLE);
            }
            // if we have data to display
        } else {
            // we have cached data - provide it to our adapter
            movieAdapter.setAdapterData(cachedMovieData);
            // If there is no network connection - the user will be informed via Toast.
            // This action will be performed by the @EndlessRecyclerViewScrollListener
        }
    }

    /*****************************************************
     * Methods that control UI functionality
     ******************************************************
     * */

    /*
    * Sets the results, loaded by the Loader, visible
    * */
    private void showLoadedResults() {
        recyclerView.setVisibility(View.VISIBLE);
        errorMessageDisplayView.setVisibility(View.INVISIBLE);
    }

    /*
    * Sets the Loader results, invisible. Error message is displayed
    * */
    private void showLoadingErrorMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        errorMessageDisplayView.setText(R.string.tv_error_loading_data);
        errorMessageDisplayView.setVisibility(View.VISIBLE);
    }

    /*
    * sets the label for the Activity
    * */
    private void setLabelForActivity(){
        String label;
        if(sortOrderOfResults.equals(AppUtilities.QUERY_PATH_POPULAR)){
            label = getString(R.string.label_all_movies_activity_most_popular_movies);
        } else {
            label = getString(R.string.label_all_movies_activity_top_rated_movies);
        }
        actionBar.setTitle(label);
    }


    /*****************************************************
     * LoaderManager methods - with these our LoaderManager will have
     * a control upon all the Loaders used in the Activity
     ******************************************************/

    // note: usually this should be a part of OnCreate method. Documented in a separate  method for more clarity.
    private void manageLoaders() {

        // note: null is used in place of a Bundle object since all additional
        // parameters for Loader are global variables

        // get LoaderManager and initialise the loader
        if (getSupportLoaderManager().getLoader(LOADER_ID_01) == null) {
            getSupportLoaderManager().initLoader(LOADER_ID_01, null, this);
        } else {
            getSupportLoaderManager().restartLoader(LOADER_ID_01, null, this);
        }
    }


    /*****************************************************
     * Methods responsible for AsyncTaskLoader lifecycle management
     ******************************************************
     * */

    /*
    * Creates an instance of AsyncTaskLoader
    *
    * @param id - unique id for each loader
    * @param args - an optional set of additional parameters passed to a Loader
    * @return Loader that will load a List of Movie objects
    * */
    @Override
    public Loader<List<Movie>> onCreateLoader(final int id, final Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {

            @Override
            protected void onStartLoading() {
                // set the progress bar to be visible and proceed with loading
                progressBar.setVisibility(View.VISIBLE);
                forceLoad();
            }

            // Method that will perform the actual url http request in another thread
            @Override
            public List<Movie> loadInBackground() {

                // obtain the Url, used for the http request
                URL url = AppUtilities.buildUrl(pageNumberBeingQueried, sortOrderOfResults, searchQuerySubmitted);

                // perform the url request
                String jsonResponseString = null;
                try {
                    jsonResponseString = AppUtilities.getResponseFromHttpUrl(url);
                } catch (IOException io_exception) {
                    io_exception.printStackTrace();
                }

                // initialise the return object
                List<Movie> movieList = null;

                // if the response String is not null - parse it
                if (jsonResponseString != null) {
                    // call helper method to parse JSON
                    Pair<List<Movie>, Integer> result = JsonUtilities.extractFromJSONString(jsonResponseString);
                    movieList = result.first;
                    jsonResponseCode = result.second;
                }
                return movieList;
            }

        };
    }

    // method checks whether the loaded data is not null or empty & updates the UI with the results
    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> data) {
        // loading has finished, so progress barr has to become invisible
        progressBar.setVisibility(View.INVISIBLE);

        // display either the loaded content, or the error message
        if (data != null && data.size() > 0) {
            showLoadedResults();

            // since data loading is being performed multiple times (each time a new page with JSON data is loaded)
            // we add the new data to our main data set. As a precaution to avoid bugs (e.g. the same Movie objects are
            // being added / duplicated) we check that our main data set does not already contain similar items.
            // note: for the check to work, Movie class has to override methods: hashCode() & equals().
            for (Movie movie : data) {
                if (!cachedMovieData.contains(movie)) {
                    cachedMovieData.add(movie);
                }
            }

            // update the data set of the adapter
            movieAdapter.setAdapterData(cachedMovieData);

        } else if (jsonResponseCode.equals(JsonUtilities.JSON_RESPONSE_NO_RESULTS)) {

                Toast.makeText(this, "NO RESULTS", Toast.LENGTH_SHORT).show(); //----------------------------------------- REPLACE WITH A VIEW

        } else {
            showLoadingErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        loader.reset();
    }

    /*****************************************************
     * Methods responsible for OnClick handling functionality - interface: MovieAdapterOnClickHandler
     ******************************************************
     * */

    /*
    * Main onClick method opens a new Activity which displays detailed info about a particular Movie
    * The method gets called by @MovieAdapter class. Further explanation is in this further class.
    *
    * @param movieThatWasClicked - Movie object that was obtained by the Adapter
    * */
    @Override
    public void onClick(Movie movieThatWasClicked) {
        // create an Intent to start a new Activity
        Intent openMovieDetailsActivity = new Intent(this, MovieDetailsActivity.class);
        // pass additional data to the Intent - in this case - the selected Movie object
        openMovieDetailsActivity.putExtra(Movie.MOVIE_OBJECT, movieThatWasClicked);
        // start the new Activity
        startActivity(openMovieDetailsActivity);
    }

    /*****************************************************
     * Helper methods for implementation of Navigation Drawer
     ******************************************************
     * */

    /*
    * initiates an action after one of the items of Navigation Drawer is clicked
    * @param navDrItemPosition - position (from top to bottom) of the clicked item
    * */
    private void performActionOnNavDrawerItem(String nameOfTheClickedItem){
        if(nameOfTheClickedItem.equals(getString(R.string.nav_dr_list_item_01_most_popular))){

            // dismiss the search mode if one was on
            searchModeIsOn = false;
            // change the sort order of results that are being queried by the Loader
            sortOrderOfResults = AppUtilities.QUERY_PATH_POPULAR;
            // call helper method to perform further actions
            performNewQuery();

        } else if (nameOfTheClickedItem.equals(getString(R.string.nav_dr__list_item_02_top_rated))){

            // dismiss the search mode if one was on
            searchModeIsOn = false;
            // change the sort order of results that are being queried by the Loader
            sortOrderOfResults = AppUtilities.QUERY_PATH_TOP_RATED;
            // call helper method to perform further actions
            performNewQuery();

        } else if (nameOfTheClickedItem.equals(getString(R.string.nav_dr__list_item_03_search))){
            // Todo(23) remove toast and change code;
            Toast.makeText(this, nameOfTheClickedItem, Toast.LENGTH_SHORT).show();

        } else if (nameOfTheClickedItem.equals(getString(R.string.nav_dr__list_item_04_about))) {

            // open a new activity
            Intent openActivity = new Intent(this, AboutActivity.class);
            startActivity(openActivity);
        }
        // after handling the click, we close the NavigationDrawer
        navDrLayout.closeDrawers();
    }

    /*
    * resets our current objects and calls Loader to initiate new query
    * */
    private void performNewQuery(){

        // update the label of the Activity
        setLabelForActivity();
        // reset the page number that is being used in queries
        pageNumberBeingQueried = 1;
        // clear the ArrayList of Movie objects
        cachedMovieData.clear();
        // notify our MovieAdapter about this
        movieAdapter.notifyDataSetChanged();
        // reset endless scroll listener when performing a new search
        recyclerViewScrollListener.resetState();

        /*
        * Loader call - case 3 of 3
        * We call loader methods as we have just performed reset of the data set
        * */
        // initiate a new query
        manageLoaders();

    }

    /*
    * prepares a List that contains @NavDrawerItem objects
    * */
    private void prepareNavDrawerContents(){
        navDrawerItems = new ArrayList<>();
        navDrawerItems.add(new NavDrawerItem(getString(R.string.nav_dr_list_item_01_most_popular)));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.nav_dr__list_item_02_top_rated)));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.nav_dr__list_item_03_search), R.drawable.ic_action_search));
        navDrawerItems.add(new NavDrawerItem(getString(R.string.nav_dr__list_item_04_about), R.drawable.ic_action_info));
    }

    /*
    * Restores the state of NavigationDrawer after an orientation change
    * */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        navDrToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        navDrToggle.onConfigurationChanged(newConfig);
    }


    /*****************************************************
     * Methods that implement Options Menu actions
     ******************************************************
     * */

    /*
    * Inflates options Menu
    * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Todo(32) gather all search view code in one place

        // further code, initializes SearchView menu item:
        // Get the SearchView and set the searchable configuration
        final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // further customization of the SearchView
        searchView.setIconifiedByDefault(true);
        searchView.setQueryHint(getString(R.string.search_hint_text_for_search_widget));
        int width = AppUtilities.getDisplayWidth(this);
        searchView.setMaxWidth(width);
        // set a Listener to the SearchView so that the Search query being entered could be persisted
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            /*
            * Method that tracks the state of SearchView (whether the user has submitted the query)
            * */
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchQueryToRestore = query;
                // update the state variable
                searchQueryIsSubmitted = true;
                // remove focus from SearchView
                rootLayout.requestFocus();
                return false;
            }

            /*
            * Method that tracks the contents of search query being entered
            * */
            @Override
            public boolean onQueryTextChange(String newText) {
                // store the search query being entered in a global object that will be used to persist data
                // note: intentionally we use another string instead of @searchQueryToRestore, as SearchView
                // query is at first set to null (at the time SearchView is initialised), before setQuery()
                // method is called
                searchQueryToListen = newText;
                return false;
            }
        });


        // Todo(31) replace to the correct place in the code

        // listener that sets boolean values for @searchModeIsOn
        MenuItemCompat.setOnActionExpandListener(menu.findItem(searchView.getId()), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchModeIsOn = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                searchModeIsOn = false;


                searchQuerySubmitted = "";

                // Todo(36) do I want to perform the new query, maybe display the previous state

                performNewQuery(); //<--------------------------------------------------------------------------- ?????????????????????????????????????


                return true;
            }
        });

        // Restore the state of SearchView (if it was open before orientation change, or before
        // leaving the activity);
        if(searchModeIsOn){
            // expandActionView() expands / gives focus to the SearchView
            // note: it needs to be called before setQuery() method
            menu.findItem(searchView.getId()).expandActionView();
            searchView.setQuery(searchQueryToRestore, false);
            // restore the focus state
            if(searchQueryIsSubmitted){
                // if submitted - remove the focus (also removes the soft keyboard)
                searchView.clearFocus();
                rootLayout.requestFocus();
            }
        }
        return true;
    }

    /*
        * sets actions / functionality for the options menu items
        * currently we have only one option item - toggle button for Navigation Drawer
        * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // if user has clicked on NavigationDrawer
        if (navDrToggle.onOptionsItemSelected(item)) {
            // Pass the event to ActionBarDrawerToggle, if it returns
            // true, then it has handled the app icon touch event
            return true;
        }

        // if user has clicked on a SearchView icon
//        if (id == R.id.action_search){
//
//            // set a Listener to the SearchView so that the Search query being entered could be persisted
//            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//
//                /*
//                * Method that is required to be implemented by OnQueryTextListener
//                * We leave it unmodified, since SearchQuery submission is controlled by
//                * SearchManager and onNewIntent() method
//                * */
//                @Override
//                public boolean onQueryTextSubmit(String query) {
//                    return false;
//                }
//
//                /*
//                * Method that tracks the search query being entered
//                * */
//                @Override
//                public boolean onQueryTextChange(String newText) {
//                    // store the search query being entered in a global object that will be used to persist
//                    // data if the activity needs to be recreated
//                    searchQueryToListen = newText;
//                    return false;
//                }
//            });
//        }

        // Todo(26) 1) Handle your other action bar / menu items...; 2) if there are more items - replace the place of the method
        // Todo(27) ar gerai, kad search pas mane pasleptas nav drawer, o ne app bar? taip pakeiciamas default elgesys

        return super.onOptionsItemSelected(item);
    }

    /*****************************************************
     * Xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx -<-------------------------------------------
     ******************************************************
     * */


    @Override
    public void onNewIntent(Intent intent){
        setIntent(intent);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            searchQuerySubmitted = intent.getStringExtra(SearchManager.QUERY);

            // Note: no check for the query being null  is needed, as  source code of
            // SearchView deliberately checks against null and empty values


            //now you can display the results
            Toast.makeText(this, "My QUERY: " + searchQuerySubmitted, Toast.LENGTH_SHORT).show();


            performNewQuery();
        }
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

        // store the page number that has been used in the last query
        outState.putInt(PAGE_NUMBER_KEY, pageNumberBeingQueried);
        outState.putParcelableArrayList(CACHED_MOVIE_LIST_KEY, cachedMovieData);
        // store the state (e.g. scroll position) of the layout
        outState.putParcelable(LINEAR_LAYOUT_MANAGER_KEY, recyclerView.getLayoutManager().onSaveInstanceState());
        outState.putBoolean(ACTIVITY_MODE_KEY, searchModeIsOn);
        // store the SearchView state (query submitted vs not)
        // before saving, check whether the query was not modified by the used after its submission
        if(!searchQueryToRestore.equals(searchQueryToListen)){
            searchQueryIsSubmitted = false;
        }
        outState.putBoolean(SEARCH_VIEW_MODE_KEY, searchQueryIsSubmitted);
        // store the search query (the one that was entered to a SearchView, but might not be submitted)
        outState.putString(SearchManager.QUERY, searchQueryToListen);
        // store the search query (the one that was submitted)
        outState.putString(SUBMITTED_SEARCH_QUERY_KEY, searchQuerySubmitted);
    }

    /*
    * Restore the values
    *
    * Note: majority of the values are restores in OnCreate method.
    * */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // In order to work the content of the recycler view has to be loaded before restoring the scroll position.
        // Our adapter has been initialised / supplied with the data in OnCreate, thus now we can restore
        // the scroll position
        Parcelable layoutManagerState = savedInstanceState.getParcelable(LINEAR_LAYOUT_MANAGER_KEY);
        recyclerView.getLayoutManager().onRestoreInstanceState(layoutManagerState);
    }

    /*
    * Commit changes made to SharedPreferences, so that these can be persisted
    * */
    @Override
    protected void onStop() {
        super.onStop();

        // We need an Editor object to make preference changes
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEFAULT_QUERY_PATH, sortOrderOfResults);

        // Commit the edits
        editor.apply();
    }

    /*****************************************************
     * Other helper methods
     ******************************************************
     * */


    /*
    * Returns the last @jsonResponseCode
    * */
    public static Integer getLastJsonResponseCode(){
        return jsonResponseCode;
    }
}
