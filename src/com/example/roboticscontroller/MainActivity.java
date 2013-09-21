package com.example.roboticscontroller;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	public Button leftBtn, rightBtn, forwardBtn, backwardBtn, stopBtn;
	public TextView responseText, connectionText;
	public ImageView webcamView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		leftBtn = (Button) findViewById(R.id.buttonLeft);
		rightBtn = (Button) findViewById(R.id.buttonRight);
		forwardBtn = (Button) findViewById(R.id.buttonForward);
		backwardBtn = (Button) findViewById(R.id.buttonBackward);
		stopBtn = (Button) findViewById(R.id.buttonStop);
		
		responseText = (TextView) findViewById(R.id.textResponse);
		connectionText = (TextView) findViewById(R.id.textResponse);
		connectionText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		new CheckConnectionToServerTask().execute();
		
		webcamView = (ImageView) findViewById(R.id.img);
		new GetImageTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void sendCommand(View view) {
		Button btnClicked = (Button) view;
		new SendCommandTask().execute(btnClicked);
	}
	
	public void updateServerResponse(String response){
		responseText.setText(response);
	}
	
	public void updateConnectionText(String str){
		TextView textConnection = (TextView) findViewById(R.id.textConnection);
		textConnection.setText(str);
	}
	
	
	private InputStream OpenHttpConnection(String urlString) throws IOException
	{
	        InputStream in = null;
	        int response = -1;
	                
	        URL url = new URL(urlString); 
	        URLConnection conn = url.openConnection();
	                  
	        if (!(conn instanceof HttpURLConnection)) throw new IOException("Not an HTTP connection");
	         
	        try{
	            HttpURLConnection httpConn = (HttpURLConnection) conn;
	            httpConn.setAllowUserInteraction(false);
	            httpConn.setInstanceFollowRedirects(true);
	            httpConn.setRequestMethod("GET");
	            httpConn.connect(); 
	 
	            response = httpConn.getResponseCode();                 
	            if (response == HttpURLConnection.HTTP_OK) {
	                in = httpConn.getInputStream();                                 
	            }                     
	        }
	        catch (Exception ex)
	        {
	            throw new IOException("Error connecting");            
	        }
	        return in;     
	}
	
	private Bitmap DownloadImage(String URL)
    {        
        Bitmap bitmap = null;
        InputStream in = null;        
        try {
            in = OpenHttpConnection(URL);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
            Log.w("Bitmap download", "Probably downloaded?");
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            Log.e("Bitmap error", "Bitmap errored...");
        }
        return bitmap;                
    }
	
	private void updateWebcamView(Bitmap _image){
		webcamView.setImageBitmap(_image);
	}
	
	@SuppressLint("UseValueOf")
	private class CheckConnectionToServerTask extends AsyncTask<Void,Void,Integer>{
		private Integer responseCode;
		public void postData() {
			HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.ardaaridici.com/kinect/list_commands.php");
			try {
				HttpResponse response = httpclient.execute(httppost);
				responseCode = new Integer(response.getStatusLine().getStatusCode());
			} catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    	Log.e("ClientProtocol","Error in client protocol ",e);
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	Log.e("IO","Error in IO protocol ",e);
		    } catch (Exception e){
		    	Log.e("Generic","Error in generic ",e);
		    }
	        
		}
		protected Integer doInBackground(Void... params) {
			// TODO Auto-generated method stub
			postData();
			return responseCode;
		}
		protected void onPostExecute(Integer intResp){
			if((int) intResp==200){
				updateConnectionText("Server accesible");
			}else{
				updateConnectionText("Cannot access to server");
			}
	    }
	}
	
	@SuppressLint("UseValueOf")
	private class GetImageTask extends AsyncTask<Void,Void,Bitmap>{
		private Bitmap bitmap;
		
		protected Bitmap doInBackground(Void... params) {
			// TODO Auto-generated method stub
			bitmap = DownloadImage("http://ardaaridici.com/kinect/get_webcam_screen.php");
			return bitmap;
		}
		protected void onPostExecute(Bitmap bmp){
			if(bitmap!=null){
				updateWebcamView(bitmap);
				Log.w("Update webcam", "Update webcam view called");
			}
			new GetImageTask().execute();
			/*
			Handler handler = new Handler();
			handler.postDelayed(new Runnable(){
				public void run(){
					new GetImageTask().execute();
				}
			}, 100);
			*/
	    }
	}
	
	private class SendCommandTask extends AsyncTask<Button,Void,String>{
		private String serverRespondedWith;
		protected String doInBackground(Button... btnClicked){
			postData(btnClicked[0]);
			return "Executed";
		}
		public void postData(Button btnClicked) {
		    // Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost("http://www.ardaaridici.com/kinect/receiver.php");
		    
		    //Log.w("Warning", "HttpPost Point reached");
		    
		    try {
		        // Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		        if(btnClicked.getText().equals(forwardBtn.getText())){
		        	nameValuePairs.add(new BasicNameValuePair("command", "Forward"));
		        }else if(btnClicked.getText().equals(backwardBtn.getText())){
		        	nameValuePairs.add(new BasicNameValuePair("command", "Backward"));
		        }else if(btnClicked.getText().equals(leftBtn.getText())){
		        	nameValuePairs.add(new BasicNameValuePair("command", "Left"));
		        }else if(btnClicked.getText().equals(rightBtn.getText())){
		        	nameValuePairs.add(new BasicNameValuePair("command", "Right"));
		        }else if(btnClicked.getText().equals(stopBtn.getText())){
		        	nameValuePairs.add(new BasicNameValuePair("command", "Stop"));
		        }
		        
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        
		        
		        serverRespondedWith = EntityUtils.toString(entity);
		        Log.w("Server response", serverRespondedWith);
		        
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    	Log.e("ClientProtocol","Error in client protocol ",e);
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    	Log.e("IO","Error in IO protocol ",e);
		    } catch (Exception e){
		    	Log.e("Generic","Error in generic ",e);
		    }
		}
		protected void onPostExecute(String str){
			if(serverRespondedWith.trim().equals("1")){
				updateServerResponse("Successful");
			}else{
				updateServerResponse("Failed");
			}
	    }
	}
}
