package com.gat3way.airpirate;

public class Station 
{
	public byte[] bt_hwaddr =  {0,0,0,0,0,0,0};
	public String hwaddr;
	public int encType;
	public int rx;
	public int data;
	public int handshake;
	public int arp;
	public long lastPacket;
	
	public Station()
	{
		hwaddr = "";
		encType=0;
		rx=data=handshake=arp=0;
		lastPacket = System.currentTimeMillis()/1000;
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
	
}
