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
import sword.allchat.Observable;
import sword.allchat.Observer;
import sword.allchat.DialogBuilder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class UseActivity extends Activity implements Observer {
    private static final String TAG = "chat.UseActivity";
    
    public static final int REQUEST_CODE_CAPTURE_CAMEIA = 0;
    public static final int REQUEST_CODE_PICK_IMAGE = 1;
    String capturePath = null;
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.use);  
        
        mHistoryList = new ChatAdapter(this, ChatList);
        mHistoryList.clearList();
        
        hlv = (ListView) findViewById(R.id.useHistoryList);
        hlv.setAdapter(mHistoryList);
        
        
        final EditText messageBox = (EditText)findViewById(R.id.useMessage);
        messageBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                	String text = view.getText().toString();
                    Log.i(TAG, "useMessage.onEditorAction(): got message " + text + ")");
    	            //将传message改为传JSON对应的字符串.
                    
                    JSONObject jsonObject = new JSONObject(); 
                    try{
                    	jsonObject.put("username", mChatApplication.getUserName());
                    	if(mChatApplication.getUserName().equals("Public"))
                    		jsonObject.put("nickname", "[P]"+mChatApplication.getNickName());
                    	else
                    		jsonObject.put("nickname", "[S]"+mChatApplication.getNickName());
                    	jsonObject.put("type", 0); //判断发送的是文字还是图片
                    	jsonObject.put("text",text);
                    }catch (JSONException ex) {  
                        // 键为null或使用json不支持的数字格式(NaN, infinities)  
                        throw new RuntimeException(ex);  
                    }
                    String message = jsonObject.toString();
                    Log.i(TAG,"sent JSON message is :"+message);
                    mChatApplication.newLocalUserMessage(jsonObject);
    	            view.setText("");
                }
                return true;
            }
        });
        
        //发送消息，解决回车发送不能的问题
        mSendmessageBtn = (Button)findViewById(R.id.sendMessage);
        mSendmessageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {String text = messageBox.getText().toString();
                Log.i(TAG, "useMessage.onEditorAction(): got message " + text + ")");
	            //将传message改为传JSON对应的字符串.
                
                JSONObject jsonObject = new JSONObject(); 
                try{
                	jsonObject.put("username", mChatApplication.getUserName());
                	if(mChatApplication.getUserName().equals("Public"))
                		jsonObject.put("nickname", "[P]"+mChatApplication.getNickName());
                	else
                		jsonObject.put("nickname", "[S]"+mChatApplication.getNickName());
                	jsonObject.put("type", 0); //判断发送的是文字还是图片
                	jsonObject.put("text",text);
                }catch (JSONException ex) {  
                    // 键为null或使用json不支持的数字格式(NaN, infinities)  
                    throw new RuntimeException(ex);  
                }
                String message = jsonObject.toString();
                Log.i(TAG,"sent JSON message is :"+message);
                mChatApplication.newLocalUserMessage(jsonObject);
                messageBox.setText("");
        	}
        });
        

        //发送图片
        mSendpicBtn = (Button)findViewById(R.id.sendPic);
        mSendpicBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	final Dialog dialog = new Dialog(UseActivity.this);
            	dialog.requestWindowFeature(dialog.getWindow().FEATURE_NO_TITLE);
            	dialog.setContentView(R.layout.pickphotodialog);
 	            dialog.show();
            	Button camera = (Button)dialog.findViewById(R.id.fromCamera);
				camera.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						String state = Environment.getExternalStorageState();
						if (state.equals(Environment.MEDIA_MOUNTED)) {
							Intent intent = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							startActivityForResult(intent,
									REQUEST_CODE_CAPTURE_CAMEIA);

						} else {
							Toast.makeText(getApplicationContext(),
									"请确认已经插入SD卡", Toast.LENGTH_LONG).show();
						}
						dialog.cancel();
					}
				});
            	
            	Button photo = (Button)dialog.findViewById(R.id.fromPhoto);
            	photo.setOnClickListener(new View.OnClickListener() {
            		public void onClick(View view) {
                    	Intent intent = new Intent();  
                        /* 开启Pictures画面Type设定为image */  
                        intent.setType("image/*");  
                        /* 使用Intent.ACTION_GET_CONTENT这个Action */  
                        intent.setAction(Intent.ACTION_PICK);   
                        /* 取得相片后返回本画面 */  
                        startActivityForResult(intent, 1);
                        dialog.cancel();
            		}
            	});
            	           	

                
                
