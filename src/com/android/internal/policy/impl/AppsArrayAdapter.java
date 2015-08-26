package com.android.internal.policy.impl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppsArrayAdapter extends ArrayAdapter<App> {


    private List<App> apps = new ArrayList<App>();
    private TextView appName;
    private ImageView appIcon;
    private LinearLayout ll;

    public AppsArrayAdapter(Context context, int textViewResourceId,
                            List<App> objects) {
        super(context, textViewResourceId, objects);

        this.apps = objects;
    }

    public int getCount() {
        return this.apps.size();
    }

    public App getItem(int index) {
        return this.apps.get(getCount() - 1 - index);
    }

    public View getView(int position, View appView, ViewGroup parent) {
        View row = appView;

        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(getLayout("appsview_item", "layout", "android"), parent, false);
        }

        App app = getItem(position);

        appName = (TextView)row.findViewById(getLayout("tVName", "id", "android"));
        appIcon = (ImageView)row.findViewById(getLayout("iVIcon", "id", "android"));
        ll = (LinearLayout)row.findViewById(getLayout("ll", "id", "android"));

        appName.setText(app.name);
        ll.setTag(app.intent);
        appIcon.setImageDrawable(app.icon);

        return row;

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



}
