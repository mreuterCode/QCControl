package com.mreuter.qccontrol;

import java.util.ArrayList;
import java.util.Set;

import android.os.Bundle;
import android.os.ParcelUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class QCControl extends Activity implements SensorEventListener {
	public BluetoothAdapter mBluetoothAdapter;
	public ArrayList mArrayList = new ArrayList();
	public ConnectedThread conTr;
	public SensorManager Sensors;
	public float[] rotationMatrix = new float[9];
	public float[] orientation = new float[3];
	public float[] mGravs = new float[6];
	public float[] mGeoMags = new float[6];
	public TextView tv1;
	public TextView tv2;
	public TextView tv3;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);

        Sensors = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        Sensors.registerListener(this,
        		Sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        		                                SensorManager.SENSOR_DELAY_UI);
        Sensors.registerListener(this,
        		Sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        		                                SensorManager.SENSOR_DELAY_UI);
        		                
        if (!mBluetoothAdapter.isEnabled()) {
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
                		ConnectThread ct = new ConnectThread(device, this);
                		ct.start();
                		while(conTr == null){}
                	 
        			}
        		}
        	}
        }
        
        
    }
	public void setConnectedThread(ConnectedThread ct2){
		conTr = ct2;
		return;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
    }
    
}
