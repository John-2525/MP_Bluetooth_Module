package Adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_bluetooth_module.Image_Fragment;
import com.example.mp_bluetooth_module.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;

import Classes.Firebase_Database_Image_Video_Audio_Upload;

public class AlbumImageAdapter extends FirebaseRecyclerAdapter<Firebase_Database_Image_Video_Audio_Upload, AlbumImageAdapter.ImageAlbumViewHolder> {

    private static final String TAG = "CheckPoint";
    private OnItemClickListener ImageListener;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AlbumImageAdapter(@NonNull FirebaseRecyclerOptions<Firebase_Database_Image_Video_Audio_Upload> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull AlbumImageAdapter.ImageAlbumViewHolder holder, int position, @NonNull Firebase_Database_Image_Video_Audio_Upload model) {
        holder.setImage(Uri.parse(model.getFileDownloadUri()));
    }

    @NonNull
    @Override
    public ImageAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_fragment_card_layout, parent, false);
        return new AlbumImageAdapter.ImageAlbumViewHolder(view);
    }

    @Override
    public void onError(DatabaseError e) {
        Log.e(TAG,"Cannot get Image options data from Firebase Realtime Database");
    }

    public class ImageAlbumViewHolder extends RecyclerView.ViewHolder {

        ImageView CardFragmentImageview;
        int position;

        public ImageAlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            CardFragmentImageview = itemView.findViewById(R.id.IndividualGridImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && ImageListener != null) {
                        ImageListener.OnItemClick(getSnapshots().getSnapshot(position),position);

                    }
                }
            });
        }

        public void setImage(Uri ImageUri) {
            Picasso.get().load(ImageUri).fit().into(CardFragmentImageview);
        }
    }

    public interface OnItemClickListener {
        void  OnItemClick(DataSnapshot ImageSnapshot, int ImagePosition);
    }

    public void setOnItemClickListener(OnItemClickListener Listener) {
        this.ImageListener = Listener;
    }
}
