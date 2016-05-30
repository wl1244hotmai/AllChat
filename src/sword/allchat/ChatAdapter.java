package sword.allchat;


import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends BaseAdapter{  
	
	private LruCache<String, Bitmap> mMemoryCache;
      
    public static final String KEY = "key";  
    public static final String VALUE = "value";  
      
    //4�ֲ�ͬ�Ĳ���  
    public static final int VALUE_LEFT_TEXT = 0;  
    public static final int VALUE_LEFT_IMAGE = 1;  
    public static final int VALUE_RIGHT_TEXT = 2;  
    public static final int VALUE_RIGHT_IMAGE = 3;
    public static final int VALUE_USER_MESSAGE = 4;
    
    private LayoutInflater mInflater;  
      
    
    private List<ChatMessage> myList;  
    public ChatAdapter(Context context, List<ChatMessage> myList){  
          
        
        this.myList = myList;  
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxMemory/16;
		// ����ͼƬ�����СΪ�����������ڴ��1/2
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount()/1024;
			}
		};  
		
//        for(ChatMessage msg:myList){  
//              
//            Log.d("myList:", msg.getType()+"");  
//        }  
          
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
    }  
      
    public void addItem(ChatMessage item) {  
        myList.add(item);
        
        // this.notifyDataSetChanged(); 	 
    }  
    
    public void clearList() {  
        myList.clear();  
        notifyDataSetChanged();  
    }  
      
    @Override  
    public int getCount() {  
        return myList.size();  
    }  
  
    @Override  
    public Object getItem(int arg0) {  
        return myList.get(arg0);  
    }  
  
    @Override  
    public long getItemId(int arg0) {  
        return arg0;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup arg2) {  
        Log.i("position","position is :"+position);
        
    	ChatMessage msg = myList.get(position);  
    	//Log.i("message in getView","message in getView is:"+msg.getMessage());
        int type = getItemViewType(position);  
        ViewHolder holder = null;  
        if(convertView == null){  
        	Log.i("positon","convertView==null position is :"+position);
            holder = new ViewHolder();  
                switch (type) {  

                                //���  
                    case VALUE_LEFT_TEXT:  
                          
                        convertView = mInflater.inflate(R.layout.list_item_left_text, null);  
                        holder.tvLeftNickname = (TextView)convertView.findViewById(R.id.tv_nickname);  
                        holder.btnLeftText = (Button)convertView.findViewById(R.id.btn_left_text);
                        holder.tvLeftNickname.setText(msg.getNickname());
                        holder.btnLeftText.setText(msg.getText());  
                        break;  
                          
                    case VALUE_LEFT_IMAGE:  
                    	addBitmapToMemoryCache(position+"",msg.getSmallImage());  
                        convertView = mInflater.inflate(R.layout.list_item_left_iamge, null);  
                        holder.tvLeftNickname = (TextView)convertView.findViewById(R.id.tv_nickname);  
                        holder.ivLeftImage = (ImageView)convertView.findViewById(R.id.iv_left_image);  
                        holder.tvLeftNickname.setText(msg.getNickname());
                        holder.ivLeftImage.setImageBitmap(getBitmapFromMemoryCache(position+""));  
                        break;  
                          
        
                                //�ұ�  
                    case VALUE_RIGHT_TEXT:  
                        convertView = mInflater.inflate(R.layout.list_item_right_text, null);  
                        holder.tvRightNickname = (TextView)convertView.findViewById(R.id.tv_nickname);  
                        holder.btnRightText = (Button)convertView.findViewById(R.id.btn_right_text);  
                        holder.tvRightNickname.setText("Me");
                        holder.btnRightText.setText(msg.getText()); 
                        Log.i("message righttext","righttext is :"+msg.getText());
                        break;  
                          
                    case VALUE_RIGHT_IMAGE:  
                    	addBitmapToMemoryCache(position+"",msg.getSmallImage());
                        convertView = mInflater.inflate(R.layout.list_item_right_iamge, null);  
                        holder.tvRightNickname = (TextView)convertView.findViewById(R.id.tv_nickname);  
                        holder.ivRightImage = (ImageView)convertView.findViewById(R.id.iv_right_image);  
                        holder.tvRightNickname.setText("Me");
                        holder.ivRightImage.setImageBitmap(getBitmapFromMemoryCache(position+""));  
                        break;  
                    case VALUE_USER_MESSAGE:
                    	convertView = mInflater.inflate(R.layout.list_user_message_text, null);  
                        holder.tvUserMessage = (TextView)convertView.findViewById(R.id.tv_user_message);  
                        holder.tvUserMessage.setText(msg.getText());
                    default:  
                        break;  
                }  
                convertView.setTag(holder);  
		} else {
			Log.i("positon", "convertView!=null position is :" + position);
			holder = (ViewHolder) convertView.getTag();
			switch (type) {
	        //���  
            case VALUE_LEFT_TEXT:  

                holder.tvLeftNickname.setText(msg.getNickname());
                holder.btnLeftText.setText(msg.getText());  
                break;  
                  
            case VALUE_LEFT_IMAGE:  
 
            	holder.tvLeftNickname.setText(msg.getNickname());
				Bitmap bitmap = getBitmapFromMemoryCache(position+"");
				if (bitmap == null) 
					addBitmapToMemoryCache(position+"",msg.getSmallImage());
                holder.ivLeftImage.setImageBitmap(getBitmapFromMemoryCache(position+""));  
                break;  
			case VALUE_RIGHT_TEXT: 
				
                holder.tvRightNickname.setText("Me");
                holder.btnRightText.setText(msg.getText()); 
                Log.i("message righttext","righttext is :"+msg.getText());
                break;  
                
			case VALUE_RIGHT_IMAGE:
			
				holder.tvRightNickname.setText("Me");
				Bitmap bitmap2 = getBitmapFromMemoryCache(position+"");
				if (bitmap2 == null) {
					addBitmapToMemoryCache(position+"",msg.getSmallImage());
					holder.ivRightImage.setImageBitmap(getBitmapFromMemoryCache(position+""));
				}
				break;
			case VALUE_USER_MESSAGE:
				holder.tvUserMessage.setText(msg.getText());
				break;
			default:
				break;
			}

		}
            return convertView;  
        }  
      
    
    /**
	 * ��ImageView����ͼƬ�����ȴ�LruCache��ȡ��ͼƬ�Ļ��棬���õ�ImageView�ϡ����LruCache��û�и�ͼƬ�Ļ��棬
	 * �͸�ImageView����һ��Ĭ��ͼƬ��
	 * 
	 * @param imageUrl
	 *            ͼƬ��URL��ַ��������ΪLruCache�ļ���
	 * @param imageView
	 *            ������ʾͼƬ�Ŀؼ���
	 */
	private void setImageView(String imageUrl, ImageView imageView) {
		Bitmap bitmap = getBitmapFromMemoryCache(imageUrl);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			imageView.setImageResource(R.drawable.empty_photo);
		}
	}

	/**
	 * ��һ��ͼƬ�洢��LruCache�С�
	 * 
	 * @param key
	 *            LruCache�ļ������ﴫ��ͼƬ��URL��ַ��
	 * @param bitmap
	 *            LruCache�ļ������ﴫ������������ص�Bitmap����
	 */
	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
		if (getBitmapFromMemoryCache(key) == null) {
			mMemoryCache.put(key, bitmap);
		}
	}

	/**
	 * ��LruCache�л�ȡһ��ͼƬ����������ھͷ���null��
	 * 
	 * @param key
	 *            LruCache�ļ������ﴫ��ͼƬ��URL��ַ��
	 * @return ��Ӧ�������Bitmap���󣬻���null��
	 */
	public Bitmap getBitmapFromMemoryCache(String key) {
		return mMemoryCache.get(key);
	}
	
	
    /** 
     * ��������Դ��position������Ҫ��ʾ�ĵ�layout��type 
     *  
     * */  
    @Override  
    public int getItemViewType(int position) {  
          
        ChatMessage msg = myList.get(position);  
        int type = msg.getType();  
        Log.e("TYPE:", ""+type);  
        return type;  
    }  
  
    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

		/**
		 * ͼƬ��URL��ַ
		 */
		private String imageUrl;

		@Override
		protected Bitmap doInBackground(String... params) {
			String bitmapString = params[0];
			Bitmap bitmap = stringtoBitmap(bitmapString);
			if (bitmap != null) {
				// ͼƬ������ɺ󻺴浽LrcCache��
				addBitmapToMemoryCache(params[0], bitmap);
			}
			return bitmap;
		}

