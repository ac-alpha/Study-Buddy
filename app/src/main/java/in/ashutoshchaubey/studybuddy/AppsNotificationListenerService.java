package in.ashutoshchaubey.studybuddy;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ashutoshchaubey on 16/06/18.
 */

public class AppsNotificationListenerService extends NotificationListenerService {

    AppsDbHelper appsDbHelper;
    SQLiteDatabase db;
    ArrayList<AppItem> apps;
    public static final String TAG = "SampleNLS";

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("NLService", "inside onCreate()");

        appsDbHelper = new AppsDbHelper(this);
        db = appsDbHelper.getReadableDatabase();
        apps = new ArrayList<AppItem>();

        Cursor cursor = db.query(Constants.AppsEntry.TABLE_NAME, null, null,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {

                    AppItem app = new AppItem();
                    app.label = cursor.getString(2);
                    app.name = cursor.getString(1);
                    int use = cursor.getInt(3);
                    if (use == 1) {
                        app.isUseful = true;
                    } else {
                        app.isUseful = false;
                    }
                    if (!app.isUseful) {
                        apps.add(app);
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }


        Log.e("NLService", "" + apps.size());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("NLService", "inside onStart()");
        return START_STICKY;

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
//        Log.e("NotifListenerService", "Notification Posted");
//        Log.e("NotifListenerService", sbn.getId() + "  " + sbn.getKey() + "  " + sbn.getPackageName());
        appsDbHelper = new AppsDbHelper(this);
        db = appsDbHelper.getReadableDatabase();
        apps = new ArrayList<AppItem>();

        Cursor cursor = db.query(Constants.AppsEntry.TABLE_NAME, null, null,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {

                    AppItem app = new AppItem();
                    app.label = cursor.getString(2);
                    app.name = cursor.getString(1);
                    int use = cursor.getInt(3);
                    if (use == 1) {
                        app.isUseful = true;
                    } else {
                        app.isUseful = false;
                    }
                    if (!app.isUseful) {
                        apps.add(app);
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        String packageName = sbn.getPackageName();
        for (AppItem app : apps) {
            if (packageName.equals(app.label)) {
                cancelNotification(sbn.getKey());
            }
        }
    }


}
