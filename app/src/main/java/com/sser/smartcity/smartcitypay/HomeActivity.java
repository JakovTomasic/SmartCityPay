package com.sser.smartcity.smartcitypay;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


// First activity after user logs in
public class HomeActivity extends AppCompatActivity {

    // TODO: add remove plate button

    // Stores current plateAdapter for updating it from static methods (changing it's data)
    private static PlateAdapter plateAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        // Set plateAdapter for displaying all user plates
        ListView userPlatesLV = findViewById(R.id.user_plates_list_view);
        plateAdapter = new PlateAdapter(this, AppData.userPlates);
        userPlatesLV.setAdapter(plateAdapter);

        // Setup button for adding new plate
        View addPlateClickableIV = findViewById(R.id.add_plate_clickable_image_view);
        addPlateClickableIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPlateDialog();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Save this activity as the current one for accessing it later (from static context)
        AppData.currentActivity = this;
    }

    // Set options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    // Handle options menu action (there are two of them: options and profile)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout: // Log out user
                FirebaseHandler.logoutUser();
                // Close this activity (open login/main activity)
                HomeActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Update list view with all user plates
    static void updatePlatesList() {
        try {
            ((BaseAdapter) plateAdapter).notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Open dialog for new plate input
    private void showAddPlateDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners for the positive and negative buttons on the dialog.
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set view of a dialog with text input
        final View dialogLayout = getLayoutInflater().inflate(R.layout.layout_add_plate_dialog, null, false);
        builder.setView(dialogLayout);

        // Set title of a dialog
        builder.setTitle(R.string.add_plate);

        // Set buttons and their onClick listeners
        builder.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Get table from EditText and trim it (remove spaces at the begging and at the end)
                final String newPlate = ((TextView) dialogLayout.findViewById(R.id.add_plate_text_input_view)).getText().toString().trim();

                // If plate is valid, add it
                if(!newPlate.isEmpty()) {
                    // Add it to list and update ListView (for UI change)
                    AppData.userPlates.add(new Plate(newPlate));
                    updatePlatesList();

                    // Add plate to the cloud and show "Done" Toast afterwards
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JsonHandler.addUserPlate(newPlate);

                                AppData.currentActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AppData.currentActivity, R.string.table_saved, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }


                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "try again" button, so dismiss the dialog and stay in quiz.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Dialog can be closed on back button click or on outside click (this is default, maybe can be removed)
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);

        alertDialog.show();
    }

}
