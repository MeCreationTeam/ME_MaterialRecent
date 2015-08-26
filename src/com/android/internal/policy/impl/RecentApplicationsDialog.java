package com.android.internal.policy.impl;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.StatusBarManager;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

public class RecentApplicationsDialog extends Dialog implements AdapterView.OnItemClickListener {

    ListView appsLV;
    AppsArrayAdapter adapter;
    List<App> appsList = new ArrayList<App>();
    private ActivityManager am;
    private static int NUM_BUTTONS = 8;
    private static int MAX_RECENT_TASKS = NUM_BUTTONS * 2;
    Context c;
    TextView noApps;
    SwipeDismissListViewTouchListener touchListener;
    LinearLayout lin;
    Button taskman;
    RelativeLayout mainlayout;
    static private StatusBarManager sStatusBar;
    IntentFilter mBroadcastIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

    public RecentApplicationsDialog(Context context) {
        super(context);
        c = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        window.setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        window.setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setTitle("Recents");

        setContentView(getLayout("recentappdialog", "layout", "android"));

        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.FILL_PARENT;
        window.setAttributes(params);
        window.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (sStatusBar == null) {
            sStatusBar = (StatusBarManager) c.getSystemService(Context.STATUS_BAR_SERVICE);
        }

        taskman = (Button) findViewById(getLayout("taskman", "id", "android"));
        lin = (LinearLayout) findViewById(getLayout("swiperecent", "id", "android"));
        noApps = (TextView) findViewById(getLayout("noapps", "id", "android"));
        mainlayout = (RelativeLayout) findViewById(getLayout("mainlayout", "id", "android"));
        mainlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        taskman.setText("Task Manager");
        taskman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("android.intent.action.MAIN");
                i.setComponent(new ComponentName("com.sec.android.app.controlpanel", "com.sec.android.app.controlpanel.activity.JobManagerActivity"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Intent closeDialog = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                c.sendBroadcast(closeDialog);
                c.startActivity(i);
            }
        });

        noApps.setText("All recent applications killed");

        appsLV = new ListView(c);
        appsLV.setOnItemClickListener(this);
        registerForContextMenu(appsLV);
        am = (ActivityManager) c.getSystemService(c.ACTIVITY_SERVICE);

        adapter = new AppsArrayAdapter(c, getLayout("appsview_item", "layout", "android"), appsList);
        appsLV.setAdapter(adapter);
        touchListener = new SwipeDismissListViewTouchListener(
                appsLV,
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
                        checkNoAppsRunning();
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
        appsLV.setSelector(com.android.internal.R.color.transparent);
        appsLV.setCacheColorHint(0x00000000);
        appsLV.setFadingEdgeLength(6);

        lin.addView(appsLV);
        checkNoAppsRunning();
    }

    public void resetadapter(){
        adapter.clear();
        adapter.notifyDataSetChanged();
    }
    public void killApp(String PackageName){
        am.killBackgroundProcesses(PackageName);
        am.restartPackage(PackageName);
        if(!PackageName.equals("com.android.stk")){
            am.forceStopPackage(PackageName);}
    }

    @Override
    protected void onStart() {
        super.onStart();
        resetadapter();
        updateRecentTasks();
        adapter.notifyDataSetChanged();
        checkNoAppsRunning();

        // receive broadcasts
        getContext().registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();

        // stop receiving broadcasts
        getContext().unregisterReceiver(mBroadcastReceiver);
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


    public void checkNoAppsRunning() {
        if(appsList.size() > 0) {
            lin.setVisibility(View.VISIBLE);
            noApps.setVisibility(View.GONE);
        } else {
            lin.setVisibility(View.GONE);
            noApps.setVisibility(View.VISIBLE);
            dismiss();
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

        int index = 0;
        int numTasks = recentTasks.size();
        for (int i = 0; i < numTasks && (index < NUM_BUTTONS); ++i) {
            ActivityManager.RecentTaskInfo info = recentTasks.get(i);

            Intent intent = new Intent(info.baseIntent);
            if (info.origActivity != null) {
                intent.setComponent(info.origActivity);
            }

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

    public int getLayout(String mDrawableName, String typeName, String packName){
        int ResID = 0;
        try {
            PackageManager manager = getContext().getPackageManager();
            Resources mApk1Resources = manager.getResourcesForApplication(packName);

            ResID = mApk1Resources.getIdentifier(mDrawableName, typeName, packName);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return ResID;
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY);
                if (! PhoneWindowManager.SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    dismiss();
                }
            }
        }
    };
}
