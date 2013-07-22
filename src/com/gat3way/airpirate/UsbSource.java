package com.gat3way.airpirate;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.support.v4.content.LocalBroadcastManager;
import android.content.Intent;

abstract public class UsbSource {
	protected MainActivity mainActivity;
	private Band band;
	
	// USB manager
	protected UsbManager mUsbManager;
	// Link to service handler
	protected Handler mServiceHandler;
	protected Context mContext;
	
	protected UsbDevice mDevice;
	protected UsbDeviceConnection mConnection;
	protected UsbManager mManager;
	protected UsbEndpoint mBulkEndpoint;
	protected UsbEndpoint mInjBulkEndpoint;
	protected PendingIntent mPermissionIntent;
	
	// counters
	protected int mLastChannel;
	protected int bytes=0;
	protected int nets=0;
	protected int stations=0;
	protected ArrayList<String> ssids = new ArrayList<String>();
	protected ArrayList<String> stas = new ArrayList<String>();
	
	
	// Weak constructor
	public UsbSource(UsbManager manager) 
	{
		// Does nothing but give us a way to add to a list for searching for cards
		mUsbManager = manager;
		band = Band.instance();
		band.setUsbSource(this);
	}

	public UsbSource(UsbManager manager, MainActivity main) 
	{
		mUsbManager = manager;
		mainActivity = main;
		band = Band.instance();
		band.setUsbSource(this);
	}
	
	
	public UsbSource(UsbManager manager, Handler servicehandler, Context context) {
		
		mUsbManager = manager;
		mServiceHandler = servicehandler;
		mContext = context;
		band = Band.instance();
		band.setUsbSource(this);
	}
	
	public String getMacString(byte[] arr, int offset)
	{
		String s_mac;
		s_mac = "";
		for (int c=0;c<6;c++)
		{
			String fmt = String.format("%02x",arr[c+offset]);
			s_mac+=fmt;
			if (c!=5) s_mac+=":";
		}
		return s_mac;
	}
	
	
	abstract public UsbSource makeSource(UsbDevice device, UsbManager manager, Handler servicehandler, 
			Context context);

	// Grab a specific device we have permission for
	abstract public int attachUsbDevice(UsbDevice device);
	
	// Return devices we want to look at
	abstract public ArrayList<UsbDevice> scanUsbDevices();
	
	// Examine a device & ask for permission if we want to use it and don't have it already
	abstract public boolean scanUsbDevice(UsbDevice device);

	public void setChannel(int ch) 
	{
		mLastChannel = ch;
	}
	
	public void doShutdown()
	{
		// DUMMY
	}
	
	protected void updateDeviceString(String str)
	{
		mainActivity.updateDeviceString(str); 
	}

