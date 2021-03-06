package Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mp_bluetooth_module.Image_Fragment;
import com.example.mp_bluetooth_module.Video_Fragment;


/**
 * This extension allows the adapter to populate pages inside a viewpager
 * but uses fragment for each page
 */
public class ViewPage2Adapter extends FragmentStateAdapter {

    public ViewPage2Adapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Loads the different fragments based on the int position of the pages,
     * Ex : Page 0 will load Image_Fragment(), Page 1 will load Video_Fragment()
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position) {
            case 0:
                return new Image_Fragment();
            case 1:
                return new Video_Fragment();
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
