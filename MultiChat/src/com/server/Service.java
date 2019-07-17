package com.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Service {
	private ChatServer server;
	private SocketChannel socketChannel;
	String UserName;
	
	public SocketChannel getsocketChannel() {
		return this.socketChannel;
	}

	public Service(ChatServer server, SocketChannel socketChannel) {
		this.server = server;
		this.socketChannel = socketChannel;
	}

	public void sendMessage(String str) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer = ByteBuffer.wrap(str.getBytes());
		if (str != null)
			socketChannel.write(buffer);
		buffer.clear();
	}

	public String readMessage() throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		socketChannel.read(buffer);
		String output = new String(buffer.array()).trim();
		buffer.clear();
		return output;
	}

	public void setName() {
		
		try {
			String input;
			if ((input = readMessage()) != null) {
				UserName = input;
				sendMessage(UserName);
				server.sendMessageAll("# " + UserName + " 님이 들어오셨습니다.");
			}

		} catch (IOException e) {
			System.err.println("닉네임 생성 오류");
			//e.printStackTrace();
		}

	}

	public void Chat() {
		try {
			
			String inputLine;
			if ((inputLine = readMessage()) != null) {
				server.sendMessageAll("[" + UserName + "]" + inputLine);
			}

		} catch (IOException e) {
			server.removeClient(this);
			server.sendMessageAll("# " + UserName + " 님이 나가셨습니다.");
			System.out.println("클라이언트 : " + socketChannel + " 에서 접속이 끊겼습니다...");
		}

	}

}