//		@Override
//		protected void onPostExecute(Bitmap bitmap) {
//			super.onPostExecute(bitmap);
//			 ����Tag�ҵ���Ӧ��ImageView�ؼ��������غõ�ͼƬ��ʾ������
//			ImageView imageView = (ImageView) mPhotoWall.findViewWithTag(imageUrl);
//			if (imageView != null && bitmap != null) {
//				imageView.setImageBitmap(bitmap);
//			}
//			taskCollection.remove(this);
//		}

	}
    
    /** 
     * �������е�layout������ 
     *  
     * */  
    @Override  
    public int getViewTypeCount() {  
        return 5;  
    }  
      
    class ViewHolder{  
          
        private TextView tvLeftNickname;//��ߵ�ͷ��  
        private Button btnLeftText;//��ߵ��ı�  
        private ImageView ivLeftImage;//��ߵ�ͼ��  
          
        private TextView tvRightNickname;//�ұߵ�ͷ��  
        private Button btnRightText;//�ұߵ��ı�  
        private ImageView ivRightImage;//�ұߵ�ͼ��  

        private TextView tvUserMessage;
    }  
  
	public Bitmap stringtoBitmap(String string) {
		// ���ַ���ת����Bitmap����
		Bitmap bitmap = null;
		try {
			byte[] bitmapArray;
			bitmapArray = Base64.decode(string.getBytes(), Base64.DEFAULT);
			bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0,
					bitmapArray.length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	
}  