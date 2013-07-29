package com.gat3way.airpirate;

import java.util.ArrayList;
import android.app.Activity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.io.*;
import android.content.Context;
import java.math.BigInteger;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

/*
 * Converted to Java/Android by Milen Rangelov <gat3way@gmail.com> 
 * Thanks to Mike Kershaw / Dragorn <dragorn@kismetwireless.net>
 * 
 * Derived from rtlwifi and urtwn drivers
 * 
 *
 * Based on the r8192cu driver, which is:
 * Copyright 2009-2012 Realtek Corporation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 */


public class Rtl8192Card extends UsbSource 
{

	String TAG = "rtl8192";


	// YOU MAY NEED TO CHANGE THAT FOR NON-92CUS DEVICES!!!
	private int RTL8188CUS_CHNLBW = 0x1807400; 
	private final int RTL8192_REQT_READ = 0xC0;
	private final int RTL8192_REQT_WRITE = 0x40;
	private final int RTL8192_REQ_GET_REG = 0x05;
	private final int RTL8192_REQ_SET_REG = 0x05;
	private Object control_lock = new Object();
	private int is_rtl8192;
	private int eeprom_chnlarea_txpwr_cck[][] = new int[2][3];
	private int eeprom_chnlarea_txpwr_ht40_1s[][] = new int[2][3];
	private int chnlarea_txpwr_cck[][] = new int[2][14];
	private int chnlarea_txpwr_ht40_1s[][] = new int[2][14];
	private int chnlarea_txpwr_ht40_2s[][] = new int[2][14];
	private int eprom_chnlarea_txpwr_ht40_2sdf[][] = new int[2][3];
	private int eeprom_pwrlimit_ht40[] = new int[3];
	private int eeprom_pwrlimit_ht20[] = new int[3];
	private int pwrlimit_ht40[][] = new int[2][14];
	private int pwrlimit_ht20[][] = new int[2][14];
	private int txpwr_ht20diff[][] = new int[2][14];
	private int txpwr_legacyhtdiff[][] = new int[2][14];
	private int legacy_ht_txpowerdiff;
	private int eeprom_tssi[] = new int[2];
	private int pwrgroup[][] = new int[64][16];
	private int pwrgroupcnt = 0;
	private int chnlbw;
	private String deviceName="";
	private byte efuse[] = new byte[128];
	private byte efuse_chnlarea_txpwr_cck[][] = new byte[2][3];
	private byte efuse_chnlarea_txpwr_ht401s[][] = new byte[2][3];
	private boolean started = false;

	private usbThread mUsbThread;
	
	private int rtl8192cuphy_reg_1tarray[] =  {
	        0x024, 0x0011800d, 
	        0x028, 0x00ffdb83,
	        0x800, 0x80040000,
	        0x804, 0x00000001,
	        0x808, 0x0000fc00,
	        0x80c, 0x0000000a,
	        0x810, 0x10005388,
	        0x814, 0x020c3d10,
	        0x818, 0x02200385,
	        0x81c, 0x00000000,
	        0x820, 0x01000100,
	        0x824, 0x00390004,
	        0x828, 0x00000000,
	        0x82c, 0x00000000,
	        0x830, 0x00000000,
	        0x834, 0x00000000,
	        0x838, 0x00000000,
	        0x83c, 0x00000000,
	        0x840, 0x00010000,
	        0x844, 0x00000000,
	        0x848, 0x00000000,
	        0x84c, 0x00000000,
	        0x850, 0x00000000,
	        0x854, 0x00000000,
	        0x858, 0x569a569a,
	        0x85c, 0x001b25a4,
	        0x860, 0x66e60230,
	        0x864, 0x061f0130,
	        0x868, 0x00000000,
	        0x86c, 0x32323200,
	        0x870, 0x07000700,
	        0x874, 0x22004000,
	        0x878, 0x00000808,
	        0x87c, 0x00000000,
	        0x880, 0xc0083070,
	        0x884, 0x000004d5,
	        0x888, 0x00000000,
	        0x88c, 0xccc000c0,
	        0x890, 0x00000800,
	        0x894, 0xfffffffe,
	        0x898, 0x40302010,
	        0x89c, 0x00706050,
	        0x900, 0x00000000,
	        0x904, 0x00000023,
	        0x908, 0x00000000,
	        0x90c, 0x81121111,
	        0xa00, 0x00d047c8,
	        0xa04, 0x80ff000c,
	        0xa08, 0x8c838300,
	        0xa0c, 0x2e68120f,
	        0xa10, 0x9500bb78,
	        0xa14, 0x11144028,
	        0xa18, 0x00881117,
	        0xa1c, 0x89140f00,
	        0xa20, 0x1a1b0000,
	        0xa24, 0x090e1317,
	        0xa28, 0x00000204,
	        0xa2c, 0x00d30000,
	        0xa70, 0x101fbf00,
	        0xa74, 0x00000007,
	        0xc00, 0x48071d40,
	        0xc04, 0x03a05611,
	        0xc08, 0x000000e4,
	        0xc0c, 0x6c6c6c6c,
	        0xc10, 0x08800000,
	        0xc14, 0x40000100,
	        0xc18, 0x08800000,
	        0xc1c, 0x40000100,
	        0xc20, 0x00000000,
	        0xc24, 0x00000000,
	        0xc28, 0x00000000,
	        0xc2c, 0x00000000,
	        0xc30, 0x69e9ac44,
	        0xc34, 0x469652cf,
	        0xc38, 0x49795994,
	        0xc3c, 0x0a97971c,
	        0xc40, 0x1f7c403f,
	        0xc44, 0x000100b7,
	        0xc48, 0xec020107,
	        0xc4c, 0x007f037f,
	        0xc50, 0x6954341e,
	        0xc54, 0x43bc0094,
	        0xc58, 0x6954341e,
	        0xc5c, 0x433c0094,
	        0xc60, 0x00000000,
	        0xc64, 0x5116848b,
	        0xc68, 0x47c00bff,
	        0xc6c, 0x00000036,
	        0xc70, 0x2c7f000d,
	        0xc74, 0x018610db,
	        0xc78, 0x0000001f,
	        0xc7c, 0x00b91612,
	        0xc80, 0x40000100,
	        0xc84, 0x20f60000,
	        0xc88, 0x40000100,
	        0xc8c, 0x20200000,
	        0xc90, 0x00121820,
	        0xc94, 0x00000000,
	        0xc98, 0x00121820,
	        0xc9c, 0x00007f7f,
	        0xca0, 0x00000000,
	        0xca4, 0x00000080,
	        0xca8, 0x00000000,
	        0xcac, 0x00000000,
	        0xcb0, 0x00000000,
	        0xcb4, 0x00000000,
	        0xcb8, 0x00000000,
	        0xcbc, 0x28000000,
	        0xcc0, 0x00000000,
	        0xcc4, 0x00000000,
	        0xcc8, 0x00000000,
	        0xccc, 0x00000000,
	        0xcd0, 0x00000000,
	        0xcd4, 0x00000000,
	        0xcd8, 0x64b22427,
	        0xcdc, 0x00766932,
	        0xce0, 0x00222222,
	        0xce4, 0x00000000,
	        0xce8, 0x37644302,
	        0xcec, 0x2f97d40c,
	        0xd00, 0x00080740,
	        0xd04, 0x00020401,
	        0xd08, 0x0000907f,
	        0xd0c, 0x20010201,
	        0xd10, 0xa0633333,
	        0xd14, 0x3333bc43,
	        0xd18, 0x7a8f5b6b,
	        0xd2c, 0xcc979975,
	        0xd30, 0x00000000,
	        0xd34, 0x80608000,
	        0xd38, 0x00000000,
	        0xd3c, 0x00027293,
	        0xd40, 0x00000000,
	        0xd44, 0x00000000,
	        0xd48, 0x00000000,
	        0xd4c, 0x00000000,
	        0xd50, 0x6437140a,
	        0xd54, 0x00000000,
	        0xd58, 0x00000000,
	        0xd5c, 0x30032064,
	        0xd60, 0x4653de68,
	        0xd64, 0x04518a3c,
	        0xd68, 0x00002101,
	        0xd6c, 0x2a201c16,
	        0xd70, 0x1812362e,
	        0xd74, 0x322c2220,
	        0xd78, 0x000e3c24,
	        0xe00, 0x2a2a2a2a,
	        0xe04, 0x2a2a2a2a,
	        0xe08, 0x03902a2a,
	        0xe10, 0x2a2a2a2a,
	        0xe14, 0x2a2a2a2a,
	        0xe18, 0x2a2a2a2a,
	        0xe1c, 0x2a2a2a2a,
	        0xe28, 0x00000000,
	        0xe30, 0x1000dc1f,
	        0xe34, 0x10008c1f,
	        0xe38, 0x02140102,
	        0xe3c, 0x681604c2,
	        0xe40, 0x01007c00,
	        0xe44, 0x01004800,
	        0xe48, 0xfb000000,
	        0xe4c, 0x000028d1,
	        0xe50, 0x1000dc1f,
	        0xe54, 0x10008c1f,
	        0xe58, 0x02140102,
	        0xe5c, 0x28160d05,
	        0xe60, 0x00000008,
	        0xe68, 0x001b25a4,
	        0xe6c, 0x631b25a0,
	        0xe70, 0x631b25a0,
	        0xe74, 0x081b25a0,
	        0xe78, 0x081b25a0,
	        0xe7c, 0x081b25a0,
	        0xe80, 0x081b25a0,
	        0xe84, 0x631b25a0,
	        0xe88, 0x081b25a0,
	        0xe8c, 0x631b25a0,
	        0xed0, 0x631b25a0,
	        0xed4, 0x631b25a0,
	        0xed8, 0x631b25a0,
	        0xedc, 0x001b25a0,
	        0xee0, 0x001b25a0,
	        0xeec, 0x6b1b25a0,
	        0xf14, 0x00000003,
	        0xf4c, 0x00000000,
	        0xf00, 0x00000300,
	};	        
	
	
	
