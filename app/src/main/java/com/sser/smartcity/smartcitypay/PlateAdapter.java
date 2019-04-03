package com.sser.smartcity.smartcitypay;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

// Adapter for showing list of user plates in the ListView
public class PlateAdapter extends ArrayAdapter<Plate> {

    PlateAdapter(Activity context, ArrayList<Plate> plates) {
        super(context, 0, plates);
    }

    // For getting a view, this is called every time user scrolls ListView for
    // every new view that is starting to show (and while creating ListView)
    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        // Try to recycle other view
        View currentListView = convertView;
        if(currentListView == null) {
            // If view can't be recycled, get new one
            currentListView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_plate, parent, false);
        }

        // Set plate text
        TextView plateTV = currentListView.findViewById(R.id.plate_text_view);
        plateTV.setText(AppData.userPlates.get(position).getPlate());

        // Return view that will be displayed
        return currentListView;
    }

}
