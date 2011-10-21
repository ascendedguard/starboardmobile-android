package com.ascend.starboardmobile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class ConnectActivity extends Activity {
   
	private EditText tvIPAddress;
	private EditText tvPort;
	
	static final int DIALOG_INVALID_IP = 0;
	static final int DIALOG_SUCCESS = 1;
	static final int DIALOG_FAILURE = 2;
	static final int DIALOG_INVALID_PORT = 3;
	static final int DIALOG_SOCKET_FAILURE = 4;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_window);
        
        this.tvIPAddress = (EditText)this.findViewById(R.id.txtIpAddress);
        this.tvPort = (EditText)this.findViewById(R.id.txtPort);
    }
    
    protected void ShowToast(int id)
    {
    	String str = null;
    	
    	switch(id) {
	    	case DIALOG_INVALID_IP: {
	    		str = "The provided IP Address was invalid.";
	    		break;
	    	}
	    	case DIALOG_SUCCESS: {
	    		str = "The connection was successful.";
	    		break;
	    	}
	    	case DIALOG_FAILURE: {
	    		str = "The connection failed.";
	    		break;
	    	}
	    	case DIALOG_INVALID_PORT: {
	    		str = "The port was invalid.";
	    		break;
	    	}
	    	case DIALOG_SOCKET_FAILURE: {
	    		str = "Socket failed to create.";
	    		break;
	    	}	
    	}
    	
    	if (str == null)
    	{
    		return;
    	}
    	
		Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
		toast.show();
    }
    
    public void attemptConnect(View v) throws IOException {
    	String ipAddress = tvIPAddress.getText().toString();
    	
    	Matcher matcher = Patterns.IP_ADDRESS.matcher(ipAddress);
    	
    	if (matcher.matches() == false)
    	{
    		ShowToast(DIALOG_INVALID_IP);
    		return;
    	}
    	
    	int server_port = 0;
    	try
    	{
    		server_port = Integer.parseInt(tvPort.getText().toString());
    	}
    	catch (NumberFormatException ex)
    	{
    		ShowToast(DIALOG_INVALID_PORT);
    		return;
    	}
    	
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(server_port);
		} catch (SocketException e) {
			ShowToast(DIALOG_SOCKET_FAILURE);
			e.printStackTrace();
			return;
		}    	
    
    	InetAddress local;
		try {
			local = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			ShowToast(DIALOG_INVALID_IP);
    		s.close();
    		return;
		}
    	
		// Sends a PING
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x90, 0 };

    	byte[] buffer = new byte[100];
    	DatagramPacket response = new DatagramPacket(buffer, buffer.length); 
    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, local, server_port);

    	// Wait 1 second for timeout.
    	s.setSoTimeout(3000);

    	s.send(p);
    	
    	
    	try
    	{
    		s.receive(response);
    	}
    	catch (IOException ex)
    	{
    		ShowToast(DIALOG_FAILURE);
    		s.close();
    		return;
    	}
    	
    	s.close();
    	
    	if (buffer[3] == (byte)0x91)
    	{   		
    		Intent intent = new Intent(this, MainControlsActivity.class);
    		Bundle b = new Bundle();
    		b.putString("ipaddress", ipAddress);
    		b.putInt("port", server_port);
    		intent.putExtras(b);
    		this.startActivity(intent);
    	}
    	else
    	{
    		ShowToast(DIALOG_FAILURE);
    	}	
    }
}