	// Not used on 1t path
	private int rtl8192cuphy_reg_2tarray[] = {
	        0x024, 0x0011800d, 
	        0x028, 0x00ffdb83,
	        0x800, 0x80040002,
	        0x804, 0x00000003,
	        0x808, 0x0000fc00,
	        0x80c, 0x0000000a,
	        0x810, 0x10005388,
	        0x814, 0x020c3d10,
	        0x818, 0x02200385,
	        0x81c, 0x00000000,
	        0x820, 0x01000100,
	        0x824, 0x00390004,
	        0x828, 0x01000100,
	        0x82c, 0x00390004,
	        0x830, 0x27272727,
	        0x834, 0x27272727,
	        0x838, 0x27272727,
	        0x83c, 0x27272727,
	        0x840, 0x00010000,
	        0x844, 0x00010000,
	        0x848, 0x27272727,
	        0x84c, 0x27272727,
	        0x850, 0x00000000,
	        0x854, 0x00000000,
	        0x858, 0x569a569a,
	        0x85c, 0x0c1b25a4,
	        0x860, 0x66e60230,
	        0x864, 0x061f0130,
	        0x868, 0x27272727,
	        0x86c, 0x2b2b2b27,
	        0x870, 0x07000700,
	        0x874, 0x22184000,
	        0x878, 0x08080808,
	        0x87c, 0x00000000,
	        0x880, 0xc0083070,
	        0x884, 0x000004d5,
	        0x888, 0x00000000,
	        0x88c, 0xcc0000c0,
	        0x890, 0x00000800,
	        0x894, 0xfffffffe,
	        0x898, 0x40302010,
	        0x89c, 0x00706050,
	        0x900, 0x00000000,
	        0x904, 0x00000023,
	        0x908, 0x00000000,
	        0x90c, 0x81121313,
	        0xa00, 0x00d047c8,
	        0xa04, 0x80ff000c,
	        0xa08, 0x8c838300,
	        0xa0c, 0x2e68120f,
	        0xa10, 0x9500bb78,
	        0xa14, 0x11144028,
	        0xa18, 0x00881117,
	        0xa1c, 0x89140f00,
	        0xa20, 0x1a1b0000,
	        0xa24, 0x090e1317,
	        0xa28, 0x00000204,
	        0xa2c, 0x00d30000,
	        0xa70, 0x101fbf00,
	        0xa74, 0x00000007,
	        0xc00, 0x48071d40,
	        0xc04, 0x03a05633,
	        0xc08, 0x000000e4,
	        0xc0c, 0x6c6c6c6c,
	        0xc10, 0x08800000,
	        0xc14, 0x40000100,
	        0xc18, 0x08800000,
	        0xc1c, 0x40000100,
	        0xc20, 0x00000000,
	        0xc24, 0x00000000,
	        0xc28, 0x00000000,
	        0xc2c, 0x00000000,
	        0xc30, 0x69e9ac44,
	        0xc34, 0x469652cf,
	        0xc38, 0x49795994,
	        0xc3c, 0x0a97971c,
	        0xc40, 0x1f7c403f,
	        0xc44, 0x000100b7,
	        0xc48, 0xec020107,
	        0xc4c, 0x007f037f,
	        0xc50, 0x6954341e,
	        0xc54, 0x43bc0094,
	        0xc58, 0x6954341e,
	        0xc5c, 0x433c0094,
	        0xc60, 0x00000000,
	        0xc64, 0x5116848b,
	        0xc68, 0x47c00bff,
	        0xc6c, 0x00000036,
	        0xc70, 0x2c7f000d,
	        0xc74, 0x0186115b,
	        0xc78, 0x0000001f,
	        0xc7c, 0x00b99612,
	        0xc80, 0x40000100,
	        0xc84, 0x20f60000,
	        0xc88, 0x40000100,
	        0xc8c, 0x20200000,
	        0xc90, 0x00121820,
	        0xc94, 0x00000000,
	        0xc98, 0x00121820,
	        0xc9c, 0x00007f7f,
	        0xca0, 0x00000000,
	        0xca4, 0x00000080,
	        0xca8, 0x00000000,
	        0xcac, 0x00000000,
	        0xcb0, 0x00000000,
	        0xcb4, 0x00000000,
	        0xcb8, 0x00000000,
	        0xcbc, 0x28000000,
	        0xcc0, 0x00000000,
	        0xcc4, 0x00000000,
	        0xcc8, 0x00000000,
	        0xccc, 0x00000000,
	        0xcd0, 0x00000000,
	        0xcd4, 0x00000000,
	        0xcd8, 0x64b22427,
	        0xcdc, 0x00766932,
	        0xce0, 0x00222222,
	        0xce4, 0x00000000,
	        0xce8, 0x37644302,
	        0xcec, 0x2f97d40c,
	        0xd00, 0x00080740,
	        0xd04, 0x00020403,
	        0xd08, 0x0000907f,
	        0xd0c, 0x20010201,
	        0xd10, 0xa0633333,
	        0xd14, 0x3333bc43,
	        0xd18, 0x7a8f5b6b,
	        0xd2c, 0xcc979975,
	        0xd30, 0x00000000,
	        0xd34, 0x80608000,
	        0xd38, 0x00000000,
	        0xd3c, 0x00027293,
	        0xd40, 0x00000000,
	        0xd44, 0x00000000,
	        0xd48, 0x00000000,
	        0xd4c, 0x00000000,
	        0xd50, 0x6437140a,
	        0xd54, 0x00000000,
	        0xd58, 0x00000000,
	        0xd5c, 0x30032064,
	        0xd60, 0x4653de68,
	        0xd64, 0x04518a3c,
	        0xd68, 0x00002101,
	        0xd6c, 0x2a201c16,
	        0xd70, 0x1812362e,
	        0xd74, 0x322c2220,
	        0xd78, 0x000e3c24,
	        0xe00, 0x2a2a2a2a,
	        0xe04, 0x2a2a2a2a,
	        0xe08, 0x03902a2a,
	        0xe10, 0x2a2a2a2a,
	        0xe14, 0x2a2a2a2a,
	        0xe18, 0x2a2a2a2a,
	        0xe1c, 0x2a2a2a2a,
	        0xe28, 0x00000000,
	        0xe30, 0x1000dc1f,
	        0xe34, 0x10008c1f,
	        0xe38, 0x02140102,
	        0xe3c, 0x681604c2,
	        0xe40, 0x01007c00,
	        0xe44, 0x01004800,
	        0xe48, 0xfb000000,
	        0xe4c, 0x000028d1,
	        0xe50, 0x1000dc1f,
	        0xe54, 0x10008c1f,
	        0xe58, 0x02140102,
	        0xe5c, 0x28160d05,
	        0xe60, 0x00000010,
	        0xe68, 0x001b25a4,
	        0xe6c, 0x63db25a4,
	        0xe70, 0x63db25a4,
	        0xe74, 0x0c1b25a4,
	        0xe78, 0x0c1b25a4,
	        0xe7c, 0x0c1b25a4,
	        0xe80, 0x0c1b25a4,
	        0xe84, 0x63db25a4,
	        0xe88, 0x0c1b25a4,
	        0xe8c, 0x63db25a4,
	        0xed0, 0x63db25a4,
	        0xed4, 0x63db25a4,
	        0xed8, 0x63db25a4,
	        0xedc, 0x001b25a4,
	        0xee0, 0x001b25a4,
	        0xeec, 0x6fdb25a4,
	        0xf14, 0x00000003,
	        0xf4c, 0x00000000,
	        0xf00, 0x00000300,
	};
	
	
	private int rtl8192curadioa_1tarray[] = {
	        0x000, 0x00030159,
	        0x001, 0x00031284,
	        0x002, 0x00098000,
	        0x003, 0x00018c63,
	        0x004, 0x000210e7,
	        0x009, 0x0002044f,
	        0x00a, 0x0001adb1,
	        0x00b, 0x00054867,
	        0x00c, 0x0008992e,
	        0x00d, 0x0000e52c,
	        0x00e, 0x00039ce7,
	        0x00f, 0x00000451,
	        0x019, 0x00000000,
	        0x01a, 0x00010255,
	        0x01b, 0x00060a00,
	        0x01c, 0x000fc378,
	        0x01d, 0x000a1250,
	        0x01e, 0x0004445f,
	        0x01f, 0x00080001,
	        0x020, 0x0000b614,
	        0x021, 0x0006c000,
	        0x022, 0x00000000,
	        0x023, 0x00001558,
	        0x024, 0x00000060,
	        0x025, 0x00000483,
	        0x026, 0x0004f000,
	        0x027, 0x000ec7d9,
	        0x028, 0x000577c0,
	        0x029, 0x00004783,
	        0x02a, 0x00000001,
	        0x02b, 0x00021334,
	        0x02a, 0x00000000,
	        0x02b, 0x00000054,
	        0x02a, 0x00000001,
	        0x02b, 0x00000808,
	        0x02b, 0x00053333,
	        0x02c, 0x0000000c,
	        0x02a, 0x00000002,
	        0x02b, 0x00000808,
	        0x02b, 0x0005b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000003,
	        0x02b, 0x00000808,
	        0x02b, 0x00063333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000004,
	        0x02b, 0x00000808,
	        0x02b, 0x0006b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000005,
	        0x02b, 0x00000808,
	        0x02b, 0x00073333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000006,
	        0x02b, 0x00000709,
	        0x02b, 0x0005b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000007,
	        0x02b, 0x00000709,
	        0x02b, 0x00063333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000008,
	        0x02b, 0x0000060a,
	        0x02b, 0x0004b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000009,
	        0x02b, 0x0000060a,
	        0x02b, 0x00053333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000a,
	        0x02b, 0x0000060a,
	        0x02b, 0x0005b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000b,
	        0x02b, 0x0000060a,
	        0x02b, 0x00063333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000c,
	        0x02b, 0x0000060a,
	        0x02b, 0x0006b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000d,
	        0x02b, 0x0000060a,
	        0x02b, 0x00073333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000e,
	        0x02b, 0x0000050b,
	        0x02b, 0x00066666,
	        0x02c, 0x0000001a,
	        0x02a, 0x000e0000,
	        0x010, 0x0004000f,
	        0x011, 0x000e31fc,
	        0x010, 0x0006000f,
	        0x011, 0x000ff9f8,
	        0x010, 0x0002000f,
	        0x011, 0x000203f9,
	        0x010, 0x0003000f,
	        0x011, 0x000ff500,
	        0x010, 0x00000000,
	        0x011, 0x00000000,
	        0x010, 0x0008000f,
	        0x011, 0x0003f100,
	        0x010, 0x0009000f,
	        0x011, 0x00023100,
	        0x012, 0x00032000,
	        0x012, 0x00071000,
	        0x012, 0x000b0000,
	        0x012, 0x000fc000,
	        0x013, 0x000287b3,
	        0x013, 0x000244b7,
	        0x013, 0x000204ab,
	        0x013, 0x0001c49f,
	        0x013, 0x00018493,
	        0x013, 0x0001429b,
	        0x013, 0x00010299,
	        0x013, 0x0000c29c,
	        0x013, 0x000081a0,
	        0x013, 0x000040ac,
	        0x013, 0x00000020,
	        0x014, 0x0001944c,
	        0x014, 0x00059444,
	        0x014, 0x0009944c,
	        0x014, 0x000d9444,
	        0x015, 0x0000f405,
	        0x015, 0x0004f405,
	        0x015, 0x0008f405,
	        0x015, 0x000cf405,
	        0x016, 0x000e0330,
	        0x016, 0x000a0330,
	        0x016, 0x00060330,
	        0x016, 0x00020330,
	        0x000, 0x00010159,
	        0x018, 0x0000f401,
	        0x0fe, 0x00000000,
	        0x0fe, 0x00000000,
	        0x01f, 0x00080003,
	        0x0fe, 0x00000000,
	        0x0fe, 0x00000000,
	        0x01e, 0x00044457,
	        0x01f, 0x00080000,
	        0x000, 0x00030159,
	};

	
	
	private int rtl8192curadioa_2tarray[] = {
	        0x000, 0x00030159,
	        0x001, 0x00031284,
	        0x002, 0x00098000,
	        0x003, 0x00018c63,
	        0x004, 0x000210e7,
	        0x009, 0x0002044f,
	        0x00a, 0x0001adb1,
	        0x00b, 0x00054867,
	        0x00c, 0x0008992e,
	        0x00d, 0x0000e52c,
	        0x00e, 0x00039ce7,
	        0x00f, 0x00000451,
	        0x019, 0x00000000,
	        0x01a, 0x00010255,
	        0x01b, 0x00060a00,
	        0x01c, 0x000fc378,
	        0x01d, 0x000a1250,
	        0x01e, 0x0004445f,
	        0x01f, 0x00080001,
	        0x020, 0x0000b614,
	        0x021, 0x0006c000,
	        0x022, 0x00000000,
	        0x023, 0x00001558,
	        0x024, 0x00000060,
	        0x025, 0x00000483,
	        0x026, 0x0004f000,
	        0x027, 0x000ec7d9,
	        0x028, 0x000577c0,
	        0x029, 0x00004783,
	        0x02a, 0x00000001,
	        0x02b, 0x00021334,
	        0x02a, 0x00000000,
	        0x02b, 0x00000054,
	        0x02a, 0x00000001,
	        0x02b, 0x00000808,
	        0x02b, 0x00053333,
	        0x02c, 0x0000000c,
	        0x02a, 0x00000002,
	        0x02b, 0x00000808,
	        0x02b, 0x0005b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000003,
	        0x02b, 0x00000808,
	        0x02b, 0x00063333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000004,
	        0x02b, 0x00000808,
	        0x02b, 0x0006b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000005,
	        0x02b, 0x00000808,
	        0x02b, 0x00073333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000006,
	        0x02b, 0x00000709,
	        0x02b, 0x0005b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000007,
	        0x02b, 0x00000709,
	        0x02b, 0x00063333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000008,
	        0x02b, 0x0000060a,
	        0x02b, 0x0004b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x00000009,
	        0x02b, 0x0000060a,
	        0x02b, 0x00053333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000a,
	        0x02b, 0x0000060a,
	        0x02b, 0x0005b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000b,
	        0x02b, 0x0000060a,
	        0x02b, 0x00063333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000c,
	        0x02b, 0x0000060a,
	        0x02b, 0x0006b333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000d,
	        0x02b, 0x0000060a,
	        0x02b, 0x00073333,
	        0x02c, 0x0000000d,
	        0x02a, 0x0000000e,
	        0x02b, 0x0000050b,
	        0x02b, 0x00066666,
	        0x02c, 0x0000001a,
	        0x02a, 0x000e0000,
	        0x010, 0x0004000f,
	        0x011, 0x000e31fc,
	        0x010, 0x0006000f,
	        0x011, 0x000ff9f8,
	        0x010, 0x0002000f,
	        0x011, 0x000203f9,
	        0x010, 0x0003000f,
	        0x011, 0x000ff500,
	        0x010, 0x00000000,
	        0x011, 0x00000000,
	        0x010, 0x0008000f,
	        0x011, 0x0003f100,
	        0x010, 0x0009000f,
	        0x011, 0x00023100,
	        0x012, 0x00032000,
	        0x012, 0x00071000,
	        0x012, 0x000b0000,
	        0x012, 0x000fc000,
	        0x013, 0x000287b3,
	        0x013, 0x000244b7,
	        0x013, 0x000204ab,
	        0x013, 0x0001c49f,
	        0x013, 0x00018493,
	        0x013, 0x0001429b,
	        0x013, 0x00010299,
	        0x013, 0x0000c29c,
	        0x013, 0x000081a0, 
	        0x013, 0x000040ac, 
	        0x013, 0x00000020, 
	        0x014, 0x0001944c,
	        0x014, 0x00059444,
	        0x014, 0x0009944c,
	        0x014, 0x000d9444,
	        0x015, 0x0000f405,
	        0x015, 0x0004f405,
	        0x015, 0x0008f405,
	        0x015, 0x000cf405,
	        0x016, 0x000e0330,
	        0x016, 0x000a0330,
	        0x016, 0x00060330,
	        0x016, 0x00020330,
	        0x000, 0x00010159,
	        0x018, 0x0000f401,
	        0x0fe, 0x00000000,
	        0x0fe, 0x00000000,
	        0x01f, 0x00080003,
	        0x0fe, 0x00000000,
	        0x0fe, 0x00000000,
	        0x01e, 0x00044457,
	        0x01f, 0x00080000,
	        0x000, 0x00030159,
	};
	
	
	
