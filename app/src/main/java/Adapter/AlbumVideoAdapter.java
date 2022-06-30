package Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_bluetooth_module.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.squareup.picasso.Picasso;

import Classes.Firebase_Database_Image_Video_Audio_Upload;

/**
 * FirebaseRecyclerAdapter is a class provided by Firebase.UI which allows for thr binding,
 * adapting and showing of database contents in recycler view
 */
public class AlbumVideoAdapter extends FirebaseRecyclerAdapter<Firebase_Database_Image_Video_Audio_Upload, AlbumVideoAdapter.VideoImageAlbumHolder> {

    /** Initializing variables */
    private static final String TAG = "CheckPoint";
    private OnItemClickListener VideoListener;
    private String PlayImageSource = "https://cdn2.iconfinder.com/data/icons/media-player-ui/512/Media-Icon-13-512.png";

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AlbumVideoAdapter(@NonNull FirebaseRecyclerOptions<Firebase_Database_Image_Video_Audio_Upload> options) {
        super(options);
    }

    /**
     * Function is to bind the view in the card layout with data from the model class
     * which is the Firebase_Database_Image_Video_Audio_Upload class
     */
    @Override
    protected void onBindViewHolder(@NonNull VideoImageAlbumHolder holder, int position, @NonNull Firebase_Database_Image_Video_Audio_Upload model) {
        /** Gets the file name and sends it to the setVideo() in the subclass below */
        holder.setVideo(model.getFileName());
    }

    /**
     * Function is to tell the Video_Fragment class about the card view in video_fragment_card_layout.xml
     * where the data from firebase will be shown
     */
    @NonNull
    @Override
    public VideoImageAlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_fragment_card_layout, parent, false);
        return new AlbumVideoAdapter.VideoImageAlbumHolder(view);
    }

    /** A Function that logs in the event onCreateViewHolder or this class encounters an error */
    @Override
    public void onError(DatabaseError e) {
        Log.e(TAG,"Cannot get Video options data from Firebase Realtime Database");
    }

    /**
     * This subclass is to create the references to the views in the card view,
     * video_fragment_card_layout.xml
     * So that it can be referenced later in onBindViewHolder()
     */
    public class VideoImageAlbumHolder extends RecyclerView.ViewHolder {

        /** Initialize variables */
        TextView CardFragmentVideoThumbnailFileName;
        ImageView CardFragmentVideoThumbnailView;
        int position;

        public VideoImageAlbumHolder(@NonNull View itemView) {
            super(itemView);

            /** Finds the view by the ID given and sets it to the variable */
            CardFragmentVideoThumbnailFileName = itemView.findViewById(R.id.VideoFileNameTextView);
            /** Finds and references the image view in the card view */
            CardFragmentVideoThumbnailView = itemView.findViewById(R.id.IndividualGridVideoThumbnail);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAbsoluteAdapterPosition();
                    /** Checks if the position selected exists and if OnItemClickListener has been initialized/exists */
                    if(position != RecyclerView.NO_POSITION && VideoListener!= null) {
                        VideoListener.OnItemClick(getSnapshots().getSnapshot(position),position );
                    }
                }
            });
        }

        /** Sets the file name into the text view and sets a static image from online into the image view */
        public void setVideo(String FileName) {
            /** replace() is needed to remove the .mp4 from the file name to make the textview look cleaner */
            CardFragmentVideoThumbnailFileName.setText(FileName.replace(".mp4",""));
            /** fit() is necessary or else it will try to load the full resolution of the image and lag the recycler view */
            Picasso.get().load(PlayImageSource).fit().into(CardFragmentVideoThumbnailView);
        }
    }

    /**
     * Function is used to detect the click and send the DataSnapshot and position of the selected
     * image back to the Video_Fragment.java class
     */
    public interface OnItemClickListener {
        void  OnItemClick(DataSnapshot VideoSnapshot, int VideoPosition);
    }

    /** Initializes the OnItemClickListener */
    public void setOnItemClickListener(AlbumVideoAdapter.OnItemClickListener Listener) {
        this.VideoListener = Listener;
    }
}
