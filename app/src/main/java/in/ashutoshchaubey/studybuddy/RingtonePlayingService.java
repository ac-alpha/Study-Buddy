package in.ashutoshchaubey.studybuddy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ashutoshchaubey on 13/06/18.
 */

public class RingtonePlayingService extends Service {
    Boolean isRunning = false;
    MediaPlayer mMediaPlayer;
    int startId = 0;
    SharedPreferences prefs;
    String TAG = "RongtonePlayingService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG,"inside onCreate()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"inside onDestroy()");
    }

    @Override
    public boolean stopService(Intent name) {
        Log.e(TAG,"inside stopService()");
        return super.stopService(name);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final NotificationManager mNM = (android.app.NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        Intent intent1 = new Intent(this.getApplicationContext(), MainActivity.class);
        intent1.putExtra("switchState",true);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification mNotify  = new Notification.Builder(this)
                .setContentTitle("ALARM IS RINGING" + "!")
                .setContentText("Wakey wakey!")
                .setSmallIcon(R.drawable.alarm_on)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        prefs = getApplicationContext().getSharedPreferences(LauncherSettingsActivity.MY_PREFERENCES,MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();


        Boolean state = intent.getExtras().getBoolean("extra");

        assert state != null;
        if(state){
            startId=1;
        }else {
            startId=0;
        }

        if(!this.isRunning && startId == 1) {
            Log.e("if there was not sound ", " and you want start");

            mMediaPlayer = MediaPlayer.create(this, R.raw.loudalarm);

            mMediaPlayer.start();


            mNM.notify(0, mNotify);

            this.isRunning = true;
            this.startId = 0;

            editor.putString("switchState", Constants.ALARM_RINGING);
            editor.apply();

        }
        else if (!this.isRunning && startId == 0){
            Log.e("if there was not sound ", " and you want end");

            this.isRunning = false;
            this.startId = 0;

        }

        else if (this.isRunning && startId == 1){
            Log.e("if there is sound ", " and you want start");

            this.isRunning = true;
            this.startId = 0;

        }
        else {
            Log.e("if there is sound ", " and you want end");

            mMediaPlayer.stop();
            mMediaPlayer.reset();

            this.isRunning = false;
            this.startId = 0;
        }


        Log.e("MyActivity", "In the service");

        return START_NOT_STICKY;

    }
}
