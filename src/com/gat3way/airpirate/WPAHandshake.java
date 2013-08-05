package com.gat3way.airpirate;

public class WPAHandshake 
{
	public String essid="?";
	public String bssid="?";
	public String hwaddr="?";
	public byte[] snonce=null;
	public byte[] anonce=null;
	public byte[] eapol;
	public int eapolSize;
	public int keyVer;
	public String keyMic;
	public boolean step1=false;
	public boolean step2=false;
	public boolean step3=false;
	public boolean step4=false;
	public byte[] frame1;
	public byte[] frame2;
	public byte[] frame3;
	public byte[] frame4;
	public byte[] beacon;
	public long timestamp;
	public int replay=-1;
	
	public WPAHandshake(String bssidstr)
	{
		Band band = Band.instance();
		frame1=frame2=frame3=frame4=null;
		for (int i=0;i<band.getNetworks();i++)
		{
			Network net = band.getNetwork(i);
			if (net!=null)
			{
				if (net.bssid.equals(bssidstr))
				{
					beacon = net.beaconFrame;
					essid = net.ssid;
				}
			}
			else beacon = null;
		}
		timestamp = System.currentTimeMillis();
	}
}
