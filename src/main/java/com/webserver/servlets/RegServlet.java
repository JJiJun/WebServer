package com.webserver.servlets;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;

import com.webserver.http.HttpRequest;
import com.webserver.http.HttpResponse;

/**
 * Servlet是JAVA EE标准定义的内容
 * @author 1214
 *
 */
public class RegServlet extends HttpServlet{
	public void service(HttpRequest request,HttpResponse response){
		System.out.println("开始处理注册！");
		/*
		 * 处理注册流程
		 * 1.通过request获取用户用户表单提交上来的注册用户信息
		 * 2.将该信息写入到文件user.dat中
		 * 3.设置response对象，将注册成功页面响应给客户端
		 */
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String nickname = request.getParameter("nickname");
		int age = Integer.parseInt(request.getParameter("age"));
		System.out.println(username+password+nickname+age);
		/*
		 * 2将注册信息写入到user.dat文件，每条记录占用100字节，其中，用户名，密码，昵称为字符串，各占32字节，年龄为int占用4字节
		 */
		try(RandomAccessFile  raf = new RandomAccessFile("user.dat", "rw");){
			raf.seek(raf.length());
			//a.先将用户名转换为一组字节
			byte[] data = username.getBytes("utf-8");
			//b.将转换的字节数组扩容到32个字节
			data = Arrays.copyOf(data, 32);
			//c.将字节数组写入文件
			raf.write(data);
			
			data = password.getBytes("utf-8");
			data = Arrays.copyOf(data, 32);
			raf.write(data);
			
			data = nickname.getBytes("utf-8");
			data = Arrays.copyOf(data, 32);
			raf.write(data);
			
			raf.writeInt(age);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		//3
		response.setEntity(new File("webapps/myweb/reg_success.html"));
		System.out.println("处理注册完毕！");
	}
}
