package com.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class ChatServer {
	Vector<Service> Clients; // 클라이언트 객체를 저장하는 곳

	public ChatServer() {
		Clients = new Vector<>();
	}

	public Service getClient(SocketChannel socketChannel) {
		for(Service s : Clients) {
			if(s.getsocketChannel().equals(socketChannel))
				return s;
		}
		
		return null;
	}

	public void addClient(Service s) { // 클라이언트가 접속하면 추가
		Clients.add(s);
	}

	public void removeClient(Service s) { // 클라이언트가 종료되면 삭제
		Clients.remove(s);
	}

	public void sendMessageAll(String str) { // 모든 클라이언트에게 받은 메시지를 출력
		try {
			for (Service s : Clients) {
				s.sendMessage(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		ChatServer server = null;
		Selector selector = null;
		ServerSocketChannel serverSocket = null;
		try {
			server = new ChatServer(); // 클라이언트를 관리하는 객체(추가, 삭제, 메시지 전달)
			selector = Selector.open();
			serverSocket = ServerSocketChannel.open();
			InetSocketAddress hostAddress = new InetSocketAddress("127.0.0.1", 9999);
			serverSocket.bind(hostAddress);
			serverSocket.configureBlocking(false);

			int ops = serverSocket.validOps();
			serverSocket.register(selector, ops, null);

		} catch (IOException e) {
			System.err.println("서버 생성 오류");
		}

		System.out.println("서버 : " + serverSocket + " 에서 연결을 기다립니다.");

		try {
			while (true) {
				
				try {
					selector.select();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				Set<SelectionKey> selectedKeys = selector.selectedKeys();
				Iterator<SelectionKey> iter = selectedKeys.iterator();
				
				while (iter.hasNext()) {
					SelectionKey ky = iter.next();
					if (ky.isAcceptable()) {
						try {
							SocketChannel client = serverSocket.accept(); // 클라이언트 접속
							
							client.configureBlocking(false);
							client.register(selector, SelectionKey.OP_READ);
							Service s = new Service(server, client);
							System.out.println("클라이언트 : " + client + " 에서 접속하였습니다.");
							s.sendMessage("\nUsername : \n");
							server.addClient(s);
							
						} catch (IOException e) {
							System.err.println("클라이언트 접속 오류");
						}
						
					} else if (ky.isReadable()) {
						
						SocketChannel client = (SocketChannel) ky.channel();
						Service s = null;
						try {
							s = server.getClient(client);						
							if(s.UserName == null)
								s.setName();
							else
								s.Chat();
						} catch (NullPointerException e) {
							try {
								client.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
						
					}
					iter.remove();	
				}	
			}
		} catch (Exception e) {
			try {
				serverSocket.close(); // 서버종료
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		System.out.println("서버를 종료합니다.");
	}

}
