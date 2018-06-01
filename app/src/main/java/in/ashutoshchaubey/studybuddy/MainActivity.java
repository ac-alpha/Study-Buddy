package in.ashutoshchaubey.studybuddy;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PackageManager manager;
    private ArrayList<AppItem> apps;
    private AppsRecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadApps();

        RecyclerView appsRecyclerView = (RecyclerView) findViewById(R.id.apps_list_recycler_view);
        int numberOfColumns = 4;
        appsRecyclerView.setLayoutManager(new GridLayoutManager(this,numberOfColumns));
        adapter = new AppsRecyclerViewAdapter(this,apps);
        appsRecyclerView.setAdapter(adapter);

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
}
