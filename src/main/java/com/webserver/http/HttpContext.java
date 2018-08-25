package com.webserver.http;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


/**
 * Http协议相关信息定义
 * @author 1214
 *
 */

public class HttpContext {
	//ASC 中对应的回车符
	public static final int CR = 13;
	//ASC 中对应的换行符
	public static final int LF = 10;
	/*
	 * 状态码与描述之间的关系        key:状态码           value:状态描述
	 */
	private static Map<Integer,String> statusCode_Reason_Mapping = new HashMap<Integer,String>();
	/*
	 * 介质类型的映射            key：资源后缀名            value：Content-Type对应的值
	 */
	private static Map<String,String> mimeMapping = new HashMap<String,String>();
	static{
		initStatusCodeReasonMapping();
		initMimeMapping();
	}
	private static void initStatusCodeReasonMapping(){
		statusCode_Reason_Mapping.put(200, "OK");
		statusCode_Reason_Mapping.put(201, "Created");
		statusCode_Reason_Mapping.put(202, "Accepted");
		statusCode_Reason_Mapping.put(204, "No Content");
		statusCode_Reason_Mapping.put(301, "Moved Permanently");
		statusCode_Reason_Mapping.put(302, "Moved Temporarily");
		statusCode_Reason_Mapping.put(304, "Not Modified");
		statusCode_Reason_Mapping.put(400, "Bad Request");
		statusCode_Reason_Mapping.put(403, "Forbidden");
		statusCode_Reason_Mapping.put(404, "Not Found");
		statusCode_Reason_Mapping.put(500, "Internal Server Error");
		statusCode_Reason_Mapping.put(501, "Not Implemented");
		statusCode_Reason_Mapping.put(502, "Bad Gateway");
		statusCode_Reason_Mapping.put(503, "rvice Unavailable");
	}
	private static void initMimeMapping(){
//		mimeMapping.put("html", "text/html");
//		mimeMapping.put("css", "text/css");
//		mimeMapping.put("js", "application/javascript");
//		mimeMapping.put("jpg", "image/jpeg");
//		mimeMapping.put("gif", "image/gif");
		/*
		 * 通过解析conf/web.xml文件来完成初始化操作
		 *1. 将web.xml文档中跟标签下所有名为<mime-mapping>的子标签解析出来
		 *2.并将其对应的两个子标签：<extension>中间的文本作为key   <mime-type>中间的文本作为value
		 *  来初始化mimeMapping这个Map
		 */
		
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(new FileInputStream("conf/web.xml"));
			
			List<Element> list = document.getRootElement().elements("mime-mapping");
			for(Element mime_Mapping : list){
				String extension = mime_Mapping.elementText("extension");
				String mime_type = mime_Mapping.elementText("mime-type");
				mimeMapping.put(extension, mime_type);
			}
//			Set<Entry<String,String>> entrySet = mimeMapping.entrySet();
//			for(Entry<String,String> entity : entrySet){
//				String key = entity.getKey();
//				String value = entity.getValue();
//				System.out.println(key+"="+value);
//			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public static String getStatusReason(int statusCode){
		return statusCode_Reason_Mapping.get(statusCode);
	}
	public static String getContentType(String ext){
		return mimeMapping.get(ext);
	}
	public static void main(String[] args) {
		System.out.println(mimeMapping.size());
	}
}
