package com.webserver.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.print.attribute.ResolutionSyntax;

import com.webserver.http.EmptyRequestException;
import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;
import com.webserver.servlets.HttpServlet;
import com.webserver.servlets.LoginServlet;
import com.webserver.servlets.RegServlet;

/**
 * 处理客户端请求
 * @author 1214
 *
 */
public class ClientHandler implements Runnable{
	private Socket socket;
	public ClientHandler(Socket socket){
		this.socket = socket;
	}
	
	public void run(){
		try{
			/*
			 * 1.准备工作
			 * 2.处理请求
			 * 3.响应客户端
			 */
			/*
			 * 1.
			 */
			HttpRequest request = new HttpRequest(socket);
			HttpResponse response = new HttpResponse(socket);
			/*
			 * 2.处理请求
			 * 根据请求的资源路径，从webapps目录中找到对应的资源，若存在则将该资源给客户端
			 * 若没有找到资源则相应404页面给用户 
			 */
			String url = request.getRequestURI();
			
			//首先判断请求是否请求一个业务处理
			//是否处理注册
			String servletName = ServerContext.getServletName(url);
			if(servletName!=null){
				//利用反射实例化该Servlet
				System.out.println("利用反射加载类");
				Class cls  = Class.forName(servletName);
				//实例化
				HttpServlet servlet = (HttpServlet)cls.newInstance();
				servlet.service(request, response);
			}else{
				File file = new File("webapps"+url);
				if(file.exists()){
					System.out.println("该资源已找到！");
					response.setEntity(file);
					System.out.println("响应完毕！");
				}else{
					System.out.println("该资源不存在！");
					response.setStatusCode(404);
					response.setEntity(new File("webapps/root/404.html"));
				}
			}
			
			/*
			 * 3.响应客户端
			 */
			response.fluse();
			
		}catch (EmptyRequestException e) {
	
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			//处理与客户端断开连接的操作
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
