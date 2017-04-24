package com.example.rajat.safehighwayapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class Em_contact extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_em_contact);
        final EditText edit = (EditText) findViewById(R.id.number_1);
        Button sv = (Button) findViewById(R.id.save_1);
        try {
            InputStream inputStream = openFileInput("number.txt");
            if (inputStream != null)
            {
                Intent intent = new Intent(Em_contact.this,MapsActivity.class);
                startActivity(intent);
            }
        } catch (FileNotFoundException e) {
            sv.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("number.txt", Context.MODE_PRIVATE));
                        try {
                            if (edit.getText().toString().length() != 10) {
                                Toast.makeText(getBaseContext(), "Invalid number!",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                outputStreamWriter.write(edit.getText().toString());
                                outputStreamWriter.close();
                                Toast.makeText(getBaseContext(), "Number saved!",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Em_contact.this, MapsActivity.class);
                                startActivity(intent);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
