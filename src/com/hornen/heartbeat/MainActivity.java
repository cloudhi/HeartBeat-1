package com.hornen.heartbeat;

import com.hornen.client.HeartBeatClient;
import com.hornen.service.HeartBeatService;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v4.app.NavUtils;

public class MainActivity extends Activity {

	 HeartBeatClient client;
	 HeartBeatService ser;
	 
	 ImageView image;
	 Button btn;
	 Matrix matrix = new Matrix();  
	 PointF startPoint = new PointF();
	 int centenX = 0;
	 int centenY = 0;
	 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        WindowManager.LayoutParams.FLAG_FULLSCREEN);  

//        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        
//        ser = new HeartBeatService(5432);
//        ser.start();
//        
//        client = new HeartBeatClient("127.0.0.1", 5432);
//        client.connect();
        
        image = (ImageView)findViewById(R.id.imageview);
        
        image.setOnTouchListener(new ImageViewOnTouchListener());
        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				matrix.postRotate(30, centenX, centenY);// ÄæÊ±ÕëÐý×ª90¶È  
				image.setImageMatrix(matrix); 
			}
		});
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//    	client.disconnect();
//    	ser.stop();
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	
	private class ImageViewOnTouchListener implements OnTouchListener{

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch(event.getAction() & MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:
				startPoint.set(event.getX(), event.getY());
				break;
			case MotionEvent.ACTION_MOVE:
				centenY = image.getHeight()/2;
				centenX = image.getWidth()/2;
				float angle = (float)(Math.atan2(event.getY() - centenY, event.getX() - centenX) * 180 / Math.PI); 
				matrix.postRotate(10, image.getWidth()/2, image.getHeight()/2);  
				image.setImageMatrix(matrix); 
				startPoint.set(event.getX(), event.getY());
				break;
			}
			image.setImageMatrix(matrix); 
			image.requestLayout();
			return true;
		}
		
	}
    
    
}
