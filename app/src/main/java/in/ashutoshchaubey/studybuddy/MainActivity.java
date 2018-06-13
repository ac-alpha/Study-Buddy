package in.ashutoshchaubey.studybuddy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
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

import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context=context;

        final SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences(LauncherSettingsActivity.MY_PREFERENCES,MODE_PRIVATE);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmSwitch = (Switch)findViewById(R.id.alarm_switch);

        String switchState = preferences.getString("switchState","error");

        if(switchState.equals("on")){
            alarmSwitch.setChecked(true);
        }

        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                int hr = preferences.getInt("hourOfDay",0);
                int min = preferences.getInt("minute",0);
                alarmCal = Calendar.getInstance();
                Calendar nowCal = Calendar.getInstance();
                alarmCal.set(Calendar.HOUR_OF_DAY,hr);
                alarmCal.set(Calendar.MINUTE,min);
                if(alarmCal.compareTo(nowCal)<=0){
                    alarmCal.set(Calendar.DATE,1);
                }

                alarmReceiverIntent = new Intent(MainActivity.this,AlarmReceiver.class);
                SharedPreferences.Editor editor = preferences.edit();

                if(b){

                    alarmReceiverIntent.putExtra("extra",b);
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this,REQ_CODE,
                            alarmReceiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmCal.getTimeInMillis(), pendingIntent);
                    editor.putString("switchState","on");


                }else{

                    alarmReceiverIntent.putExtra("extra",b);
                    pendingIntent = PendingIntent.getBroadcast(MainActivity.this,REQ_CODE,
                            alarmReceiverIntent,PendingIntent.FLAG_UPDATE_CURRENT);
                    sendBroadcast(alarmReceiverIntent);
                    editor.putString("switchState","off");
                    if(pendingIntent!=null){
                        alarmManager.cancel(pendingIntent);
                    }


                }
                editor.apply();
            }
        });

        mMainParentHomeScreen = (RelativeLayout) findViewById(R.id.main_parent_home_screen);

        Logger.addLogAdapter(new AndroidLogAdapter());
        loadApps("");

        RecyclerView appsRecyclerView = (RecyclerView) findViewById(R.id.apps_list_recycler_view);
        int numberOfColumns = 4;
        appsRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new AppsRecyclerViewAdapter(this, apps);
        adapter.setClickListener(this);
        appsRecyclerView.setAdapter(adapter);

        mPanelLabelParentCollapsed = (LinearLayout)findViewById(R.id.apps_list_label_parent_collapsed);
        mPanelLabelParentExpanded = (LinearLayout) findViewById(R.id.apps_list_label_parent_expanded);

        mSearchEditText = (EditText) findViewById(R.id.search_apps_edit_text);

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
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Logger.i("onPanelStateChanger1"+slideOffset);
                mMainParentHomeScreen.setAlpha(255-(Math.round(slideOffset*255)));
                mPanelLabelParentCollapsed.setAlpha(255-(Math.round(slideOffset*255)));
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
                                            SlidingUpPanelLayout.PanelState newState) {
                Logger.i("onPanelStateChanger2"+newState);
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

        loadFabs();

        fabOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mLauncherSettingsParent.getVisibility()==View.VISIBLE && mPhoneSettingsParent.getVisibility()==View.VISIBLE){
                    mLauncherSettingsParent.setVisibility(GONE);
                    mPhoneSettingsParent.setVisibility(GONE);
                    mLauncherSettingsParent.startAnimation(mHideLayout);
                    mPhoneSettingsParent.startAnimation(mHideLayout);
                    fabOptions.startAnimation(mHideButton);
                }else{
                    mLauncherSettingsParent.setVisibility(View.VISIBLE);
                    mPhoneSettingsParent.setVisibility(View.VISIBLE);
                    mLauncherSettingsParent.startAnimation(mShowLayout);
                    mPhoneSettingsParent.startAnimation(mShowLayout);
                    fabOptions.startAnimation(mShowButton);
                }
            }
        });

        fabLauncherSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LauncherSettingsActivity.class));
            }
        });

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
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private void loadApps(String pattern) {
        manager = getPackageManager();
        apps = new ArrayList<AppItem>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {

            if(ri.loadLabel(manager).toString().toLowerCase().contains(pattern)) {
                AppItem app = new AppItem();
                app.label = ri.activityInfo.packageName;
                app.name = ri.loadLabel(manager);
                app.icon = ri.loadIcon(manager);
                apps.add(app);
            }

        }

        Collections.sort(apps, new Comparator<AppItem>() {
            @Override
            public int compare(AppItem app1, AppItem app2) {
                if(app1.name.toString().compareToIgnoreCase(app2.name.toString())>0){
                    return 1;
                }else{
                    return -1;
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        Logger.d("Clicked at position : " + position);
        Intent intent = manager.getLaunchIntentForPackage(apps.get(position).label.toString());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        Logger.i("Back button pressed from launcher home");
        if(mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            Toast.makeText(this, "You are at launcher home", Toast.LENGTH_SHORT).show();
        }else {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    private void loadFabs(){
        fabOptions = (FloatingActionButton) findViewById(R.id.fab_settings_menu);
        fabLauncherSettings = (FloatingActionButton) findViewById(R.id.fab_settings_launcher);
        fabPhoneSettings = (FloatingActionButton) findViewById(R.id.fab_settings_phone);

        //Animations
        mShowButton = AnimationUtils.loadAnimation(this,R.anim.show_button);
        mHideButton = AnimationUtils.loadAnimation(this,R.anim.hide_button);
        mShowLayout = AnimationUtils.loadAnimation(this,R.anim.show_layout);
        mHideLayout = AnimationUtils.loadAnimation(this,R.anim.hide_layout);

        mPhoneSettingsParent = (LinearLayout) findViewById(R.id.phone_settings_parent);
        mLauncherSettingsParent = (LinearLayout) findViewById(R.id.launcher_settings_parent);
    }

}
