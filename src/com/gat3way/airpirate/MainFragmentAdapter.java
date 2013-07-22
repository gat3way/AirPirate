package com.gat3way.airpirate;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import com.actionbarsherlock.app.SherlockFragment;


public class MainFragmentAdapter extends SherlockFragment
{
    

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	return inflater.inflate(R.layout.fragment_mainadapter, null);
    }
    
    @Override
    public void onStart() 
    {
    	super.onStart();
    }
    @Override
	public void onResume() 
    {
		TextView text;
    	super.onResume();
        
		/*
    	text = (TextView)getActivity().findViewById(R.id.textView111);
		text.setTextSize(18);
		text.setText("SSID");
		*/
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
}