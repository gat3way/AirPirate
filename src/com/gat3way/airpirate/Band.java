package com.gat3way.airpirate;

import java.util.ArrayList;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import android.util.Log;

public class Band 
{
	public ArrayList<Network> networks;
	public ArrayList<WPAHandshake> wpahandshakes;
	private static Band instance;
	private Object band_lock = new Object();
	private UsbSource usbSource = null;
	public int rx=0;
	public int stations=0;
	public int handshakes=0;
	public int nets=0;
	public boolean capture=false;
	public DataOutputStream captureWriter=null;
	private String[] channels = { "Channel 1", "Channel 2", "Channel 3", "Channel 4",
								  "Channel 5", "Channel 6", "Channel 7", "Channel 8",
								  "Channel 9", "Channel 10", "Channel 11", "Channel 12",
								  "Channel 13" };
			
	private boolean channels_enabled[] = {true,true,true,true,true,true,true,true,true,true,true,true,true};
	public boolean sourceActive = false;
	public boolean warMode = false;
	
	public String[] getChannels()
	{
		return channels;
	}
	
	public boolean[] getChannelsEnabled()
	{
		return channels_enabled;
	}
	
	public void setChannelsEnabled(boolean[] enabled)
	{
		channels_enabled = enabled;
	}
	
	
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
		networks = null;
		networks = new ArrayList<Network>();
		usbSource.doShutdown();
		usbSource = null;
		rx = 0;
		stations = 0;
		handshakes = 0;
		nets = 0;
		usbSource = null;
		band_lock = new Object();
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
		wpahandshakes = new ArrayList<WPAHandshake>();
	}
	
	public void deAuth(String bssid,String hwaddr)
	{
		usbSource.sendDeauth(bssid,hwaddr);
	}
	
	/*
	public Network getNetwork(int index)
	{
		Network result = null;
		try
		{
			result = networks.get(index);
		}
		catch (Exception e)
		{
			result = null;
		}
		return result;
	}
	*/
	
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
				Network network = new Network(bssid,"");
				networks.add(network);
				if (usbSource!=null)
				{
					//usbSource.addNetworkOnUi(network.ssid);
				}
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
		updateNetworksDetails();
	}

	
	
	public void updateNetworksTimeStamp()
	{
		synchronized (band_lock)
		{
			for (int a=0;a<networks.size();a++)
			{
				Network network = networks.get(a);
				if (((System.currentTimeMillis()/1000) - network.lastBeacon)>30)
				{
					if (network.stations.size()==0)
					{
						networks.remove(network);
						if (usbSource!=null)
						{
							usbSource.removeNetworkOnUi(network.ssid);
						}
					}
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

				String extra = " | "+((network.encType==0) ? "Open" : (network.encType==3) ? "WPA" : (network.encType==2) ? "WPA2" : "WEP")+" | Channel: "+network.channel+ " | RX: "+formatted + " | Beacons: "+network.beacon+" |";
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
		    		
					String extra = "| SSID: "+network.ssid+" | BSSID: "+network.bssid+" | RX: "+formatted+" | Security: ";
					extra += (network.encType==0) ? "Open" : (network.encType==1) ? "WEP" : (network.encType==3) ? "WPA" : "WPA2";
					extra += " |";
							
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


	public void startCapture(String filename)
	{
		if (usbSource!=null)
		{
			try 
			{
				FileOutputStream fileOut = new FileOutputStream(filename);
				captureWriter = new DataOutputStream(fileOut);
				// Write header data
				byte header[] = new byte[24];
				header[0]=(byte)0xd4;
				header[1]=(byte)0xc3;
				header[2]=(byte)0xb2;
				header[3]=(byte)0xa1;
				header[4]=(byte)2;
				header[5]=0;
				header[6]=(byte)4;
				header[7]=0;
				header[8]=header[9]=header[10]=header[11]=0;
				header[12]=header[13]=header[14]=header[15]=0;
				header[16]=(byte)((2500)&0xff);
				header[17]=(byte)((2500>>8)&0xff);
				header[18]=header[19]=0;
				header[20]=(byte)105;
				header[21]=header[22]=header[23]=0;
				captureWriter.write(header);
			}
			catch (Exception e)
			{
				captureWriter = null;
				e.printStackTrace();
			}
		}
	}
	
	
	public void stopCapture()
	{
		try
        {
        	captureWriter.close();
        }
		catch (Exception e)
		{
			captureWriter = null;
			e.printStackTrace();
		}
	}
	
	public void saveFrame(byte[] buffer, int offset,int len)
	{
        if (captureWriter!=null)
        {
        	// Write packet data
        	int aLen = len-offset-4;
        	byte packet[] = new byte[16];
			// Timestamp (micro)
			long start = System.nanoTime();
			long end = System.nanoTime();
			long microseconds = (end - start) / 1000;
			long seconds = System.currentTimeMillis()/1000;
			packet[0]=(byte)(seconds&0xff);
			packet[1]=(byte)((seconds>>8)&0xff);
			packet[2]=(byte)((seconds>>16)&0xff);
			packet[3]=(byte)((seconds>>24)&0xff);
			packet[4]=(byte)(microseconds&0xff);
			packet[5]=(byte)((microseconds>>8)&0xff);
			packet[6]=(byte)((microseconds>>16)&0xff);
			packet[7]=(byte)((microseconds>>24)&0xff);
			packet[8]=(byte)(aLen&0xff);
			packet[9]=(byte)((aLen>>8)&0xff);
			packet[10]=(byte)((aLen>>16)&0xff);
			packet[11]=(byte)((aLen>>24)&0xff);
			packet[12]=(byte)(aLen&0xff);
			packet[13]=(byte)((aLen>>8)&0xff);
			packet[14]=(byte)((aLen>>16)&0xff);
			packet[15]=(byte)((aLen>>24)&0xff);
			
			try
			{
				captureWriter.write(packet);
				packet = null;
				captureWriter.write(buffer,offset,aLen);
			}
			catch (Exception e)
			{
				// TODO?
				e.printStackTrace();
			}
        }
	}
}
