# Study! Buddy
***
This is an android launcher to keep yourself away from distractions during Exams along with a bunch of shortcuts to save your precious time during exams. This app motivates you for studying by hiding the launch icons and notifications of apps belonging to specific categories in the play store. The launcher is inspired by the default launcher for Android Nougat.
**Note : Project Name is Study! Buddy... Due to github repo naming guidelines, I had to use Study-Buddy**

## Using the Launcher
Just install the launcher apk. Then go to Settings -> Apps -> Click on the settings icon on top -> Go  to Home App -> Select Study! Buddy...
**This might vary depending on your android version**

## Features
- Automatically identifying useful apps
- One tap alarm on/off from launcher home
- Text/Image Note taking on launcher home
- Blocking notifications of app categorised as non useful (**Not completely functional**)

## Working
To identify the useful apps, the [basic idea](https://stackoverflow.com/questions/28321493/how-to-get-an-app-category-from-play-store-by-its-package-name-in-android/34675866) is to get the html from the google play store of a particular app and to search genre of the app from that html. 
To get all the available apps installed on the device, the follwowing code is used : 
```
manager = getPackageManager();
apps = new ArrayList<AppItem>();

Intent i = new Intent(Intent.ACTION_MAIN, null);
i.addCategory(Intent.CATEGORY_LAUNCHER);

List<ResolveInfo> availableActivities = manager.queryIntentActivities(i, 0);
for (ResolveInfo ri : availableActivities) {
    final AppItem app = new AppItem();
    app.label = ri.activityInfo.packageName;
    app.name = ri.loadLabel(manager);
    app.icon = ri.loadIcon(manager);
    apps.add(app);
}
```

To get the html and populate apps list to database, the following code is used : 
```
private void taskIdentifyUseful(final int no) {

    if(no<apps.size()) {
        final AppItem app = apps.get(no);
        Ion.with(getApplicationContext()).load("https://play.google.com/store/apps/details?id=" + app.label)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        int index = result.indexOf("\"genre\" href=\"https://play.google.com/store/apps/category/");
                        int length = "\"genre\" href=\"https://play.google.com/store/apps/category/".length();
                        String sub = result.substring(index + length, index + length + 30);
                        int isuse = 1;
                        for(String cat:badCategories){
                            if(sub.contains(cat)){
                                isuse = 0;
                            }
                        }
                        cv.put(AppsEntry.COLUMN_APP_NAME, app.name.toString());
                        cv.put(AppsEntry.COLUMN_APP_PACKAGE, app.label.toString());
                        cv.put(AppsEntry.COLUMN_APP_USEFUL, isuse);
                        sqLiteDatabase.insert(AppsEntry.TABLE_NAME,null,cv);
                        cv.clear();
                        if(no==(apps.size()-1)){
                            progressDialog.dismiss();
                            findViewById(R.id.useful_apps_found).setVisibility(View.VISIBLE);
                            prefEditor.putBoolean("identified",true);
                            prefEditor.apply();
                        }
                        taskIdentifyUseful(no+1);
                    }
                });
    }

}
```