	private int rtl8192cumac_2t_array[] = {
	        0x420, 0x00000080,
	        0x423, 0x00000000,
	        0x430, 0x00000000,
	        0x431, 0x00000000,
	        0x432, 0x00000000,
	        0x433, 0x00000001,
	        0x434, 0x00000004,
	        0x435, 0x00000005,
	        0x436, 0x00000006,
	        0x437, 0x00000007,
	        0x438, 0x00000000,
	        0x439, 0x00000000,
	        0x43a, 0x00000000,
	        0x43b, 0x00000001,
	        0x43c, 0x00000004,
	        0x43d, 0x00000005,
	        0x43e, 0x00000006,
	        0x43f, 0x00000007,
	        0x440, 0x0000005d,
	        0x441, 0x00000001,
	        0x442, 0x00000000,
	        0x444, 0x00000015,
	        0x445, 0x000000f0,
	        0x446, 0x0000000f,
	        0x447, 0x00000000,
	        0x458, 0x00000041,
	        0x459, 0x000000a8,
	        0x45a, 0x00000072,
	        0x45b, 0x000000b9,
	        0x460, 0x00000066,
	        0x461, 0x00000066,
	        0x462, 0x00000008,
	        0x463, 0x00000003,
	        0x4c8, 0x000000ff,
	        0x4c9, 0x00000008,
	        0x4cc, 0x000000ff,
	        0x4cd, 0x000000ff,
	        0x4ce, 0x00000001,
	        0x500, 0x00000026,
	        0x501, 0x000000a2,
	        0x502, 0x0000002f,
	        0x503, 0x00000000,
	        0x504, 0x00000028,
	        0x505, 0x000000a3,
	        0x506, 0x0000005e,
	        0x507, 0x00000000,
	        0x508, 0x0000002b,
	        0x509, 0x000000a4,
	        0x50a, 0x0000005e,
	        0x50b, 0x00000000,
	        0x50c, 0x0000004f,
	        0x50d, 0x000000a4,
	        0x50e, 0x00000000,
	        0x50f, 0x00000000,
	        0x512, 0x0000001c,
	        0x514, 0x0000000a,
	        0x515, 0x00000010,
	        0x516, 0x0000000a,
	        0x517, 0x00000010,
	        0x51a, 0x00000016,
	        0x524, 0x0000000f,
	        0x525, 0x0000004f,
	        0x546, 0x00000040,
	        0x547, 0x00000000,
	        0x550, 0x00000010,
	        0x551, 0x00000010,
	        0x559, 0x00000002,
	        0x55a, 0x00000002,
	        0x55d, 0x000000ff,
	        0x605, 0x00000030,
	        0x608, 0x0000000e,
	        0x609, 0x0000002a,
	        0x652, 0x00000020,
	        0x63c, 0x0000000a,
	        0x63d, 0x0000000e,
	        0x63e, 0x0000000a,
	        0x63f, 0x0000000e,
	        0x66e, 0x00000005,
	        0x700, 0x00000021,
	        0x701, 0x00000043,
	        0x702, 0x00000065,
	        0x703, 0x00000087,
	        0x708, 0x00000021,
	        0x709, 0x00000043,
	        0x70a, 0x00000065,
	        0x70b, 0x00000087,
	};
	
	private int rtl8192cuagctab_1tarray[] = {
	        0xc78, 0x7b000001,
	        0xc78, 0x7b010001,
	        0xc78, 0x7b020001,
	        0xc78, 0x7b030001,
	        0xc78, 0x7b040001,
	        0xc78, 0x7b050001,
	        0xc78, 0x7a060001,
	        0xc78, 0x79070001,
	        0xc78, 0x78080001,
	        0xc78, 0x77090001,
	        0xc78, 0x760a0001,
	        0xc78, 0x750b0001,
	        0xc78, 0x740c0001,
	        0xc78, 0x730d0001,
	        0xc78, 0x720e0001,
	        0xc78, 0x710f0001,
	        0xc78, 0x70100001,
	        0xc78, 0x6f110001,
	        0xc78, 0x6e120001,
	        0xc78, 0x6d130001,
	        0xc78, 0x6c140001,
	        0xc78, 0x6b150001,
	        0xc78, 0x6a160001,
	        0xc78, 0x69170001,
	        0xc78, 0x68180001,
	        0xc78, 0x67190001,
	        0xc78, 0x661a0001,
	        0xc78, 0x651b0001,
	        0xc78, 0x641c0001,
	        0xc78, 0x631d0001,
	        0xc78, 0x621e0001,
	        0xc78, 0x611f0001,
	        0xc78, 0x60200001,
	        0xc78, 0x49210001,
	        0xc78, 0x48220001,
	        0xc78, 0x47230001,
	        0xc78, 0x46240001,
	        0xc78, 0x45250001,
	        0xc78, 0x44260001,
	        0xc78, 0x43270001,
	        0xc78, 0x42280001,
	        0xc78, 0x41290001,
	        0xc78, 0x402a0001,
	        0xc78, 0x262b0001,
	        0xc78, 0x252c0001,
	        0xc78, 0x242d0001,
	        0xc78, 0x232e0001,
	        0xc78, 0x222f0001,
	        0xc78, 0x21300001,
	        0xc78, 0x20310001,
	        0xc78, 0x06320001,
	        0xc78, 0x05330001,
	        0xc78, 0x04340001,
	        0xc78, 0x03350001,
	        0xc78, 0x02360001,
	        0xc78, 0x01370001,
	        0xc78, 0x00380001,
	        0xc78, 0x00390001,
	        0xc78, 0x003a0001,
	        0xc78, 0x003b0001,
	        0xc78, 0x003c0001,
	        0xc78, 0x003d0001,
	        0xc78, 0x003e0001,
	        0xc78, 0x003f0001,
	        0xc78, 0x7b400001,
	        0xc78, 0x7b410001,
	        0xc78, 0x7b420001,
	        0xc78, 0x7b430001,
	        0xc78, 0x7b440001,
	        0xc78, 0x7b450001,
	        0xc78, 0x7a460001,
	        0xc78, 0x79470001,
	        0xc78, 0x78480001,
	        0xc78, 0x77490001,
	        0xc78, 0x764a0001,
	        0xc78, 0x754b0001,
	        0xc78, 0x744c0001,
	        0xc78, 0x734d0001,
	        0xc78, 0x724e0001,
	        0xc78, 0x714f0001,
	        0xc78, 0x70500001,
	        0xc78, 0x6f510001,
	        0xc78, 0x6e520001,
	        0xc78, 0x6d530001,
	        0xc78, 0x6c540001,
	        0xc78, 0x6b550001,
	        0xc78, 0x6a560001,
	        0xc78, 0x69570001,
	        0xc78, 0x68580001,
	        0xc78, 0x67590001,
	        0xc78, 0x665a0001,
	        0xc78, 0x655b0001,
	        0xc78, 0x645c0001,
	        0xc78, 0x635d0001,
	        0xc78, 0x625e0001,
	        0xc78, 0x615f0001,
	        0xc78, 0x60600001,
	        0xc78, 0x49610001,
	        0xc78, 0x48620001,
	        0xc78, 0x47630001,
	        0xc78, 0x46640001,
	        0xc78, 0x45650001,
	        0xc78, 0x44660001,
	        0xc78, 0x43670001,
	        0xc78, 0x42680001,
	        0xc78, 0x41690001,
	        0xc78, 0x406a0001,
	        0xc78, 0x266b0001,
	        0xc78, 0x256c0001,
	        0xc78, 0x246d0001,
	        0xc78, 0x236e0001,
	        0xc78, 0x226f0001,
	        0xc78, 0x21700001,
	        0xc78, 0x20710001,
	        0xc78, 0x06720001,
	        0xc78, 0x05730001,
	        0xc78, 0x04740001,
	        0xc78, 0x03750001,
	        0xc78, 0x02760001,
	        0xc78, 0x01770001,
	        0xc78, 0x00780001,
	        0xc78, 0x00790001,
	        0xc78, 0x007a0001,
	        0xc78, 0x007b0001,
	        0xc78, 0x007c0001,
	        0xc78, 0x007d0001,
	        0xc78, 0x007e0001,
	        0xc78, 0x007f0001,
	        0xc78, 0x3800001e,
	        0xc78, 0x3801001e,
	        0xc78, 0x3802001e,
	        0xc78, 0x3803001e,
	        0xc78, 0x3804001e,
	        0xc78, 0x3805001e,
	        0xc78, 0x3806001e,
	        0xc78, 0x3807001e,
	        0xc78, 0x3808001e,
	        0xc78, 0x3c09001e,
	        0xc78, 0x3e0a001e,
	        0xc78, 0x400b001e,
	        0xc78, 0x440c001e,
	        0xc78, 0x480d001e,
	        0xc78, 0x4c0e001e,
	        0xc78, 0x500f001e,
	        0xc78, 0x5210001e,
	        0xc78, 0x5611001e,
	        0xc78, 0x5a12001e,
	        0xc78, 0x5e13001e,
	        0xc78, 0x6014001e,
	        0xc78, 0x6015001e,
	        0xc78, 0x6016001e,
	        0xc78, 0x6217001e,
	        0xc78, 0x6218001e,
	        0xc78, 0x6219001e,
	        0xc78, 0x621a001e,
	        0xc78, 0x621b001e,
	        0xc78, 0x621c001e,
	        0xc78, 0x621d001e,
	        0xc78, 0x621e001e,
	        0xc78, 0x621f001e,
	};
	
	
	
