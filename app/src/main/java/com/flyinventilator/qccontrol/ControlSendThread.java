package com.flyinventilator.qccontrol;

import android.util.Log;

public class ControlSendThread extends Thread {
	public QCControl qcc;
	public ConnectedThread conTr;
	public int sensorAffinity = 7;

	private boolean running = true;
	private boolean stop = false;

	public ControlSendThread(QCControl qcc, ConnectedThread conTr){
		this.qcc = qcc;
		this.conTr = conTr;
	}
	
	public void run(){
		while(running){
			if(stop){
				int[] instruction_stop = {125,125,125,125};
				sendInstructions(instruction_stop);
			} else {
				sendInstructions(calculateInstructions());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public int[] calculateInstructions(){
		/*instruction[0] = north motor
		instruction[1] = east motor
		instruction[2] = south motor
		instruction[3] = west motor
		*/
		
		int constant = (int) (qcc.getThrottle() * 1.25 + 125);
		int[] instruction = new int[4];
		
		instruction[1] = constant - (int) (qcc.getSensorData()[1] * sensorAffinity );
		instruction[0] = constant - (int) (qcc.getSensorData()[2] * sensorAffinity);
		instruction[3] = constant + (int) (qcc.getSensorData()[1] * sensorAffinity);
		instruction[2] = constant + (int) (qcc.getSensorData()[2] * sensorAffinity);
		
		return instruction;
	}
	
	public void sendInstructions(int[] instruction){
		Log.i("QCCtrl", "      " + instruction[0] + "");
		Log.i("QCCtrl", instruction[3] + "       " + instruction[1]);
		Log.i("QCCtrl", "      " + instruction[2] + "");

		byte[] data = new byte[9];
		
		data[0] = (byte) 'B';
		data[1] = (byte) 'G';
		data[2] = (byte) 'N';
		data[3] = (byte) instruction[0];
		data[4] = (byte) instruction[1];
		data[5] = (byte) instruction[2];
		data[6] = (byte) instruction[3];

		conTr.write(data);
		
	}

	public void stop_qc(){
		int[] throttle_zero = {125, 125, 125, 125};
		sendInstructions(throttle_zero);
		stop = true;
		return;
	}
}
