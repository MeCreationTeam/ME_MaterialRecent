package com.tenten.SwipeRecentApps;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;


public class YALOG extends Dialog implements AdapterView.OnItemClickListener {

    LinearLayout swiperecent;
    Button taskman;
    TextView noapps;
    ListView appsLV;
    AppsArrayAdapter adapter;
    List<App> appsList = new ArrayList<App>();
    Context c;
    private ActivityManager am;
    SwipeDismissListViewTouchListener touchListener;
    private static int NUM_BUTTONS = 8;
    private static int MAX_RECENT_TASKS = NUM_BUTTONS * 2;

    public YALOG(Context context) {
        super(context, com.android.internal.R.style.Theme_Dialog_RecentApplications);
        c = context;

    }

    protected void onCreate(Bundle b){
        super.onCreate(b);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setTitle("Recents");

        setContentView(R.layout.recentappdialog);

        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.FILL_PARENT;
        window.setAttributes(params);
        window.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        swiperecent = (LinearLayout) findViewById(R.id.swiperecent);
        taskman = (Button) findViewById(R.id.taskman);
        noapps = (TextView) findViewById(R.id.noapps);

        appsLV = new ListView(c);
        appsLV.setOnItemClickListener(this);
        registerForContextMenu(appsLV);
        am = (ActivityManager) c.getSystemService(c.ACTIVITY_SERVICE);

        adapter = new AppsArrayAdapter(c, R.layout.appsview_item, appsList);
        appsLV.setAdapter(adapter);
        touchListener = new SwipeDismissListViewTouchListener(
                appsLV,
                new SwipeDismissListViewTouchListener.OnDismissCallback() {
                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        for (int position : reverseSortedPositions) {
                            String pkgname = adapter.getItem(position).pkgName;
                            killApp(pkgname);
                            adapter.remove(adapter.getItem(position));
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        appsLV.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        appsLV.setOnScrollListener(touchListener.makeScrollListener());
        appsLV.setStackFromBottom(true);
        appsLV.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        appsLV.setDividerHeight(0);
        appsLV.setOverScrollMode(View.OVER_SCROLL_NEVER);
        appsLV.setVerticalScrollBarEnabled(false);

        swiperecent.addView(appsLV);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = (Intent)view.getTag();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
            try {
                c.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Log.w("Recent", "Unable to launch recent task", e);
            }
        }
        dismiss();
    }

    public void killApp(String PackageName){
        am.killBackgroundProcesses(PackageName);
        am.restartPackage(PackageName);
    }

    public void resetadapter(){
        for(int x = 0; x < adapter.getCount(); x++){
            adapter.remove(adapter.getItem(x));
        }
    }

    private void updateRecentTasks() {

        final Context context = c;
        final PackageManager pm = context.getPackageManager();
        final ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RecentTaskInfo> recentTasks =
                am.getRecentTasks(MAX_RECENT_TASKS, ActivityManager.RECENT_IGNORE_UNAVAILABLE);

        ActivityInfo homeInfo =
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                        .resolveActivityInfo(pm, 0);

        // Performance note:  Our android performance guide says to prefer Iterator when
        // using a List class, but because we know that getRecentTasks() always returns
        // an ArrayList<>, we'll use a simple index instead.
        int index = 0;
        int numTasks = recentTasks.size();
        for (int i = 0; i < numTasks && (index < NUM_BUTTONS); ++i) {
            ActivityManager.RecentTaskInfo info = recentTasks.get(i);

            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }

            // Skip the current home activity.
            if (homeInfo != null) {
                if (homeInfo.packageName.equals(
                        intent.getComponent().getPackageName())
                        && homeInfo.name.equals(
                        intent.getComponent().getClassName())) {
                    continue;
                }
            }

            intent.setFlags((intent.getFlags()&~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                final ActivityInfo activityInfo = resolveInfo.activityInfo;
                String LocalApp = info.baseIntent + "";
                int indexPackageNameBegin = LocalApp.indexOf("cmp=")+4;
                int indexPackageNameEnd = LocalApp.indexOf("/", indexPackageNameBegin);
                String PackageName = LocalApp.substring(indexPackageNameBegin, indexPackageNameEnd);

                String title2;
                Drawable icon2 = null;
                try {
                    title2 = activityInfo.loadLabel(pm).toString();
                    icon2 = activityInfo.loadIcon(pm);
                }catch(Exception e) {
                    title2 = null;
                }

                if(title2 != null && icon2 != null) {
                    App app  = new App(title2,PackageName, intent, icon2);
                    appsList.add(app);
                }
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        resetadapter();
        updateRecentTasks();
        adapter.notifyDataSetChanged();
    }
}
