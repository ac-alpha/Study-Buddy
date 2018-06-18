package in.ashutoshchaubey.studybuddy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import in.ashutoshchaubey.studybuddy.Constants.AppsEntry;

/**
 * Created by ashutoshchaubey on 14/06/18.
 */

public class AppsDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AppsList6";
    private static final int DATABASE_VERSION = 1;

    public static final String CREATE_TABLE_APPS = "CREATE TABLE " + AppsEntry.TABLE_NAME + "("+
            AppsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            AppsEntry.COLUMN_APP_NAME + " TEXT, " +
            AppsEntry.COLUMN_APP_PACKAGE + " TEXT, "+
            AppsEntry.COLUMN_APP_USEFUL+" INTEGER DEFAULT 1 );";

    public AppsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        sqLiteDatabase.execSQL(CREATE_TABLE_APPS);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + AppsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
