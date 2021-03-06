package in.ashutoshchaubey.studybuddy;

import android.provider.BaseColumns;

/**
 * Created by ashutoshchaubey on 14/06/18.
 */

public class Constants {
    public static String ALARM_ON = "on";
    public static String ALARM_OFF = "off";
    public static String ALARM_RINGING = "ringing";
    public static String SAVED_NOTE_CONTENT = "savedNoteContent";
    public static String SAVED_IMAGE_PATH = "savedImagePath";

    public Constants(){}

    public class AppsEntry implements BaseColumns{
        public static final String TABLE_NAME = "apps";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_APP_NAME = "name";
        public static final String COLUMN_APP_PACKAGE = "packageName";
        public static final String COLUMN_APP_USEFUL = "isUseful";
    }

}
