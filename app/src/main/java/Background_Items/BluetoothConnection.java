package Background_Items;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnection extends Thread {

    private static final String TAG = "Debug";
    private BluetoothDevice Device;
    private BluetoothSocket DeviceSocket;


    public BluetoothConnection(BluetoothDevice BluetoothDevice, UUID DeviceUUID, BluetoothSocket Socket) {
        Device = BluetoothDevice;
        DeviceSocket = Socket;

        if(Socket != null) {
            try {
                DeviceSocket.close();
                Log.d(TAG,"Closed existing bluetooth device connection");
            }

            catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG,"Failed to clos existing bluetooth device connection");
            }
        }

        try {
            DeviceSocket = Device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);
            Log.d(TAG,"Bluetooth socket connection established");

        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"Bluetooth socket connection failed to establish");
        }

    }

    public void run() {
        try {
            DeviceSocket.connect();
            Log.d(TAG, "Bluetooth is connected to device");

        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Bluetooth failed to connect to device");

        }

    }

}
