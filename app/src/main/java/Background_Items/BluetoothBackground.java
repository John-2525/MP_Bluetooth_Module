package Background_Items;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


// Referencing this https://stackoverflow.com/questions/33461075/implement-bluetooth-connection-into-service-or-application-class-without-losing
// My guess is that it is putting the thread into the service to prevent thread from being wiped out when activity is destroyed

public class BluetoothBackground extends Service {


    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread Connection = new Thread(new BluetoothConnection(startId));
        Connection.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final class BluetoothConnection implements Runnable {

        private static final String TAG = "CheckPoint";
        BluetoothDevice Device;
        BluetoothSocket DeviceSocket;
        OutputStream DataOutput;

        int ServiceID;

        BluetoothConnection(int ID) {
            this.ServiceID = ID;
        }

        public void Connect(BluetoothDevice BluetoothDevice, UUID DeviceUUID) {
            Device = BluetoothDevice;
            BluetoothSocket Socket = null;
            OutputStream Data = null;

            try {
                // Creates a new socket connection with the selected bluetooth device's UUID
                Socket = Device.createInsecureRfcommSocketToServiceRecord(DeviceUUID);
                Log.d(TAG, "Bluetooth socket connection established");
                Data = Socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "Bluetooth socket connection failed to establish");
            }
            DeviceSocket = Socket;
            DataOutput = Data;
        }


        @Override
        public void run() {
            try {
                // Connects to selected bluetooth device from listview
                DeviceSocket.connect();
                Log.d(TAG, "Bluetooth is connected to device");

            } catch (IOException e) {
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
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Bluetooth failed to disconnect successfully");
                }
                stopSelf(this.ServiceID);
            }
        }

    }

}