	protected void updateDeviceStringOnUi(String str)
	{
		final String text=str;
		mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateDeviceString(text);
            }
		});
	}
	
	protected void updateStatusString(String str)
	{
		mainActivity.updateStatusString(str); 
	}

	protected void updateStatusStringOnUi(String str)
	{
		final String text=str;
		mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateStatusString(text);
            }
		});
	}

	protected void updateDeviceStatusString(String str)
	{
		mainActivity.updateDeviceStatusString(str); 
	}

	protected void addNetwork(String network)
	{
		mainActivity.addNetwork(network); 
		band.updateNetworks(network, "");
	}

	protected void addNetworkOnUi(String network)
	{
		final String text=network;
		mainActivity.runOnUiThread(new Runnable() 
		{
            @Override
            public void run() {
            	mainActivity.addNetwork(text);
            }
		});
	}

	protected void updateNetwork(String network,String extra)
	{
		mainActivity.updateNetwork(network,extra); 
	}

	protected void updateNetworkOnUi(String network,String extra)
	{
		final String text=network;
		final String text2=extra;
		mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateNetwork(text,text2);
            }
		});
	}
	
	protected void removeNetwork(String network)
	{
		mainActivity.removeNetwork(network); 
	}

	protected void removeNetworkOnUi(String network)
	{
		final String text=network;
		mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mainActivity.removeNetwork(text);
            }
		});
	}


	protected void addStation(String station)
	{
		mainActivity.addNetwork(station); 
		band.updateNetworks(station, "");
	}

	protected void addStationOnUi(String station)
	{
		final String text=station;
		mainActivity.runOnUiThread(new Runnable() 
		{
            @Override
            public void run() {
            	mainActivity.addStation(text);
            }
		});
	}

	protected void updateStation(String station,String extra)
	{
		mainActivity.updateStation(station,extra); 
	}

	protected void updateStationOnUi(String station,String extra)
	{
		final String text=station;
		final String text2=extra;
		mainActivity.runOnUiThread(new Runnable() 
		{
            @Override
            public void run() {
            	mainActivity.updateStation(text,text2);
            }
		});
	}
	
	protected void removeStation(String station)
	{
		mainActivity.removeStation(station); 
	}

	protected void removeStationOnUi(String station)
	{
		final String text=station;
		mainActivity.runOnUiThread(new Runnable() 
		{
            @Override
            public void run() 
            {
            	mainActivity.removeStation(text);
            }
		});
	}
	
	
	
	protected void updateDeviceStatusStringOnUi(String str)
	{
		final String text=str;
		mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateDeviceStatusString(text);
            }
		});
	}
	
	public void parseFrame(byte[] buffer, int offset, int l)
	{
		// Beacon
		if (l>(24+12+10+offset))
		{
			if ((buffer[offset]==(byte)0x80)&&(buffer[offset+1]==(byte)0x00))
    		{
				int noffset = offset+24+12;
				int bssid_offset = offset+16; 
				String ssid="";
				int channel=0;
				int enctype=0; // 0-open 1-wep 2-wpa 3-wpa2
				while ((noffset+6)<l)
				{
					int type=buffer[noffset];
					int len=buffer[noffset+1];
					switch (type)
					{
						// SSID? 
						case 0:
							if ((len+noffset+2)<l)
							ssid = new String(buffer,noffset+2,len);
							break;
						case 3:
							channel = buffer[noffset+2];
							break;
						case (byte)0xdd:
							enctype = (buffer[noffset+7]==1) ? 2 : 3;
							break;
					}
					noffset+=(len+2);
				}
				band.updateNetworks(ssid, getMacString(buffer,bssid_offset));
    			Network network = band.getNetwork(getMacString(buffer,bssid_offset));
    			network.beacon++;
    			network.rx+=(l-offset);
    			network.channel = channel;
    			network.encType = enctype;
    			network.updateTimestamp();
    		}
			
		}
		// Probe Response
		if (l>(24+12+10+offset))
		{
    		if ((buffer[offset]==(byte)0x50)&&(buffer[offset+1]==(byte)0x00)&&(buffer[offset+24+12]==(byte)0))
    		{
    			int noffset = offset+24+12;
				int bssid_offset = offset+16; 
				String ssid="";
				int channel=0;
				int enctype=0; // 0-open 1-wep 2-wpa 3-wpa2
				while ((noffset+6)<l)
				{
					int type=buffer[noffset];
					int len=buffer[noffset+1];
					switch (type)
					{
						// SSID? 
						case 0:
							if ((len+noffset+2)<l)
							ssid = new String(buffer,noffset+2,len);
							break;
						case 3:
							channel = buffer[noffset+2];
							break;
						case (byte)0xdd:
							enctype = (buffer[noffset+7]==1) ? 2 : 3;
							break;
					}
					noffset+=(len+2);
				}
				band.updateNetworks(ssid, getMacString(buffer,bssid_offset));
    			Network network = band.getNetwork(getMacString(buffer,bssid_offset));
    			network.beacon++;
    			network.rx+=(l-offset);
    			network.channel = channel;
    			network.encType = enctype;
    			network.updateTimestamp();
    		}
		}
		// Data
		if (l>(24+offset))
		{
    		if (buffer[offset]==(byte)0x08)
    		{
				// Examine DS flag
    			int sta_offset=0,bssid_offset=0;
    			boolean handshake=false;
    			if ((buffer[offset+1]&2)==2)
    			{
    				sta_offset = offset+4;
    				bssid_offset=offset+10;
    			}
    			else 
    			{
    				sta_offset = offset+16;
    				bssid_offset=offset+4;
    			}
    			
    			// Examine if handshake
    			if ((offset+24+8+8)<l)
    			if (((byte)buffer[offset+24+6]==(byte)0x88)&&(((byte)buffer[offset+24+7]==(byte)0x8e)))
    			if ((byte)buffer[offset+24+8+1]==(byte)3)
    			{
    				handshake=true;
    			}
    			
				// Add new station?
    			Network network = band.getNetwork(getMacString(buffer, bssid_offset));
    			network.updateTimestamp();
    			network.data++;
    			network.rx+=(l-offset);
    			if (bssid_offset>0)
    			{
	    			String stastr = getMacString(buffer, sta_offset);
	    			if ((!stastr.equals("ff:ff:ff:ff:ff:ff"))&&(!stastr.substring(0,8).equals("01:00:5e"))&&(!stastr.substring(0,5).equals("33:33")))
	    			{
	    				network.updateStations(getMacString(buffer, sta_offset));
	    				network.updateStationData(getMacString(buffer, sta_offset));
	    				network.updateStationTimestamp(getMacString(buffer, sta_offset));
	    				network.updateStationRx(getMacString(buffer, sta_offset), l-offset);
	    			}
    			}
    			if (handshake)
    			{
    				network.handshake++;
    				network.updateStationHandshake(getMacString(buffer, sta_offset));
    			}
    		}
		}		
	}
	
	public void sendDeauth(String bssid, String hwaddr)
	{
		//TODO
	}
	
}