package Classes;

import java.io.Serializable;
import java.util.Calendar;

public class Reminder_For_Weekly_And_Single implements Serializable {

    private Calendar ReminderCalendar;
    private String ReminderMessage;
    private int RequestCode;

    public Reminder_For_Weekly_And_Single() {
    }

    public Reminder_For_Weekly_And_Single(Calendar InputCalendar
        ,String Message, int Code) {
        this.ReminderCalendar = InputCalendar;
        this.ReminderMessage = Message;
        this.RequestCode = Code;
    }

    public Calendar getReminderCalendar() {
        return ReminderCalendar;
    }

    public void setReminderCalendar(Calendar reminderCalendar) {
        ReminderCalendar = reminderCalendar;
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
}
