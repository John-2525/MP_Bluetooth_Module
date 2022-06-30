package Classes;

import java.io.Serializable;
import java.util.Calendar;

public class Reminder_For_Weekly_And_Single implements Serializable {

    private Calendar ReminderCalendar;
    private String ReminderMessage;

    public Reminder_For_Weekly_And_Single() {
    }

    public Reminder_For_Weekly_And_Single(Calendar InputCalendar
        ,String Message) {
        this.ReminderCalendar = InputCalendar;
        this.ReminderMessage = Message;
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
}