	private int rtl8192cuagctab_2tarray[] = {
	        0xc78, 0x7b000001,
	        0xc78, 0x7b010001,
	        0xc78, 0x7b020001,
	        0xc78, 0x7b030001,
	        0xc78, 0x7b040001,
	        0xc78, 0x7b050001,
	        0xc78, 0x7a060001,
	        0xc78, 0x79070001,
	        0xc78, 0x78080001,
	        0xc78, 0x77090001,
	        0xc78, 0x760a0001,
	        0xc78, 0x750b0001,
	        0xc78, 0x740c0001,
	        0xc78, 0x730d0001,
	        0xc78, 0x720e0001,
	        0xc78, 0x710f0001,
	        0xc78, 0x70100001,
	        0xc78, 0x6f110001,
	        0xc78, 0x6e120001,
	        0xc78, 0x6d130001,
	        0xc78, 0x6c140001,
	        0xc78, 0x6b150001,
	        0xc78, 0x6a160001,
	        0xc78, 0x69170001,
	        0xc78, 0x68180001,
	        0xc78, 0x67190001,
	        0xc78, 0x661a0001,
	        0xc78, 0x651b0001,
	        0xc78, 0x641c0001,
	        0xc78, 0x631d0001,
	        0xc78, 0x621e0001,
	        0xc78, 0x611f0001,
	        0xc78, 0x60200001,
	        0xc78, 0x49210001,
	        0xc78, 0x48220001,
	        0xc78, 0x47230001,
	        0xc78, 0x46240001,
	        0xc78, 0x45250001,
	        0xc78, 0x44260001,
	        0xc78, 0x43270001,
	        0xc78, 0x42280001,
	        0xc78, 0x41290001,
	        0xc78, 0x402a0001,
	        0xc78, 0x262b0001,
	        0xc78, 0x252c0001,
	        0xc78, 0x242d0001,
	        0xc78, 0x232e0001,
	        0xc78, 0x222f0001,
	        0xc78, 0x21300001,
	        0xc78, 0x20310001,
	        0xc78, 0x06320001,
	        0xc78, 0x05330001,
	        0xc78, 0x04340001,
	        0xc78, 0x03350001,
	        0xc78, 0x02360001,
	        0xc78, 0x01370001,
	        0xc78, 0x00380001,
	        0xc78, 0x00390001,
	        0xc78, 0x003a0001,
	        0xc78, 0x003b0001,
	        0xc78, 0x003c0001,
	        0xc78, 0x003d0001,
	        0xc78, 0x003e0001,
	        0xc78, 0x003f0001,
	        0xc78, 0x7b400001,
	        0xc78, 0x7b410001,
	        0xc78, 0x7b420001,
	        0xc78, 0x7b430001,
	        0xc78, 0x7b440001,
	        0xc78, 0x7b450001,
	        0xc78, 0x7a460001,
	        0xc78, 0x79470001,
	        0xc78, 0x78480001,
	        0xc78, 0x77490001,
	        0xc78, 0x764a0001,
	        0xc78, 0x754b0001,
	        0xc78, 0x744c0001,
	        0xc78, 0x734d0001,
	        0xc78, 0x724e0001,
	        0xc78, 0x714f0001,
	        0xc78, 0x70500001,
	        0xc78, 0x6f510001,
	        0xc78, 0x6e520001,
	        0xc78, 0x6d530001,
	        0xc78, 0x6c540001,
	        0xc78, 0x6b550001,
	        0xc78, 0x6a560001,
	        0xc78, 0x69570001,
	        0xc78, 0x68580001,
	        0xc78, 0x67590001,
	        0xc78, 0x665a0001,
	        0xc78, 0x655b0001,
	        0xc78, 0x645c0001,
	        0xc78, 0x635d0001,
	        0xc78, 0x625e0001,
	        0xc78, 0x615f0001,
	        0xc78, 0x60600001,
	        0xc78, 0x49610001,
	        0xc78, 0x48620001,
	        0xc78, 0x47630001,
	        0xc78, 0x46640001,
	        0xc78, 0x45650001,
	        0xc78, 0x44660001,
	        0xc78, 0x43670001,
	        0xc78, 0x42680001,
	        0xc78, 0x41690001,
	        0xc78, 0x406a0001,
	        0xc78, 0x266b0001,
	        0xc78, 0x256c0001,
	        0xc78, 0x246d0001,
	        0xc78, 0x236e0001,
	        0xc78, 0x226f0001,
	        0xc78, 0x21700001,
	        0xc78, 0x20710001,
	        0xc78, 0x06720001,
	        0xc78, 0x05730001,
	        0xc78, 0x04740001,
	        0xc78, 0x03750001,
	        0xc78, 0x02760001,
	        0xc78, 0x01770001,
	        0xc78, 0x00780001,
	        0xc78, 0x00790001,
	        0xc78, 0x007a0001,
	        0xc78, 0x007b0001,
	        0xc78, 0x007c0001,
	        0xc78, 0x007d0001,
	        0xc78, 0x007e0001,
	        0xc78, 0x007f0001,
	        0xc78, 0x3800001e,
	        0xc78, 0x3801001e,
	        0xc78, 0x3802001e,
	        0xc78, 0x3803001e,
	        0xc78, 0x3804001e,
	        0xc78, 0x3805001e,
	        0xc78, 0x3806001e,
	        0xc78, 0x3807001e,
	        0xc78, 0x3808001e,
	        0xc78, 0x3c09001e,
	        0xc78, 0x3e0a001e,
	        0xc78, 0x400b001e,
	        0xc78, 0x440c001e,
	        0xc78, 0x480d001e,
	        0xc78, 0x4c0e001e,
	        0xc78, 0x500f001e,
	        0xc78, 0x5210001e,
	        0xc78, 0x5611001e,
	        0xc78, 0x5a12001e,
	        0xc78, 0x5e13001e,
	        0xc78, 0x6014001e,
	        0xc78, 0x6015001e,
	        0xc78, 0x6016001e,
	        0xc78, 0x6217001e,
	        0xc78, 0x6218001e,
	        0xc78, 0x6219001e,
	        0xc78, 0x621a001e,
	        0xc78, 0x621b001e,
	        0xc78, 0x621c001e,
	        0xc78, 0x621d001e,
	        0xc78, 0x621e001e,
	        0xc78, 0x621f001e,
	};
	
    
    private int[] macaddr = new int[6];

    	
	private int rtl8192_ioread8(int addr) {
		byte[] val = {0};

		mConnection.controlTransfer(RTL8192_REQT_READ, RTL8192_REQ_GET_REG, addr, 0, val, 1, 10);
        
		// Dalvik is LE so just shift it in
        return val[0];
	}
	
	private int rtl8192_ioread16(int addr) {
		byte[] val = {0, 0};
		
        mConnection.controlTransfer(RTL8192_REQT_READ, RTL8192_REQ_GET_REG, addr, 0, val, 2, 1);
                
        return (val[1] << 8) + val[0];
	}
	
	private int rtl8192_ioread32(int addr) {
		byte[] val = {0, 0, 0, 0};
		
        mConnection.controlTransfer(RTL8192_REQT_READ, RTL8192_REQ_GET_REG, addr, 0, val, 4, 1);

        // Dalvik is LE so just shift it in
        return (val[3] << 24) + (val[2] << 16) + (val[1] << 8) + val[0];
	}
	
	public static final byte[] intToByteArray(int value) {
		return new byte[] {
				(byte) value,
				(byte)(value >>> 8),
				(byte)(value >>> 16),
				(byte)(value >>> 24)};
	}
	
	private void rtl8192_iowrite32(int addr, int value) 
	{
		byte[] val = intToByteArray(value);
				
        mConnection.controlTransfer(RTL8192_REQT_WRITE, RTL8192_REQ_GET_REG, addr, 0, val, 4, 1);     
	}
	
	
	private void rtl8192_iowrite16(int addr, int value) {
		byte[] val = intToByteArray(value);
		// Log.d(TAG, "iowrite16 controlxfer to " + addr);
		// only take first 2 bytes
        mConnection.controlTransfer(RTL8192_REQT_WRITE, RTL8192_REQ_GET_REG, addr, 0, val, 2, 1);     
	}
	

	private void rtl8192_iowrite8(int addr, int value) {
		byte[] val = {(byte) value};
				
        mConnection.controlTransfer(RTL8192_REQT_WRITE, RTL8192_REQ_GET_REG, addr, 0, val, 1, 1);     
	}
	

	


	@Override
	public ArrayList<UsbDevice> scanUsbDevices() {  
		ArrayList<UsbDevice> rl = new ArrayList<UsbDevice>();
		
        HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while(deviceIterator.hasNext()){
            UsbDevice device = deviceIterator.next();
            
            if (scanUsbDevice(device)) {
            	rl.add(device);
            }
        }
    
        return rl;
	}

	@Override
	public boolean scanUsbDevice(UsbDevice device) 
	{
    	if (device.getVendorId() == 0x0846 && device.getProductId() == 0x9041)
    	{
    		deviceName="Netgear WNA1000M";
    	}
    	else if (device.getVendorId() == 0x0846 && device.getProductId() == 0x9041)
    	{
    		deviceName="RTL8188CUS";
    	}    		
    	else if (device.getVendorId() == 0x050d && device.getProductId() == 0x1102)
    	{
    		deviceName="Belkin F7D1102";
    	}  
		
		return true;
	}
	

	public void doShutdown() 
	{
		super.doShutdown();
		mUsbThread.stopUsb();
	}
	
	
	@Override
	public void setChannel(int c) {
		super.setChannel(c);
		rtl8192_set_channel(c);
	}


	
	
	private boolean llt_write(int address,int data)
	{
		int value = (((address) & 0xFF) << 8) | ((data) & 0xFF) | (((1) & 0x3) << 30);
		rtl8192_iowrite32(0x01E0,value);
		int reg;
		
		boolean pollbit = false;
		int pollretries=0;
		while ((pollretries<100)&&(pollbit==false))
		{
			reg = rtl8192_ioread32(0x01E0);
			reg &= (1<<1);
			if (0==(((reg) >> 30) & 0x3)) pollbit=true;
			pollretries++;
			SystemClock.sleep(1);
		}
		if (pollretries>=100) 
		{
			return false;
		}
		return true;
	}
	
	
	private int calculate_bitshift(int bitmask)
	{
		int i;
		for (i=0;i<=31;i++)
		{
			if (((bitmask>>i)&1)!=0) break;
		}
		return i;
	}
	
	private void set_bbreg(int addr,int bitmask,int data)
	{
		int datas=data,originalvalue,bitshift;
		
		if (bitmask!=0xffffffff)
		{
			originalvalue = rtl8192_ioread32(addr);
			bitshift = calculate_bitshift(bitmask);
			datas = (originalvalue &~(bitmask))|(data<<bitshift);
		}
		rtl8192_iowrite32(addr,datas);
	}
	
	private int get_bbreg(int regaddr,int mask)
	{
		int returnvalue,originalvalue,bitshift;
		originalvalue = rtl8192_ioread32(regaddr);
		bitshift = calculate_bitshift(mask);
		returnvalue = (originalvalue&mask)>>bitshift;
		return returnvalue;
	}
	
	
	private int phy_rf_serial_read(int rf, int offset)
	{
		int newoffset,tmplong,tmplong2,retvalue,rfpi_enable=0;
		
		offset &=0x3f;
		newoffset = offset;
		tmplong = get_bbreg(0x824,0xFFFFFFFF);
		if (rf==0) tmplong2 = tmplong;
		else tmplong2  = get_bbreg(0x82c,0xffffffff);
		tmplong2=(tmplong2&(~0x7f800000))|(newoffset<<23)|0x80000000;
		
		set_bbreg(0x824,0xFFFFFFFF,tmplong &(~0x80000000));
		SystemClock.sleep(1);
		set_bbreg(0x824,0xFFFFFFFF,tmplong2);
		SystemClock.sleep(1);
		set_bbreg(0x824,0xFFFFFFFF,tmplong|0x80000000);
		if (rf==0) rfpi_enable = get_bbreg(0x820,(1<<8));
		else rfpi_enable = get_bbreg(0x828,(1<<8));
		if (rfpi_enable!=0) retvalue = get_bbreg(0x8b8,0xfffff);
		else retvalue = get_bbreg(0x8a0,0xfffff);
		return retvalue;
	}

	private void phy_rf_serial_write(int rf,int offset,int data)
	{
		int newoffset,dataandaddr;
		
		offset&=0x3f;
		newoffset=offset;
		dataandaddr = ((newoffset<<20)|(data&0x000fffff))&0x0fffffff;
		set_bbreg(0x840,0xffffffff,dataandaddr);
	}
	
	
	private void set_rfreg(int rf, int regaddr,int bitmask,int data)
	{
		int bitshift,originalvalue;
		if (bitmask!=0xfffff)
		{
			originalvalue = phy_rf_serial_read(rf,regaddr);
			bitshift = calculate_bitshift(bitmask);
			data = (originalvalue&(~bitmask)|(data<<bitshift));
		}
		phy_rf_serial_write(rf,regaddr,data);
	}
	
