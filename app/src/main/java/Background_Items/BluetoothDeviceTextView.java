package Background_Items;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.mp_bluetooth_module.R;

public class BluetoothDeviceTextView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_device_text_view);

        // Purpose is to create a LinearLayout with textview template rows
        // The LinearLayout is used to store the textview boxes within them
        // The textview boxes serve as a template for how subsequent rows of data are to be displayed
        // The LinearLayout will then be inserted into the LayoutView in diffuser_listview.java and its layout file
    }
}