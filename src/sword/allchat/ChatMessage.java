package sword.allchat;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Base64;

public class ChatMessage {  
  
    private int type;//指定是哪种类型  
    private String value;//值  
    //JSONObject jsonObject = new JSONObject(); 
    JSONObject item;
    String message="";
    String nickname="";
    String text="";
    Bitmap bitmap;
    String bitmapString;
    
    public int getType() {  
        return type;  
    }  
    public void setType(int type) {  
        this.type = type;  
    }  
    public String getValue() {  
        return value;  
            }  
    
    public String getMessage(){
    	return message;
    }
    
    public void setValue(JSONObject value) {  
        //this.value = value;
        //message=this.value;
    	item=value;
		//item = new JSONObject(message);
		nickname=item.optString("nickname", "");
		if(item.optInt("type",-1)==0)
			text=item.optString("text", "");
		if(item.optInt("type",-1)==1)
			bitmapString = item.optString("image", "");
			//bitmap = stringtoBitmap(bitmapString);
		if(item.optInt("type",-1)==2)
			text=item.optString("text", "");
        
    }  
    public String getNickname()
    {
    	return nickname;
    }
    public String getText()
    {
    	return text;
    }
    
	public Bitmap getBitmap() {
		// TODO Auto-generated method stub
		return stringtoBitmap(bitmapString);
	}
	
	public Bitmap getLargeImage()
	{
		return stringtoBitmap(bitmapString);
	}
	
	public Bitmap getSmallImage()
	{
		Bitmap bitmap = stringtoBitmap(bitmapString);
		return bitmap;
		//return ThumbnailUtils.extractThumbnail(bitmap, 300, 300 , 0x2);
	}
	
    
	/**
	 * 将base64转换成bitmap图片
	 * 
	 * @param string
	 *            base64字符串
	 * @return bitmap
	 */
	public Bitmap stringtoBitmap(String string) {
		// 将字符串转换成Bitmap类型
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