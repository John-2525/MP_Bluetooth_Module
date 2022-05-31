package Background_Items;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class BluetoothConnection extends Thread {

    private static final String TAG = "Debug";
    private final BluetoothDevice Device;
    private final BluetoothSocket DeviceSocket;


    public BluetoothConnection(BluetoothDevice BluetoothDevice, UUID DeviceUUID) {
        Device = BluetoothDevice;
        BluetoothSocket Socket = null;

        try {
            Socket = Device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);
            Log.d(TAG,"Bluetooth socket connection established");

        }
        catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"Bluetooth socket connection failed to establish");
        }
        DeviceSocket = Socket;

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

    public void Cancel() {
        if (DeviceSocket != null && DeviceSocket.isConnected()) {
            try {
                DeviceSocket.close();
                Log.d(TAG, "Bluetooth disconnected successfully");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Bluetooth failed to disconnect successfully");
            }
        }
    }

}
