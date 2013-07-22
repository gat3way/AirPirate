package com.gat3way.airpirate;

import java.util.ArrayList;
import android.util.Log;

public class Band 
{
	private ArrayList<Network> networks;
	private static Band instance;
	private Object band_lock = new Object();
	private UsbSource usbSource = null;
	
	public static Band instance()
	{
		if (instance==null)
		{
			instance = new Band();
		}
		return instance;
	}
	
	public void reset()
	{
		networks = new ArrayList<Network>();
		usbSource.doShutdown();
		usbSource = null;
	}
	
	
	public void setUsbSource(UsbSource source)
	{
		usbSource = source;
	}

	public UsbSource getUsbSource()
	{
		return usbSource;
	}
	
	public Band()
	{
		networks = new ArrayList<Network>();
	}
	
	public void deAuth(String bssid,String hwaddr)
	{
		usbSource.sendDeauth(bssid,hwaddr);
	}
	
	public Network getNetwork(String bssid)
	{
		boolean found = false;
		
		synchronized (band_lock)
		{
			for (Network network : networks)
			{
				if (network.bssid.equals(bssid))
				{
					return network;
				}
			}
			if (!found)
			{
				Network network = new Network("",bssid);
				networks.add(network);
				return network;
			}
			return null;
		}
	}
	

	public void updateNetworks(String ssid, String bssid)
	{
		boolean flag=false;
		synchronized (band_lock)
		{
			for (Network network : networks)
			{
				if (network.bssid.equals(bssid))
				{
					network.ssid = ssid;
					flag = true;
				}
			}
			if (!flag)
			{
				Network new_network = new Network(bssid,ssid);
				new_network.bssid = bssid;
				new_network.ssid = ssid;
				networks.add(new_network);
				if (usbSource!=null)
				{
					usbSource.addNetworkOnUi(new_network.ssid);
				}
			}
		}
	}

	

	
	
	
	
	public void updateNetworksTimeStamp()
	{
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				Network network = networks.get(a);
				if (((System.currentTimeMillis()/1000) - network.lastBeacon)>60)
				{
					if (usbSource!=null)
					{
						usbSource.removeNetworkOnUi(network.ssid);
					}
					networks.remove(network);
				}
			}
		}
	}
	
	public void updateStationsTimeStamp()
	{
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				Network network = networks.get(a);
				network.updateStationsTimestamp();
			}
		}
	}
	
	
	
	public void updateNetworksDetails()
	{
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				Network network = networks.get(a);
	    		String formatted="";
	    		if (network.rx>1024*1024*1024)
	    		{
	    			formatted = String.format("%.02f", (double)((double)network.rx/(1024*1024*1024)))+" GB";
	    		}
	    		else if (network.rx>1024*1024)
	    		{
	    			formatted = String.format("%.02f", (double)((double)network.rx/(1024*1024)))+" MB";
	    		}
	    		else
	    		{
	    			formatted = String.format("%.02f", (double)((double)network.rx/(1024)))+" KB";
	    		}

				String extra = "| Channel: "+network.channel+" | RX: "+formatted+" | Beacons: "+network.beacon+" |";
				if (usbSource!=null)
				{
					usbSource.updateNetworkOnUi(network.ssid,extra);
				}
			}
		}
	}
	
	public void updateStationsDetails()
	{
		synchronized (band_lock)
		{
			ArrayList<Station> stations;
			for (int a=0;a<networks.size();a++)
			{
				Network network = networks.get(a);
				stations = network.getStationsList();
				
				for (int b=0;b<stations.size();b++)
				{
					Station station = stations.get(b);
					String formatted="";
		    		if (station.rx>1024*1024*1024)
		    		{
		    			formatted = String.format("%.02f", (double)((double)station.rx/(1024*1024*1024)))+" GB";
		    		}
		    		else if (network.rx>1024*1024)
		    		{
		    			formatted = String.format("%.02f", (double)((double)station.rx/(1024*1024)))+" MB";
		    		}
		    		else
		    		{
		    			formatted = String.format("%.02f", (double)((double)station.rx/(1024)))+" KB";
		    		}
		    		

		    		
					String extra = "| SSID: "+network.ssid+" | BSSID: "+network.bssid+" | RX: "+formatted+" |";
					if (usbSource!=null)
					{
						usbSource.updateStationOnUi(station.hwaddr,extra);
					}
				}
			}
		}
	}
	
	
	
	public long getTraffic()
	{
		long bytes=0;
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				bytes+=networks.get(a).rx;
			}
		}
		return bytes;
	}


	public int getNetworks()
	{
		int bytes=0;
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				bytes++;
			}
		}
		return bytes;
	}
	
	public int getHandshakes()
	{
		int bytes=0;
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				bytes+=networks.get(a).handshake;
			}
		}
		return bytes/4;
	}
	

	public int getStations()
	{
		int bytes=0;
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				bytes+=networks.get(a).getStations();
			}
		}
		return bytes;
	}

	
}
