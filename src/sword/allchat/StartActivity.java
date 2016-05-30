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

import sword.allchat.ChatApplication;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;



public class StartActivity extends Activity {

    private static final String TAG = "chat.TabWidget";
    private Button mStartBtn;
    private EditText mText;
    private ChatApplication mChatApplication = null;
    public void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        mStartBtn=(Button)findViewById(R.id.startBtn);
        mText=(EditText)findViewById(R.id.enterNickname);      
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	Intent intent;
            	String nickname=mText.getText().toString();
            	if(!nickname.equals(""))
            	{
            		mChatApplication = (ChatApplication)getApplication();
            		mChatApplication.setNickName(nickname);
					intent = new Intent(StartActivity.this, TabWidget.class);
					StartActivity.this.startActivity(intent);
					StartActivity.this.finish();
				}
            	else
            	{
            		Toast.makeText(getApplicationContext(), "Enter your nickname and start", Toast.LENGTH_SHORT).show();
            	}
        	}
        });
    }
}