	private int get_rfreg(int rf, int regaddr, int bitmask)
	{
		int originalvalue,readbackvalue;//,bitshift;
		
		originalvalue = phy_rf_serial_read(rf,regaddr);
		//bitshift = calculate_bitshift(bitmask);
		readbackvalue = (originalvalue&0x000fffff);
		return readbackvalue;
	}
	
	
	
	private void rtl8192_set_ether_addr(byte addr[])
	{
		rtl8192_iowrite8(0x0610,addr[0]);
		rtl8192_iowrite8(0x0611,addr[1]);
		rtl8192_iowrite8(0x0612,addr[2]);
		rtl8192_iowrite8(0x0613,addr[3]);
		rtl8192_iowrite8(0x0614,addr[4]);
		rtl8192_iowrite8(0x0615,addr[5]);
	}
	
	// not used currently
	private void rtl8192_set_bssid(byte addr[])
	{
		rtl8192_iowrite8(0x0618,addr[0]);
		rtl8192_iowrite8(0x0619,addr[1]);
		rtl8192_iowrite8(0x0620,addr[2]);
		rtl8192_iowrite8(0x0621,addr[3]);
		rtl8192_iowrite8(0x0622,addr[4]);
		rtl8192_iowrite8(0x0623,addr[5]);
	}
	
	// currently unused
	private void rtl8192_set_basic_rate(int rate)
	{
		int rcfg = rate;
		int rindex=0;
		
		rcfg&=0x15f;
		rcfg|=1;
		rtl8192_iowrite8(0x0440,rcfg&0xff);
		rtl8192_iowrite8(0x0441,(rcfg>>8)&0xff);
		
		while (rcfg>1)
		{
			rcfg>>=1;
			rindex++;
		}
		rtl8192_iowrite8(0x0480,rindex);
	}
	
	
	private void rtl8192_efuse_power_switch(boolean enabled)
	{
		int reg;
		if (enabled==true)
		{
			reg = rtl8192_ioread16(0x0000);
			if ((reg&(1<<15))==0)
			{
				reg|=(1<<15);
				rtl8192_iowrite16(0x0000,reg);
			}
			reg = rtl8192_ioread16(0x0002);
			if ((reg&(1<<12))==0)
			{
				reg|=(1<<12);
				rtl8192_iowrite16(0x0002,reg);
			}
			reg = rtl8192_ioread16(0x0008);
			if (((reg&(1<<12))==0)||(reg&(1<<1))==0)
			{
				reg|=(1<<12)|(1<<1);
				rtl8192_iowrite16(0x0008,reg);
			}
		}
	}
	
	private byte rtl8192_read_efuse_byte(int offset)
	{
		int readbyte,retry=0,val;
		
		rtl8192_iowrite8(0x0030+1,offset&0xff);
		readbyte=rtl8192_ioread8(0x0030+2);
		rtl8192_iowrite8(0x0030+2,((offset>>8)&3)|(readbyte&0xfc));
		readbyte=rtl8192_ioread8(0x0030+3);
		rtl8192_iowrite8(0x0030+3,(readbyte&0x7f));
		val = rtl8192_ioread32(0x0030);
		while (((((val>>24)&0xff)&0x80)==0) && (retry<10000))
		{
			val = rtl8192_ioread32(0x0030);
			retry++;
		}
		SystemClock.sleep(5);
		val = rtl8192_ioread32(0x0030);
		int ret = val&0xff;
		return ((byte)ret);
	}
	
	
	private void rtl8192_read_efuse()
	{
		int rtemp,efuseaddr=0,u1temp=0;
		int efusemaxsection = 16;
		int offset,wren;
		int efuseword[][] = new int[4][16];
		byte efusetbl[] = new byte[16*8*4*2];
		

		rtemp = rtl8192_read_efuse_byte(efuseaddr);
		if (rtemp!=0xff)
		{
			efuseaddr++;
		}
		
		while ((rtemp!=0xff)&&(efuseaddr<128))
		{
			if ((rtemp&0x1f)==0x0f)
			{
				u1temp=(rtemp&0xe0)>>5;
				rtemp=rtl8192_read_efuse_byte(efuseaddr);
				if ((rtemp&0x0f)==0x0f)
				{
					efuseaddr++;
					rtemp=rtl8192_read_efuse_byte(efuseaddr);
					if ((rtemp!=0xff)&&(efuseaddr<128)) efuseaddr++;
					continue;	
				}
				else
				{
					offset = ((rtemp>>4)&0x0f)|u1temp;
					wren = (rtemp&0x0f);
					efuseaddr++;
				}
			}
			else
			{
				offset = ((rtemp>>4)&0x0f);
				wren = (rtemp&0x0f);
			}
			
			if (offset< efusemaxsection)
			{
				for (int i=0;i<4;i++)
				{
					if ((wren&1)==0)
					{
						rtemp = rtl8192_read_efuse_byte(efuseaddr);
						efuseaddr++;
						efuseword[i][offset] = (rtemp&0xff);
						if (efuseaddr>=128) break;
						rtemp = rtl8192_read_efuse_byte(efuseaddr);
						efuseaddr++;
						efuseword[i][offset] |= ((rtemp<<8)&0xff00);
						if (efuseaddr>=128) break;
					}
					wren>>=1;
				}
			}
			
			rtemp = rtl8192_read_efuse_byte(efuseaddr);
			if ((rtemp!=0xff)&&(efuseaddr<128))
			{
				efuseaddr++;
			}
		}

		for (int i=0;i<16;i++)
		for (int j=0;j<4;j++)
		{
			efusetbl[i*8+j*2]=(byte)(efuseword[j][i]&0xff);
			efusetbl[i*8+j*2+1]=(byte)((efuseword[j][i]>>8)&0xff);
		}
		for (int i=0;i<128;i++) efuse[i] = efusetbl[i];
		
		efuse_chnlarea_txpwr_cck[0][0] = efuse[0x5A+0*3+0];
		efuse_chnlarea_txpwr_cck[0][1] = efuse[0x5A+0*3+1];
		efuse_chnlarea_txpwr_cck[0][2] = efuse[0x5A+0*3+2];
		efuse_chnlarea_txpwr_cck[1][0] = efuse[0x5A+1*3+0];
		efuse_chnlarea_txpwr_cck[1][1] = efuse[0x5A+1*3+1];
		efuse_chnlarea_txpwr_cck[1][2] = efuse[0x5A+1*3+2];
		efuse_chnlarea_txpwr_ht401s[0][0] = efuse[0x60+0*3+0];
		efuse_chnlarea_txpwr_ht401s[0][1] = efuse[0x60+0*3+1];
		efuse_chnlarea_txpwr_ht401s[0][2] = efuse[0x60+0*3+2];
		efuse_chnlarea_txpwr_ht401s[1][0] = efuse[0x60+1*3+0];
		efuse_chnlarea_txpwr_ht401s[1][1] = efuse[0x60+1*3+1];
		efuse_chnlarea_txpwr_ht401s[1][2] = efuse[0x60+1*3+2];

	}
	
	
	
	
	private void rtl8192_set_channel(int channel)
	{
		// set CCK txpower
		/*
		set_bbreg(0xe08,0xff00,0x20);
		set_bbreg(0x86c,0xffffff00,0x20);
		set_bbreg(0x86c,0xff,0x20);
		set_bbreg(0x838,0xffffff00,0x20202020&0x00ffffff);

		// SET OFDM txpower
		int pwrval=0x00000000;//should be 0
		set_bbreg(0xe00,0xFFFFFFFF,pwrval);
		set_bbreg(0xe04,0xFFFFFFFF,pwrval);
		set_bbreg(0xe10,0xFFFFFFFF,pwrval);
		set_bbreg(0xe14,0xFFFFFFFF,pwrval);
		set_bbreg(0xe18,0xFFFFFFFF,pwrval);
		set_bbreg(0xe1c,0xFFFFFFFF,pwrval);
		*/
		
		set_bbreg(0xe08,0xff00,0x25);
		set_bbreg(0x86c,0xffffff00,0x25252500);
		set_bbreg(0x86c,0xff,0);
		set_bbreg(0x838,0xffffff00,0);
		set_bbreg(0xe00,0xFFFFFFFF,0x30303030);
		set_bbreg(0x830,0xffffffff,0);
		set_bbreg(0xe04,0xFFFFFFFF,0x2d2e3030);
		set_bbreg(0x834,0xffffffff,0);
		set_bbreg(0xe10,0xFFFFFFFF,0x2c2c2c2c);
		set_bbreg(0x83c,0xffffffff,0);
		set_bbreg(0xe14,0xFFFFFFFF,0x292b2c2c);
		set_bbreg(0x848,0xffffffff,0);
		set_bbreg(0xe18,0xFFFFFFFF,0x2c2c2c2c);
		set_bbreg(0x84c,0xffffffff,0);
		set_bbreg(0xe1c,0xFFFFFFFF,0x292b2c2c);
		set_bbreg(0x868,0xffffffff,0);
		
		// WRITEREG RF_CHNLBW / channel 
		set_bbreg(0x840,0xFFFFFFFF,RTL8188CUS_CHNLBW+channel);
	}
	
	
	
	// unused currently
	private void rtl8192_set_pwrindex(int regaddr,int bitmask,int data)
	{
		int index=0;
		if (regaddr==0xe00) index=0;
		else if (regaddr==0xe04) index=1;
		else if (regaddr==0xe08) index=6;
		else if ((regaddr==0x86c)&&(bitmask==0xffffff00)) index=7;
		else if (regaddr==0xe10) index=2;
		else if (regaddr==0xe14) index=3;
		else if (regaddr==0xe18) index=4;
		else if (regaddr==0xe1c) index=5;
		else if (regaddr==0x830) index=8;
		else if (regaddr==0x834) index=9;
		else if (regaddr==0x838) index=14;
		else if ((regaddr==0x86c)&&(bitmask==0x000000ff)) index=15;
		else if (regaddr==0x83c) index=10;
		else if (regaddr==0x848) index=11;
		else if (regaddr==0x84c) index=12;
		else if (regaddr==0x868) index=13;
		pwrgroup[pwrgroupcnt][index]=data;
		if (index==13) pwrgroupcnt++;
	}
	

