package com.meui.SwipeRecentApps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MyActivity extends Activity {

    Context c = this;
    @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView fck = (TextView) findViewById(R.id.fck);
        fck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecentApplicationsDialog rp;
                rp = new RecentApplicationsDialog(c);
                rp.show();
            }
        });
    }
}
