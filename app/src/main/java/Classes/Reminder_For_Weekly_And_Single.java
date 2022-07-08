package Classes;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Reminder_For_Weekly_And_Single implements Serializable {

    private Date ReminderDate;
    private String ReminderMessage, ReminderAudioDownloadURL;
    private int RequestCode;
    private boolean Looping;

    public Reminder_For_Weekly_And_Single() {
    }

    public Reminder_For_Weekly_And_Single(Date InputDate, String Message, int Code, String DownloadUrl,
                                          boolean LoopStatus) {
        this.ReminderDate = InputDate;
        this.ReminderMessage = Message;
        this.RequestCode = Code;
        this.ReminderAudioDownloadURL = DownloadUrl;
        this.Looping = LoopStatus;
    }

    public Date getReminderDate() {
        return ReminderDate;
    }

    public void setReminderDate(Date reminderDate) {
        ReminderDate = reminderDate;
    }

    public String getReminderMessage() {
        return ReminderMessage;
    }

    public void setReminderMessage(String reminderMessage) {
        ReminderMessage = reminderMessage;
    }

    public int getRequestCode() {
        return RequestCode;
    }

    public void setRequestCode(int requestCode) {
        RequestCode = requestCode;
    }

    public String getReminderAudioDownloadURL() {
        return ReminderAudioDownloadURL;
    }

    public void setReminderAudioDownloadURL(String reminderAudioDownloadURL) {
        ReminderAudioDownloadURL = reminderAudioDownloadURL;
    }

    public boolean isLooping() {
        return Looping;
    }

    public void setLooping(boolean looping) {
        Looping = looping;
    }
}
