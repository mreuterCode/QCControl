package com.flyinventilator.qccontrol;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class QCControl extends Activity implements SensorEventListener, OnSeekBarChangeListener, View.OnClickListener {

	//Threads
	public ControlSendThread control;
	public ConnectedThread conTr;
	public ConnectThread ct;
	public ControlGraph MainControlGraph;

	//Devices
	public SensorManager Sensors;
	public BluetoothAdapter mBluetoothAdapter;

	//Matrices
	public float[] rotationMatrix = new float[9];
	public float[] orientation = new float[3];
	public float[] orientation_calibrated = new float[3];
	public float[] mGravs = new float[6];
	public float[] mGeoMags = new float[6];

	public byte[] msg;

	public int Throttle = 0;
	private float calibration_inc = 0.05f;

	//ControlMode: 	0 => calibration Buttons
	//				1 => Sensor
	public int ControlMode = 1;

	//View Elements
	public RelativeLayout rel;

	public TextView tv1;
	public TextView tv2;
	public TextView tv3;
	public TextView tv4;
	public TextView TV_Control;

	public Button[] StopButton = new Button[2];
	public Button[] CalibrationButtons = new Button[4];
	public Button ConnectButton;
	public Button ToggleControl;
	public Button ResetCalibration;

	private SeekBar ThrottleBar;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ThrottleBar = (SeekBar) findViewById(R.id.seekBar1);
        ThrottleBar.setOnSeekBarChangeListener(this);
        
        MainControlGraph = new ControlGraph(this);
        MainControlGraph.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        
        rel = (RelativeLayout) findViewById(R.id.rl1);
        rel.addView(MainControlGraph);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        tv4 = (TextView) findViewById(R.id.tv4);
		TV_Control = (TextView) findViewById(R.id.tv_control_mode);
		TV_Control.setText("mode_sensor");

		//registering Buttons
		StopButton[0] = (Button) findViewById(R.id.button);
		StopButton[1] = (Button) findViewById(R.id.button2);
		ConnectButton = (Button) findViewById(R.id.connect_button);
		ToggleControl = (Button) findViewById(R.id.switch_toggle_control);
		ResetCalibration = (Button) findViewById(R.id.reset_calibration);

		StopButton[0].setOnClickListener(this);
		StopButton[1].setOnClickListener(this);
		ConnectButton.setOnClickListener(this);
		ToggleControl.setOnClickListener(this);
		ResetCalibration.setOnClickListener(this);

		ResetCalibration.setVisibility(View.INVISIBLE);

		CalibrationButtons[0] = (Button) findViewById(R.id.button_m_north);
		CalibrationButtons[1] = (Button) findViewById(R.id.button_m_east);
		CalibrationButtons[2] = (Button) findViewById(R.id.button_m_south);
		CalibrationButtons[3] = (Button) findViewById(R.id.button_m_west);

		for (int i = 0; i < 4; i++) {
			CalibrationButtons[i].setOnClickListener(this);
			CalibrationButtons[i].setVisibility(View.INVISIBLE);
		}

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

	@Override
	protected void onPause() {
		// stop everything on activity pause
		super.onPause();

		ThrottleBar.setProgress(0);
		for(int i = 0; i < 3; i++){
			orientation_calibrated[i] = 0.0f;
		}
		if(control != null) {
			this.control.stop_qc();
		}
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
        if(ControlMode == 1) {
			SensorManager.getRotationMatrix(rotationMatrix, null, mGravs, mGeoMags);
			SensorManager.getOrientation(rotationMatrix, orientation);
		} else {
			 orientation = orientation_calibrated;
		}

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
				tv4.setText("read: " + QCControl.this.msg[0] + QCControl.this.msg[1] + " ");
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
		Log.e("QCCtrl", "STOP");
		switch (v.getId()){
			case R.id.button :
				ThrottleBar.setProgress(0);
				Log.e("QCCtrl", "button1_STOP");
				if(control != null) {
					this.control.stop_qc();
				}
				break;

			case R.id.button2 :
				ThrottleBar.setProgress(0);
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

			case R.id.button_m_north:
				orientation_calibrated[2] += calibration_inc;
				break;

			case R.id.button_m_east:
				orientation_calibrated[1] -= calibration_inc;
				break;

			case R.id.button_m_south:
				orientation_calibrated[2] -= calibration_inc;
				break;

			case R.id.button_m_west:
				orientation_calibrated[1] += calibration_inc;
				break;

			case R.id.switch_toggle_control:
				toggleControlMode();
				break;

			case R.id.reset_calibration:
				orientation_calibrated[0] = 0.0f;
				orientation_calibrated[1] = 0.0f;
				orientation_calibrated[2] = 0.0f;
				break;

			default:
				break;
		}

	}

	public void toggleControlMode(){
		if(ControlMode == 0){
			//engage sensor mode
			ControlMode = 1;
			TV_Control.setText("mode_sensor");

			//hide calibration buttons
			for(int i = 0; i<4; i++){
				CalibrationButtons[i].setVisibility(View.INVISIBLE);
			}
			ResetCalibration.setVisibility(View.INVISIBLE);

		} else{
			//engage calibration mode
			ControlMode = 0;
			TV_Control.setText("mode_calibration");

			//show calibration buttons
			for(int i = 0; i<4; i++){
				CalibrationButtons[i].setVisibility(View.VISIBLE);
			}
			ResetCalibration.setVisibility(View.VISIBLE);
		}
	}
}
