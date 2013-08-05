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
import android.os.SystemClock;
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
	protected volatile boolean stopped = false;
	
	
	// counters
	protected int mLastChannel;
	protected int bytes=0;
	protected int nets=0;
	protected int stations=0;
	
	
	// Weak constructor
	public UsbSource(UsbManager manager) 
	{
		// Does nothing but give us a way to add to a list for searching for cards
		mUsbManager = manager;
		band = Band.instance();
		band.setUsbSource(this);
		sourceEnabled();
	}

	public UsbSource(UsbManager manager, MainActivity main) 
	{
		mUsbManager = manager;
		mainActivity = main;
		band = Band.instance();
		band.setUsbSource(this);
		sourceEnabled();
	}
	
	
	public UsbSource(UsbManager manager, Handler servicehandler, Context context) {
		
		mUsbManager = manager;
		mServiceHandler = servicehandler;
		mContext = context;
		band = Band.instance();
		band.setUsbSource(this);
		sourceEnabled();
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
		stopped = true;
	}
	
	public void sourceEnabled()
	{
		Thread hopthread = new Thread()
  	  	{
			public void run()
			{
				int hop=0;
				Band band = Band.instance();
				while (!stopped)
				{
					if (  (band.getChannelsEnabled()[hop]==true)&&(band.sourceActive==true))
					{
						setChannel(hop+1);
					}
					hop++;
					if (hop==band.getChannelsEnabled().length) hop=0;
					SystemClock.sleep(100);
				}
			}
  	    };
  	    hopthread.start();
	}
	
	protected void updateDeviceString(String str)
	{
		mainActivity.updateDeviceString(str); 
	}

	protected void updateDeviceStringOnUi(String str)
	{
		final String text=str;
		Runnable run = new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateDeviceString(text);
            }
		};
		mainActivity.runOnUiThread(run);
		run = null;
	}
	
	protected void updateStatusString(String str)
	{
		mainActivity.updateStatusString(str); 
	}

	protected void updateStatusStringOnUi(String str)
	{
		final String text=str;
		Runnable run = new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateStatusString(text);
            }
		};
		mainActivity.runOnUiThread(run);
		run=null;
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
		Runnable run = new Runnable() 
		{
            @Override
            public void run() {
            	mainActivity.addNetwork(text);
            }
		};
		band.nets++;
		mainActivity.runOnUiThread(run);
		run=null;
	}

	protected void updateNetwork(String network,String extra)
	{
		mainActivity.updateNetwork(network,extra); 
	}

	protected void updateNetworkOnUi(String network,String extra)
	{
		final String text=network;
		final String text2=extra;
		Runnable run = new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateNetwork(text,text2);
            }
		};
		mainActivity.runOnUiThread(run);
		run=null;
	}
	
	protected void removeNetwork(String network)
	{
		mainActivity.removeNetwork(network);
	}

	protected void removeNetworkOnUi(String network)
	{
		final String text=network;
		Runnable run = new Runnable() {
            @Override
            public void run() {
            	mainActivity.removeNetwork(text);
            }
		};
		mainActivity.runOnUiThread(run);
		run=null;
	}

	protected void addStation(String station)
	{
		mainActivity.addNetwork(station); 
		band.updateNetworks(station, "");
		band.stations++;
	}

	protected void addStationOnUi(String station)
	{
		final String text=station;
		Runnable run = new Runnable() 
		{
            @Override
            public void run() {
            	mainActivity.addStation(text);
            }
		};
		band.stations++;
		mainActivity.runOnUiThread(run);
		run=null;
	}

	protected void updateStation(String station,String extra)
	{
		mainActivity.updateStation(station,extra); 
	}

	protected void updateStationOnUi(String station,String extra)
	{
		final String text=station;
		final String text2=extra;
		Runnable run = new Runnable() 
		{
            @Override
            public void run() {
            	mainActivity.updateStation(text,text2);
            }
		};
		mainActivity.runOnUiThread(run);
		run=null;
	}
	
	protected void removeStation(String station)
	{
		mainActivity.removeStation(station);
		band.stations--;
	}

	protected void removeStationOnUi(String station)
	{
		final String text=station;
		Runnable run = new Runnable() 
		{
            @Override
            public void run() 
            {
            	mainActivity.removeStation(text);
            }
		};
		band.stations--;
		mainActivity.runOnUiThread(run);
		run=null;
	}
	
	
	
	protected void updateDeviceStatusStringOnUi(String str)
	{
		final String text=str;
		Runnable run = new Runnable() {
            @Override
            public void run() {
            	mainActivity.updateDeviceStatusString(text);
            }
		};
		mainActivity.runOnUiThread(run);
		run=null;
	}
	
	public void parseFrame(byte[] buffer, int offset, int l)
	{
		// Are we capturing traffic?
		band = Band.instance();
		if (band.capture==true)
		{
			band.saveFrame(buffer,offset,l);
		}
		
		// Beacon
		if ((l>(24+12+10+offset))&&((buffer[offset+1]&2)!=2))
		{
			if ((buffer[offset]==(byte)0x80)&&(buffer[offset+1]==(byte)0x00))
    		{
				// fixed parameters
				int noffset = offset+24+10;
				int bssid_offset = offset+16; 
				String ssid="";
				int channel=0;
				int enctype=0; // 0-open 1-wep 2-wpa 3-wpa2
				if (((buffer[noffset]>>4)&1)==1) enctype = 1;
				noffset = offset+24+12;
				while ((noffset+6)<l)
				{
					int type=buffer[noffset];
					int len=buffer[noffset+1];
					if (len<0) break;
					switch (type)
					{
						// SSID? 
						case 0:
							if ((len+noffset+2+1)<l)
							ssid = new String(buffer,noffset+2,len);
							break;
						case 3:
							channel = buffer[noffset+2];
							break;
						case (byte)0xdd:
							if (((noffset+7)<l)&&(buffer[noffset+6]==1))
								enctype = (buffer[noffset+7]==1) ? 2 : 3;
							break;
					}
					noffset+=(len+2);
				}
				String bssidstr = getMacString(buffer,bssid_offset);
				band.updateNetworks(ssid, bssidstr);
    			Network network = band.getNetwork(bssidstr);
    			if (network.ssid.equals("")) network.ssid=ssid;
    			network.beacon++;
    			network.rx+=(l-offset);
    			band.rx+=(l-offset);
    			network.channel = channel;
    			network.encType = enctype;
    			network.updateTimestamp();
    			if (network.beaconFrame==null)
    			{
    				network.beaconFrame = new byte[l-offset];
    				for (int i=0;i<(l-offset-4);i++)
    				{
    					network.beaconFrame[i]=(byte)buffer[i+offset];
    				}
    			}
    			ssid=null;
    		}
		}
		// Probe Response
		if ((l>(24+12+10+offset))&&((buffer[offset+1]&2)!=2))
		{
    		if ((buffer[offset]==(byte)0x50)&&(buffer[offset+1]==(byte)0x00)&&(buffer[offset+24+12]==(byte)0))
    		{
				// fixed parameters
				int noffset = offset+24+10;
				int bssid_offset = offset+16; 
				String ssid="";
				int channel=0;
				int enctype=0; // 0-open 1-wep 2-wpa 3-wpa2
				if (((buffer[noffset]>>4)&1)==1) enctype = 1;
				noffset = offset+24+12;
				while ((noffset+6)<l)
				{
					if (noffset<0) break;
					int type=buffer[noffset];
					int len=(int)buffer[noffset+1];
					if (len<0) break;
					switch (type)
					{
						// SSID? 
						case 0:
							if ((len+noffset+2+1)<l)
							ssid = new String(buffer,noffset+2,len);
							break;
						case 3:
							if ((noffset+2+1+1)<l)
							channel = buffer[noffset+2];
							break;
						case (byte)0xdd:
							if (((noffset+7)<l)&&(buffer[noffset+6]==1))
								enctype = (buffer[noffset+7]==1) ? 2 : 3;
							break;
					}
					noffset+=(len+2);
				}
				String bssidstr = getMacString(buffer,bssid_offset);
				band.updateNetworks(ssid, bssidstr);
    			Network network = band.getNetwork(bssidstr);
    			if (network.ssid.equals("")) network.ssid=ssid;
    			network.beacon++;
    			network.rx+=(l-offset);
    			band.rx+=(l-offset);
    			network.channel = channel;
    			network.encType = enctype;
    			network.updateTimestamp();
    			if (network.beaconFrame==null)
    			{
    				network.beaconFrame = new byte[l-offset];
    				for (int i=0;i<(l-offset-4);i++)
    				{
    					network.beaconFrame[i]=(byte)buffer[i+offset];
    				}
    			}
    			ssid = null;
    		}
		}
		
		// Data
		if (l>(24+offset))
		{
    		boolean tods=true;
			if (buffer[offset]==(byte)0x08)
    		{
				// Examine DS flag
    			int sta_offset=0,bssid_offset=0;
    			if ((buffer[offset+1]&2)==2)
    			{
    				sta_offset = offset+4;
    				bssid_offset=offset+10;
    				tods=false;
    			}
    			else if ((buffer[offset+1]&1)==1)
    			{
    				sta_offset = offset+10;
    				bssid_offset=offset+4;

    			}
    			// Do not get station-station data for now...
    			else
    			{
    				return;
    			}
    			
    			String bssidstr = getMacString(buffer, bssid_offset);
    			Network network = band.getNetwork(bssidstr);
    			network.updateTimestamp();
    			network.data++;
    			network.rx+=(l-offset);
    			band.rx+=(l-offset);
    			String stastr = getMacString(buffer, sta_offset);
    			if (bssid_offset>0)
    			{
	    			if ((!stastr.equals(bssidstr))&&(!stastr.equals("ff:ff:ff:ff:ff:ff"))&&(!stastr.substring(0,8).equals("01:00:5e"))&&(!stastr.substring(0,8).equals("00:00:00"))&&(!stastr.substring(0,8).equals("01:80:c2"))&&(!stastr.substring(0,5).equals("33:33"))&&(!stastr.substring(0,8).equals("01:00:0c")))
	    			{
	    				network.updateStations(stastr);
	    				network.updateStationData(stastr);
	    				network.updateStationTimestamp(stastr);
	    				network.updateStationRx(stastr, l-offset);
	    			}
	    			else return;
    			}

    			// Here be dragons and dark magic...
    			int keyver=0;
    			boolean handshake=false;
    			if ((offset+80)<l)
    			{
	        		if ((!tods) &&(buffer[offset+30]==(byte)0x88)&&((buffer[offset+31]==(byte)0x8e)))
	        		{
	        			// EAPOL RSN/WPA key? Then we have a zero key info byte 1? It must be an AP challenge
	        			// TODO: replay counter logic
	        			if ( ((buffer[offset+36]==(byte)0x02)||(buffer[offset+36]==(byte)0xfe)) && ((buffer[offset+37]==(byte)0)))
	        			{
	        				WPAHandshake hshake = null;
	        				handshake=true;
	        				hshake = new WPAHandshake(bssidstr);
	        				hshake.keyVer = ((byte)(buffer[offset+38]&3)==2) ? 2 : 1;
	        				hshake.hwaddr=stastr;
	        				hshake.bssid=bssidstr;
	        				hshake.anonce = new byte[32];
	        				hshake.frame1 = new byte[l-offset-4];
	        				for (int i=0;i<32;i++) hshake.anonce[i]=buffer[offset+49+i];
	        				for (int i=0;i<l-offset-4;i++) hshake.frame1[i]=buffer[offset+i];
	        				hshake.step1 = true;
	        				band.wpahandshakes.add(hshake);
	        				mainActivity.addHandshake(hshake);
	        				mainActivity.updateHandshake(hshake);
	        			}
	        		}	
        			// EAPOL RSN/WPA key? Then we have a 1 key info byte 1? It must be an SP response
        			// TODO: better replay counter logic
	        		if ((tods) && ((buffer[offset+36]==(byte)0x02)||(buffer[offset+36]==(byte)0xfe)) && ((buffer[offset+37]==(byte)1)))
        			{
        				WPAHandshake hshake = null;
        				for (int i=0;i<band.wpahandshakes.size();i++)
        				{
        					// It is generally possible that frames come out of order and 3 is received before 2. Weird
        					// But replay counter should stay 0 at that point
        					if ((band.wpahandshakes.get(i).replay==-1)&&(band.wpahandshakes.get(i).step2==false)/*&&(band.wpahandshakes.get(i).step3==false)&&(band.wpahandshakes.get(i).step4==false)&&(band.wpahandshakes.get(i).hwaddr.equals(stastr))*/&&(band.wpahandshakes.get(i).bssid.equals(bssidstr)))
        					{
        						hshake = band.wpahandshakes.get(i);
        					}
        				}
        				if (hshake == null) 
        				{
        					hshake = new WPAHandshake(bssidstr);
        					handshake=true;
            				hshake.keyVer = ((byte)(buffer[offset+38]&3)==2) ? 2 : 1;
            				hshake.hwaddr=stastr;
            				hshake.bssid=bssidstr;
            				hshake.snonce = new byte[32];
            				hshake.frame2 = new byte[l-offset-4];
            				for (int i=0;i<32;i++) hshake.snonce[i]=buffer[offset+49+i];
            				for (int i=0;i<l-offset-4;i++) hshake.frame2[i]=buffer[offset+i];
            				hshake.step2 = true;
            				hshake.replay=buffer[offset+48];
            				band.wpahandshakes.add(hshake);
            				mainActivity.addHandshake(hshake);
            				mainActivity.updateHandshake(hshake);
        				}
        				else 
        				{
	        				hshake.frame2 = new byte[l-offset-4];
	        				hshake.snonce = new byte[32];
	        				for (int i=0;i<32;i++) hshake.snonce[i]=buffer[offset+49+i];
	        				for (int i=0;i<l-offset-4;i++) hshake.frame2[i]=buffer[offset+i];
	        				hshake.step2 = true;
	        				hshake.replay=buffer[offset+48];
	        				mainActivity.updateHandshake(hshake);
        				}
        			}

        			// EAPOL RSN/WPA key? Then we have a 19 or 1 key info byte 1? It must be an SP response
        			// TODO: replay counter logic
	        		if ((buffer[offset+48]==1)&&(!tods) && ((buffer[offset+36]==(byte)0x02)||(buffer[offset+36]==(byte)0xfe)) && ( ((buffer[offset+37]==(byte)19))||((buffer[offset+37]==(byte)1))))
        			{
        				WPAHandshake hshake = null;
        				for (int i=0;i<band.wpahandshakes.size();i++)
        				{
        					if ((band.wpahandshakes.get(i).step3==false)/*&&(band.wpahandshakes.get(i).step4==false)*/&&(band.wpahandshakes.get(i).hwaddr.equals(stastr))&&(band.wpahandshakes.get(i).bssid.equals(bssidstr)))
        					if (((buffer[offset+48]==band.wpahandshakes.get(i).replay+1)&&(band.wpahandshakes.get(i).step2==true))||((band.wpahandshakes.get(i).replay==-1)&&(band.wpahandshakes.get(i).step2==false)))
        					{
        						hshake = band.wpahandshakes.get(i);
        					}
        				}
        				if (hshake == null) 
        				{
        					hshake = new WPAHandshake(bssidstr);
        					handshake=true;
            				hshake.keyVer = ((byte)(buffer[offset+38]&3)==2) ? 2 : 1;
            				hshake.hwaddr=stastr;
            				hshake.bssid=bssidstr;
            				if (hshake.anonce==null) hshake.anonce = new byte[32];
	        				hshake.frame3 = new byte[l-offset-4];
	        				for (int i=0;i<32;i++) hshake.anonce[i]=buffer[offset+49+i];
	        				for (int i=0;i<l-offset-4;i++) hshake.frame3[i]=buffer[offset+i];
	        				hshake.step3 = true;
	        				band.wpahandshakes.add(hshake);
	        				mainActivity.addHandshake(hshake);
	        				mainActivity.updateHandshake(hshake);
        				}
        				else 
        				{
	        				hshake.frame3 = new byte[l-offset-4];
	        				if (hshake.anonce==null) hshake.anonce = new byte[32];
	        				for (int i=0;i<32;i++) hshake.anonce[i]=buffer[offset+49+i];
	        				for (int i=0;i<l-offset-4;i++) hshake.frame3[i]=buffer[offset+i];
	        				hshake.step3 = true;
	        				mainActivity.updateHandshake(hshake);
        				}
        			}
        			
	        		// last packet
	        		if ((tods) && ((buffer[offset+37]==(byte)0x01)&&(buffer[offset+36]==(byte)0xfe)) || ( ((buffer[offset+37]==(byte)3))&&((buffer[offset+36]==(byte)2))))
        			{
        				WPAHandshake hshake = null;
        				for (int i=0;i<band.wpahandshakes.size();i++)
        				{
        					if ((band.wpahandshakes.get(i).step4==false)&&(band.wpahandshakes.get(i).hwaddr.equals(stastr))&&(band.wpahandshakes.get(i).bssid.equals(bssidstr)))
        					{
        						hshake = band.wpahandshakes.get(i);
        					}
        				}
        				if (hshake == null) 
        				{
        					hshake = new WPAHandshake(bssidstr);
        					handshake=true;
            				hshake.keyVer = ((byte)(buffer[offset+38]&3)==2) ? 2 : 1;
            				hshake.hwaddr=stastr;
            				hshake.bssid=bssidstr;
	        				hshake.frame4 = new byte[l-offset-4];
	        				for (int i=0;i<l-offset-4;i++) hshake.frame4[i]=buffer[offset+i];
	        				hshake.step4 = true;
	        				band.wpahandshakes.add(hshake);
	        				mainActivity.addHandshake(hshake);
	        				mainActivity.updateHandshake(hshake);
        				}
        				else if ((!hshake.step4))
        				{
	        				hshake.frame4 = new byte[l-offset-4];
	        				for (int i=0;i<l-offset-4;i++) hshake.frame4[i]=buffer[offset+i];
	        				hshake.step4 = true;
	        				mainActivity.updateHandshake(hshake);
        				}
        			}        			
        		}
    			
    			if (handshake==true)
    			{
    				network.handshake++;
    				band.handshakes++;
    				network.updateStationHandshake(stastr);
    			}
    		}
    		// Get null Function too
    		else if (buffer[offset]==(byte)0x48)
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

    			Network network = band.getNetwork(getMacString(buffer, bssid_offset));
    			network.updateTimestamp();
    			network.data++;
    			network.rx+=(l-offset);
    			band.rx+=(l-offset);
    			if (bssid_offset>0)
    			{
	    			String stastr = getMacString(buffer, sta_offset);
	    			if ((!stastr.equals("ff:ff:ff:ff:ff:ff"))&&(!stastr.substring(0,8).equals("01:00:5e"))&&(!stastr.substring(0,8).equals("00:00:00"))&&(!stastr.substring(0,8).equals("01:80:c2"))&&(!stastr.substring(0,5).equals("33:33"))&&(!stastr.substring(0,8).equals("01:00:0c")))
	    			{
	    				network.updateStations(stastr);
	    				network.updateStationData(stastr);
	    				network.updateStationTimestamp(stastr);
	    				network.updateStationRx(stastr, l-offset);
	    			}
    			}
    		}
		}		
	}
	
	public void sendDeauth(String bssid, String hwaddr)
	{
		//TODO
	}
	
}