package com.webserver.demo;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 读出user.dat中的数据
 * @author 1214
 *
 */
public class ShowAllOfUsers {
	public static void main(String[] args) throws IOException {
		RandomAccessFile raf = new RandomAccessFile("user.dat", "r");
		byte[] data = new byte[32];
		for(int i=0; i<raf.length()/100; i++){
			raf.read(data);
			String username = new String(data,"UTF-8").trim();
		
			raf.read(data);
			String password = new String(data,"UTF-8").trim();
		
			raf.read(data);
			String nickname = new String(data,"UTF-8").trim();
		
			int age = raf.readInt();
			
			System.out.println("姓名:"+username+"  密码:"+password+"  昵称:"+nickname+"  年龄:"+age);
		}
		raf.close();	
	}
}
