package in.ashutoshchaubey.studybuddy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AppsRecyclerViewAdapter.ItemClickListener {

    private PackageManager manager;
    private ArrayList<AppItem> apps;
    private AppsRecyclerViewAdapter adapter;
    private SlidingUpPanelLayout mLayout;
    private RelativeLayout mMainParentHomeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainParentHomeScreen = (RelativeLayout) findViewById(R.id.main_parent_home_screen);

        Logger.addLogAdapter(new AndroidLogAdapter());
        loadApps();

        RecyclerView appsRecyclerView = (RecyclerView) findViewById(R.id.apps_list_recycler_view);
        int numberOfColumns = 4;
        appsRecyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        adapter = new AppsRecyclerViewAdapter(this, apps);
        adapter.setClickListener(this);
        appsRecyclerView.setAdapter(adapter);

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Logger.i("onPanelStateChanger1"+slideOffset);
                mMainParentHomeScreen.setAlpha(255-(Math.round(slideOffset*255)));
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Logger.i("onPanelStateChanger2"+newState);
//                mMainParentHomeScreen.setVisibility(View.INVISIBLE);
            }
        });

        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });


    }

    private void loadApps() {
        manager = getPackageManager();
        apps = new ArrayList<AppItem>();

        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
        for (ResolveInfo ri : availableActivities) {

            AppItem app = new AppItem();
            app.label = ri.activityInfo.packageName;
            app.name = ri.loadLabel(manager);
            app.icon = ri.loadIcon(manager);
            apps.add(app);

        }
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
        Toast.makeText(this, "You are at launcher home", Toast.LENGTH_SHORT).show();
    }
}
