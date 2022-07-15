package Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_bluetooth_module.R;
import com.google.firebase.database.DataSnapshot;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

public class SelectedGameImageAdapter extends RecyclerView.Adapter<SelectedGameImageAdapter.ImageViewHolder> {


    private ArrayList<Uri> ImageUri;
    private OnItemClickListener SelectedGameImageListener;

    public SelectedGameImageAdapter(ArrayList<Uri> uriArray) {
        this.ImageUri = uriArray;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_game_image_card_layout,parent,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        holder.SetSelectedImage(ImageUri.get(position));
    }

    @Override
    public int getItemCount() {
        return ImageUri.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView ASelectedImage;
        private int Position;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ASelectedImage = itemView.findViewById(R.id.IndividualGridSelectedGameImage);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Position = getAbsoluteAdapterPosition();
                    if(Position != RecyclerView.NO_POSITION && SelectedGameImageListener != null) {
                        SelectedGameImageListener.OnItemClick(Position,itemView);
                    }
                }
            });
        }

        public void SetSelectedImage(Uri selectedImageUri) {
            Picasso.get().load(selectedImageUri).fit().into(ASelectedImage);
        }
    }

    public interface OnItemClickListener {
        void OnItemClick(int Position, View v);
    }

    public void setOnItemClickListener(SelectedGameImageAdapter.OnItemClickListener Listener) {
        this.SelectedGameImageListener = Listener;
    }
}
