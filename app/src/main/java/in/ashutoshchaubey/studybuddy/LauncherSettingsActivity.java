package in.ashutoshchaubey.studybuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.Calendar;

public class LauncherSettingsActivity extends AppCompatActivity {

    LinearLayout defaultAlarmParent;
    TimePickerDialog timePickerDialog;
    public static String MY_PREFERENCES = "My Preferences";
    SharedPreferences pref;
    SharedPreferences.Editor prefEditor;
    TextView alarmTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_settings);

        Logger.addLogAdapter(new AndroidLogAdapter());

        pref = getApplicationContext().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);

        alarmTime = (TextView) findViewById(R.id.default_alarm_time);
        String timeText = "";
        int hour = pref.getInt("hourOfDay",0);
        Logger.i(hour+"");
        int min = pref.getInt("minute",0);
        alarmTime.setText(Utilities.getTimeText(hour,min));

        defaultAlarmParent = (LinearLayout)findViewById(R.id.default_alarm_settings);
        defaultAlarmParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTimePickerDialog();
            }
        });

    }

    private void openTimePickerDialog() {

        Calendar calendar = Calendar.getInstance();

        timePickerDialog = new TimePickerDialog(LauncherSettingsActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        alarmTime.setText(Utilities.getTimeText(hourOfDay,minute));
                        prefEditor = pref.edit();
                        prefEditor.putInt("hourOfDay",hourOfDay);
                        Logger.i(hourOfDay+"");
                        prefEditor.putInt("minute",minute);
                        prefEditor.apply();
                        Toast.makeText(LauncherSettingsActivity.this, "Settings changed successfully", Toast.LENGTH_SHORT).show();
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), false);
        timePickerDialog.setTitle("Set Alarm Time");

        timePickerDialog.show();

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(LauncherSettingsActivity.this, MainActivity.class));
    }

}