	private int rtl8192_power_init()
	{
	    int reg;
	    boolean pollbit;
	    int pollretries=0;

		pollbit = false;
		pollretries=0;
		while ((pollretries<100)&&(pollbit==false))
		{
			reg = rtl8192_ioread8(0x0004);
			reg &= (1<<1);
			if (reg!=0) pollbit=true;
			pollretries++;
			SystemClock.sleep(1);
		}
		if (pollretries>=100) 
		{
			updateDeviceStatusStringOnUi("Power init failed (poll1)");
			return -1;
		}
		
		rtl8192_iowrite8(0x001C,0);
		rtl8192_iowrite8(0x0011,0x2b);
		SystemClock.sleep(100);
		reg = rtl8192_ioread8(0x0021);
		if ((reg&1)==0)
		{
			reg|=1;
			rtl8192_iowrite8(0x0021,reg);
			SystemClock.sleep(100);
			reg = rtl8192_ioread8(0);
			reg &= ~1;
			rtl8192_iowrite8(0,reg);
		}
		
		reg = rtl8192_ioread16(0x0004);
		reg |= (1<<8);
		rtl8192_iowrite16(0x0004,reg);

		pollbit = false;
		pollretries=0;
		while ((pollretries<100)&&(pollbit==false))
		{
			reg = rtl8192_ioread16(0x0004);
			reg &= (1<<8);
			if (reg!=0) pollbit=true;
			pollretries++;
			SystemClock.sleep(1);
		}
		if (pollretries>=100) 
		{
			updateDeviceStatusStringOnUi("Power init failed (poll2)");
			return -1;
		}

		rtl8192_iowrite16(0x0004,0x0812);
		
		reg = rtl8192_ioread16(0);
		reg &= ~(1<<9);
		rtl8192_iowrite16(0,reg);
		
		reg = rtl8192_ioread8(0x0600);
		reg &= ~(1<<6);
		rtl8192_iowrite8(0x0600,reg);
		
		pollbit = false;
		pollretries=0;
		while ((pollretries<200)&&(pollbit==false))
		{
			reg = rtl8192_ioread8(0x0600);
			reg &= (1<<7);
			if (reg==0) pollbit=true;
			pollretries++;
			SystemClock.sleep(1);
		}
		if (pollretries>=200) 
		{
			updateDeviceStatusStringOnUi("Power init failed (poll3)");
			return -1;
		}
		
		reg = rtl8192_ioread16(0x0100);
		reg |= ((1)|(1<<1)|(1<<2)|(1<<3)|(1<<4)|(1<<5)|(1<<6)|(1<<7)|(1<<9));
		rtl8192_iowrite16(0x0100,reg);
		
		return 0;
	}
	
	
	private int rtl8192_firmware_download()
	{
		int reg,pollretries;
		boolean pollbit;
		byte[] buffer;
		try 
		{
	        InputStream stream = ApplicationContextProvider.getContext().getAssets().open("rtl8192cufw.bin");
	        int size = stream.available();
	        int size1 = size-32;
	        while ((size1%4)!=0) size1++;
	        byte[] bufferbin = new byte[size];
	        buffer = new byte[size1];
	        
	        stream.read(bufferbin);
	        stream.close();
	        for (int a=0;a<size1;a++) buffer[a]=0;
	        for (int a=32;a<size;a++) buffer[a-32]=bufferbin[a];
	    } 
		catch (Exception e) 
		{
			updateDeviceStatusStringOnUi("Firmware open error...");
			return -1;
	    }

		// We now have the firmware, burn it
		// Enable fw dl
		reg = rtl8192_ioread32(0x0080);
		reg |= (1); // reg &=~1;
		rtl8192_iowrite32(0x0080,reg);

		// Write firmware
		for (int page=0;page<(buffer.length/4096);page++)
		{
			reg = (rtl8192_ioread8(0x0082)&0xf8)|(page&7);
			rtl8192_iowrite8(0x0082,reg);
			for (int i=0;i<4096;i++)
			{
				int offset = i+page*4096;
				int offset2 = i;
				int val = buffer[(offset)];
				rtl8192_iowrite8(0x1000+offset2,val);
			}
		}

		if ((buffer.length%4096)>0)
		{
			int page=buffer.length/4096;
			reg = (rtl8192_ioread8(0x0082)&0xf8)|(page&7);
			rtl8192_iowrite8(0x0082,reg);
			for (int i=0;i<(buffer.length%4096);i++)
			{
				int offset = i+page*4096;
				int offset2 = i;
				int val = buffer[(offset)];
				rtl8192_iowrite8(0x1000+offset2,val);
			}
		}
		
		// Disable fw dl
		reg = rtl8192_ioread32(0x0080);
		reg &=~1;
		rtl8192_iowrite32(0x0080,reg);

		// Free to go?
		pollbit = false;
		pollretries=0;
		while ((pollretries<100)&&(pollbit==false))
		{
			reg = rtl8192_ioread8(0x0080);
			reg &= (1<<2);
			if (reg!=0) pollbit=true;
			pollretries++;
			SystemClock.sleep(1);
		}
		if (pollretries>=100) 
		{
			updateDeviceStatusStringOnUi("Firmware checksum failed!");
			return -1;
		}
		
		reg = rtl8192_ioread32(0x0080);
		reg |=(1<<1);
		reg &=~(1<<6);
		rtl8192_iowrite32(0x0080,reg);
		
		pollbit = false;
		pollretries=0;
		while ((pollretries<1000)&&(pollbit==false))
		{
			reg = rtl8192_ioread32(0x0080);
			if ((reg&(1<<6))!=0) pollbit=true;
			pollretries++;
			SystemClock.sleep(10);
		}
		if (pollretries>=1000) 
		{
			updateDeviceStatusStringOnUi("Firmware not ready!");
			return -1;
		}
		
		return 0;
	}
	
	
	
