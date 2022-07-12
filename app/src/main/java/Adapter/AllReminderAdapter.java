package Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mp_bluetooth_module.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import Classes.Reminder_For_Weekly_And_Single;

public class AllReminderAdapter extends FirebaseRecyclerAdapter<Reminder_For_Weekly_And_Single, AllReminderAdapter.ReminderViewHolder> {

    private static final String TAG = "CheckPoint";
    private OnItemClickListener ReminderListener;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public AllReminderAdapter(@NonNull FirebaseRecyclerOptions<Reminder_For_Weekly_And_Single> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ReminderViewHolder holder, int position, @NonNull Reminder_For_Weekly_And_Single model) {
        holder.setLoopingTextAndDateTime(model.isLooping(),model.getReminderDate());
        holder.setMessage(model.getReminderMessage());
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_card_layout,parent,false);
        return new AllReminderAdapter.ReminderViewHolder(view);
    }

    @Override
    public void onError(DatabaseError e) {
        Log.e(TAG,"Cannot get Reminder options data from Firebase Realtime Database");
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder {

        TextView MessageTextView, DateTimeTextView, LoopingTextView;
        int Position;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);

            MessageTextView = itemView.findViewById(R.id.SetNotificationMessageTextView);
            DateTimeTextView = itemView.findViewById(R.id.SetNotificationDateTimeTextView);
            LoopingTextView = itemView.findViewById(R.id.SetNotificationLoopingTextView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Position = getAbsoluteAdapterPosition();
                    if(Position != RecyclerView.NO_POSITION && ReminderListener != null) {
                        ReminderListener.OnItemClick(getSnapshots().getSnapshot(Position),Position,v);

                    }
                }
            });
        }

        public void setMessage(String Message) {
            MessageTextView.setText(Message);
        }

        public void setLoopingTextAndDateTime (boolean looping, Date NotificationDateTime) {
            if(looping) {
                String LoopMsg = "Daily Looping Notification at the above time";
                LoopingTextView.setText(LoopMsg);
                SimpleDateFormat LoopingdateFormat = new SimpleDateFormat("HH:mm / hh:mm a");
                DateTimeTextView.setText(LoopingdateFormat.format(NotificationDateTime));
            }
            else {
                String NonloopMsg = "Single use notification for the above date and time";
                LoopingTextView.setText(NonloopMsg);
                SimpleDateFormat NonLoopingdateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm / hh:mm a");
                DateTimeTextView.setText(NonLoopingdateFormat.format(NotificationDateTime));
            }
        }
    }

    /**
     * Function is used to detect the click and send the DataSnapshot and position of the selected
     * reminder back to the Display_All_Reminders.java class
     */
    public interface OnItemClickListener {
        void  OnItemClick(DataSnapshot ReminderSnapshot, int ReminderPosition, View v);
    }

    /** Initializes the OnItemClickListener */
    public void setOnItemClickListener(AllReminderAdapter.OnItemClickListener Listener) {
        this.ReminderListener = Listener;
    }
}
