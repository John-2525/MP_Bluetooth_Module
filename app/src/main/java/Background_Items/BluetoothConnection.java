package Background_Items;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.example.mp_bluetooth_module.Diffuser_Listview;

import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BluetoothConnection extends Thread {

    private static final String TAG = "CheckPoint";
    private final BluetoothDevice Device;
    private final BluetoothSocket DeviceSocket;
    private final OutputStream DataOutput;
    String command;
    byte[] toSend;


    /** Template of code to connect to bluetooth, disconnect from it and send signals to it */

    public BluetoothConnection(BluetoothDevice BluetoothDevice, UUID DeviceUUID) {
        Device = BluetoothDevice;
        BluetoothSocket Socket = null;
        OutputStream Data = null;

        try {
            // Creates a new socket connection with the selected bluetooth device's UUID
            Socket = Device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);
            Log.d(TAG, "Bluetooth socket connection established");
            Data = Socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Bluetooth socket connection failed to establish");
        }
        DeviceSocket = Socket;
        DataOutput = Data;
    }


    public void Connect() {

        try {
            // Connects to selected bluetooth device from listview
            DeviceSocket.connect();
            Log.d(TAG, "Bluetooth is connected to device");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Bluetooth failed to connect to device");

        }


    }

    // Function to close socket connection to disconnect from bluetooth device
    public void Disconnect() {
        // Checks that there is an actual connection and socket to prevent any errors from
        // occurring when disconnecting
        if (DeviceSocket != null && DeviceSocket.isConnected()) {
            try {
                DeviceSocket.close();
                Log.d(TAG, "Bluetooth disconnected successfully");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Bluetooth failed to disconnect successfully");
            }
        }
    }

    public void SendString (String Number) {
        command = Number;
        toSend = command.getBytes(StandardCharsets.UTF_8);
        Log.d(TAG,"Bytes are "+toSend);

        try {
            if(DataOutput != null && toSend != null) {
                Log.d(TAG, "OutputStream is " + DataOutput);
                DataOutput.write(toSend);
                Log.d(TAG, "Message sent is " + command);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }

}
