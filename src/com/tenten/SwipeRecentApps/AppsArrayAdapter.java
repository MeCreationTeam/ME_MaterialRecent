/*
**
** Copyright 2012, Hussein Ala
**
** Licensed under the Apache License, Version 2.0 (the "License"); 
** you may not use this file except in compliance with the License. 
** You may obtain a copy of the License at 
**
**       http://www.apache.org/licenses/LICENSE-2.0 
**
** Unless required by applicable law or agreed to in writing, software 
** distributed under the License is distributed on an "AS IS" BASIS, 
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
** See the License for the specific language governing permissions and 
** limitations under the License.
*/


package com.tenten.SwipeRecentApps;

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
			row = inflater.inflate(getLayout("appsview_item", "layout", "com.tenten.SwipeRecentApps"), parent, false);
		}

		App app = getItem(position);
		
		appName = (TextView)row.findViewById(getLayout("tVName", "id", "com.tenten.SwipeRecentApps"));
		appIcon = (ImageView)row.findViewById(getLayout("iVIcon", "id", "com.tenten.SwipeRecentApps"));
		ll = (LinearLayout)row.findViewById(getLayout("ll", "id", "com.tenten.SwipeRecentApps"));
		
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
