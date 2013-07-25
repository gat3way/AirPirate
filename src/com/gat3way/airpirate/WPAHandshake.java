package com.gat3way.airpirate;

public class WPAHandshake 
{
	public String essid;
	public byte[] mac1;
	public byte[] mac2;
	public byte[] nonce1;
	public byte[] nonce2;
	public byte[] eapol;
	public int eapolSize;
	public int keyVer;
	public String keyMic;
	public boolean step1=false;
	public boolean step2=false;
	public boolean step3=false;
	public boolean step4=false;
}
