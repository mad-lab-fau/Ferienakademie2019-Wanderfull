package com.example.ARMap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class DisplaySavedTracks extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start the normal navigation toolbar
        setContentView(R.layout.activity_saved_tracks);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Manage the floating action button
        FloatingActionButton tracks_button = findViewById(R.id.tracks_button);
        tracks_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "There are no tracks to add", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        /*
        * List View Stuff is managed
        * */

        ListView mlistView = (ListView) findViewById(R.id.idListView);
        String[] trackList = new String[] {"Für Leistungssportler (Freitag)", "FA Sonntagswanderung", "Map 3"}; //Liste mit allen Tracks
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        //add the items from trackList to the list
        mlistView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, trackList));

        // onClick Listener that sends the clicked
        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                        Toast.LENGTH_SHORT).show();
                String sText = ((TextView) view).getText().toString();
                editor.putString("trackID", sText); //InputString: from the EditText
                editor.apply();
                finish(); // close the current activity
            }
        });


    }

}
