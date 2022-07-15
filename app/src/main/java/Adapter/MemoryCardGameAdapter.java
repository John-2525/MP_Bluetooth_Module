package Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.mp_bluetooth_module.R;
import com.squareup.picasso.Picasso;

public class MemoryCardGameAdapter extends BaseAdapter {


    private Context context;

    public MemoryCardGameAdapter(Context Adaptercontext) {
        this.context = Adaptercontext;
    }

    @Override
    public int getCount() {
        return 16;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView cardImage;
        if(convertView == null) {
            cardImage = new ImageView(this.context);
            cardImage.setLayoutParams(new ViewGroup.LayoutParams(166,260));
            cardImage.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        else {
            cardImage = (ImageView) convertView;
        }
        Picasso.get().load(R.drawable.hidden_img).fit().into(cardImage);
        return cardImage;
    }
}
