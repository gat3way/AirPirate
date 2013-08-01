package com.gat3way.airpirate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
public class SettingsFragmentPagerAdapter extends FragmentPagerAdapter
{
    final int PAGE_COUNT = 2;
    private SettingsFragmentChannels androidFragment1;
    private SettingsFragmentMode androidFragment2;
    
    
    public SettingsFragmentPagerAdapter(FragmentManager fm) 
    {
    	super(fm);
    }
    
    @Override
    public Fragment getItem(int arg0) 
    {
        Bundle data = new Bundle();
        switch(arg0)
        {
            case 0:
                androidFragment1 = new SettingsFragmentChannels();
                data.putInt("current_page", arg0+0);
                return androidFragment1;
            case 1:
                androidFragment2 = new SettingsFragmentMode();
                data.putInt("current_page", arg0+1);
                return androidFragment2;        
        }
        return null;
    }
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}