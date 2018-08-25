package com.webserver.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/*
 * 服务器环境信息
 */
public class ServerContext {
	public static String protocol = "HTTP/1.1";
	public static int port = 8080;
	public static String URIEncoding = "UTF-8";
	public static int maxThreads = 150;
	public static Map<String,String> servletMapping = new HashMap<String,String>();
	
	
	static{
		init();
		initServletMapping();
	}
	
	/*
	 * 解析conf/server.xml，将所有配置项用于初始化ServerContent对应属性
	 */
	public static void init(){
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File("conf/server.xml"));
			Element ConEle = document.getRootElement().element("Connector");
			protocol = ConEle.attributeValue("protocol");
			port = Integer.parseInt(ConEle.attributeValue("port"));
			URIEncoding = ConEle.attributeValue("URIEncoding");
			maxThreads = Integer.parseInt(ConEle.attributeValue("maxThreads"));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void initServletMapping(){
		try{
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File("conf/servlets.xml"));
			List<Element> list = document.getRootElement().elements("servlet");
			for(Element serEle : list){
				String key = serEle.attributeValue("url");
				String value = serEle.attributeValue("className");
				servletMapping.put(key,value);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String getServletName(String url){
		return servletMapping.get(url);
	}
}
