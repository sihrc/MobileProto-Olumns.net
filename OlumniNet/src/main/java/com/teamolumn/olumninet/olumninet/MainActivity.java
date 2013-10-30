package com.teamolumn.olumninet.olumninet;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
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

        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ListsFragment listsFragment = new ListsFragment();
        EventsFragment eventsFragment = new EventsFragment();
        ProfileFragment profileFragment = new ProfileFragment();

        ActionBar.Tab listsTab = actionBar.newTab().setText(R.string.tab1);
        listsTab.setTabListener(new NavTabListener(listsFragment));

        ActionBar.Tab eventsTab = actionBar.newTab().setText(R.string.tab2);
        eventsTab.setTabListener(new NavTabListener(eventsFragment));

        ActionBar.Tab profileTab = actionBar.newTab().setText(R.string.tab3);
        profileTab.setTabListener(new NavTabListener(profileFragment));

        actionBar.addTab(listsTab);
        actionBar.addTab(eventsTab);
        actionBar.addTab(profileTab);

        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.android_dark_blue)));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
