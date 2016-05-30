/*
 * Copyright (c) 2011, AllSeen Alliance. All rights reserved.
 *
 *    Permission to use, copy, modify, and/or distribute this software for any
 *    purpose with or without fee is hereby granted, provided that the above
 *    copyright notice and this permission notice appear in all copies.
 *
 *    THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *    WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *    MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *    ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *    WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *    ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *    OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package sword.allchat;

import android.os.Bundle;
import android.app.ActionBar.LayoutParams;
import android.app.TabActivity;
import android.view.Gravity;
import android.widget.TabHost;
import android.widget.TextView;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

public class TabWidget extends TabActivity {
    private static final String TAG = "chat.TabWidget";
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        intent = new Intent().setClass(this, UseActivity.class);
        spec = tabHost.newTabSpec("use").setIndicator("Chat").setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, HostActivity.class);
        spec = tabHost.newTabSpec("host").setIndicator("Host").setContent(intent);
        tabHost.addTab(spec);

        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++)  
        {  
            TextView textView = (TextView)tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);  
            textView.setTextSize(18);  
            textView.setGravity(Gravity.CENTER);  
            textView.getLayoutParams().height = LayoutParams.MATCH_PARENT;  
            textView.getLayoutParams().width = LayoutParams.MATCH_PARENT; 
        }
        tabHost.setCurrentTab(0);
    }
}