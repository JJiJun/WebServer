package com.webserver.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.webserver.core.ServerContext;

/**
 * 相应对象
 * 一个相应对象应当包含三部分：
 * 状态行           响应头              响应正文
 * @author 1214
 *
 */
public class HttpResponse {
	/*
	 * 状态行相关信息
	 */
	//状态行相关信息
	private int statusCode = 200;
	//状态描述
	private String statusReason = "OK";
	/*
	 * 响应头相关信息
	 */
	private Map<String,String> headers = new HashMap<String,String>();
	
	/*
	 * 响应正文相关信息
	 */
	//响应实体文件
	private File entity;
	
	private Socket socket;
	//通过Socket获取的输出流，用于给客户端发送响应内容
	private OutputStream out;
	
	public HttpResponse(Socket socket) {
		try{
			this.socket = socket;
			this.out = socket.getOutputStream();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * 将当前相应对象内容发送给客户端
	 */
	public void fluse(){
		/*
		 * 1.发送状态行
		 * 2.发送响应头
		 * 3.发送响应正文
		 */
		sendStatusLine();
		sendSHeaders();
		sentContent();
	}
	//1.发送状态行
	private void sendStatusLine(){
		try {
			String line = ServerContext.protocol+" "+statusCode+" "+statusReason;
			println(line);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//2.发送响应头
	private void sendSHeaders(){
		try{
			Set<Entry<String,String>> entrySet = headers.entrySet();
			for(Entry<String,String> header : entrySet){
				String key = header.getKey();
				String value = header.getValue();
				String line = key+": "+value;
				println(line);
			}
			//单独发送一个CRLF表示响应头发送完毕
			println("");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	//3.发送响应正文
	private void sentContent(){
		if(entity!=null){
			try(FileInputStream fis = new FileInputStream(entity)){
				int len = -1;
				byte[] data = new byte[1024*10];
				while((len=fis.read(data))!=-1){
					out.write(data, 0, len);
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public File getEntity() {
		return entity;
	}
	
	/*
	 *向客户端发送一行字符串，该字符串发送后会会单独发送CR，LF
	 */
	private void println(String line){
			try {
				out.write(line.getBytes("ISO8859-1"));
				out.write(HttpContext.CR);
				out.write(HttpContext.LF);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	/*
	 * 设置要响应给客户端的实体资源文件，在设置的同时会自动添加两个响应头：Content-Type和Content-Length
	 */
	public void setEntity(File entity) {
		this.entity = entity;
		this.headers.put("Content-Length", entity.length()+"");
		/*
		 * 设置Content-Type时，要先根据文件名的后缀得到对应的值
		 */
		String fileName = entity.getName();
		int index = fileName.lastIndexOf(".")+1;
		String ext = fileName.substring(index);
		String contentType = HttpContext.getContentType(ext);
		this.headers.put("Content-Type",contentType);
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
		this.statusReason = HttpContext.getStatusReason(statusCode);
	}
	public String getStatusReason() {
		return statusReason;
	}
	public void setStatusReason(String statusReason) {
		this.statusReason = statusReason;
	}
	/*
	 * 向当前响应中设置一个响应头信息（后期自行重构时，还会添加获取头，以及删除头的操作）
	 * @param name
	 * @param value
	 */
	public void putHeader(String name,String value){
		this.headers.put(name, value);
	}
}
