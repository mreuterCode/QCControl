package com.flyinventilator.qccontrol;

import java.util.ArrayList;
import java.util.Set;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class QCControl extends Activity implements SensorEventListener, OnSeekBarChangeListener, View.OnClickListener {
	public BluetoothAdapter mBluetoothAdapter;
	public ArrayList mArrayList = new ArrayList();

	public ControlSendThread control;
	public ConnectedThread conTr;
	public ConnectThread ct;
	public SensorManager Sensors;
	public float[] rotationMatrix = new float[9];
	public float[] orientation = new float[3];
	public float[] mGravs = new float[6];
	public float[] mGeoMags = new float[6];
	
	public TextView tv1;
	public TextView tv2;
	public TextView tv3;
	public TextView tv4;
	public byte[] msg;

	public Button[] StopButton = new Button[2];
	public Button ConnectButton;

	public int Throttle = 0;
	
	public RelativeLayout rel;
	public ControlGraph MainControlGraph;
	
	long lastStamp = System.currentTimeMillis();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar sb = (SeekBar) findViewById(R.id.seekBar1);
        sb.setOnSeekBarChangeListener(this);
        
        MainControlGraph = new ControlGraph(this);
        MainControlGraph.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        rel = (RelativeLayout) findViewById(R.id.rl1);
        rel.addView(MainControlGraph);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);

		//registering Buttons
		StopButton[0] = (Button) findViewById(R.id.button);
		StopButton[1] = (Button) findViewById(R.id.button2);
		ConnectButton = (Button) findViewById(R.id.connect_button);

		StopButton[0].setOnClickListener(this);
		StopButton[1].setOnClickListener(this);
		ConnectButton.setOnClickListener(this);


		Sensors = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensors.registerListener(this,
        		Sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        		                                SensorManager.SENSOR_DELAY_UI);
        Sensors.registerListener(this,
        		Sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        		                                SensorManager.SENSOR_DELAY_UI);

       /* if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 3);

        } else {
        	Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        	// If there are paired devices
        	if (pairedDevices.size() > 0) {
        		// Loop through paired devices
        		for (BluetoothDevice device : pairedDevices) {
        			// Add the name and address to an array adapter to show in a ListView
        			if(device.getName().equals("mr-drone")){
                		ct = new ConnectThread(device, this);
                		ct.start();
                		while(conTr == null){}
                		
                        ControlSendThread control = new ControlSendThread(this, conTr);
                        control.start();
        			}
        		}
        	}
        }*/

    }
	
	public void enableBluetooth(){
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 3);
        }
        return;
	}
	
	public boolean connectToQuadrocopter(){

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				// Add the name and address to an array adapter to show in a ListView
				if (device.getName().equals("mr-drone")) {
					ConnectThread ct = new ConnectThread(device, this);
					ct.start();
					//while (conTr == null) {}


				}
			}
		}

		return false;
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case R.id.connect_qc:
    			if(!mBluetoothAdapter.isEnabled()){
    				enableBluetooth();
    			}
    			return(connectToQuadrocopter());

 /*   		case R.id.disconnectButton:
    			ct.cancel();
    			return true;*/

    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    protected void onActivityResult( int requestCode, int resultCode, Intent data){
    	if(requestCode == 3){
    		Log.e("QCCtrl", "RC: " + resultCode);
    	}
    }
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
        	case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, mGravs, 0, 3);
                break;
        	case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, mGeoMags, 0, 3);
                break;
        	default:
                return;
        }
        
        SensorManager.getRotationMatrix(rotationMatrix, null, mGravs, mGeoMags);
        SensorManager.getOrientation(rotationMatrix, orientation);
        tv1.setText("azimuth: " + orientation[0] + " ");
        tv2.setText("pitch: " + orientation[1] + " ");
        tv3.setText("roll: " + orientation[2] + " ");
        
		
        MainControlGraph.setRoll(orientation[2]);
        MainControlGraph.setPitch(orientation[1]);
        MainControlGraph.invalidate();

    }
	
	public void onReceive(byte[] msg){
		this.msg = msg;
		runOnUiThread(new Runnable(){
			public void run(){
				tv4.setText(QCControl.this.msg[0] + QCControl.this.msg[1] + " ");
			}
		});
	}

	public void onConnected(ConnectedThread connectedTr){
		String toast_text;
		control = new ControlSendThread(this, connectedTr);
		control.start();


	}

	public int getThrottle(){
		return Throttle;
	}
	
	public float[] getSensorData(){
		return orientation;
	}
	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		Throttle = arg1;

	}
	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		
	}

	@Override
	public void onClick(View v) {
		Log.e("QCCtrl", "STOP!");
		switch (v.getId()){
			case R.id.button :
				Log.e("QCCtrl", "button1_STOP");
				if(control != null) {
					this.control.stop_qc();
				}
				break;

			case R.id.button2 :
				Log.e("QCCtrl", "button2_STOP");
				if(control != null) {
					this.control.stop_qc();
				}
				break;

			case R.id.connect_button :
				Log.e("QCCtrl", "connecting QC");
				enableBluetooth();
				connectToQuadrocopter();
				break;

			default:
				break;
		}

	}
}
