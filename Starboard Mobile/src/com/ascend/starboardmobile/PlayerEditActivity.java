package com.ascend.starboardmobile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class PlayerEditActivity extends Activity {

	private int player;
	
	private Spinner raceSpinner;
	private Spinner colorSpinner;
	
	private EditText txtPlayerName; 
	private EditText txtScore; 
	
	private InetAddress serverPath;
	private int serverPort;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_window);
        
        raceSpinner = (Spinner) findViewById(R.id.spinnerRace);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.race_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        raceSpinner.setAdapter(adapter);
        
        colorSpinner = (Spinner) findViewById(R.id.spinnerColor);
        ArrayAdapter<CharSequence> adapterColor = ArrayAdapter.createFromResource(
                this, R.array.color_array, android.R.layout.simple_spinner_item);
        adapterColor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(adapterColor);

        txtPlayerName = (EditText) findViewById(R.id.txtPlayerName);
        txtScore = (EditText) findViewById(R.id.txtScore);
        
        Bundle b = getIntent().getExtras();
        if (b != null)
        {
        	this.player = b.getInt("playerId");
        	
	        String path = b.getString("ipaddress");
	        
	        try {
				serverPath = InetAddress.getByName(path);
			} catch (UnknownHostException e) {
				e.printStackTrace();
	    		return;
			}
	        
	        serverPort = b.getInt("port");
        }
        
        TextView tv = (TextView) findViewById(R.id.txtPlayerLabel);
        tv.setText("Player " + player);
        
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	byte[] bytes = new byte[] { 0x30, 1, 0, (byte)0x40, (byte)this.player };

    	DatagramPacket p = new DatagramPacket(bytes, bytes.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	try {
			s.setSoTimeout(3000);
	    	s.send(p);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	byte[] buffer = new byte[500];
    	DatagramPacket response = new DatagramPacket(buffer, buffer.length); 
    	
    	try
    	{
    		s.receive(response);
    	}
    	catch (IOException ex)
    	{
    		s.close();
    		return;
    	}
    	
    	ParsePlayerUpdateResponse(buffer);
    	
    	s.close();
    }
  
    private void ParsePlayerUpdateResponse(byte[] buffer)
    {
    	// 0x00 = Magic Number: 0x30
    	byte numCommands = buffer[1];
    	
    	int offset = 2; // starting offset
    	
    	for(int i = 0; i < numCommands; i++)
    	{
    		byte cmd = buffer[offset++]; // Command type
    		
    		if (cmd == 0x00) // EmptyCommand
    		{
    			offset = ParseEmptyCommand(buffer, offset);
    		}
    		else if (cmd == 0x01) // StringCommand
    		{
    			offset = ParseStringCommand(buffer, offset);
    		}
    		else if (cmd == 0x02) // Int32Command
    		{
    			offset = ParseInt32Command(buffer, offset);
    		}
    		else if (cmd == 0x03) // ByteCommand
    		{
    			offset = ParseByteCommand(buffer, offset);
    		}
    	}
    }
    
    /* returns the new offset */
    private int ParseEmptyCommand(byte[] buffer, int offset)
    {
    	byte cmd = buffer[offset++];
    	byte player = buffer[offset++];
    	
    	// No empty commands are expected to be parsed.
    	
    	return offset;
    }
    
    /* returns the new offset */
    private int ParseStringCommand(byte[] buffer, int offset)
    {
    	byte cmd = buffer[offset++];
    	byte player = buffer[offset++];
    	int length = byteArrayToInt(buffer, offset);
    	
    	offset += 4;
    	
    	byte[] str = new byte[length];
    	
    	for(int i = 0; i < length; i++)
    	{
    		str[i] = buffer[offset++];
    	}
    	
    	String s;
		try {
			s = new String(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return offset;
		}
    	
    	// Handle any expected StringCommands here.
    	
    	if (cmd == 0x01) // UpdatePlayerName
    	{
    		txtPlayerName.setText(s);
    	}
    	
    	return offset;
    }
    
    private int ParseInt32Command(byte[] buffer, int offset)
    {
    	byte cmd = buffer[offset++];
    	byte player = buffer[offset++];
    	int data = byteArrayToInt(buffer, offset);
    	String score = Integer.toString(data);
    	offset += 4;
    	
    	if (cmd == 0x02) // UpdateScore
    	{
    		txtScore.setText(score);
    	}
    	
    	return offset;
    }
    
    private int ParseByteCommand(byte[] buffer, int offset)
    {
    	byte cmd = buffer[offset++];
    	byte player = buffer[offset++];
    	byte data = buffer[offset++];
    	
    	if (cmd == 0x03) // UpdateRace
    	{
    		raceSpinner.setSelection(data);
    	}
    	else if (cmd == 0x04) // UpdateColor
    	{
    		colorSpinner.setSelection(data);
    	}
    	
    	return offset;
    }
    
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }
    
    public void updatePlayer(View v) throws IOException {
    	String name = txtPlayerName.getText().toString();
    	int score = Integer.parseInt(txtScore.getText().toString());
    	int race = raceSpinner.getSelectedItemPosition();
    	int color = colorSpinner.getSelectedItemPosition();

    	byte[] byteNameArray;
    	try {
			byteNameArray = name.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
    	  	
    	byte magicNumber = 0x30;
    	
    	List<Byte> bytes = new ArrayList<Byte>();
    	bytes.add(magicNumber);
    	
    	// 4 updates: name score race color
    	bytes.add((byte)4); // number of commands being sent
    	
    	// PlayerName
    	bytes.add((byte)0x01); // StringCommand
    	bytes.add((byte)0x01); // UpdatePlayerName
    	bytes.add((byte)player); // player
    	
    	int length = byteNameArray.length;
    	// Have to send length as int32
    	bytes.add((byte)(length));
    	bytes.add((byte)(length >>> 8));
    	bytes.add((byte)(length >>> 16));
    	bytes.add((byte)(length >>> 24));
    	
    	// This avoids issues with addAll... i wasn't sure how to convert from byte[] to Byte[] easily
    	for (int i = 0; i < byteNameArray.length; i++)    	
    	{
    		bytes.add(byteNameArray[i]);
    	}
    	
    	// Race
    	bytes.add((byte)0x03); // ByteCommand
    	bytes.add((byte)0x03); // UpdatePlayerRace
    	bytes.add((byte)player); // player
    	bytes.add((byte)race);
    	
    	// Color
    	bytes.add((byte)0x03);
    	bytes.add((byte)0x04);
    	bytes.add((byte)player);
    	bytes.add((byte)color);
    	
    	// Score
    	bytes.add((byte)0x02); // Int32
    	bytes.add((byte)0x02); // UpdatePlayerScore
    	bytes.add((byte)player);

    	// Convert int32 to byte[]
    	bytes.add((byte)(score));
    	bytes.add((byte)(score >>> 8));
    	bytes.add((byte)(score >>> 16));
    	bytes.add((byte)(score >>> 24));
    	
    	// Send the data.
    	DatagramSocket s;
    	try {
			s = new DatagramSocket(serverPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return;
		}    	
    	
    	Byte[] byteArray = bytes.toArray(new Byte[bytes.size()]);
    	
    	byte[] b = new byte[byteArray.length];
    	
    	for(int i = 0; i < byteArray.length; i++)
    	{
    		b[i] = byteArray[i];
    	}
    	
    	DatagramPacket p = new DatagramPacket(b, b.length, serverPath, serverPort);

    	// Wait 1 second for timeout.
    	try {
			s.setSoTimeout(3000);
	    	s.send(p);
		} catch (SocketException e) {
			e.printStackTrace();
			s.close();
			return;
		}

    	s.close();
    }
}
