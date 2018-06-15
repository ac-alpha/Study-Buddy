package in.ashutoshchaubey.studybuddy;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.ashutoshchaubey.studybuddy.Constants.AppsEntry;

public class LauncherSettingsActivity extends AppCompatActivity {

    LinearLayout defaultAlarmParent, identifyUsefulParent;
    TimePickerDialog timePickerDialog;
    public static String MY_PREFERENCES = "My Preferences";
    SharedPreferences pref;
    SharedPreferences.Editor prefEditor;
    TextView alarmTime;
    ProgressDialog progressDialog;
    private PackageManager manager;
    private ArrayList<AppItem> apps;
    private AppsDbHelper appsDbHelper;
    ContentValues cv;
    SQLiteDatabase sqLiteDatabase;
    private String[] badCategories = {"COMMUNICATION", "GAME", "BEAUTY","COMICS",
            "DATING", "ENTERTAINMENT", "LIFESTYLE", "MUSIC_AND_AUDIO", "SHOPPING", "SOCIAL", "SPORTS", "VIDEO_PLAYERS"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_settings);

        Logger.addLogAdapter(new AndroidLogAdapter());

        progressDialog = new ProgressDialog(LauncherSettingsActivity.this);
        progressDialog.setMessage("Identifying... Please wait!");

        appsDbHelper = new AppsDbHelper(this);
        sqLiteDatabase = appsDbHelper.getWritableDatabase();

        pref = getApplicationContext().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE);
        prefEditor = pref.edit();

        if(pref.getBoolean("identified",false)){
            findViewById(R.id.useful_apps_found).setVisibility(View.VISIBLE);
        }

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

        cv=new ContentValues();

        identifyUsefulParent = (LinearLayout) findViewById(R.id.identify_useful_apps);
        identifyUsefulParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(pref.getBoolean("identified",false)){
                    Toast.makeText(LauncherSettingsActivity.this, "Already identified useful apps", Toast.LENGTH_SHORT).show();
                }else {

                    progressDialog.show();

                    manager = getPackageManager();
                    apps = new ArrayList<AppItem>();

                    Intent i = new Intent(Intent.ACTION_MAIN, null);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);

                    List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
                    for (ResolveInfo ri : availableActivities) {
                        final AppItem app = new AppItem();
                        app.label = ri.activityInfo.packageName;
                        app.name = ri.loadLabel(manager);
                        app.icon = ri.loadIcon(manager);
                        apps.add(app);
                    }

                    taskIdentifyUseful(0);
                }

            }
        });

    }

    private void taskIdentifyUseful(final int no) {

        if(no<apps.size()) {
            final AppItem app = apps.get(no);
            Ion.with(getApplicationContext()).load("https://play.google.com/store/apps/details?id=" + app.label)
                    .asString()
                    .setCallback(new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            int index = result.indexOf("\"genre\" href=\"https://play.google.com/store/apps/category/");
                            int length = "\"genre\" href=\"https://play.google.com/store/apps/category/".length();
                            String sub = result.substring(index + length, index + length + 30);
                            int isuse = 1;
                            for(String cat:badCategories){
                                if(sub.contains(cat)){
                                    isuse = 0;
                                }
                            }
                            cv.put(AppsEntry.COLUMN_APP_NAME, app.name.toString());
                            cv.put(AppsEntry.COLUMN_APP_PACKAGE, app.label.toString());
                            cv.put(AppsEntry.COLUMN_APP_USEFUL, isuse);
                            sqLiteDatabase.insert(AppsEntry.TABLE_NAME,null,cv);
                            cv.clear();
                            if(no==(apps.size()-1)){
                                progressDialog.dismiss();
                                findViewById(R.id.useful_apps_found).setVisibility(View.VISIBLE);
                                prefEditor.putBoolean("identified",true);
                                prefEditor.apply();
                            }
                            taskIdentifyUseful(no+1);
                        }
                    });
        }

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
