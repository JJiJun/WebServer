package com.webserver.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.webserver.core.ServerContext;

/**
 * 请求对象
 * HttpRequest的每一个实例用于表示客户端发送过来的一个具体的请求内容。
 * 一个请求分为三部分构成：请求行，请求头，消息正文
 * @author 1214
 *
 */
public class HttpRequest {
	/*
	 * 请求行相关信息定义
	 */
	//请求方式
	private String method;
	//请求资源路径
	private String url;
	//协议版本
	private String protocol;
	//url中的请求部分      url中“？”左侧内容
	private String requestURI;
	//url中的参数部分      url中“？”右侧内容
	private String queryString;
	//所有参数
	private Map<String,String> parameters = new HashMap<String,String>();
	/*
	 * 消息头相关定义
	 */
	private Map<String,String> headers = new HashMap<String,String>();
 	/*
	 * 消息头相关信息定义
	 */
	
	/*
	 * 消息正文相关信息定义
	 */
	
	/*
	 * 构造方法，用来初始化HttpRequest
	 */
	//对应客户端的socket
	private Socket socket;
	//用于读取客户端发送过来消息的输入流
	private InputStream in;
	public HttpRequest(Socket socket) throws EmptyRequestException{
		try{
			this.socket = socket;
			this.in = socket.getInputStream();
			/*
			 * 1.解析请求行
			 * 2.解析消息头
			 * 3.解析消息正文
			 */
			parseRequestLine();
			parseHeaders();
			parseContent();
			
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/*
	 * 解析请求行
	 */
	private void parseRequestLine() throws EmptyRequestException{
		System.out.println("解析请求行...");
		try{
			String line = readLine();
			System.out.println("请求行:"+line);
			/*
			 * 解析请求行的步骤：1，将请求行内容按照空格拆分为三部分，2，分别将三部分内容设置到对应的属性上method，url，protocol
			 * 这里将来会抛出数组下标越界，原因在于HTTP协议中越有所提及，允许客户端连接后发送空请求（实际是什么也没发过来）。这个
			 * 时候若解析请求行是拆分不了三项的。后面遇到在解决
			 */
			String[] data = line.split("\\s");
			if(data.length<3){
				//空请求
				throw new EmptyRequestException();
			}
			method = data[0];
			url = data[1];
			parseUrl();
			protocol = data[2];
			
			System.out.println("method:"+method);
			System.out.println("url:"+url);
			System.out.println("protocol:"+protocol);
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("解析请求完毕！");
	}
	/*
	 * 进一步解析url部分
	 */
	private void parseUrl(){
		/*
		 * 首先对url进行转码，将含有的%XX内容转换为对应的字符
		 */
		try {
			this.url = URLDecoder.decode(url, ServerContext.URIEncoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		/*
		 * 1.先判断当前url是否含有参数部分（是否含有？）
		 * 若没有参数部分，则直接将url赋值给requestURI。含有？才进行下一步操作
		 * 2.按照“？”将url拆分为两部分。将？之前的内容设置到属性requestURI上，将？后面的内容设置到属性queryString上
		 * 3.将queryString内容进行进一步解析，首先按照“&”拆分出每一个参数。然后再讲每个参数按照“=”拆分为参数名
		 * 与参数值，并put到属性parameters这个Map中。
		 */
		if(this.url.indexOf('?')==-1){
			this.requestURI = this.url;
		}else{
			String[] data = this.url.split("\\?");
			this.requestURI = data[0];
			/*
			 * 这里根据？拆分之后之所以要判断数组长度是否>1，原因在于，有的请求发送过来可能如下：/myweb/reg?
			 * ?之后实际没有带任何参数。（页面form表单中所有的输入域都没有指定name属性时就会出现这样的情况）如果
			 * 不判断，可能会出现下标越界的情况
			 */
			if(data.length>1){
				this.queryString = data[1];
				parseParameters(this.queryString);
			}
		}
		System.out.println("requestURI:"+requestURI);
		System.out.println("queryString:"+queryString);
		System.out.println("parameters:"+parameters);
	}
	/*
	 * 解析参数部分：该内容格式为name=value&name=value&.....
	 */
	private void parseParameters(String paraLine){
		String[] line = paraLine.split("&");
		for(String para : line){
			String[] parameter = para.split("=");
			/*
			 * 这里判断parameter.length>1的原因是因为，如果在表单中某个输入框没有值，那么传递过来的数据回是这样：
			 * myweb/reg?username=&password=123&.....
			 * 像用户名这样，如果没有输入，=右边是没有内容的，拆分后不判断数组长度会出现下标越界的情况
			 */
			if(parameter.length>1){
				String key = parameter[0];
				String value = parameter[1];
				this.parameters.put(key, value);
			}else{
				this.parameters.put(parameter[0], null);
			}
		}
	}
	/*
	 * 解析消息头
	 */
	private void parseHeaders(){
		try{
			/*
			 * 循环调用readLine方法读取每一行字符串，如果读到的字符串是空字符串则表示单独读取到了CRLF，那么表示消息头部分 读取完毕，停止循环即可。
			 * 否则读取一行字符串后应当是一个消息内容，接下来讲给字符串按照“：”拆分为两项，第一项是消息头的名字，第二项为对应的值，存入属性headers即可
			 */
			System.out.println("解析消息头...");
			while(true){
				String line = readLine();
				if("".equals(line)){
					break;
				}
				String[] data = line.split(": ");
				headers.put(data[0],data[1]);
			}
			System.out.println(headers);
			System.out.println("解析请求完毕！");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * 解析消息正文
	 */
	private void parseContent(){
		System.out.println("解析消息正文...");
		/*
		 * 根据消息头中是否含有Content-Length来判断当前请求中是否含有消息正文
		 */
		if(headers.containsKey("Content-Length")){
			//取得消息正文的长度
			int length = Integer.parseInt(headers.get("Content-Length"));
			//根据长度读取消息正文的内容
			try{
				byte[] data = new byte[length];
				in.read(data);
				//根据消息头Content-Type判断正文内容
				String ContentType = headers.get("Content-Type");
				//判断正文类型是否为form表单提交的数据
				if("application/x-www-form-urlencoded".equals(ContentType)){
					//将消息正文字节转换为字符串（原GET请求地址栏“？”右侧内容
					
					String line = new String (data,"ISO8859-1");
					try{
						line = URLDecoder.decode(line,ServerContext.URIEncoding);
						
					}catch (Exception e) {
						e.printStackTrace();
					}
					parseParameters(line);
				}
				//将来还可以添加其他分支，判断其他种类数据
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		
		System.out.println("解析请求完毕！");
	}
	
	/*
	 * 读取一行字符串，结束时以连续读取到了CRLF符号为止返回的字符串中不含有最后读取到的CRLF
	 * 
	 */
	private String readLine() throws IOException{
		StringBuilder builder = new StringBuilder();
		int d = -1;
		//c1表示上次读取到的字符，c2表示本次读取到的字符
		char c1 = 'a',c2 = 'a';
		while((d=in.read())!=-1){
			c2 = (char)d;
			//判断是否读取到了CRLF
			if(c1==HttpContext.CR&&c2==HttpContext.LF){
				break;
			}
			builder.append(c2);
			c1 = c2;
		}
		return builder.toString().trim();
	}
	
	
	public String getMethod() {
		return method;
	}
	public String getUrl() {
		return url;
	}
	public String getProtocol() {
		return protocol;
	}
	public String getRequestURI() {
		return requestURI;
	}
	public String getQueryString() {
		return queryString;
	}
	/**
	 * 根据参数名获取对应的参数值
	 * @param name
	 * @return
	 */
	public String getParameter(String name){
		return this.parameters.get(name);
	}
}
