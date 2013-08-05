package com.gat3way.airpirate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
public class MainFragmentPagerAdapter extends FragmentPagerAdapter
{
    final int PAGE_COUNT = 4;
    private MainFragmentAdapter androidFragment1;
    private MainFragmentNets androidFragment2;
    private MainFragmentStations androidFragment3;
    private MainFragmentHandshakes androidFragment4;
    
    
    public MainFragmentNets getMainFragmentNets()
    {
    	return androidFragment2;
    }

    public MainFragmentStations getMainFragmentStations()
    {
    	return androidFragment3;
    }

    public MainFragmentHandshakes getMainFragmentHandshakes()
    {
    	return androidFragment4;
    }
    
    public MainFragmentPagerAdapter(FragmentManager fm) 
    {
    	super(fm);
    }
    
    @Override
    public Fragment getItem(int arg0) 
    {
        Bundle data = new Bundle();
        switch(arg0){
            case 0:
                androidFragment1 = new MainFragmentAdapter();
                data.putInt("current_page", arg0+1);
                return androidFragment1;
            case 1:
                androidFragment2 = new MainFragmentNets();
                data.putInt("current_page", arg0+2);
                return androidFragment2;
            case 2:
                androidFragment3 = new MainFragmentStations();
                data.putInt("current_page", arg0+3);
                return androidFragment3;
            case 3:
                androidFragment4 = new MainFragmentHandshakes();
                data.putInt("current_page", arg0+3);
                return androidFragment4;                
        }
        return null;
    }
    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}