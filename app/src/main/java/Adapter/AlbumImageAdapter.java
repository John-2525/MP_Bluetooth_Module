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

/**
 * FirebaseRecyclerAdapter is a class provided by Firebase.UI which allows for thr binding,
 * adapting and showing of database contents in recycler view
 */
public class AlbumImageAdapter extends FirebaseRecyclerAdapter<Firebase_Database_Image_Video_Audio_Upload, AlbumImageAdapter.ImageAlbumViewHolder> {

    /** Initializing variables */
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

    /**
     * Function is to bind the view in the card layout with data from the model class
     * which is the Firebase_Database_Image_Video_Audio_Upload class
     */
    @Override
    protected void onBindViewHolder(@NonNull AlbumImageAdapter.ImageAlbumViewHolder holder, int position, @NonNull Firebase_Database_Image_Video_Audio_Upload model) {
        /** Gets the uri, converted from image download url, and sends it to setImage() in the subclass below */
        holder.setImage(Uri.parse(model.getFileDownloadUri()));
    }

    /**
     * Function is to tell the Image_Fragment class about the card view in image_fragment_card_layout.xml
     * where the data from firebase will be shown
     */
    @NonNull
    @Override
    public ImageAlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_fragment_card_layout, parent, false);
        return new AlbumImageAdapter.ImageAlbumViewHolder(view);
    }

    /** A Function that logs in the event onCreateViewHolder or this class encounters an error */
    @Override
    public void onError(DatabaseError e) {
        Log.e(TAG,"Cannot get Image options data from Firebase Realtime Database");
    }

    /**
     * This subclass is to create the references to the views in the card view,
     * image_fragment_card_layout.xml
     * So that it can be referenced later in onBindViewHolder()
     */
    public class ImageAlbumViewHolder extends RecyclerView.ViewHolder {

        /** Initialize variables */
        ImageView CardFragmentImageview;
        int position;

        public ImageAlbumViewHolder(@NonNull View itemView) {
            super(itemView);

            /** Finds the view by the ID given and sets it to the variable */
            CardFragmentImageview = itemView.findViewById(R.id.IndividualGridImage);
            /** Sets a listener to get the data snapshot of the selected position in the recycler view */
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAbsoluteAdapterPosition();
                    /** Checks if the position selected exists and if OnItemClickListener has been initialized/exists */
                    if(position != RecyclerView.NO_POSITION && ImageListener != null) {
                        ImageListener.OnItemClick(getSnapshots().getSnapshot(position),position);

                    }
                }
            });
        }

        /** Loads the image into image view in the card view */
        public void setImage(Uri ImageUri) {
            /** fit() is necessary or else it will try to load the full resolution of the image and lag the recycler view */
            Picasso.get().load(ImageUri).fit().into(CardFragmentImageview);
        }
    }

    /**
     * Function is used to detect the click and send the DataSnapshot and position of the selected
     * image back to the Image_Fragment.java class
     */
    public interface OnItemClickListener {
        void  OnItemClick(DataSnapshot ImageSnapshot, int ImagePosition);
    }

    /** Initializes the OnItemClickListener */
    public void setOnItemClickListener(OnItemClickListener Listener) {
        this.ImageListener = Listener;
    }
}
