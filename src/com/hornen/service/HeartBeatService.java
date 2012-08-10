package com.hornen.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import android.util.Log;

public class HeartBeatService {
	private static final String SER_TAG = "HeartBeatService";
	private int mPort;
	private ServerSocket mSerSocket;
	private LinkedList<SerClient> mSerCliList;
	private Thread mSerThread;
	
	public HeartBeatService(int port){
		mPort = port;
		mSerThread = null;
		mSerSocket = null;
		mSerCliList = new LinkedList<HeartBeatService.SerClient>();
	}
	
	public void start(){
		if(mSerThread != null){
			return;
		}
		
		mSerThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					mSerSocket = new ServerSocket(mPort);
					while(true){
						SerClient serClient = new SerClient(mSerSocket.accept());
						serClient.start();
						
						synchronized (serClient) {
							mSerCliList.add(serClient);
						}
						
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		mSerThread.start();
	}
	
	public void stop(){
		
		synchronized (mSerCliList) {
			for(int i = 0; i < mSerCliList.size(); i++){
				mSerCliList.get(i).stop();
			}
			mSerCliList.clear();
		}
		
		if(mSerSocket == null){
			return;
		}
		
		try {
			mSerSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mSerThread.interrupt();
		mSerThread = null;
		mSerSocket = null;
		
	}
	
	public interface ServiceListener{
		void onAccept(String ip);
		void onBeat(String ip);
		void onDeath(String ip);
	}
	
	private class SerClient{
		private Socket mCliSocket;
		private Thread mCliThread;
		private Timer  mCliTimer;
		private String mCliIP;
		private long    mTimScape;
		private BufferedReader read;
		private PrintWriter   outs;
		
		private SerClient(Socket socket){
			mCliSocket = socket;
			mTimScape = System.currentTimeMillis();
			mCliIP = null;
		}
		
		private void start(){
			
			mCliIP = mCliSocket.getInetAddress().getHostAddress();
			
			mCliThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String strLine = null;
					try {
						read = new BufferedReader(new InputStreamReader(mCliSocket.getInputStream()));
						outs = new PrintWriter(mCliSocket.getOutputStream());
						
						while(true){
							strLine = read.readLine();
							if(strLine == null){
								continue;
							}
							
							if(strLine.equals("service-beat")){
								mTimScape = System.currentTimeMillis();
								Log.e(SER_TAG, "receive client-beat " + getIP());
							}else{
								
							}
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						mCliSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					mCliThread = null;
					mCliSocket = null;
					if(null != mCliTimer){
						mCliTimer.cancel();
						mCliTimer = null;
					}
					
					synchronized (mSerCliList) {
						mSerCliList.remove(this);
					}
					
				}
			});
			mCliThread.start();
			
			mCliTimer = new Timer();
			mCliTimer.schedule(new TimerTask() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(System.currentTimeMillis() - mTimScape > (1000 * 10)){
						mCliTimer.cancel();
						mCliTimer = null;
						
						try {
							if(null != mCliSocket){
								mCliSocket.close();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Log.e(SER_TAG, "client death " + getIP());
					}else{
						if(outs != null){
							outs.println("service-beat");
//							outs.flush();
							Log.e(SER_TAG, "send service-beat " + getIP());
						}
					}
				}
			}, 1000, 1000);
		}
		
		private void stop(){
			
			if(null != mCliSocket){
				try {
					mCliSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mCliSocket = null;
			}
			
			if(mCliThread != null){
				mCliThread.interrupt();
				mCliThread = null;
			}
			
			if(mCliTimer != null){
				mCliTimer.cancel();
				mCliTimer = null;
			}
			
			synchronized (mSerCliList) {
				mSerCliList.remove(this);
			}
		}
		
		private String getIP(){
			return mCliIP;
		}
	}
}
