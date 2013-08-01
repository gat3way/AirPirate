package com.gat3way.airpirate;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.view.ViewGroup.LayoutParams;
import android.content.Context;
import com.actionbarsherlock.app.SherlockFragment;


public class SettingsFragmentMode extends SherlockFragment
{
    private String str_options[];
    private Band band;
    



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	return inflater.inflate(R.layout.fragment_settingsmode, null);
    }
    
    @Override
    public void onStart() 
    {
    	band = Band.instance();
    	CheckBox checkbox = ( CheckBox )getActivity().findViewById( R.id.checkBoxWarMode);
    	checkbox.setChecked(band.warMode);
    	checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener()
    	{
    	    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    	    {
	            Band band = Band.instance();
    	        if ( isChecked )
    	        {
    	            band.warMode = true;
    	        }
    	        else band.warMode = false;
    	    }
    	});
    	super.onStart();
    }
    
    @Override
	public void onResume() 
    {
		super.onResume();
    }
    
    @Override
    public void onCreate(Bundle bundle) 
    {
    	if (bundle!=null)
    	{
    	}
    	super.onCreate(bundle);
    }
    @Override
    public void onSaveInstanceState(Bundle bundle) 
    {
    	  //bundle.putString("SSID", SSID);
    	  super.onSaveInstanceState(bundle);    
    }  
    
/*
    @Override
    public void onListItemClick (ListView l, View v, int position, long id)
    {
    	int cnt;
    }
*/    
}