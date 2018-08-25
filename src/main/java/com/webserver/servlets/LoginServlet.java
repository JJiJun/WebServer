package com.webserver.servlets;

import java.io.File;
import java.io.RandomAccessFile;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

public class LoginServlet extends HttpServlet{
	public void service(HttpRequest request,HttpResponse response){
		System.out.println("开始处理登录");
		System.out.println("处理登录完成");
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		System.out.println(username+password);
		try(RandomAccessFile raf = new RandomAccessFile("user.dat", "r")){
			byte[] data = new byte[32];
			boolean flag = false;
			for(int i=0; i<raf.length()/100; i++){
				raf.seek(100*i);
				raf.read(data);
				String name = new String(data,"UTF-8").trim();
				raf.read(data);
				String pasword = new String(data,"UTF-8").trim();
				System.out.println(name+pasword);
				
				if(name.equals(username)&&pasword.equals(password)){
					flag = true;
					break;
				}
			}
			response.setEntity(flag==true?new File("webapps/myweb/login_success.html"):new File("webapps/myweb/login_fail.html"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
