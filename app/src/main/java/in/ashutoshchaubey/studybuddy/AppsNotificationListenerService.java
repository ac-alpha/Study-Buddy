package in.ashutoshchaubey.studybuddy;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

/**
 * Created by ashutoshchaubey on 16/06/18.
 */

public class AppsNotificationListenerService extends NotificationListenerService {

    AppsDbHelper appsDbHelper;
    SQLiteDatabase db;
    ArrayList<AppItem> apps;

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.addLogAdapter(new AndroidLogAdapter());
        appsDbHelper = new AppsDbHelper(this);
        db = appsDbHelper.getReadableDatabase();
        apps = new ArrayList<AppItem>();

        Cursor cursor = db.query(Constants.AppsEntry.TABLE_NAME,null, null,
                null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            if (cursor.moveToFirst()) {
                do {

                    AppItem app = new AppItem();
                    app.label = cursor.getString(2);
                    app.name = cursor.getString(1);
                    int use = cursor.getInt(3);
                    if(use == 1){
                        app.isUseful=true;
                    }else{
                        app.isUseful=false;
                    }
                    if(!app.isUseful){
                        apps.add(app);
                    }

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);
        String packageName = sbn.getPackageName();
        Logger.i("You are about to cancel notification");
        Logger.i(apps.size()+"");
        for(AppItem app : apps){
            if(packageName.equals(app.label)){
                cancelNotification(sbn.getKey());
            }
        }
    }
}
