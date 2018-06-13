package in.ashutoshchaubey.studybuddy;

/**
 * Created by ashutoshchaubey on 13/06/18.
 */

public class Utilities {

    public static String getTimeText(int hr, int min){
        String amPm = "";
        String timeText = "";
        if(hr>=12){
            amPm = "PM";
        }else {
            amPm = "AM";
        }
        if(hr<10){
            timeText += "0"+hr;
        }else if(hr<=12){
            timeText += hr;
        }else if (hr>12 && hr<22){
            timeText += "0"+(hr-12);
        }else {
            timeText += (hr-12);
        }
        timeText+=":";
        if(min<10){
            timeText+="0";
        }
        timeText+=min;
        timeText+=amPm;
        return timeText;
    }

}
