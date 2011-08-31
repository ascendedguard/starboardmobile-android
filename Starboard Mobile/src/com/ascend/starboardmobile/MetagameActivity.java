package com.ascend.starboardmobile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

public class MetagameActivity extends Activity {
   
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
    
    protected Dialog onCreateDialog(int id)
    {
    	Dialog dialog = null;
    	
    	switch(id) {
    	case DIALOG_INVALID_IP: {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("The provided IP Address was invalid.");
    		
    		dialog = builder.create();
    		break;
    	}
    	case DIALOG_SUCCESS: {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("The connection was successful.");
    		
    		dialog = builder.create();
    		break;
    	}
    	case DIALOG_FAILURE: {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("The connection failed.");
    		
    		dialog = builder.create();
    		break;
    	}
    	case DIALOG_INVALID_PORT: {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("The port was invalid.");
    		
    		dialog = builder.create();
    		break;
    	}
    	case DIALOG_SOCKET_FAILURE: {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setMessage("Socket failed to create.");
    		
    		dialog = builder.create();
    		break;
    	}
    	
    	}
    	
    	return dialog;
    }
    
    public void attemptConnect(View v) throws IOException {
    	String ipAddress = tvIPAddress.getText().toString();
    	
    	Matcher matcher = Patterns.IP_ADDRESS.matcher(ipAddress);
    	
    	if (matcher.matches() == false)
    	{
    		showDialog(DIALOG_INVALID_IP);
    		return;
    	}
    	
    	int server_port = 0;
    	try
    	{
    		server_port = Integer.parseInt(tvPort.getText().toString());
    	}
    	catch (NumberFormatException ex)
    	{
    		showDialog(DIALOG_INVALID_PORT);
    		return;
    	}
    	
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(server_port);
		} catch (SocketException e) {
    		showDialog(DIALOG_SOCKET_FAILURE);
			e.printStackTrace();
			return;
		}    	
    
    	InetAddress local;
		try {
			local = InetAddress.getByName(ipAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
    		showDialog(DIALOG_INVALID_IP);
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
    		showDialog(DIALOG_FAILURE);
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
    		showDialog(DIALOG_FAILURE);
    	}	
    }
}