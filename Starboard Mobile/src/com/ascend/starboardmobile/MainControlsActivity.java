package com.ascend.starboardmobile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainControlsActivity extends Activity {
	private String ipAddress;
	private InetAddress serverPath;
	private int serverPort;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_window);
        
        Bundle b = getIntent().getExtras();
        if (b != null)
        {
	        String path = b.getString("ipaddress");
	        ipAddress = path;
	        
	        try {
				serverPath = InetAddress.getByName(path);
			} catch (UnknownHostException e) {
				e.printStackTrace();
	    		return;
			}
	        
	        serverPort = b.getInt("port");
        }
    }
    
    public void showScoreboard(View v) throws IOException {
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x10, 0 };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);
    	s.close();
    }
    
    public void toggleSubbar(View v) throws IOException {
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x21, 0 };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);
    	s.close();
    }  
    
    public void toggleAnnouncement(View v) throws IOException {
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x20, 0 };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);   	
    	s.close();
    }
    
    public void swapPlayer(View v) throws IOException {
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x22, 0 };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);
    	s.close();
    }
    
    public void incrementPlayer1(View v) throws IOException
    {
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x30, 1 };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);
    	s.close();
    }
    
    public void incrementPlayer2(View v) throws IOException
    {
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x30, 2 };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);
    	s.close();
    }
    
    public void updatePlayer1(View v)
    {
		Intent intent = new Intent(this, PlayerEditActivity.class);
		Bundle b = new Bundle();
		b.putString("ipaddress", ipAddress);
		b.putInt("port", serverPort);
		b.putInt("playerId", 1);
		intent.putExtras(b);
		this.startActivity(intent);
    }
    
    public void updatePlayer2(View v)
    {
		Intent intent = new Intent(this, PlayerEditActivity.class);
		Bundle b = new Bundle();
		b.putString("ipaddress", ipAddress);
		b.putInt("port", serverPort);
		b.putInt("playerId", 2);
		intent.putExtras(b);
		this.startActivity(intent);
    }
}
