package com.ifeng.common.misc;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

/**

  @author :chenyong
  @version 1.0
  @date 2012-2-9
 */

public class CoderUtil {
	
	public static String object2String(Object obj){
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(bout);
		encoder.writeObject(obj);
	    encoder.flush();
	    encoder.close();
	    String str = new String(bout.toByteArray(),Charset.forName("utf8"));
		return str;
	}
	
	public static Object string2Object(String str){		
		if(null == str){
			return null;
		}
		InputStream fis = new ByteArrayInputStream(str.getBytes(Charset.forName("utf8")));
		XMLDecoder decoder = new XMLDecoder(fis);
		Object obj = null;
		try{
			obj=decoder.readObject();
		}catch(Exception e){
			throw new RuntimeException("xml string format is wrong");
		}
		return obj;		
	}

}
