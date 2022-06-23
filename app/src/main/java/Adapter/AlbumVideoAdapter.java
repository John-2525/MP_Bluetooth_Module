package Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
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

import java.io.IOException;

import Classes.Firebase_Database_Image_Video_Audio_Upload;

public class AlbumVideoAdapter extends FirebaseRecyclerAdapter<Firebase_Database_Image_Video_Audio_Upload, AlbumVideoAdapter.VideoImageAlbumHolder> {

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

    @Override
    protected void onBindViewHolder(@NonNull VideoImageAlbumHolder holder, int position, @NonNull Firebase_Database_Image_Video_Audio_Upload model) {
        holder.setVideo(model.getFileName());
    }

    @NonNull
    @Override
    public VideoImageAlbumHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_fragment_card_layout, parent, false);
        return new AlbumVideoAdapter.VideoImageAlbumHolder(view);
    }

    @Override
    public void onError(DatabaseError e) {
        Log.e(TAG,"Cannot get Video options data from Firebase Realtime Database");
    }

    public class VideoImageAlbumHolder extends RecyclerView.ViewHolder {

        TextView CardFragmentVideoThumbnailFileName;
        ImageView CardFragmentVideoThumbnailView;
        int position;

        public VideoImageAlbumHolder(@NonNull View itemView) {
            super(itemView);

            CardFragmentVideoThumbnailFileName = itemView.findViewById(R.id.VideoFileNameTextView);
            CardFragmentVideoThumbnailView = itemView.findViewById(R.id.IndividualGridVideoThumbnail);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    position = getAbsoluteAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && VideoListener!= null) {
                        VideoListener.OnItemClick(getSnapshots().getSnapshot(position),position );
                    }
                }
            });
        }

        public void setVideo(String FileName) {
            CardFragmentVideoThumbnailFileName.setText(FileName.replace(".mp4",""));
            Picasso.get().load(PlayImageSource).fit().into(CardFragmentVideoThumbnailView);
        }
    }

    public interface OnItemClickListener {
        void  OnItemClick(DataSnapshot VideoSnapshot, int VideoPosition);
    }

    public void setOnItemClickListener(AlbumVideoAdapter.OnItemClickListener Listener) {
        this.VideoListener = Listener;
    }
}
