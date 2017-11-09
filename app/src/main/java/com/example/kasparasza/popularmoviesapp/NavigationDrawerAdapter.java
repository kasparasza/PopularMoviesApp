package com.example.kasparasza.popularmoviesapp;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * An adapter class that will populate NavigationDrawer
 */

public class NavigationDrawerAdapter extends ArrayAdapter<NavDrawerItem> {

    private static final String TAG = NavigationDrawerAdapter.class.getSimpleName();

    // class variables
    Context context;                   // context, where the NavDrawer will be used
    int layoutResource;                // The resource ID for a layout file containing a Layout for an Item of the NavDrawer;
    List<NavDrawerItem> objects;       // List of objects that each represent a line of NavigationDrawer

    // Constructor:
    public NavigationDrawerAdapter(Context context, int layoutResource, List<NavDrawerItem> objects) {
        super(context, 0, objects);
        this.context = context;
        this.layoutResource = layoutResource;
        this.objects = objects;
    }


    // method that will generate views for the adapter
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // instruct the method to reuse the views; if the view is null - we create a new one
        View listViewItem = convertView;
        if(listViewItem == null){
            listViewItem = LayoutInflater.from(context).inflate(layoutResource, parent, false);
        }

        // initialise and populate with data childViews of the newly created View
        TextView listItemName = (TextView) listViewItem.findViewById(R.id.nav_dr_tv_list_item_name);
        ImageView listItemIcon = (ImageView) listViewItem.findViewById(R.id.nav_dr_iv_list_item_icon);
        Integer iconResourceId = objects.get(position).getListItemIconResourceId();
        if(iconResourceId != null){
            listItemIcon.setImageResource(iconResourceId);
        }
        listItemName.setText(objects.get(position).getListItemName());

        // return the View
        return listViewItem;
    }
}
