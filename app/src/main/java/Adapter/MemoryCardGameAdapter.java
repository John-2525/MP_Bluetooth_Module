package Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.mp_bluetooth_module.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MemoryCardGameAdapter extends BaseAdapter {


    private Context context;
    private ArrayList<ImageView> viewCardList = new ArrayList<ImageView>(16);

    public MemoryCardGameAdapter(Context Adaptercontext) {
        this.context = Adaptercontext;
        for(int i = 0; i < 16; ++i){
            ImageView cardImage;
            cardImage = new ImageView(this.context);
            cardImage.setLayoutParams(new ViewGroup.LayoutParams(166,260));
            cardImage.setScaleType(ImageView.ScaleType.FIT_XY);
            viewCardList.add(cardImage);
        }

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

    public ImageView getCardImageViewByPosition(int position){
        return viewCardList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView cardImage = viewCardList.get(position);
        return cardImage;
    }
}