	private int rtl8192_start() 
	{
	    int reg;

		// Some constants per path (we actually use just first path though)
	    int phyreg_rfintfs[] = {0x870,0x870,0x874,0x874};
		int phyreg_rfintfo[] = {0x860,0x864};
		int phyreg_rfintfe[] = {0x860,0x864};
		int phyreg_rfhssi_para1[] = {0x820,0x828};
		int phyreg_rfhssi_para2[] = {0x824,0x82c};
		//unused
		/*
		int phyreg_rfintfi[] = {0x8e0,0x8e0,0x8e4,0x8e4};
		int phyreg_rf3wire_offset[] = {0x840,0x844};
		int phyreg_rflssi_select[] = {0x878,0x878,0x87c,0x87c};
		int phyreg_rftxgain_stage[] = {0x80c,0x80c,0x80c,0x80c};
		int phyreg_rfsw_ctrl[] = {0x858,0x858,0x85c,0x85c};
		int phyreg_rfagc_control1[] = {0xc50,0xc58,0xc60,0xc68};
		int phyreg_rfagc_control2[] = {0xc54,0xc5c,0xc64,0xc6c};
		int phyreg_rfrx_imbal[] = {0xc14,0xc1c,0xc24,0xc2c};
		int phyreg_rfrx_afe[] = {0xc10,0xc18,0xc20,0xc28};
		int phyreg_rftx_imbal[] = {0xc80,0xc88,0xc90,0xc98};
		int phyreg_rftx_afe[] = {0xc84,0xc8c,0xc94,0xc9c};
		int phyreg_rf_rb[] = {0x8a0,0x8a4,0x8a8,0x8ac};
		int phyreg_rf_rbpi[] = {0x8b8,0x8bc};
		*/
	    

	    // Check if EEPROM is valid
	    rtl8192_efuse_power_switch(true);
	    rtl8192_read_efuse();
		if ((efuse[0]!=0x29)&&(efuse[1]!=0x81))
		{
			updateDeviceStatusStringOnUi("EEPROM invalid!");
			return -1;
		}
	    
		
	    // Initialize hardware
		updateDeviceStatusStringOnUi("Initializing power...");
		if (rtl8192_power_init()!=0) return -2;
		
		
		// Initialize LLT Table (boundary=0xf9)
		updateDeviceStatusStringOnUi("Initializing LLT...");
        
        for (int i=0;i<248;i++)
        {
                if (llt_write(i,i+1)==false)
                {
                        updateDeviceStatusStringOnUi("LLT Init failed (loop1)");
                        return -3;
                }
        }
        llt_write(248,0xff);
        
        for (int i=249;i<255;i++)
        {
                if (llt_write(i,i+1)==false)
                {
                        updateDeviceStatusStringOnUi("LLT Init failed (loop2)");
                        return -4;
                }
        }
        llt_write(255,249);
		
        
		// Initialize Queue reserved page
		updateDeviceStatusStringOnUi("Initializing queue reserved page...");
		
		reg=rtl8192_ioread16(0xfe66);
        int hashq,hasnq,haslq,nqueues,npages,nrempages;
        hashq=hasnq=haslq=0;
        if (((reg&0x0f)>>0)!=0) hashq=1;
        if (((reg&0xf0)>>4)!=0) hasnq=1;
        if (((reg&0xf00)>>8)!=0) hasnq=1;
        // 1 1 0
        nqueues=hashq+haslq+hasnq;
        npages = 17 / nqueues;
        nrempages = 17 % nqueues;
        if (hasnq==1)
        {
                rtl8192_iowrite8(0x0214,npages);
        }
        else rtl8192_iowrite8(0x0214,0);
        rtl8192_iowrite32(0x0200,
                        ((231<<16)&0x00ff0000) |
                        ((((hashq==1)?(npages+nrempages):0)<<0)&0xff) |
                        ((((haslq==1)?(npages):0)<<8)&0x0000ff00) |
                        0x80000000);
		
		

		// Initialize TX/RX buffer
        updateDeviceStatusStringOnUi("Initializing TX/RX Buffer...");
		rtl8192_iowrite8(0x0424,249);
		rtl8192_iowrite8(0x0425,249);
		rtl8192_iowrite8(0x045D,249);
		rtl8192_iowrite8(0x0114,249);
		rtl8192_iowrite8(0x0208,249);
		rtl8192_iowrite8(0x0114+2,0x27FF);
		reg = 1 | (1<<4);
		rtl8192_iowrite8(0x0104,reg);
		
		
		
		// info queue priority
		reg = rtl8192_ioread16(0x010C)&(~0xfff0);
		reg |= 0xfaf0;
		rtl8192_iowrite16(0x010C,reg);
		
		
		// page boundary
		
		
		// driver info size
		rtl8192_iowrite8(0x060F,4);
		
		// enable interrupts
		rtl8192_iowrite32(0x0120,0xffffffff);
		rtl8192_iowrite32(0x0128,0xffffffff);

		
		// init wmac setting
		reg = 0x1|0x2|0x4|0x8|0x200|0x2000|0x4000|0x40000000|0x10000000|0x80000000;
		rtl8192_iowrite32(0x0608,reg);
		rtl8192_iowrite32(0x0620,0xFFFFFFFF);
		rtl8192_iowrite32(0x0624,0xFFFFFFFF);
		reg=0xFFFF;
		rtl8192_iowrite16(0x06A0,reg);
		rtl8192_iowrite16(0x06A2,0);
		rtl8192_iowrite16(0x06A4,reg);
		
		// init adaptive ctrl
		reg = rtl8192_ioread32(0x0440);
		reg &= ~0xFFFFF;
		reg |= 0xFFFF1;
		rtl8192_iowrite32(0x0440,reg);
		reg = ((0x30) & 0x3F) | (((0x30) & 0x3F) << 8);
		rtl8192_iowrite32(0x042A,reg);
		
		
		// EDCA
		rtl8192_iowrite16(0x0428,0x100a);
		rtl8192_iowrite16(0x063a,0x100a);
		rtl8192_iowrite16(0x0514,0x100a);
		rtl8192_iowrite16(0x0516,0x100a);
		rtl8192_iowrite32(0x0508,0x005ea42b);
		rtl8192_iowrite32(0x050C,0x0000a44f);
		rtl8192_iowrite32(0x0504,0x005ea324);
		rtl8192_iowrite32(0x0500,0x002fa226);
		rtl8192_iowrite8(0x0512,0x1c);
		rtl8192_iowrite8(0x051A,0x16);
		rtl8192_iowrite16(0x0546,0x40);
		rtl8192_iowrite8(0x0559,2);
		rtl8192_iowrite8(0x055A,2);
		
		
		// rate fallback 
		// retry function
		
		// init usb aggregation
		rtl8192_iowrite32(0x0458,0x99997631);
		rtl8192_iowrite8(0x051A,0x16);
		rtl8192_iowrite16(0x04ca,0x0708);
		//tx
		reg = rtl8192_ioread32(0x0208);
        reg = (reg &~0xf0)|((6<<4)&0xf0);
		
		
		// set min space
		rtl8192_iowrite8(0x045C,0x0a);



		
		// Update firmware
		updateDeviceStatusStringOnUi("Initializing firmware...");
		if (rtl8192_firmware_download()!=0) return -5;
		
		
		
		// Initialize PHY 
		updateDeviceStatusStringOnUi("Initializing PHY...");

		for (int i=0;i<rtl8192cumac_2t_array.length;i+=2)
		{
			rtl8192_iowrite8(rtl8192cumac_2t_array[i],rtl8192cumac_2t_array[i+1]);
		}
		
		
		// Initialize BB
		updateDeviceStatusStringOnUi("Initializing BB...");

		
		// write to registers the magic values

		reg = rtl8192_ioread16(0x0002);
		rtl8192_iowrite16(0x0002,reg|(1<<13)|(1<<1)|1);
		rtl8192_iowrite8(0x0028,0x83);
		rtl8192_iowrite8(0x0029,0xdb);
		rtl8192_iowrite8(0x001F,1|(1<<1)|(1<<2));
		rtl8192_iowrite8(0x0002,(1<<2)|(1<<4)|(1<<1)|1);
		rtl8192_iowrite8(0x0022,0x0f);
		rtl8192_iowrite8(0x0015,0xe9);
		rtl8192_iowrite8(0x0025,0x80);
		
		// Do the actual baseband initialization
		// 1t codepath hardcoded

		for (int i=0;i<rtl8192cuphy_reg_1tarray.length;i+=2)
		{
			switch (rtl8192cuphy_reg_1tarray[i])
			{
				case 0xfe:
					SystemClock.sleep(50);break;
				case 0xfd:
					SystemClock.sleep(5);break;
				case 0xfc:
					SystemClock.sleep(1);break;
				case 0xfb:
					SystemClock.sleep(1);break;
				case 0xfa:
					SystemClock.sleep(1);break;
				case 0xf9:
					SystemClock.sleep(1);break;
				default:
					
			}
			set_bbreg(rtl8192cuphy_reg_1tarray[i],0xFFFFFFFF,rtl8192cuphy_reg_1tarray[i+1]);
			SystemClock.sleep(1);
		}
		
		
		// Initialize AGC
		for (int i=0;i<rtl8192cuagctab_1tarray.length;i+=2)
		{
			set_bbreg(rtl8192cuagctab_1tarray[i],0xFFFFFFFF,rtl8192cuagctab_1tarray[i+1]);
			SystemClock.sleep(1);
		}
		
		
		// Initialize RF
		updateDeviceStatusStringOnUi("Initializing RF...");
		
		reg = get_bbreg(phyreg_rfintfs[0],0x10);
		set_bbreg(phyreg_rfintfe[0],0x10<<16,0x1);
		SystemClock.sleep(1);
		set_bbreg(phyreg_rfintfo[0],0x10,0x1);
		SystemClock.sleep(1);
		set_bbreg(phyreg_rfhssi_para1[0],0x400,0);
		SystemClock.sleep(1);
		set_bbreg(phyreg_rfhssi_para2[0],0x400,0);
		SystemClock.sleep(1);
		
		for (int i=0;i<rtl8192curadioa_1tarray.length;i+=2)
		{
			if (rtl8192curadioa_1tarray[i]==0xfe) SystemClock.sleep(50);
			else if (rtl8192curadioa_1tarray[i]==0xfd) SystemClock.sleep(5);
			else if (rtl8192curadioa_1tarray[i]==0xfc) SystemClock.sleep(1);
			else if (rtl8192curadioa_1tarray[i]==0xfb) SystemClock.sleep(1);
			else if (rtl8192curadioa_1tarray[i]==0xfa) SystemClock.sleep(1);
			else if (rtl8192curadioa_1tarray[i]==0xf9) SystemClock.sleep(1);
			else set_rfreg(0,rtl8192curadioa_1tarray[i],0xfffff,rtl8192curadioa_1tarray[i+1]);
		}
		
		
		// Do not initialize path B - change for non-RTL8188CUS devices
		/*
		for (int i=0;i<rtl8192curadiob_2tarray.length;i+=2)
		{
			if (rtl8192curadiob_2tarray[i]==0xfe) SystemClock.sleep(50);
			else if (rtl8192curadiob_2tarray[i]==0xfd) SystemClock.sleep(5);
			else if (rtl8192curadiob_2tarray[i]==0xfc) SystemClock.sleep(1);
			else if (rtl8192curadiob_2tarray[i]==0xfb) SystemClock.sleep(1);
			else if (rtl8192curadiob_2tarray[i]==0xfa) SystemClock.sleep(1);
			else if (rtl8192curadiob_2tarray[i]==0xf9) SystemClock.sleep(1);
			else set_rfreg(1,rtl8192curadiob_2tarray[i],0xfffff,rtl8192curadiob_2tarray[i+1]);
		}
		*/
		
		
		set_bbreg(phyreg_rfintfs[0],0x10,reg);
		chnlbw = get_rfreg(0,0x18,0xfffff);
		
		
		// BB Block on
		set_bbreg(0x800,0x1000000,1);
		set_bbreg(0x800,0x2000000,1);

		
		// cam_reset_all_entry
		rtl8192_iowrite32(0x0670,(1<<31)|(1<<30));
		
		// set mac addr
		// WTF ARE YOU SMOKING DUDE?!?
		byte macaddr[] =new byte[] {0x1,0x2,0x3,0x4,0x5,0x6};
		rtl8192_set_ether_addr(macaddr);
		rtl8192_set_bssid(macaddr);
		
		// phy_set_rfpath_switch
		set_bbreg(0x0878,(1<<13),1);
		set_bbreg(0x004C,(1<<23),1);
		set_bbreg(0x0860,0x300,2);
		
		// get power info
		// Do we really need that?!?
		// Perhaps yes in future - for packet injection 
		for (int rfpath=0;rfpath<2;rfpath++)
		for (int i=0;i<3;i++)
		{
			eeprom_chnlarea_txpwr_cck[rfpath][i] = efuse[0x5a+rfpath*3+i];
			eeprom_chnlarea_txpwr_ht40_1s[rfpath][i] = efuse[0x60+rfpath*3+i];
		}

		for (int i=0;i<3;i++)
		{
			reg = efuse[0x66+i];
			eprom_chnlarea_txpwr_ht40_2sdf[0][i] = reg&0xf;
			eprom_chnlarea_txpwr_ht40_2sdf[1][i] = (reg>>4)&0xf;
		}
		for (int rfpath=0;rfpath<2;rfpath++)
		for (int i=0;i<14;i++)
		{
			int index;
			if (i<3) index=0;
			else if (i<9) index=1;
			else index=2;
			
			chnlarea_txpwr_cck[rfpath][i] = eeprom_chnlarea_txpwr_cck[rfpath][index];
			chnlarea_txpwr_ht40_1s[rfpath][i] = eeprom_chnlarea_txpwr_ht40_1s[rfpath][index];
			if ((eeprom_chnlarea_txpwr_ht40_1s[rfpath][index] - eprom_chnlarea_txpwr_ht40_2sdf[rfpath][index]) >0)
			{
				chnlarea_txpwr_ht40_2s[rfpath][i]=(eeprom_chnlarea_txpwr_ht40_1s[rfpath][index] - eprom_chnlarea_txpwr_ht40_2sdf[rfpath][index]);
			}
			else chnlarea_txpwr_ht40_2s[rfpath][i]=0;
		}
		for (int i=0;i<3;i++)
		{
			eeprom_pwrlimit_ht40[i] = efuse[0x6F+i];
			eeprom_pwrlimit_ht20[i] = efuse[0x6F+i+3];
		}
		for (int rfpath=0;rfpath<2;rfpath++)
		for (int i=0;i<14;i++)
		{
			int index;
			if (i<3) index=0;
			else if (i<9) index=1;
			else index=2;
			if (rfpath==0)
			{
				pwrlimit_ht40[rfpath][i] = eeprom_pwrlimit_ht40[index]&0xf;
				pwrlimit_ht20[rfpath][i] = eeprom_pwrlimit_ht20[index]&0xf;
			}
			else
			{
				pwrlimit_ht40[rfpath][i] = (eeprom_pwrlimit_ht40[index]&0xf0)>>4;
				pwrlimit_ht20[rfpath][i] = (eeprom_pwrlimit_ht20[index]&0xf0)>>4;
			}
		}

		for (int i=0;i<14;i++)
		{
			int index,tempval;
			if (i<3) index=0;
			else if (i<9) index=1;
			else index=2;
			tempval = efuse[0x69+index];
			txpwr_ht20diff[0][i]=tempval&0xf;
			txpwr_ht20diff[1][i]=(tempval>>4)&0xf;
			if ((txpwr_ht20diff[0][i]&(1<<3))!=0) txpwr_ht20diff[0][i] |= 0xf0;
			if ((txpwr_ht20diff[1][i]&(1<<3))!=0) txpwr_ht20diff[1][i] |= 0xf0;
			tempval = efuse[0x6c+index];
			txpwr_legacyhtdiff[0][i]=tempval&0xf;
			txpwr_legacyhtdiff[1][i]=(tempval>>4)&0xf;
		}
		legacy_ht_txpowerdiff=txpwr_legacyhtdiff[0][7];
		eeprom_tssi[0]=efuse[0x76];
		eeprom_tssi[1]=efuse[0x77];
		
		
		
		
		
		// omit phy iq calibration

		// omit dm_check_txpower_tracking
		
		// lc calibration -not needed
		
		int rval=0,cval=0;
		reg = rtl8192_ioread8(0xd03);
		if ((reg&0x70)!=0) rtl8192_iowrite8(0xd03,reg&0x8f);
		else rtl8192_iowrite8(0x522,0xff);
		if ((reg&0x70)!=0)
		{
			cval = get_rfreg(0,0,0xfff);
			set_rfreg(0,0,0xfff,(cval&0x8ffff)|0x10000);
		}
		rval = get_rfreg(0,0x18,0xfff);
		set_rfreg(0,0x18,0xfff,rval|0x8000);
		SystemClock.sleep(100);
		if ((reg&0x70)!=0)
		{
			rtl8192_iowrite8(0xd03,reg);
			set_rfreg(0,0,0xfff,cval);
		}
		else rtl8192_iowrite8(0x522,0);
				
		
		// hw configure quirks
		
		rtl8192_iowrite8(0x0022,0xf);
		rtl8192_iowrite8(0x15,0xe9);
		rtl8192_iowrite8(0x423,0xff);
		rtl8192_iowrite8(0xfe40,0xe0);
		rtl8192_iowrite8(0xfe41,0x8d);
		rtl8192_iowrite8(0xfe42,0x80);
		rtl8192_iowrite32(0x20c,0xfd0320);
		

		// omit InitPABias
		
		// update mac setting - omit
		
		// dm_init - omit
		
		// Now init network type to monitor
		reg = 1|0x80000000;
		rtl8192_iowrite32(0x0608,reg);
		rtl8192_iowrite8(0x0102,2);
		
		// Set channel to 11
		rtl8192_set_channel(11);
		
		
		/*
		rtl8192_set_basic_rate(0xfff);
		// varslottime 10+28=38
		rtl8192_iowrite8(0x0500,38);
		rtl8192_iowrite8(0x0504,38);
		rtl8192_iowrite8(0x0508,38);
		rtl8192_iowrite8(0x050c,38);
		// acparam
		rtl8192_iowrite32(0x0508,0xff03); //0=be
		rtl8192_iowrite32(0x050c,0xff07); //1=bk
		rtl8192_iowrite32(0x0504,0xbcff02); //2=vi
		rtl8192_iowrite32(0x0500,0x66f702); //3=vo
		// 20mhz
		reg = rtl8192_ioread8(0x603);
		int val = rtl8192_ioread8(0x442);
		rtl8192_iowrite8(0x050c,38);
		reg|=(1<<2);
		rtl8192_iowrite8(0x603,reg);
		set_bbreg(0x0800,0x1,0x0);
		set_bbreg(0x0900,0x1,0x0);
		set_bbreg(0x0884,(1<<10),1);
		//rtsel
		rtl8192_iowrite8(0x480,3);
		//sifs time
		rtl8192_iowrite8(0x515,10);
		rtl8192_iowrite8(0x517,10);
		rtl8192_iowrite8(0x429,10);
		rtl8192_iowrite8(0x63b,10);
		rtl8192_iowrite8(0x63d,10);
		rtl8192_iowrite8(0x63f,10);
		*/

		updateDeviceStringOnUi(deviceName);
		updateDeviceStatusStringOnUi("Running.");
		Log.d(TAG, "Started rtl8192");
		started = true;
		return 0;
	}
	

	
	// Do we need that?
	void dumpOneFrame() {
		int max = mBulkEndpoint.getMaxPacketSize();
		
		byte[] buf = new byte[max];
		
		int r = mConnection.bulkTransfer(mBulkEndpoint, buf, max, 1000);
		
		Log.d(TAG, "dumpOneFrame, bulkxfer: " + r);
		
		String s = "";
		for (int i = 0; i < r; i++) {
			s = s + " " + Integer.toHexString(buf[i]);
		}
		
		Log.d(TAG, "got frame: " + s);
	}
	

	
    private class usbThread extends Thread 
    {

    	private volatile UsbSource usbsource;

    	
    	public usbThread(UsbSource s) {
    		super();
    		
    		usbsource = s;
    	}

    	
    	public void stopUsb() {
    		stopped = true;
    	}

    	
    	@Override
    	public void run() 
    	{
    		// Why 2500?
    		int sz = 2500;
			byte[] buffer = new byte[sz];
			Band band = Band.instance();

			
			while (!stopped) 
			{
				int l=0;
				//synchronized(this)
				{
					l = mConnection.bulkTransfer(mBulkEndpoint, buffer, sz, 2000);
				}
				if (l > 0) 
				{
					// Offset rcvbuf by (deviceinfo+rtlinfo) bytes
					int fdword = buffer[0]|(buffer[1]<<8)|(buffer[2]<<16)|(buffer[3]<<24);
		    		int offset = ((fdword>>16)&4)*8 + 24;
		    		parseFrame(buffer,offset,l);
				} 
				else if (l < 0) 
				{
					//updateDeviceStatusStringOnUi("Failed to do bulk transfer "+l);
					//updateStatusStringOnUi("Error");
					Log.e(TAG, "Failed to do bulkio");
					//return;
				}

			}
    	}
    };
    

    
    
