package Classes;

/** Custom class to be used for the listview in Diffuser_Listview.java */

public class PairedBluetoothDevice {

    private String BluetoothDeviceName;
    private String BluetoothDeviceAddress;

    public String getDeviceName() {
        return BluetoothDeviceName;
    }

    public String getDeviceAddress() {
        return BluetoothDeviceAddress;
    }

    public void setDeviceName (String DeviceName) {
        this.BluetoothDeviceName = DeviceName;
    }

    public void setDeviceAddress (String DeviceAddress) {
        this.BluetoothDeviceAddress = DeviceAddress;
    }

    public void addDevice(String DeviceName, String DeviceAddress) {
        this.BluetoothDeviceName = DeviceName;
        this.BluetoothDeviceAddress = DeviceAddress;
    }


}
