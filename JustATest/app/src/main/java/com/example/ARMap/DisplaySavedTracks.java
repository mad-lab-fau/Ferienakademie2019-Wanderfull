package com.example.ARMap;

import android.content.Intent;
import android.os.Bundle;
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
        setContentView(R.layout.activity_saved_tracks);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton tracks_button = findViewById(R.id.tracks_button);
        tracks_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "There are no tracks to add", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        ListView mlistView = (ListView) findViewById(R.id.idListView);
        mlistView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                new String[] {"Für Leistungssportler (Freitag)", "FA Sonntagswanderung", "Map 3"}));

        mlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // When clicked, show a toast with the TextView text Game, Help, Home
                Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                        Toast.LENGTH_SHORT).show();
                String sText = ((TextView) view).getText().toString();
                Intent intent = null;
                if(sText.equals("Für Leistungssportler (Freitag)")) {
                    intent = new Intent(getBaseContext(), MainActivity.class);
                } else if(sText.equals("FA Sonntagswanderung")) {
                    intent = new Intent(getBaseContext(), MainActivity.class);
                } else if(sText.equals("Map 3")) {
                    intent = new Intent(getBaseContext(), MainActivity.class);
                }
                if(intent != null)
                    finish();
                    startActivity(intent);
            }
        });


    }

}
