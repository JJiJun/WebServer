package com.webserver.core;


import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer主类
 * @author 1214
 *
 */
public class WebServer {
	private ServerSocket server;
	//管理用于处理客户端请求的线程
	private ExecutorService threadPool;
	/*
	 * 构造方法，用来初始化服务端
	 */
	public WebServer() {
		try{
			System.out.println("正在启动服务端...");
			server = new ServerSocket(ServerContext.port);
			threadPool = Executors.newFixedThreadPool(ServerContext.maxThreads);
			System.out.println("服务端启动完毕！");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * 服务端启动方法
	 */
	public void start(){
		try{
			/*
			 * 循环接收客户端请求的工作暂时不启动。测试阶段自接收一次请求
			 */
			while(true){
				System.out.println("等待客户端...");
				Socket socket = server.accept();
				System.out.println("一个客户端连接了！");
				//启动一个线程处理该客户端请求
				ClientHandler handler = new ClientHandler(socket);
				
				threadPool.execute(handler);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		WebServer server = new WebServer();
		server.start();
	}
}
