package com.hornen.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class HeartBeatClient {
	private static final String CLI_TAG = "HeartBeatClient";
	private String mIPAddr;
	private int mPort;
	private Thread mThread;
	private Timer mTimer;
	
	private BufferedReader mread;
	private PrintWriter mouts;
	private Socket mSocket;
	private long mTimeScape;
	
	public HeartBeatClient(String ip, int port){
		mIPAddr = ip;
		mPort = port;
		mTimeScape = System.currentTimeMillis();
	}
	
	public void connect(){
		mThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				try {
					mSocket = new Socket(mIPAddr, mPort);
					mread = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
					mouts = new PrintWriter(mSocket.getOutputStream());
					String strLine = null;
					while(true){
						strLine = mread.readLine();
						if(null == strLine){
							continue;
						}
						
						if(strLine.equals("service-beat")){
							mTimeScape = System.currentTimeMillis();
							Log.i(CLI_TAG, "recieve service-beat");
						}else{
							
						}
					}
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(mSocket != null){
					try {
						mSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					mSocket = null;
				}
				mThread = null;
				if(null != mTimer){
					mTimer.cancel();
					mTimer = null;
				}
			}
		});
		mThread.start();
		
		mTimer = new Timer();
		mTimer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(System.currentTimeMillis() - mTimeScape > 10000){
					disconnect();
					Log.i(CLI_TAG, "server death!!!");
				}else{
					if(mouts != null){
						mouts.println("client-beat");
						mouts.flush();
						Log.i(CLI_TAG, "send client-beat");
					}
				}
			}
		}, 1000, 1000);
	}
	
	public void disconnect(){
		
		if(mSocket != null){
			try {
				mSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mSocket = null;
		}
		
		if(mThread != null){
			mThread.interrupt();
			mThread = null;
		}
		
		if(mTimer != null){
			mTimer.cancel();
			mTimer = null;
		}
	}
}
