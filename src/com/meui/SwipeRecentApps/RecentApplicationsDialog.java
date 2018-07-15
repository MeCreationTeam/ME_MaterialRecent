package com.meui.SwipeRecentApps;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.graphics.drawable.*;
import android.net.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.android.internal.policy.impl.*;
import com.nineoldandroids.view.*;
import java.util.*;
import tk.zielony.materialrecents.*;

public class RecentApplicationsDialog extends Dialog { //implements AdapterView.OnItemClickListener {

    //ListView appsLV;
    //AppsArrayAdapter adapter;
  	private List<App> appsList = new ArrayList<App>();
    private ActivityManager am;
    private static int NUM_BUTTONS = 7;
    private static int MAX_RECENT_TASKS = 7;//NUM_BUTTONS * 2;
    Context c;
    private TextView noApps;
    //SwipeDismissListViewTouchListener touchListener;
    //LinearLayout lin;
    private Button taskman;
    private RelativeLayout mainlayout;
    private static StatusBarManager sStatusBar;
    IntentFilter mBroadcastIntentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);


	private RecentsList mRecents;
    public RecentApplicationsDialog(Context context) {
        super(context);
        c = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
		window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER, WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER);
        //window.setTitle("Recents");

        setContentView(getLayout("recentappdialog", "layout", "com.meui.SwipeRecentApps"));

        final WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        params.height = WindowManager.LayoutParams.FILL_PARENT;
        window.setAttributes(params);
        window.setFlags(0, WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        if (sStatusBar == null) {
            sStatusBar = (StatusBarManager) c.getSystemService(Context.STATUS_BAR_SERVICE);
        }

        taskman = (Button) findViewById(getLayout("taskman", "id", "com.meui.SwipeRecentApps"));
        //lin = (LinearLayout) findViewById(getLayout("swiperecent", "id", "com.meui.SwipeRecentApps"));
        noApps = (TextView) findViewById(getLayout("noapps", "id", "com.meui.SwipeRecentApps"));
        mainlayout = (RelativeLayout) findViewById(getLayout("mainlayout", "id", "com.meui.SwipeRecentApps"));
        mainlayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					dismiss();
				}
			});
        taskman.setText("结束进程");
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

        noApps.setText(getLayout("no_recent_tasks", "string", "android"));

        //appsLV = new ListView(c);
        //appsLV.setOnItemClickListener(this);
		// registerForContextMenu(appsLV);
        am = (ActivityManager) c.getSystemService(c.ACTIVITY_SERVICE);


        //adapter = new AppsArrayAdapter(c, getLayout("appsview_item", "layout", "com.meui.SwipeRecentApps"), appsList);
        //appsLV.setAdapter(adapter);

		mRecents = (RecentsList)findViewById(getLayout("recent_frame", "id", "com.meui.SwipeRecentApps"));
		//mFrame.setAdapter(adapter);

        /* touchListener = new SwipeDismissListViewTouchListener(
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
		 });*/
        //appsLV.setOnTouchListener(touchListener);
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        //appsLV.setOnScrollListener(touchListener.makeScrollListener());
        //appsLV.setStackFromBottom(true);
        //appsLV.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_NORMAL);
        //appsLV.setDividerHeight(0);
        //appsLV.setOverScrollMode(View.OVER_SCROLL_NEVER);
        //appsLV.setVerticalScrollBarEnabled(false);
        //appsLV.setSelector(com.android.internal.R.color.transparent);
        //appsLV.setCacheColorHint(0x00000000);
        //appsLV.setFadingEdgeLength(6);
		//appsLV.setClipToPadding(false);

		//ViewGroup.LayoutParams lp=lin.getLayoutParams();
		//lp.width=ViewGroup.LayoutParams.MATCH_PARENT;
        //lin.addView(appsLV,lp);
		//lp=null;

        checkNoAppsRunning();
    }


    /*public void resetadapter(){
	 adapter.clear();
	 adapter.notifyDataSetChanged();
	 }*/
    public void killApp(String PackageName) {
        am.killBackgroundProcesses(PackageName);
        am.restartPackage(PackageName);
        if (!PackageName.equals("com.android.stk")) {
            am.forceStopPackage(PackageName);}
    }

    @Override
    protected void onStart() {
        super.onStart();
        //resetadapter();
        updateRecentTasks();
        //adapter.notifyDataSetChanged();
        checkNoAppsRunning();

        // receive broadcasts
        getContext().registerReceiver(mBroadcastReceiver, mBroadcastIntentFilter);
		final int[] cardColors=new int[]{0xff009688,0xff2196f3,0xff9c27b0,0xffff9800,0xff795548,0xff9e9e9e,0xff607d8b, 0xffffffff};
		final int[] headColors=new int[]{0xff00796b,0xff1976d2,0xff7b1fa2,0xfff57C00,0xff5d4037,0xff616161,0xff455a64, 0xff000000};

		mRecents.setAdapter(new RecentsAdapter() {

				@Override
				public String getTitle(int position) {
					return appsList.get(appsList.size() - position - 1).name;
				}

				@Override
				public View getView(int position) {
					FrameLayout card = new FrameLayout(c);
					card.setBackgroundColor(cardColors[appsList.size() - position - 1]);
					return card;
				}

				@Override
				public Drawable getIcon(int position) {
					return appsList.get(appsList.size() - position - 1).icon;
				}

				@Override
				public int getHeaderColor(int position) {
					return headColors[appsList.size() - position - 1];
				}

				@Override
				public int getCount() {
					return appsList.size();
				}
			});
    }

    @Override
    public void onStop() {
        super.onStop();

        // stop receiving broadcasts
        getContext().unregisterReceiver(mBroadcastReceiver);
    }

    /*@Override
	 public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
	 Intent intent = (Intent)view.getTag(R.id.noapps);
	 if (intent != null) {
	 intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
	 try {
	 c.startActivity(intent);
	 } catch (ActivityNotFoundException e) {
	 Log.w("Recent", "Unable to launch recent task", e);
	 }
	 }
	 dismiss();
	 }*/


    public void checkNoAppsRunning() {
        if (appsList.size() > 0) {
            mRecents.setVisibility(View.VISIBLE);
            noApps.setVisibility(View.GONE);
        } else {
            mRecents.setVisibility(View.GONE);
            noApps.setVisibility(View.VISIBLE);
            dismiss();
        }
    }

    private void updateRecentTasks() {

        final Context context = c;
        final PackageManager pm = context.getPackageManager();
        final ActivityManager am = (ActivityManager)
			context.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RecentTaskInfo> recentTasks =am.getRecentTasks(MAX_RECENT_TASKS,//存疑：API11前是不是只有WITH_EXCLUDED?
																			ActivityManager.RECENT_IGNORE_UNAVAILABLE);
		if (Build.VERSION.SDK_INT > 20) {
			for (int i=0;i < 3;i++) {
				recentTasks.addAll(1, recentTasks);
			}
			recentTasks.remove(0);
		}
        ActivityInfo homeInfo =new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).resolveActivityInfo(pm, 0);

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

            intent.setFlags((intent.getFlags() & ~Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
							| Intent.FLAG_ACTIVITY_NEW_TASK);
            final ResolveInfo resolveInfo = pm.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                final ActivityInfo activityInfo = resolveInfo.activityInfo;
                String LocalApp = info.baseIntent.toString();//MEUI
                int indexPackageNameBegin = LocalApp.indexOf("cmp=") + 4;
                int indexPackageNameEnd = LocalApp.indexOf("/", indexPackageNameBegin);
                String PackageName = LocalApp.substring(indexPackageNameBegin, indexPackageNameEnd);

                String title2;
                Drawable icon2 = null;
                try {
                    title2 = activityInfo.loadLabel(pm).toString();
                    icon2 = activityInfo.loadIcon(pm);
                } catch (Exception e) {
                    title2 = null;
                }

                if (title2 != null && icon2 != null) {
                    App app  = new App(title2, PackageName, intent, icon2);
                    appsList.add(app);
				}
            }
        }
		mRecents.setOnItemClickListener(new RecentsList.OnItemClickListener(){

				@Override
				public void onItemClick(View view, int position) {
					// Toast.makeText(context, "POSITION = " + position, Toast.LENGTH_SHORT).show();
					context.startActivity(appsList.get(appsList.size() - position - 1).intent);
				}
			});
    }

    public int getLayout(String mDrawableName, String typeName, String packName) {
        int resID = 0;
        try {
            PackageManager manager = getContext().getPackageManager();
            Resources mApk1Resources = manager.getResourcesForApplication(packName);

            resID = mApk1Resources.getIdentifier(mDrawableName, typeName, packName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return resID;
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