    @Override
    public int attachUsbDevice(UsbDevice device) 
    {	 
    	if (device.getVendorId() == 0x0846 && device.getProductId() == 0x9041)
    	{
    		is_rtl8192 = 1;
    		deviceName="Netgear WNA1000M";
    	}
    	else if (device.getVendorId() == 0x0846 && device.getProductId() == 0x9041)
    	{
    		is_rtl8192 = 1;
    		deviceName="RTL8188CUS";
    	}    		
    	else if (device.getVendorId() == 0x050d && device.getProductId() == 0x1102)
    	{
    		is_rtl8192 = 1;
    		deviceName="Belkin F7D1102";
    	}    		
    	updateDeviceString(deviceName);
    	updateDeviceStatusString("Scanning...");
    	
    	if (device.getInterfaceCount() != 1) {
    		updateDeviceStatusString("Could not find USB interface, getInterfaceCount != 1");
    		return -1;
		}
		
		UsbInterface intf = device.getInterface(0);
		if (intf.getEndpointCount() == 0) {
			updateDeviceStatusString("Could not find USB endpoints");
			return -2;
		}
		
        UsbDeviceConnection connection = mUsbManager.openDevice(device);
        if (connection != null && connection.claimInterface(intf, true)) {
        	mConnection = connection;
        	mDevice = device;
        } else {            
        	updateDeviceStatusString("Could not claim and open USB device");
    		
            return -3;
        }
        
		
		UsbEndpoint ep = null;

		ep = intf.getEndpoint(0);
		if (ep.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK ||
				ep.getDirection() != UsbConstants.USB_DIR_IN)
			ep = null;
		
		if (ep == null) {
			updateDeviceString("Unable to find bulk IO USB endpoint"+intf.getEndpointCount());
			return -4;
		}
		
		
		mBulkEndpoint = ep;
		
		UsbEndpoint ep1 = null;
		ep1 = intf.getEndpoint(1);
		if (ep1.getType() != UsbConstants.USB_ENDPOINT_XFER_BULK ||
				ep1.getDirection() != UsbConstants.USB_DIR_OUT)
			ep1 = null;
		
		if (ep1 == null) {
			updateDeviceString("Unable to find bulk IO write USB endpoint"+intf.getEndpointCount());
			return -4;
		}
		
		mInjBulkEndpoint = ep1;
		mUsbThread = new usbThread(this);

		Thread thread = new Thread()
		{
		      @Override
		      public void run() 
		      {
		    	  int hws = rtl8192_start();
		    	  if (hws != 0) 
		    	  {
		    		  return;
		    	  } 
		    	  else 
		    	  {
		    		  Log.d(TAG, "Successfully calibrated & started hw");
		    	  }
		    	  
		    	  mUsbThread.start();
		    	  Thread hopthread = new Thread()
		    	  {
		    		  public void run()
		    		  {
	    				int hop=0;
	    				int chans[]={1,7,11};
						while (!stopped)
	    				{
							//synchronized(this)
							{
								setChannel(chans[hop]);
							}
							hop++;
							if (hop==3) hop=0;
							SystemClock.sleep(100);
						}
		    		  }
		    	  };
		    	  hopthread.start();
		      }
		};
		thread.start();
        return 1;
	}

    // Stub constructor
    public Rtl8192Card(UsbManager manager) 
    {
    	super(manager);
    }
    
	public Rtl8192Card(UsbManager manager, Handler usbhandler, 
			Context context) 
	{
		super(manager, usbhandler, context);
	}

	public Rtl8192Card(UsbManager manager, MainActivity main) 
	{
		super(manager, main);
	}
	
	
	@Override
	public UsbSource makeSource(UsbDevice device, UsbManager manager, Handler servicehandler, 
			Context context) 
	{
		UsbSource s = (UsbSource) new Rtl8192Card(manager, servicehandler, context);
		s.attachUsbDevice(device);
		return s;
	}
	
	
	
	@Override
	public void sendDeauth(String bssid, String hwaddr)
	{
		int val,val1,val2,val3,val4,val5,val6,val7;
		int pktlen=26;
		byte packet[] = new byte[26+32];
		byte s_bssid[] = new byte[6];
		byte s_hwaddr[] = new byte[6];
		

		// get hwaddr and bssid
		s_hwaddr[0]=(byte)Integer.parseInt(hwaddr.substring(0, 2),16);
		s_hwaddr[1]=(byte)Integer.parseInt(hwaddr.substring(3, 5),16);
		s_hwaddr[2]=(byte)Integer.parseInt(hwaddr.substring(6, 8),16);
		s_hwaddr[3]=(byte)Integer.parseInt(hwaddr.substring(9, 11),16);
		s_hwaddr[4]=(byte)Integer.parseInt(hwaddr.substring(12, 14),16);
		s_hwaddr[5]=(byte)Integer.parseInt(hwaddr.substring(15, 17),16);
		
		s_bssid[0]=(byte)Integer.parseInt(bssid.substring(0, 2),16);
		s_bssid[1]=(byte)Integer.parseInt(bssid.substring(3, 5),16);
		s_bssid[2]=(byte)Integer.parseInt(bssid.substring(6, 8),16);
		s_bssid[3]=(byte)Integer.parseInt(bssid.substring(9, 11),16);
		s_bssid[4]=(byte)Integer.parseInt(bssid.substring(12, 14),16);
		s_bssid[5]=(byte)Integer.parseInt(bssid.substring(15, 17),16);
		
		
		// Zero out the packet
		for (int i=0;i<26+32;i++) packet[i]=0;
		val = val1 = val2 = val3 = val4 = val5 = val6 = val7 =0;

		// val: usb bits 31,26,27 size at first 16 bits, offset at next 8
		val = (32<<16)|(pktlen)|(1<<31)|(1<<26)|(1<<27);
		// val1: bit 6 , queue id (0x12) 5 bits at 8
		val1 = (1<<6)|(0x12<<8);
		// val2/3/4/6=0
		// val5: 0x1f at 8, 0xf at 14 
		val5 = (0x1f<<8)|(0xf<<13);
		
		// fill in the values
		packet[0]=(byte)(val&0xff);
		packet[1]=(byte)((val>>8)&0xff);
		packet[2]=(byte)((val>>16)&0xff);
		packet[3]=(byte)((val>>24)&0xff);
		packet[4]=(byte)(val1&0xff);
		packet[5]=(byte)((val1>>8)&0xff);
		packet[6]=(byte)((val1>>16)&0xff);
		packet[7]=(byte)((val1>>24)&0xff);
		packet[8]=(byte)(val2&0xff);
		packet[9]=(byte)((val2>>8)&0xff);
		packet[10]=(byte)((val2>>16)&0xff);
		packet[11]=(byte)((val2>>24)&0xff);
		packet[12]=(byte)(val3&0xff);
		packet[13]=(byte)((val3>>8)&0xff);
		packet[14]=(byte)((val3>>16)&0xff);
		packet[15]=(byte)((val3>>24)&0xff);
		packet[16]=(byte)(val4&0xff);
		packet[17]=(byte)((val4>>8)&0xff);
		packet[18]=(byte)((val4>>16)&0xff);
		packet[19]=(byte)((val4>>24)&0xff);
		packet[20]=(byte)(val5&0xff);
		packet[21]=(byte)((val5>>8)&0xff);
		packet[22]=(byte)((val5>>16)&0xff);
		packet[23]=(byte)((val5>>24)&0xff);
		packet[24]=(byte)(val6&0xff);
		packet[25]=(byte)((val6>>8)&0xff);
		packet[26]=(byte)((val6>>16)&0xff);
		packet[27]=(byte)((val6>>24)&0xff);
		packet[28]=(byte)(val7&0xff);
		packet[29]=(byte)((val7>>8)&0xff);
		packet[30]=(byte)((val7>>16)&0xff);
		packet[31]=(byte)((val7>>24)&0xff);
		
		// checksum
		val7 = (val7 &(~((0xffffffff>>(32-16))<<0))) | ((0&(0xffffffff>>(32-16)))<<0);
		int checksum=0;
		for (int a=0;a<16;a++)
		{
			int v = packet[a*2] | (packet[a*2+1]<<8);
			checksum = checksum ^ v;
		}
		val7 = (val7 &(~((0xffffffff>>(32-16))<<0))) | ((checksum&(0xffffffff>>(32-16)))<<0);
		packet[28]=(byte)(val7&0xff);
		packet[29]=(byte)((val7>>8)&0xff);
		
		for (int k=0;k<3;k++)
		{
			// now the first deauth frame
			packet[32]=(byte)0xc0;
			packet[33]=(byte)0x0;
			packet[34]=(byte)0x3a;
			packet[35]=(byte)0x01;
			
			packet[36]=(byte)s_bssid[0];
			packet[37]=(byte)s_bssid[1];
			packet[38]=(byte)s_bssid[2];
			packet[39]=(byte)s_bssid[3];
			packet[40]=(byte)s_bssid[4];
			packet[41]=(byte)s_bssid[5];
			packet[42]=(byte)s_hwaddr[0];
			packet[43]=(byte)s_hwaddr[1];
			packet[44]=(byte)s_hwaddr[2];
			packet[45]=(byte)s_hwaddr[3];
	
			packet[46]=(byte)s_hwaddr[4];
			packet[47]=(byte)s_hwaddr[5];
			packet[48]=(byte)s_bssid[0];
			packet[49]=(byte)s_bssid[1];
			packet[50]=(byte)s_bssid[2];
			packet[51]=(byte)s_bssid[3];
			packet[52]=(byte)s_bssid[4];
			packet[53]=(byte)s_bssid[5];
	
			packet[54]=(byte)(((k*4))<<4);
			packet[55]=(byte)((k*4+1));
			packet[56]=0x03;
			packet[57]=0x00;
			
			// send teh frame!
			if ((mConnection!=null)&&(mInjBulkEndpoint!=null))
			{
				int l = mConnection.bulkTransfer(mInjBulkEndpoint, packet, packet.length, 100);
				
				if (l < 0)
				{
					updateDeviceStatusStringOnUi("Failed to do out bulk transfer "+l);
					updateStatusStringOnUi("Error");
					Log.e(TAG, "Failed to do bulkio");
					return;
				}
				else
				{
				}
			}
	
			// now the second deauth frame
			packet[32]=(byte)0xc0;
			packet[33]=(byte)0x0;
			packet[34]=(byte)0x3a;
			packet[35]=(byte)0x01;
			
			packet[36]=(byte)s_hwaddr[0];
			packet[37]=(byte)s_hwaddr[1];
			packet[38]=(byte)s_hwaddr[2];
			packet[39]=(byte)s_hwaddr[3];
			packet[40]=(byte)s_hwaddr[4];
			packet[41]=(byte)s_hwaddr[5];
			packet[42]=(byte)s_bssid[0];
			packet[43]=(byte)s_bssid[1];
			packet[44]=(byte)s_bssid[2];
			packet[45]=(byte)s_bssid[3];
	
			packet[46]=(byte)s_bssid[4];
			packet[47]=(byte)s_bssid[5];
			packet[48]=(byte)s_bssid[0];
			packet[49]=(byte)s_bssid[1];
			packet[50]=(byte)s_bssid[2];
			packet[51]=(byte)s_bssid[3];
			packet[52]=(byte)s_bssid[4];
			packet[53]=(byte)s_bssid[5];
	
			packet[54]=(byte)(((k*4+2))<<4);
			packet[55]=(byte)((k*4+3));
			packet[56]=0x03;
			packet[57]=0x00;
			
			// send teh frame!
			if ((mConnection!=null)&&(mInjBulkEndpoint!=null))
			{
				int l = mConnection.bulkTransfer(mInjBulkEndpoint, packet, packet.length, 100);
				
				if (l < 0)
				{
					updateDeviceStatusStringOnUi("Failed to do out bulk transfer "+l);
					updateStatusStringOnUi("Error");
					Log.e(TAG, "Failed to do bulkio");
					return;
				}
				else
				{
				}
			}
		}

		super.sendDeauth(bssid, hwaddr);
	}
	
	
};
