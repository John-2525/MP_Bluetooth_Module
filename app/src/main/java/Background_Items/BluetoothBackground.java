package Background_Items;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;


/**
 * Service being used here is a bound service
 * Referenced this documentation : https://developer.android.com/guide/components/bound-services#Additional_Notes
 * Try to read more about it and update the comments for easier understanding
 */

public class BluetoothBackground extends Service {

    private static final String TAG = "CheckPoint";
    private BluetoothDevice Device;
    private BluetoothSocket DeviceSocket;
    private OutputStream DataOutput;
    private String command;
    private byte[] toSend;
    private CountDownTimer timer;

    /** Binder (instance of ?) given to client side */

    private final IBinder binder = new LocalBinder();


    /**
     * (Rewrite this to make it easier to understand)
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */

    public class LocalBinder extends Binder {
        public BluetoothBackground getService() {
    /** Return this instance of LocalService so clients can call public methods */
            return BluetoothBackground.this;
        }
    }


    @Override
    /** Returns this instance of binder */
    public IBinder onBind(Intent intent) {
        return binder;
    }


    /**
     * For Connect(), Disconnect(), SendString(), CheckBluetoothStatus()
     * When the class BluetoothConnect is not run yet, the parameters such as BluetoothSocket
     * and OutputStream are all null
     * So to check the parameters, use != null or == null to do so, if not it will return a null
     * pointer error of some sort, crashing the app
     */

    public void BluetoothConnect(BluetoothDevice BluetoothDevice, UUID DeviceUUID) {
        Device = BluetoothDevice;
        BluetoothSocket Socket = null;
        OutputStream Data = null;

        try {
            /** Creates a new socket connection with the selected bluetooth device's UUID */
            Socket = Device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);
            Log.d(TAG, "Bluetooth socket connection established");
            /** Creates a new OutputStream connection using the newly created socket from above */
            Data = Socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Bluetooth socket connection failed to establish");
        }
        /** Sets the newly created Socket and OutputStream to a final variable */
        DeviceSocket = Socket;
        DataOutput = Data;
    }



    /** Function : Connect to bluetooth using socket connection */
    public void Connect() {

        try {
            /** Connects to selected bluetooth device selected from listview using socket created in BluetoothConnect */
            DeviceSocket.connect();
            Log.d(TAG, "Bluetooth is connected to device");

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Bluetooth failed to connect to device");

        }


    }

    /** Function : Close socket connection to disconnect from bluetooth device */
    public void Disconnect() {
        /**
         * Checks that there is an actual connection and socket to prevent any errors from
         * occurring when disconnecting
         */
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

    /** Function : Send a string to the HC-05 bluetooth module from android appilcation */
    public void SendString (String Number) {
        command = Number;
        /** Converts the string to byte array so that it can be transmitted */
        toSend = command.getBytes(StandardCharsets.UTF_8);
        Log.d(TAG,"Bytes are "+toSend);

        try {
            /** Checks if OutputStream is not null and byte array is not empty to prevent null errors from occuring */
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

    /** Checks if the the bluetooth device is connected via socket connection and returns a boolean value */
    public boolean CheckBluetoothStatus() {

        if(DeviceSocket!=null) {
            return true;
        }
        else {
            return false;
        }
    }
}
