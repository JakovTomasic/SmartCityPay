package com.sser.smartcity.smartcitypay;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

        // Set listener for deleting plate
        View deletePlateView = currentListView.findViewById(R.id.delete_plate_clickable_image_view);
        deletePlateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Add plate deletion to the cloud and show "Done" Toast afterwards
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // TODO: this can last very long time, show progress bar on click
                            JsonHandler.setNewPlateData(AppData.userPlates.get(position).getPlate(), false);

                            AppData.currentActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        AppData.userPlates.remove(position);
                                        HomeActivity.updatePlatesList();
                                    } catch (Exception ignored) {}

                                    Toast.makeText(AppData.currentActivity, R.string.table_deleted, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        // Return view that will be displayed
        return currentListView;
    }

}
