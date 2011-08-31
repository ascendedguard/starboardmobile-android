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
