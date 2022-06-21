package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.mp_bluetooth_module.R;

import java.util.ArrayList;
import java.util.List;

import Classes.PairedBluetoothDevice;

public class BlueToothListViewAdapter extends ArrayAdapter<PairedBluetoothDevice> {

    private static final String TAG = "BlueToothListViewAdapter";

    private Context Bcontext;
    int BResource;

    public BlueToothListViewAdapter(@NonNull Context context, int resource, ArrayList<PairedBluetoothDevice> objects) {
        super(context, resource, objects);
        Bcontext = context;
        BResource = resource;
    }

    @NonNull
    @Override
    /** Responsible for getting the view and attaching it to listview */
    public View getView(int position, View convertView, ViewGroup parent) {
        /** Gets the data to be inserted into the listview */
        String Name = getItem(position).getDeviceName();
        String Address = getItem(position).getDeviceAddress();

        /** Creates an instance of the custom class the adapter is based on */
        PairedBluetoothDevice bluetoothDevice = new PairedBluetoothDevice();
        bluetoothDevice.setDeviceAddress(Address);
        bluetoothDevice.setDeviceName(Name);

        LayoutInflater inflater = LayoutInflater.from(Bcontext);
        convertView = inflater.inflate(BResource, parent, false);

        /** Finds the view from the custom text view activity to be used in the listview */
        TextView DeviceName = convertView.findViewById(R.id.BTDeviceName);
        TextView DeviceAddress = convertView.findViewById(R.id.BTDeviceAddress);

        /** Sets the data in the custom text view */
        DeviceName.setText(Name);
        DeviceAddress.setText(Address);

        return convertView;
    }
}
