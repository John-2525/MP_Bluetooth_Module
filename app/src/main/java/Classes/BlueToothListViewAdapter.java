package Classes;

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
    // Responsible for getting the view and attaching it to listview
    public View getView(int position, View convertView, ViewGroup parent) {
        String Name = getItem(position).getDeviceName();
        String Address = getItem(position).getDeviceAddress();

        PairedBluetoothDevice bluetoothDevice = new PairedBluetoothDevice();
        bluetoothDevice.setDeviceAddress(Address);
        bluetoothDevice.setDeviceName(Name);

        LayoutInflater inflater = LayoutInflater.from(Bcontext);
        convertView = inflater.inflate(BResource, parent, false);

        TextView DeviceName = convertView.findViewById(R.id.BTDeviceName);
        TextView DeviceAddress = convertView.findViewById(R.id.BTDeviceAddress);

        DeviceName.setText(Name);
        DeviceAddress.setText(Address);

        return convertView;
    }
}
