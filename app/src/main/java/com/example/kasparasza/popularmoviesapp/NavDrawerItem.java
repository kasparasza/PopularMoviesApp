package com.example.kasparasza.popularmoviesapp;

/**
 * Class that defines a NavigationDrawer item object
 */

public class NavDrawerItem {

    private static final String TAG = NavDrawerItem.class.getSimpleName();

    // class variables:
    private String listItemName;
    private Integer listItemIconResourceId;

    // class constructors:
    public NavDrawerItem(String listItemName){
        this.listItemName = listItemName;
        listItemIconResourceId = null;
    }

    public NavDrawerItem(String listItemName, Integer listItemIconResourceId){
        this.listItemName = listItemName;
        this.listItemIconResourceId = listItemIconResourceId;
    }

     /*
     * Getter methods
     * */
     public String getListItemName(){
         return listItemName;
     }

    public Integer getListItemIconResourceId() {
        return listItemIconResourceId;
    }
}
