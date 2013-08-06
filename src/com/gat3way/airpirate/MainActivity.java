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
import android.view.WindowManager.LayoutParams;

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

public class MainActivity extends SherlockFragmentActivity 
{
    private ActionBar mActionBar; 
    private ViewPager mPager;
    private MainFragmentPagerAdapter fragmentPagerAdapter;
    private int curpage = 0;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private PendingIntent mPermissionIntent;
    private UsbSource mUsbSource;
    private UsbManager mUsbManager;
    private MainActivity instance = this;

    
    // broadcast receiver
 	private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
 	{
	    public void onReceive(Context context, Intent intent) 
	    {
	    	String action = intent.getAction();
	    	if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) 
			{
         		updateDeviceString("Attached USB device"); 
         		UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
         		mUsbSource = new Rtl8192Card(mUsbManager,MainActivity.this);
         		if (mUsbSource.scanUsbDevice(device))
         		{
         			mUsbManager.requestPermission(device, mPermissionIntent);
         		}
			}
	    	if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) 
			{
         		updateDeviceString("No USB Device Attached");
         		updateDeviceStatusString("");
         		//cleanup
         		Band band = Band.instance();
         		if (band.getUsbSource()!=null) band.getUsbSource().stopped=true;
         		band.reset();
			}

	    	if (ACTION_USB_PERMISSION.equals(action)) 
 		    {
 		    	synchronized (this) 
 		    	{
 		    		UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
 		            if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
 		            {
 		            	if(device != null)
 		            	{
 		            		TextView text = (TextView)MainActivity.this.findViewById(R.id.deviceText);
 		            		int err = mUsbSource.attachUsbDevice(device);
 		            		if (err>0)
 		         			{
 		            			// TODO
 		            			Band band = Band.instance();
 		            			mUsbSource.stopped = false;
 		         			}
 		         			else 
 		         			{
 		         				//updateDeviceString("No Permission to access device!") 
 		         			}
 		                }
 		            } 
 		            else 
 		            {
 		                   //Log.d("", "permission denied for device " + device);
 		                   updateDeviceString("Permission denied for device "+device);
 		            }
 		        }
 		    }
 		}
 	};
    
    
 	
 	
 	public void updateDeviceString(String msg) 
 	{
 		TextView text = (TextView)findViewById(R.id.deviceText);
 		if (text!=null) text.setText(msg);
 	}
 	
 	public void updateDeviceStatusString(String msg) 
 	{
 		TextView text = (TextView)findViewById(R.id.deviceStatus);
 		if (text!=null) text.setText(msg);	} 	
 	
 	public void updateStatusString(String msg) 
 	{
 		TextView text = (TextView)findViewById(R.id.statusText);
 		if (text!=null) text.setText(msg);
 	}
 	
 	public void addNetwork(String network) 
 	{
 		MainFragmentNets nets = fragmentPagerAdapter.getMainFragmentNets();
 		nets.addNetwork(network,"");
 	}
 	
 	public void updateNetwork(String network,String extra) 
 	{
 		MainFragmentNets nets = fragmentPagerAdapter.getMainFragmentNets();
 		nets.updateNetwork(network,extra);
 	}
 	
 	public void removeNetwork(String network) 
 	{
 		MainFragmentNets nets = fragmentPagerAdapter.getMainFragmentNets();
 		nets.removeNetwork(network);
 	}
 	
 	public void addStation(String station) 
 	{
 		MainFragmentStations stations = fragmentPagerAdapter.getMainFragmentStations();
 		if (stations!=null) stations.addStation(station,"");
 	}
 	
 	public void updateStation(String station,String extra) 
 	{
 		MainFragmentStations stations = fragmentPagerAdapter.getMainFragmentStations();
 		if (stations!=null) stations.updateStation(station,extra);
 	}
 	
 	public void removeStation(String station) 
 	{
 		MainFragmentStations stations = fragmentPagerAdapter.getMainFragmentStations();
 		if (stations!=null)	stations.removeStation(station);
 	}
 	
 	// Handshake-related routines are a bit more special
 	public void addHandshake(final WPAHandshake handshake) 
 	{
 		Runnable run = new Runnable() {
            @Override
            public void run() 
            {
         		MainFragmentHandshakes hs = fragmentPagerAdapter.getMainFragmentHandshakes();
         		hs.addHandshake(handshake);
            }
		};
		runOnUiThread(run);
		run=null;
 	}
 	
 	public void updateHandshake(final WPAHandshake handshake) 
 	{
 		Runnable run = new Runnable() {
            @Override
            public void run() 
            {
         		MainFragmentHandshakes hs = fragmentPagerAdapter.getMainFragmentHandshakes();
         		hs.updateHandshake(handshake);
            }
		};
		runOnUiThread(run);
 	}
 	
 	public void removeHandshake(final WPAHandshake handshake) 
 	{
 		Runnable run = new Runnable() {
            @Override
            public void run() 
            {
         		MainFragmentHandshakes hs = fragmentPagerAdapter.getMainFragmentHandshakes();
         		hs.removeHandshake(handshake);
            }
		};
		runOnUiThread(run);
 	}
 	
 	
 	
 	public void onCapturePressed(View v)
 	{
 		Band band = Band.instance();
 		Button button = (Button)findViewById(R.id.captureButton);
 		TextView text = (TextView)findViewById(R.id.captureStatus);
 		if (band.capture==false)
 		{
 			button.setText("Stop Capture");
 			SimpleDateFormat s = new SimpleDateFormat("dd-MM-yyyy-hh:mm:ss");
 			String format = s.format(new Date());
 			String filename = "/sdcard/"+format+".pcap";
 			text.setText("Capturing: "+filename);
 			band.capture=true;
 			band.startCapture(filename);
 		}
 		else
 		{
 			button.setText("Start Capture");
 			text.setText("");
 			band.capture=true;
 			band.stopCapture();
 		}
 	}

 	
 	public void onSettingsPressed(View v)
 	{
 		Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
 	}

 	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// instantiate ui
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// register usb intents
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		registerReceiver(mUsbReceiver, filter);
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		
		// wakelock alternative 
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		
		mActionBar = getSupportActionBar();
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(5);
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
        fragmentPagerAdapter = new MainFragmentPagerAdapter(fm);
        //fragmentPagerAdapter.getItem(1);
        //fragmentPagerAdapter.getItem(2);
        //fragmentPagerAdapter.getItem(3);
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
                .setText("Adapter")
                .setTabListener(tabListener);
        mActionBar.addTab(tab);
        tab = mActionBar.newTab()
                .setText("Networks")
                .setTabListener(tabListener);
        mActionBar.addTab(tab);	
        tab = mActionBar.newTab()
                .setText("Stations")
                .setTabListener(tabListener);
        mActionBar.addTab(tab);	        
        tab = mActionBar.newTab()
                .setText("Handshakes")
                .setTabListener(tabListener);
        mActionBar.addTab(tab);        
    	// Start status update thread
		Thread thread = new Thread()
		{
		      @Override
		      public void run() 
		      {
		    	  Band band = Band.instance();
		    	  while (band.getUsbSource()==null)
		    	  {
		    		  SystemClock.sleep(300);
		    	  }
		    	  while (true)
		    	  {
		    		  SystemClock.sleep(1000);
		    		  int stations = band.stations;
		    		  int networks = band.nets;
		    		  int handshakes = band.handshakes;
		    		  long rx = band.rx;
		    		  String formatted="";
			    	  if (rx>1024*1024*1024)
			    	  {
			    		  formatted = String.format("%.02f", (double)((double)rx/(1024*1024*1024)))+" GB";
			    	  }
			    	  else if (rx>1024*1024)
			    	  {
			    		  formatted = String.format("%.02f", (double)((double)rx/(1024*1024)))+" MB";
			    	  }
			    	  else
			    	  {
			    		  formatted = String.format("%.02f", (double)((double)rx/(1024)))+" KB";
			    	  }
			    	  final String text="| RX: "+formatted+" | Networks: "+networks+" | Stations: "+stations+" | Handshakes: "+handshakes;
			    	  formatted=null;
			  		  if (band.getUsbSource()!=null)
			  		  {
				    	  Runnable run = 
			  			  new Runnable() 
				  		  {
				              @Override
				              public void run() 
				              {
				            	  updateStatusString(text);
				              }
				  		  };
				  		runOnUiThread(run);
				  		run=null;
			  		  }
			  		  else
			  		  {
			  			final String emptyText = "| RX: 0 | Networks: 0 | Stations: 0 | Handshakes: 0";
			  			Runnable run = new Runnable() 
				  		    {
				              @Override
				              public void run() 
				              {
				            	  updateStatusString(emptyText);
				              }
				  		    };
				  		runOnUiThread(run);
					  	run=null;
			  		  }
		    	  }
		      }
		};
		thread.start();
	}

	@Override
	public void onBackPressed() 
	{
		Band band = Band.instance();
		band.stopCapture();
		unregisterReceiver(mUsbReceiver);
		if (band.getUsbSource()!=null) band.getUsbSource().stopped=true;
		finish();
	}
}
