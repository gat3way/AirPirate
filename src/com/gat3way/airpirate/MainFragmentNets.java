package com.gat3way.airpirate;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
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
import android.content.Context;
import com.actionbarsherlock.app.SherlockFragment;


public class MainFragmentNets extends SherlockFragment
{
    private String str_options[];
    private ArrayAdapter<String[]> adapter;
    private ListView list;
    private Band band;
    
    public void addNetwork(String ssid,String extra)
    {
    	String[] toadd = new String[2];
    	toadd[0]=ssid;toadd[1]=extra;
    	adapter.add(toadd);
    	adapter.notifyDataSetChanged();
    }

    public void removeNetwork(String ssid)
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

    public void updateNetwork(String ssid,String extra)
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
    	//ArrayList<String> lst = new ArrayList<String>();
		//adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_2, lst);
    	
		//return super.onCreateView(inflater, container, savedInstanceState);
    	return inflater.inflate(R.layout.fragment_mainnets, null);
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
                return row;
	        }
	    };
    	list = (ListView)this.getActivity().findViewById(R.id.netList);
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
		    		  band.updateNetworksTimeStamp();
		    		  band.updateNetworksDetails();
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
    
/*
    @Override
    public void onListItemClick (ListView l, View v, int position, long id)
    {
    	int cnt;
    }
*/    
}