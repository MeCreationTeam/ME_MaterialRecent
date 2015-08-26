package com.tenten.SwipeRecentApps;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class TentenSwipeListView extends ListView {

    AppsArrayAdapter adapter;
    List<App> appsList = new ArrayList<App>();
    SwipeDismissListViewTouchListener touchListener;
    ActivityManager am;

    public TentenSwipeListView(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = (Intent)view.getTag();
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
                    try {
                        context.startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Log.w("Recent", "Unable to launch recent task", e);
                    }
                }
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(closeDialog);
            }
        });

        adapter = new AppsArrayAdapter(context, getLayout("appsview_item"), appsList);
        setAdapter(adapter);

        touchListener = new SwipeDismissListViewTouchListener(
                this,
                new SwipeDismissListViewTouchListener.OnDismissCallback() {
                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            String pkgname = adapter.getItem(position).pkgName;
                            killApp(pkgname);
                            try{
                                adapter.remove(adapter.getItem(position));
                            }catch(IndexOutOfBoundsException ignored){}
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    public void killApp(String PackageName){
        am.killBackgroundProcesses(PackageName);
        am.restartPackage(PackageName);
        if(!PackageName.equals("com.android.stk")){
            am.forceStopPackage(PackageName);}
    }

    public int getLayout(String mDrawableName){
        final String packName = "com.tenten.SwipeRecentApps";
        int ResID = 0;
        try {
            PackageManager manager = getContext().getPackageManager();
            Resources mApk1Resources = manager.getResourcesForApplication(packName);

            ResID = mApk1Resources.getIdentifier(mDrawableName, "layout", packName);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ResID;
    }
}
