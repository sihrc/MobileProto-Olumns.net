package com.teamolumn.olumninet.olumninet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.example.olumninet.R;
/**
 * Created by chris on 10/27/13.
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
