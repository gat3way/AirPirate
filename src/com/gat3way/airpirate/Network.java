package com.gat3way.airpirate;

import java.util.ArrayList;

import android.util.Log;

public class Network 
{
	private ArrayList<Station> stations;
	public String ssid;
	public String bssid;
	public int encType;
	public long lastBeacon;
	public int rx;
	public int data;
	public int beacon;
	public int probe;
	public int handshake;
	public int arp;
	public int channel;
	private Object station_lock = new Object();
	
	
	public Network(String new_bssid, String new_ssid)
	{
		stations = new ArrayList<Station>();
		lastBeacon = System.currentTimeMillis()/1000;
		rx=data=beacon=probe=handshake=arp=0;
		encType=0;
		bssid = new_bssid;
		ssid = new_ssid;
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
	
	public void updateStations(String mac)
	{
		boolean flag=false;
		
		Band band = Band.instance();
		synchronized (station_lock)
		{
			if (band.getUsbSource()!=null)
			{
				for (Station station : stations)
				{
					if (station.hwaddr.equals(mac))
					{
						flag = true;
					}
				}
				if (!flag)
				{
		    		Station station = new Station();
		    		station.hwaddr = mac;
		    		stations.add(station);
		    		band.getUsbSource().addStationOnUi(mac);
				}
			}	
		}
	}
	
	
	
	public ArrayList<Station> getStationsList()
	{
		return stations;
	}
	
	public void updateTimestamp()
	{
		lastBeacon = System.currentTimeMillis()/1000;
	}
	
	public void removeStation(String hwaddr)
	{
		
		synchronized (station_lock)
		{
			for (Station station : stations)
			{
				if (station.hwaddr.equals(hwaddr))
				{
					stations.remove(station);
					break;
				}
			}
		}
	}
	
	
	public void updateStationRx(String hwaddr,int rx)
	{
		boolean found = false;
		
		synchronized (station_lock)
		{
			for (Station station : stations)
			{
				if (station.hwaddr.equals(hwaddr))
				{
					station.rx += rx;
					found = true;
					break;
				}
			}
			if (!found)
			{
				Station station = new Station();
				station.hwaddr = hwaddr;
				station.rx = rx;
				stations.add(station);
			}
		}
	}
	
	public void updateStationData(String hwaddr)
	{
		boolean found = false;
		
		synchronized (station_lock)
		{
			for (Station station : stations)
			{
				if (station.hwaddr.equals(hwaddr))
				{
					station.data++;
					found = true;
					break;
				}
			}
			if (!found)
			{
				Station station = new Station();
				station.hwaddr = hwaddr;
				station.data = 1;
				stations.add(station);
			}
		}
	}
	
	public void updateStationHandshake(String hwaddr)
	{
		boolean found = false;
		
		synchronized (station_lock)
		{
			for (Station station : stations)
			{
				if (station.hwaddr.equals(hwaddr))
				{
					station.handshake++;
					found = true;
					break;
				}
			}
			if (!found)
			{
				Station station = new Station();
				station.hwaddr = hwaddr;
				station.handshake = 1;
				stations.add(station);
			}
		}
	}
	
	
	public void updateStationTimestamp(String hwaddr)
	{
		boolean found = false;

		synchronized (station_lock)
		{
			for (Station station : stations)
			{
				if (station.hwaddr.equals(hwaddr))
				{
					station.lastPacket = System.currentTimeMillis()/1000;;
					found = true;
					break;
				}
			}
			if (!found)
			{
				Station station = new Station();
				station.hwaddr = hwaddr;
				station.lastPacket = System.currentTimeMillis()/1000;;
				stations.add(station);
			}
		}
	}
	
	
	// Update all stations' timestamps, remove stale
	public void updateStationsTimestamp()
	{
		Band band = Band.instance();
		synchronized (station_lock)
		{
			for (int a=0;a<stations.size();a++)
			{
				Station station = stations.get(a);
				if (((System.currentTimeMillis()/1000) - station.lastPacket)>30)
				{
					if (band.getUsbSource()!=null)
					{
						// TODO
						band.getUsbSource().removeStationOnUi(station.hwaddr);
					}
					stations.remove(station);
				}
			}
		}
	}
	
	public int getStations()
	{
		return stations.size();
	}
	
}