//            	JSONObject jsonObject = new JSONObject(); 
//            	
//            	bitmapString=bitmaptoString(bitmap,100);
//            	
//                try{
//                	jsonObject.put("nickname", mChatApplication.getNickName());
//                	jsonObject.put("type", 1); //判断发送的是文字还是图片
//                	jsonObject.put("image",bitmapString); //修改此处为图片路径实现传图
//                }catch (JSONException ex) {  
//                    // 键为null或使用json不支持的数字格式(NaN, infinities)  
//                    throw new RuntimeException(ex);  
//                }
//                String message = jsonObject.toString();
//                mChatApplication.newLocalUserMessage(message);
                
                
        	}
        });

        mChangeModeBtn = (Button)findViewById(R.id.changeMode);
        mChangeModeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
            	AllJoynService.UseChannelState channelState = mChatApplication.useGetChannelState();
				switch (channelState) {
				case IDLE:
					Toast.makeText(getApplicationContext(), "尚未加入频道，无法选择模式", Toast.LENGTH_LONG).show();
					break;
				case JOINED:
					showDialog(DIALOG_PRICACY_CHAT_ID);
					break;
				}
            	
        	}
        });
        
        mJoinButton = (Button)findViewById(R.id.useJoin);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_JOIN_ID);
                UserName.setText("Public");
        	}
        });

        mLeaveButton = (Button)findViewById(R.id.useLeave);
        mLeaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_LEAVE_ID);
            }
        });
        
        UserName =(TextView)findViewById(R.id.userName);
        
        mChannelName = (TextView)findViewById(R.id.useChannelName);
        mChannelStatus = (TextView)findViewById(R.id.useChannelStatus);
        
        /*
         * Keep a pointer to the Android Appliation class around.  We use this
         * as the Model for our MVC-based application.    Whenever we are started
         * we need to "check in" with the application so it can ensure that our
         * required services are running.
         */
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.checkin();
        
        /*
         * Call down into the model to get its current state.  Since the model
         * outlives its Activities, this may actually be a lot of state and not
         * just empty.
         */
        updateChannelState();
        updateHistory();
        
        /*
         * Now that we're all ready to go, we are ready to accept notifications
         * from other components.
         */
        mChatApplication.addObserver(this);
    }
    
	// 拍照和相册
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Uri uri = null;

			ContentResolver cr = this.getContentResolver();
			JSONObject jsonObject = new JSONObject();

			if (requestCode == REQUEST_CODE_PICK_IMAGE) {
				uri = data.getData();
				Log.i(TAG,"uri is"+ uri);
				/**
				 * 压缩图片，取得压缩比例
				 */
				BitmapFactory.Options options = new BitmapFactory.Options();
				// options.inSampleSize=10;
				options.inJustDecodeBounds = true;
				try {
					BitmapFactory.decodeStream(cr.openInputStream(uri), null,
							options);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				options.inSampleSize = calculateInSampleSize(options, 200, 200);// 自定义一个宽和高
				options.inJustDecodeBounds = false;
				try {
					bitmap = BitmapFactory.decodeStream(
							cr.openInputStream(uri), null, options);
				} catch (FileNotFoundException e) {
					Log.e("Exception", e.getMessage(), e);
				}

				bitmap = compressImage(bitmap);

				/**
				 * 获取照片的路径位置
				 * 
				 */
				// public static String getRealPathFromURI(Uri uri,
				// ContentResolver resolver)
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = cr.query(uri, proj, null, null, null);
				int column_index = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				String filePath = cursor.getString(column_index);
				cursor.close();

				/**
				 * 尝试将图片旋转到正确位置
				 */
				int degree = readPictureDegree(filePath);
				Log.i(TAG, "readPictureDegree is" + degree);
				bitmap = rotateBitmap(bitmap, degree);
			} else 
				if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA) {
				Log.i(TAG, "requestCode == REQUEST_CODE_CAPTURE_CAMEIA");
	            if(data == null )  
	            {  
					Toast.makeText(this, "图片获取失败，data is null" , Toast.LENGTH_LONG ).show();
	            }
	            uri = data.getData();  
				if (uri == null) { 
					Log.i(TAG,"uri == null");
					// use bundle to get data
					Bundle bundle = data.getExtras();
					if (bundle != null) {
						bitmap = (Bitmap) bundle.get("data"); // get bitmap
					} else {
						Toast.makeText(this, "无法显示照片", Toast.LENGTH_LONG)
								.show();
						return;
					}    
	            }else
	            {
	            	Log.i(TAG,"uri = data.getData() seccussful");
	            	BitmapFactory.Options options = new BitmapFactory.Options();
					// options.inSampleSize=10;
					options.inJustDecodeBounds = true;
					try {
						BitmapFactory.decodeStream(cr.openInputStream(uri), null,
								options);
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					options.inSampleSize = calculateInSampleSize(options, 200, 200);// 自定义一个宽和高
					options.inJustDecodeBounds = false;
					try {
						bitmap = BitmapFactory.decodeStream(
								cr.openInputStream(uri), null, options);
					} catch (FileNotFoundException e) {
						Log.e("Exception", e.getMessage(), e);
					}

					bitmap = compressImage(bitmap);

					/**
					 * 获取照片的路径位置
					 * 
					 */
					// public static String getRealPathFromURI(Uri uri,
					// ContentResolver resolver)
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor cursor = cr.query(uri, proj, null, null, null);
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					cursor.moveToFirst();
					String filePath = cursor.getString(column_index);
					cursor.close();

					/**
					 * 尝试将图片旋转到正确位置
					 */
					int degree = readPictureDegree(filePath);
					Log.i(TAG, "readPictureDegree is" + degree);
					bitmap = rotateBitmap(bitmap, degree);
	            }
			}

			bitmapString = bitmaptoString(bitmap, 100);

			try {
				jsonObject.put("username", mChatApplication.getUserName());
				if (mChatApplication.getUserName().equals("Public"))
					jsonObject.put("nickname",
							"[P]" + mChatApplication.getNickName());
				else
					jsonObject.put("nickname",
							"[S]" + mChatApplication.getNickName());
				jsonObject.put("type", 1); // 判断发送的是文字还是图片
				jsonObject.put("image", bitmapString); // 修改此处为图片路径实现传图
			} catch (JSONException ex) {
				// 键为null或使用json不支持的数字格式(NaN, infinities)
				throw new RuntimeException(ex);
			}
			// String message = jsonObject.toString();
			mChatApplication.newLocalUserMessage(jsonObject);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
    
	/**
	 * 将bitmap转换成base64字符串
	 * 
	 * @param bitmap
	 * @return base64 字符串
	 */
	public String bitmaptoString(Bitmap bitmap, int bitmapQuality) {

		// 将Bitmap转换成字符串
		String string = null;
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, bitmapQuality, bStream);
		byte[] bytes = bStream.toByteArray();
		string = Base64.encodeToString(bytes, Base64.DEFAULT);
		Log.i(TAG,"bitmapToString size is:"+string.length());
		Log.i(TAG,"bitmap size is:"+bitmap.getByteCount()/1024);
		return string;
	}

	//计算图片的缩放值 用于按照宽高大小缩放
    public  int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;//获取图片的高
        final int width = options.outWidth;//获取图片的框
        Log.i(TAG,"options.outHeight:"+height);
        Log.i(TAG,"options.outWidth:"+width);
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
             final int heightRatio = Math.round((float) height/ (float) reqHeight);
             final int widthRatio = Math.round((float) width / (float) reqWidth);
             inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        Log.i(TAG,"options.inSampleSize is "+inSampleSize);
		return inSampleSize;
    }
    	
	public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mChatApplication = (ChatApplication)getApplication();
        mChatApplication.deleteObserver(this);
        //System.exit(0); 彻底关闭程序。
    	super.onDestroy();
 	}
    
    public static final int DIALOG_JOIN_ID = 0;
    public static final int DIALOG_LEAVE_ID = 1;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 2;
    public static final int DIALOG_PRICACY_CHAT_ID=3;

    protected Dialog onCreateDialog(int id) {
    	Log.i(TAG, "onCreateDialog()");
        Dialog result = null;
        switch(id) {
        case DIALOG_JOIN_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder();
	        	result = builder.createUseJoinDialog(this, mChatApplication);
	        }        	
        	break;
        case DIALOG_LEAVE_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder();
	        	result = builder.createUseLeaveDialog(this, mChatApplication);
	        }
	        break;
        case DIALOG_ALLJOYN_ERROR_ID:
	        { 
	        	DialogBuilder builder = new DialogBuilder();
	        	result = builder.createAllJoynErrorDialog(this, mChatApplication);
	        }
	        break;
        case DIALOG_PRICACY_CHAT_ID:
        	{
        		DialogBuilder builder = new DialogBuilder();
	        	result = builder.createUserSelectedDialog(this, mChatApplication);
        	}
        break;
        }
        return result;
    }
    
    public synchronized void update(Observable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;
        
        if (qualifier.equals(ChatApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.HISTORY_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(ChatApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
        if (qualifier.equals(ChatApplication.USER_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_USER_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
    }
    
	private void updateHistory() {
		Log.i(TAG, "updateHistory()");
		// mHistoryList.clear();
		// List<String> messages = mChatApplication.getHistory();
		// for (String message : messages) {
		// mHistoryList.add(message);
		// }
		if (mChatApplication.getListSize() == 0)
			return;
		JSONObject item = mChatApplication.getNewItem();
		int messageType = -1;
		//DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		// Date date = new Date();
		//JSONObject item;
		String nickname = "";
		//	item = new JSONObject(message);
			nickname = item.optString("nickname", "");
			nickname = nickname.substring(3);
			messageType = item.optInt("type", -1); // 消息类型，是图片还是文字
			// text=item.optString("message", "");

		Log.i(TAG,"getNewItem() JSON message is："+item.toString());
		chatmsg = new ChatMessage();
		
		switch (messageType) {
		case 0:
			Log.i(TAG,"case 0 text");
			// 如果是自己发出的，则，则发送者改为"我“;
			if (nickname.equals(mChatApplication.getNickName())) {
				// nickname = "我";
				chatmsg.setType(ChatAdapter.VALUE_RIGHT_TEXT);
			} else {
				chatmsg.setType(ChatAdapter.VALUE_LEFT_TEXT);
			}
			// mHistoryList.add("[" + dateFormat.format(date) + "] (" + nickname
			// + ") " + text);
			
			chatmsg.setValue(item);
			ChatList.add(chatmsg);
			
			//Log.i(TAG,"ChatList(0) message is ："+ChatList.get(0).getMessage());
			//Log.i(TAG,"ChatList(n-1) message is ："+ChatList.get(ChatList.size()-1).getMessage());
			//Log.i(TAG,"Received JSON message is"+item.toString());
			chatmsg = null;
			//hlv.setAdapter(mHistoryList);
			
			mHistoryList.notifyDataSetChanged();
			Log.i(TAG,"case 0 mHistoryList.notifyDataSetChanged()");
			
			break;

		case 1:
			Log.i(TAG,"case 1 image");
			if (nickname.equals(mChatApplication.getNickName())) {
				// nickname = "我";
				chatmsg.setType(ChatAdapter.VALUE_RIGHT_IMAGE);
			} else {
				chatmsg.setType(ChatAdapter.VALUE_LEFT_IMAGE);
			}
			chatmsg.setValue(item);
			ChatList.add(chatmsg);
			
			//Log.i(TAG,"ChatList(0) message is ："+ChatList.get(0).getMessage());
			//Log.i(TAG,"ChatList(n-1) message is ："+ChatList.get(ChatList.size()-1).getMessage());
			//Log.i(TAG,"Received JSON message is"+message);
			chatmsg = null;
			//hlv.setAdapter(mHistoryList);
			mHistoryList.notifyDataSetChanged();
			break;
			
		case 2:
			Log.i(TAG,"case 2 user message");
			chatmsg.setType(ChatAdapter.VALUE_USER_MESSAGE);
			chatmsg.setValue(item);
			ChatList.add(chatmsg);
			chatmsg = null;
			mHistoryList.notifyDataSetChanged();
			Log.i(TAG,"case 2 mHistoryList.notifyDataSetChanged()");
			break;
		}
		
		
		
	}
    
    
    private void updateChannelState() {
        //Log.i(TAG, "updateHistory()");
    	AllJoynService.UseChannelState channelState = mChatApplication.useGetChannelState();
    	String name = mChatApplication.useGetChannelName();
    	if (name == null) {
    		name = "Not set";
    	}
        mChannelName.setText(name);
        
        switch (channelState) {
        case IDLE:
        	mHistoryList.clearList();

            mChannelStatus.setText("Idle");
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(false);
            
            break;
        case JOINED:
            mChannelStatus.setText("Joined");
            mJoinButton.setEnabled(false);
            mLeaveButton.setEnabled(true);
            break;	
        }
    }
    
    /**
     * An AllJoyn error has happened.  Since this activity pops up first we
     * handle the general errors.  We also handle our own errors.
     */
    private void alljoynError() {
    	if (mChatApplication.getErrorModule() == ChatApplication.Module.GENERAL ||
    		mChatApplication.getErrorModule() == ChatApplication.Module.USE) {
    		showDialog(DIALOG_ALLJOYN_ERROR_ID);
    	}
    }
    
    private void updateUserName() 
    {
    	if(mChatApplication.getUserName().equals("Public"))
    		UserName.setText(mChatApplication.getUserName());
    	else
    		UserName.setText("PM to "+mChatApplication.getUserName());
    }
    
    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 2;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;
    private static final int HANDLE_USER_CHANGED_EVENT = 4;
    
    private Handler mHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_APPLICATION_QUIT_EVENT:
	            {
	            	Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
	                finish();
	            }
	            break; 
            case HANDLE_HISTORY_CHANGED_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
                    updateHistory();
                    break;
                }
            case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
	                updateChannelState();
	                break;
	            }
            case HANDLE_ALLJOYN_ERROR_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
	                alljoynError();
	                break;
	            }
            case HANDLE_USER_CHANGED_EVENT:
            	{
            		Log.i(TAG,"mHandler.handleMessage(): HANDLE_USER_CHANGED_EVENT");
            		updateUserName();
            	}
            default:
                break;
            }
        }
    };
    
    private static int readPictureDegree(String path) {    
        int degree  = 0;    
        try {    
                ExifInterface exifInterface = new ExifInterface(path);    
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);    
                switch (orientation) {    
                case ExifInterface.ORIENTATION_ROTATE_90:    
                        degree = 90;    
                        break;    
                case ExifInterface.ORIENTATION_ROTATE_180:    
                        degree = 180;    
                        break;    
                case ExifInterface.ORIENTATION_ROTATE_270:    
                        degree = 270;    
                        break;    
                }    
        } catch (IOException e) {    
                e.printStackTrace();    
        }    
        return degree;    
    }   
    
    private static Bitmap rotateBitmap(Bitmap bitmap, int rotate){  
        if(bitmap == null)  
            return null ;  
          
        int w = bitmap.getWidth();  
        int h = bitmap.getHeight();  
  
        // Setting post rotate to 90  
        Matrix mtx = new Matrix();  
        mtx.postRotate(rotate);  
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);  
    } 
    
    private Bitmap compressImage(Bitmap image) {  
    	  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中  
        int options = 100;  
        while ( baos.toByteArray().length / 1024>12) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩         
            baos.reset();//重置baos即清空baos  
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中  
            options -= 5;//每次都减少10  
        }  
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中  
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片  
        return bitmap;  
    }  
    
    /**
    * 用时间戳生成照片名称
    * @return
    */
    public String getPhotoFileName() {
    Date date = new Date(System.currentTimeMillis());
    SimpleDateFormat dateFormat = new SimpleDateFormat("IMG'_yyyyMMdd_HHmmss");
    return dateFormat.format(date) + ".jpg";
    }
    
    private ChatApplication mChatApplication = null;
    private ChatAdapter mHistoryList;
    
    private Button mJoinButton;
    private Button mLeaveButton;
    private TextView UserName;
    private TextView mChannelName;
      
    private TextView mChannelStatus;
    
    private Button mChangeModeBtn;
    private Button mSendpicBtn;
    private Button mSendmessageBtn;
    
    private ListView hlv;
    private List<ChatMessage> ChatList = new ArrayList<ChatMessage>();
    private ChatMessage chatmsg;
    private Bitmap bitmap;
    private String bitmapString;
}
