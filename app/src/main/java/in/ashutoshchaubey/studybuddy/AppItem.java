package in.ashutoshchaubey.studybuddy;

import android.graphics.drawable.Drawable;

/**
 * Created by ashutoshchaubey on 02/06/18.
 */

public class AppItem {

    CharSequence label; //package name
    CharSequence name; //app name
    Drawable icon; //app icon

//    public AppItem(CharSequence label, CharSequence name, Drawable icon) {
//        this.label = label;
//        this.icon = icon;
//        this.name = name;
//    }

    public CharSequence getLabel() {
        return label;
    }

    public CharSequence getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setLabel(CharSequence label) {
        this.label = label;
    }

    public void setName(CharSequence name) {
        this.name = name;
    }
}
