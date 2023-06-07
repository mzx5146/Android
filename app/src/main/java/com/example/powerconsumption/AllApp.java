package com.example.powerconsumption;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

public class AllApp {

    private Context mContext;

    public AllApp(Context context) {
        mContext = context;
    }
    

    public void openAllApps() {

        PackageManager pm = mContext.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        //获取应用名称
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, PackageManager.PERMISSION_GRANTED);
        for (ResolveInfo app : apps) {
            //遍历
            Intent launchIntent = pm.getLaunchIntentForPackage(app.activityInfo.packageName);
            if (launchIntent != null) {
                mContext.startActivity(launchIntent);
            }
        }
    }

}
