package in.ashutoshchaubey.studybuddy;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements AppsRecyclerViewAdapter.ItemClickListener {

    private static final int REQ_CODE = 0;
    private PackageManager manager;
    private ArrayList<AppItem> apps;
    private AppsRecyclerViewAdapter adapter;
    private SlidingUpPanelLayout mLayout;
    private RelativeLayout mMainParentHomeScreen;
    private FloatingActionButton fabOptions, fabPhoneSettings, fabLauncherSettings;
    private Animation mShowButton, mHideButton, mShowLayout, mHideLayout;
    private LinearLayout mPhoneSettingsParent, mLauncherSettingsParent, mSearchAppsParent, mPanelLabelParentCollapsed,
            mPanelLabelParentExpanded;
    private EditText mSearchEditText;
    private Switch alarmSwitch;
    private Calendar alarmCal;
    private Intent alarmReceiverIntent;
    private PendingIntent pendingIntent;
    Context context;
    private AlarmManager alarmManager;
    AppsDbHelper appsDbHelper;
    SQLiteDatabase db;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    private AlertDialog enableNotificationListenerAlertDialog;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = context;

        /*
          Logger for making the logs readable
         */
        Logger.addLogAdapter(new AndroidLogAdapter());

        /*
          Getting shared preferences
         */
        preferences = getApplicationContext()
                .getSharedPreferences(LauncherSettingsActivity.MY_PREFERENCES, MODE_PRIVATE);
        editor = preferences.edit();


        /*
          Initializing the SQLiteOpenHelper and populating the SQLiteDatabase
         */
        appsDbHelper = new AppsDbHelper(this);
        db = appsDbHelper.getReadableDatabase();

        /*
          Getting alarm system service for ringing alarm
         */
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        /*
          Getting Switch from xml
         */
        alarmSwitch = (Switch) findViewById(R.id.alarm_switch);

        /*
          Getting the switch state from previous launch through shared preferences
          3 states
          Constants.ALARM_ON
          Constants.ALARM_OFF
          Constants.ALARM_RINGING
         */
        String switchState = preferences.getString("switchState", "error");

        /*
          If switchState was Constants.ALARM_ON
         */
        if (switchState.equals(Constants.ALARM_ON)) {
            alarmSwitch.setChecked(true);
        }
        /*
          If switchState was Constants.ALARM_RINGING
         */
        else if (switchState.equals(Constants.ALARM_RINGING)) {
            alarmSwitch.setChecked(true);
            /*
              Creating an AlertDialog to ask for snooze or to stop the alarm
             */
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Snooze Alarm?");
            builder.setMessage("Press Snooze to set alarm for after 10 minutes");
            builder.setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /*
                      If user presses Snooze button then get default time of alarm from SharedPreferences
                      and increment it by 10min and set another alarm
                     */
                    int hr = preferences.getInt("hourOfDay", 0);
                    int min = preferences.getInt("minute", 0);
                    int snoozemin = preferences.getInt("snooze_time", 1);
                    min += snoozemin;
                    if (min > 60) {
                        min -= 60;
                        hr += 1;
                    }
                    alarmCal = Calendar.getInstance();
                    Calendar nowCal = Calendar.getInstance();
                    alarmCal.set(Calendar.HOUR_OF_DAY, hr);
                    alarmCal.set(Calendar.MINUTE, min);
                    if (alarmCal.compareTo(nowCal) <= 0) {
                        alarmCal.set(Calendar.DATE, 1);
                    }

                    /*
                      Creating an intent for the BroadcastReceiver
                     */
                    alarmReceiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);

                    /*
                      extra to tell that the alarm is to be switched on or off
                     */
                    alarmReceiverIntent.putExtra("extra", true);
                    /*
                      Creating PendingIntent because we don't want the intent to be performed just now
                     */
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQ_CODE,
                            alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //Setting the alarmManager
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), pendingIntent);
                    /*
                      Changing the switchState to Constants.ALARM_ON
                     */
                    editor.putString("switchState", Constants.ALARM_ON);
                    editor.apply();
                }
            });
            builder.setNegativeButton("Stop", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    alarmReceiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);
                    alarmReceiverIntent.putExtra("extra", false);
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQ_CODE,
                            alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    //Stopping the alarm while ringing
                    sendBroadcast(alarmReceiverIntent);
                    editor.putString("switchState", Constants.ALARM_OFF);
                    if (pendingIntent != null) {
                        //Cancelling the PendingIntent
                        alarmManager.cancel(pendingIntent);
                    }
                    editor.apply();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        //Making the OnCheckedChangeListener to listen for changes in switch state in UI
        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                /*
                  Getting default time of alarm from SharedPreferences
                  Value can be changed from LauncherSettingsActivity
                 */
                int hr = preferences.getInt("hourOfDay", 0);
                int min = preferences.getInt("minute", 0);
                alarmCal = Calendar.getInstance();
                Calendar nowCal = Calendar.getInstance();
                alarmCal.set(Calendar.HOUR_OF_DAY, hr);
                alarmCal.set(Calendar.MINUTE, min);
                if (alarmCal.compareTo(nowCal) <= 0) {
                    alarmCal.set(Calendar.DATE, 1);
                }

                //Same steps as for Constants.ALARM_RINGING
                alarmReceiverIntent = new Intent(MainActivity.this, AlarmReceiver.class);

                if (b) {

                    alarmReceiverIntent.putExtra("extra", b);
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQ_CODE,
                            alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), pendingIntent);
                    editor.putString("switchState", Constants.ALARM_ON);


                } else {

                    alarmReceiverIntent.putExtra("extra", b);
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this, REQ_CODE,
                            alarmReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    sendBroadcast(alarmReceiverIntent);
                    editor.putString("switchState", Constants.ALARM_OFF);
                    if (pendingIntent != null) {
                        alarmManager.cancel(pendingIntent);
                    }


                }
                editor.apply();
            }
        });

        //Parent of home screen when collapsed
        mMainParentHomeScreen = (RelativeLayout) findViewById(R.id.main_parent_home_screen);

        /*
          Loading the apps to populate the RecyclerView
          See declaration for more
         */
        loadApps("");

        /*
          Checking whether the service has special access to Notifications
          If not the creating AlertDialog for asking permission
          If yes then starting the Service
         */
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }else{
            startService(new Intent(MainActivity.this, AppsNotificationListenerService.class));
        }

        /*
          Adding functionality to the RecyclerView and setting it to GridLayout
         */
        RecyclerView appsRecyclerView = (RecyclerView) findViewById(R.id.apps_list_recycler_view);
        int numberOfColumns = 4;
        appsRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new AppsRecyclerViewAdapter(this, apps);
        adapter.setClickListener(this);
        appsRecyclerView.setAdapter(adapter);

        /*
          2 states of the SlidingPanelLayout
         */
        mPanelLabelParentCollapsed = (LinearLayout) findViewById(R.id.apps_list_label_parent_collapsed);
        mPanelLabelParentExpanded = (LinearLayout) findViewById(R.id.apps_list_label_parent_expanded);

        //EditText to search among installed apps
        mSearchEditText = (EditText) findViewById(R.id.search_apps_edit_text);

        //Hide the text "Search for apps" when clicked on it and show the EditText to enter text
        mSearchAppsParent = (LinearLayout) findViewById(R.id.search_apps_parent);
        mSearchAppsParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchAppsParent.setVisibility(GONE);
                mSearchEditText.setVisibility(View.VISIBLE);
            }
        });

        final Window window = getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        //Setting the initial state of the panel as collapsed always when the activity is started
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        /*
          Adding the PanelSlideListener to make changes to UI
          Changing the Status and Navigation Bar colors accordingly
         */
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
//                Logger.i("onPanelStateChanger1" + slideOffset);
                mMainParentHomeScreen.setAlpha(255 - (Math.round(slideOffset * 255)));
                mPanelLabelParentCollapsed.setAlpha(255 - (Math.round(slideOffset * 255)));
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
//                Logger.i("onPanelStateChanger2" + newState);
                /*
                  If device is running on Android Lollipop+
                  setNavigationBarColor() method not available for previous versions
                 */
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        getWindow().setNavigationBarColor(Color.parseColor("#aa000000"));
                        window.setStatusBarColor(Color.parseColor("#bb000000"));
                        mPanelLabelParentCollapsed.setVisibility(GONE);
                        mPanelLabelParentExpanded.setVisibility(View.VISIBLE);
                        mSearchAppsParent.setVisibility(View.VISIBLE);
                        mSearchEditText.setVisibility(GONE);
                        mLauncherSettingsParent.setVisibility(GONE);
                        mPhoneSettingsParent.setVisibility(GONE);

                    } else {
                        getWindow().setNavigationBarColor(Color.parseColor("#00ffffff"));
                        window.setStatusBarColor(Color.parseColor("#00ffffff"));
                        mPanelLabelParentCollapsed.setVisibility(View.VISIBLE);
                        mPanelLabelParentExpanded.setVisibility(GONE);
                    }
                }
                //mMainParentHomeScreen.setVisibility(View.INVISIBLE);
            }
        });

        //Loading FABs from the UI and the animations to show and hide them
        loadFabs();

        /*
          Adding the functionality to the animations of FAB
         */
        fabOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLauncherSettingsParent.getVisibility() == View.VISIBLE && mPhoneSettingsParent.getVisibility() == View.VISIBLE) {
                    mLauncherSettingsParent.setVisibility(GONE);
                    mPhoneSettingsParent.setVisibility(GONE);
                    mLauncherSettingsParent.startAnimation(mHideLayout);
                    mPhoneSettingsParent.startAnimation(mHideLayout);
                    fabOptions.startAnimation(mHideButton);
                } else {
                    mLauncherSettingsParent.setVisibility(View.VISIBLE);
                    mPhoneSettingsParent.setVisibility(View.VISIBLE);
                    mLauncherSettingsParent.startAnimation(mShowLayout);
                    mPhoneSettingsParent.startAnimation(mShowLayout);
                    fabOptions.startAnimation(mShowButton);
                }
            }
        });

        //Opening Launcher Settings
        fabLauncherSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LauncherSettingsActivity.class));
            }
        });

        //Opening Phone Settings
        fabPhoneSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
            }
        });

        /*
          Adding TextChangeListener to mSearchEditText
          If text changes then it updates the apps ArrayList and repopulates the RecyclerView
         */
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                loadApps(charSequence.toString());
                adapter.setmData(apps);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Always open the launcher with panel state collapsed
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    /**
     * Method to populate the apps ArrayList
     * @author ac-alpha
     * @param pattern a String object to search for apps containing specified string in app name
     */
    private void loadApps(String pattern) {
        //Getting the PackageManager
        manager = getPackageManager();
        //Initializing instance of ArrayList
        apps = new ArrayList<AppItem>();
        /*
         The following code helps retrieve the info about all apps available on the device
        */
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);

        //Checking if the Database which identifies useful apps is ready
        Boolean appsIdentified = preferences.getBoolean("identified", false);
        //If yes, then the ArrayList is populated from the Database
        if(appsIdentified){
            Cursor cursor = db.query(Constants.AppsEntry.TABLE_NAME,null, null,
                    null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                if (cursor.moveToFirst()) {
                    do {

                        AppItem app = new AppItem();
                        app.label = cursor.getString(2);
                        app.name = cursor.getString(1);
                        for (ResolveInfo ri : availableActivities){
                            if(ri.loadLabel(manager).toString().equals(app.name)){
                                app.icon = ri.loadIcon(manager);
                            }
                        }
                        int use = cursor.getInt(3);
                        //Only add AppItem instance to ArrayList if it is useful
                        app.isUseful = use == 1;
                        if(app.isUseful && app.name.toString().contains(pattern)){
                            apps.add(app);
                        }

                    } while (cursor.moveToNext());
                }
                cursor.close();
            }

        }else {

            //Populating the ArrayList with all the available apps
            for (ResolveInfo ri : availableActivities) {

                if (ri.loadLabel(manager).toString().toLowerCase().contains(pattern)) {
                    final AppItem app = new AppItem();
                    app.label = ri.activityInfo.packageName;
                    app.name = ri.loadLabel(manager);
                    app.icon = ri.loadIcon(manager);
                    apps.add(app);
                }

            }
        }

        //Sorting the ArrayList according to lexicographical order
        Collections.sort(apps, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem app1, AppItem app2) {
                if (app1.name.toString().compareToIgnoreCase(app2.name.toString()) > 0) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    //onItemClick() for the RecyclerView adapter
    @Override
    public void onItemClick(View view, int position) {
        Logger.d("Clicked at position : " + position);
        Intent intent = manager.getLaunchIntentForPackage(apps.get(position).label.toString());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Logger.i("Back button pressed from launcher home");
        /*
         If back button is pressed from the collapsed state - Don nothing
         Else make the panel collapsed
         */
        if (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            Toast.makeText(this, "You are at launcher home", Toast.LENGTH_SHORT).show();
        } else {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    /**
     * Method to load all xml related to FABs at once ;)
     */
    private void loadFabs() {
        fabOptions = (FloatingActionButton) findViewById(R.id.fab_settings_menu);
        fabLauncherSettings = (FloatingActionButton) findViewById(R.id.fab_settings_launcher);
        fabPhoneSettings = (FloatingActionButton) findViewById(R.id.fab_settings_phone);

        //Animations
        mShowButton = AnimationUtils.loadAnimation(this, R.anim.show_button);
        mHideButton = AnimationUtils.loadAnimation(this, R.anim.hide_button);
        mShowLayout = AnimationUtils.loadAnimation(this, R.anim.show_layout);
        mHideLayout = AnimationUtils.loadAnimation(this, R.anim.hide_layout);

        mPhoneSettingsParent = (LinearLayout) findViewById(R.id.phone_settings_parent);
        mLauncherSettingsParent = (LinearLayout) findViewById(R.id.launcher_settings_parent);
    }

    /**
     * @author Fábio Alves Martins Pereira (Chagall)
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @author Fábio Alves Martins Pereira (Chagall)
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Notification Listener Service");
        alertDialogBuilder.setMessage("For the the app. to work you need to enable the Notification Listener Service. Enable it now?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }

}
