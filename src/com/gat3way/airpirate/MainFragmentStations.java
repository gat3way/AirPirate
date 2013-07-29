package com.gat3way.airpirate;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.view.ViewGroup;
import android.util.Log;
import java.util.LinkedList;
import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.TwoLineListItem;
import java.util.ArrayList;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.AdapterView.OnItemClickListener;
import com.actionbarsherlock.app.SherlockFragment;
import android.app.AlertDialog;



public class MainFragmentStations extends SherlockFragment implements OnItemClickListener 
{
    private String str_options[];
    private ArrayAdapter<String[]> adapter;
    private ListView list;
    private Band band;
    
    public void addStation(String ssid,String extra)
    {
    	String[] toadd = new String[2];
    	toadd[0]=ssid;toadd[1]=extra;
    	adapter.add(toadd);
    	adapter.notifyDataSetChanged();
    	toadd=null;
    }

    public void removeStation(String ssid)
    {
    	for (int a=0;a<adapter.getCount();a++)
    	{
    		if (adapter.getItem(a)[0].equals(ssid))
    		{
    			adapter.remove(adapter.getItem(a));
    		}
    	}
    	adapter.notifyDataSetChanged();
    }

    public void updateStation(String ssid,String extra)
    {
    	for (int a=0;a<adapter.getCount();a++)
    	{
    		if (adapter.getItem(a)[0].equals(ssid))
    		{
    			adapter.getItem(a)[1]=extra;
    		}
    	}
    	adapter.notifyDataSetChanged();
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
    	return inflater.inflate(R.layout.fragment_mainstations, null);
    }
    
    @Override
    public void onStart() 
    {
    	band = Band.instance();
    	
    	
    	final List<String[]> lst = new LinkedList<String[]>();
    	adapter = new ArrayAdapter<String[]>(getActivity().getBaseContext(),android.R.layout.simple_list_item_2,lst)
		{
	        @Override
	        public View getView(int position, View convertView, ViewGroup parent)
	        {
	        	TwoLineListItem row;            
	            if(convertView == null){
	                LayoutInflater inflater = (LayoutInflater)ApplicationContextProvider.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                row = (TwoLineListItem)inflater.inflate(android.R.layout.simple_list_item_2, null);                    
	            }else{
	                row = (TwoLineListItem)convertView;
	            }
	            row.getText1().setText(this.getItem(position)[0]);
	            row.getText2().setText(this.getItem(position)[1]);
	            final String text1 = this.getItem(position)[0];
	            final String text2 = this.getItem(position)[1];

	            row.setOnClickListener(new TwoLineListItem.OnClickListener() 
	            {  
	                @Override
	            	public void onClick(View v)
	                {
	                	String bssid = text2.substring(text2.indexOf("BSSID: ")+7, text2.indexOf("BSSID: ")+7+17);
	        	    	final String text3 = bssid;
	                	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        	        builder.setTitle("Deauth Attack");
	        	        builder.setMessage("Perform deauth attack on client "+text1+" associated with BSSID "+bssid);
	        	        builder.setCancelable(true);
	        	        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() 
	        	        {
	        	            public void onClick(DialogInterface dialog, int id) 
	        	            {
	        	                dialog.cancel();
	        	            }
	        	        });
	        	        builder.setPositiveButton("Confirm",new DialogInterface.OnClickListener() 
	        	        {
	        	        	public void onClick(DialogInterface dialog, int id) 
	        	            {
	    	                	Band band = Band.instance();
	    	                	band.deAuth(text3, text1);
	        	            }
	        	        });
	                	AlertDialog alert = builder.create();
	                    alert.show();

	                }
	             });
	            
	            
                return row;
	        }
	    };
    	list = (ListView)this.getActivity().findViewById(R.id.stationList);
    	list.setAdapter(adapter);
    	

    	// Start checker thread
		
    	Thread thread = new Thread()
		{
		      @Override
		      public void run() 
		      {
		    	  while (true)
		    	  {
		    		  SystemClock.sleep(1000);
		    		  // STA update
		    		  band.updateStationsTimeStamp();
		    		  band.updateStationsDetails();
		    		  if (band.getUsbSource()==null)
		    		  {
		    			  Runnable run = new Runnable() 
				  		  {
				              @Override
				              public void run() 
				              {
				    			  for (int i = 0; i < adapter.getCount(); i++) 
								  {
									  adapter.remove(adapter.getItem(i));
								  }
				              }
				  		  };
				  		  getActivity().runOnUiThread(run);
		    		  }
		    	  }
		      }
		};
		thread.start();
		
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
    
    @Override
    public void onItemClick (AdapterView<?> adapter, View arg1, int pos,long arg3)
    {
    	TwoLineListItem row = (TwoLineListItem)adapter.getItemAtPosition(pos);
    	Log.w("BLA",row.getText1()+" "+row.getText2());
    }
    
}