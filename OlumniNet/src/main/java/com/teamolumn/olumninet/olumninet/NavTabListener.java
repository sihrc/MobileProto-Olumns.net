package com.teamolumn.olumninet.olumninet;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;

import com.example.olumninet.R;

/**
 * Created by zach on 10/30/13.
 */
public class NavTabListener implements ActionBar.TabListener {

    public Fragment fragment;

    public NavTabListener(Fragment fragment){
        this.fragment = fragment;
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        ft.replace(R.id.fragmentContainer, fragment);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        ft.remove(fragment);
    }
}