package Background_Items;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;

public class BluetoothDisconnect extends Thread{

    private static final String TAG = "Debug";
    private BluetoothSocket DisconnectSocket;

    public BluetoothDisconnect(BluetoothSocket Socket) {

        DisconnectSocket = Socket;

        if (DisconnectSocket != null && DisconnectSocket.isConnected()) {
            try {
                DisconnectSocket.close();
                Log.d(TAG, "Bluetooth disconnected successfully");
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Bluetooth failed to disconnect successfully");
            }
        }
    }

}
