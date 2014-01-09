package com.mreuter.qccontrol;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    public ConnectedThread conTr;
    public QCControl qcc;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public ConnectThread(BluetoothDevice device, QCControl Qcc) {
        BluetoothSocket tmp = null;
        mmDevice = device;
        this.qcc = Qcc;
 
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // MY_UUID is the app's UUID string, also used by the server code
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) { }
        mmSocket = tmp;
    }
 
    public void run() {
        // Cancel discovery because it will slow down the connection
    	
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
 
        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            mmSocket.connect();
            conTr = new ConnectedThread(mmSocket);
            qcc.setConnectedThread(conTr);
            conTr.start();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

    }
    
    public ConnectedThread getConnectedThread(){
    	return conTr;
    }
    /** Will cancel an in-progress connection, and close the open socket */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
}