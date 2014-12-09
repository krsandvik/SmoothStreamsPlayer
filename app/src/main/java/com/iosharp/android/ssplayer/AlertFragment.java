package com.iosharp.android.ssplayer;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.iosharp.android.ssplayer.service.SmoothService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class AlertFragment extends DialogFragment {
    public static final String EXTRA_NAME = "name";
    public static final String EXTRA_CHANNEL = "channel";
    public static final String EXTRA_TIME = "time";

    public static final String TIME_FORMAT = "EEE MMM dd yyyy HH:mm";

    private String mSelectedValue;
    private Spinner mSpinner;
    private String mEventName;
    private int mEventChannel;
    private long mEventTime;

    public AlertFragment() {
    }

    public AlertFragment(String eventName, int eventChannel, long eventTime) {
        mEventName = eventName;
        mEventChannel = eventChannel;
        mEventTime = eventTime;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TypedArray spinnerValues = getResources().obtainTypedArray(R.array.list_times_values);

        AlertDialog.Builder d = new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Set Alert")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer id = (int) System.currentTimeMillis() / 1000;
                        int reminder = Integer.valueOf(mSelectedValue);
                        int reminderMilliseconds = reminder * 60 * 1000;

                        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(getActivity(), SmoothService.AlertReceiver.class);
                        intent.putExtra(EXTRA_NAME, mEventName);
                        intent.putExtra(EXTRA_TIME, mEventTime);
                        intent.putExtra(EXTRA_CHANNEL, mEventChannel);

                        mEventTime = new Date().getTime();

                        PendingIntent alertIntent = PendingIntent.getBroadcast(getActivity(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                        System.out.println("reminder: " + reminder);

                        am.setRepeating(AlarmManager.RTC_WAKEUP, mEventTime, mEventTime + 5000, alertIntent);

                        Toast.makeText(getActivity(), "Alert made!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_alert, null);

        TextView nameView = (TextView) view.findViewById(R.id.reminder_dialog_event_name_field);
        nameView.setText(mEventName);

        TextView channelView = (TextView) view.findViewById(R.id.reminder_dialog_event_channel_field);
        channelView.setText(String.valueOf(mEventChannel));

        TextView timeView = (TextView) view.findViewById(R.id.reminder_dialog_event_time_field);
        timeView.setText(Utils.formatNotificationDate(mEventTime, TIME_FORMAT));

        mSpinner = (Spinner) view.findViewById(R.id.reminder_dialog_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedValue = spinnerValues.getString(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        d.setView(view);
        return d.create();
    }
}