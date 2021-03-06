package com.example.kasparasza.popularmoviesapp;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that defines a Movie object
 */

public class Movie implements Parcelable{

    private static final String TAG = Movie.class.getSimpleName();

    // static values used in packing Movie and other objects to Intents:
    public static final String MOVIE_OBJECT = "MOVIE_OBJECT";

    // static values used in parsing Movie objects in JSON:
    public static final int NO_ID_AVAILABLE = 0;
    public static final String NO_TITLE_AVAILABLE = "No title is available";
    public static final String NO_POSTER_AVAILABLE = "No poster available";
    public static final String NO_SYNOPSIS_AVAILABLE = "No plot synopsis is available.";
    public static final String NO_USER_RATING_AVAILABLE = "not available";
    public static final int NO_VOTE_COUNT_AVAILABLE = 0;
    public static final String NO_RELEASE_DATE_AVAILABLE = "date not available";

    // class variables:
    private int id;
    private String title;
    private String originalTitle;
    private String smallPosterLink;
    private String bigPosterLink;
    private String plotSynopsis;
    private String userRating;
    private int voteCount;
    private String releaseDate;
    private int[] genreIdList;

    // class constructors:
    public Movie(int id, String title, String originalTitle, String smallPosterLink,
                 String bigPosterLink, String plotSynopsis, String userRating,
                 int voteCount, String releaseDate, int[] genreIdList){
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.smallPosterLink = smallPosterLink;
        this.bigPosterLink = bigPosterLink;
        this.plotSynopsis = plotSynopsis;
        this.userRating = userRating;
        this.voteCount = voteCount;
        this.releaseDate = releaseDate;
        this.genreIdList = genreIdList;
    }


    /*
     * Getter & setter methods
     * */
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSmallPosterLink() {
        return smallPosterLink;
    }

    public String getBigPosterLink() {
        return bigPosterLink;
    }

    public String getPlotSynopsis() {
        return plotSynopsis;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public int[] getGenreIdList() {
        return genreIdList;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSmallPosterLink(String smallPosterLink) {
        this.smallPosterLink = smallPosterLink;
    }

    public void setBigPosterLink(String bigPosterLink) {
        this.bigPosterLink = bigPosterLink;
    }

    public void setPlotSynopsis(String plotSynopsis) {
        this.plotSynopsis = plotSynopsis;
    }

    public void setUserRating(String userRating) {
        this.userRating = userRating;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public void setGenreIdList(int[] genreIdList) {
        this.genreIdList = genreIdList;
    }

    /*****************************************************
     * Other methods implemented by the class
     ******************************************************
     * */

    /*
    * hashCode() & equals(Object obj) methods are overridden, so that it would be possible to compare
    * Movie objects between themselves
    * */
    @Override
    public int hashCode() {
        // id of each Movie is unique. If two Movie objects have the same id - they are two instances
        // of the same Movie. Thus, they should be assigned the same hashCode.
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Movie)){
            return false;
        }
        Movie anotherMovie = (Movie) obj;
        return this.hashCode() == anotherMovie.hashCode();
    }

    /*****************************************************
     * Methods that implement Parcelable interface
     ******************************************************
     * */

    // Interface that must be implemented (by a class that implements Parcelable) and provided
    // as a public CREATOR field that generates instances of the Parcelable class from a Parcel
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){

        // creates a new Movie object from a parcel
        @Override
        public Movie createFromParcel(Parcel source) {
            // basically, we refer the work to our constructor
            return new Movie(source);
        }

        // basically our Parcel is an Array of Movie objects
        // this method returns the size of this Array
        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    /*
    * constructor that creates Movie object from a Parcel as an input parameter
    *
    * NOTE: the order of variables in the body must match the order of variables in writeToParcel(),
    * */
    public Movie(Parcel inputParcel){
        id = inputParcel.readInt();
        title = inputParcel.readString();
        originalTitle = inputParcel.readString();
        smallPosterLink = inputParcel.readString();
        bigPosterLink = inputParcel.readString();
        plotSynopsis = inputParcel.readString();
        userRating = inputParcel.readString();
        voteCount = inputParcel.readInt();
        releaseDate = inputParcel.readString();
        genreIdList = inputParcel.createIntArray();
    }

    /*
    * Writes, the Movie class variables to a Parcel object
    * @parameter destinationParcel - a Parcel object into which the data will be stored
    * @parameter flags - Additional flags about how the object should be written. May be 0 or 1.
    * Not used in this method however.
    *
    * NOTE: as afterwards the Parcel object will be used as an input for Movie constructor,
    * the order of variables in writeToParcel() method body has to be similar to: public Movie(Parcel inputParcel)
    * */
    @Override
    public void writeToParcel(Parcel destinationParcel, int flags) {
        destinationParcel.writeInt(id);
        destinationParcel.writeString(title);
        destinationParcel.writeString(originalTitle);
        destinationParcel.writeString(smallPosterLink);
        destinationParcel.writeString(bigPosterLink);
        destinationParcel.writeString(plotSynopsis);
        destinationParcel.writeString(userRating);
        destinationParcel.writeInt(voteCount);
        destinationParcel.writeString(releaseDate);
        destinationParcel.writeIntArray(genreIdList);
    }

    @Override
    public int describeContents() {
        return 0; // a standard method implementation was left
    }
}
