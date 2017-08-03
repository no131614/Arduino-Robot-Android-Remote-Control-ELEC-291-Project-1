package com.led.ELEC291_PROJECT1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

/*This MainActivity class is the main class for the android app*/
public class MainActivity extends ActionBarActivity {

    //Initialize variables
    Button btnStart, btnAbout;
    Context context = this;

    //Sets all of the button functionality
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button to go to the DeviceList activity/start the app
        btnStart = (Button)findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), DeviceList.class);
                startActivity(i);//Open the DeviceList Page
            }
        });


        //Button to the About page
        btnAbout = (Button)findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new AlertDialog.Builder(context).setTitle("Version 1.0").setMessage("This app was created as a part of UBC ELEC 291 Project course.").setNeutralButton("Close", null).show();

            }
        });



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
