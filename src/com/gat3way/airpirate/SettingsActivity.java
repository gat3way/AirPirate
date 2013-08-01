package com.gat3way.airpirate;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.content.LocalBroadcastManager;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import android.hardware.usb.*;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.util.Log;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.view.View;

public class SettingsActivity extends SherlockFragmentActivity 
{
	private ActionBar mActionBar; 
	private ViewPager mPager;
	private SettingsFragmentPagerAdapter fragmentPagerAdapter;
	private int curpage = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// instantiate ui
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mPager = (ViewPager) findViewById(R.id.pagerSettings);
        mPager.setOffscreenPageLimit(3);
        FragmentManager fm = getSupportFragmentManager();
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position) 
            {
                super.onPageSelected(position);
                curpage = position;
                Log.w("","curpage="+curpage);
                mActionBar.setSelectedNavigationItem(position);
            }
        };
        mPager.setOnPageChangeListener(pageChangeListener);
        fragmentPagerAdapter = new SettingsFragmentPagerAdapter(fm);
        mPager.setAdapter(fragmentPagerAdapter);
        mActionBar.setDisplayShowTitleEnabled(true);
        ActionBar.TabListener tabListener = new ActionBar.TabListener() 
        {
            @Override
            public void onTabUnselected(Tab tab, FragmentTransaction ft) 
            {
            }
            @Override
            public void onTabSelected(Tab tab, FragmentTransaction ft) 
            {
                mPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabReselected(Tab tab, FragmentTransaction ft) {
            }
        };
        Tab tab = mActionBar.newTab()
                .setText("Channels")
                .setTabListener(tabListener);
        mActionBar.addTab(tab);
        Tab tab1 = mActionBar.newTab()
                .setText("Mode")
                .setTabListener(tabListener);
        mActionBar.addTab(tab1);
	}

}