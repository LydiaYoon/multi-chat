package com.client;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ChatClient extends Frame implements Runnable {

	InetSocketAddress hostAddress = null;
	SocketChannel client = null;

	TextArea outputArea;
	TextField inputField;

	public ChatClient(String title) {
		super(title);

		setLayout(new BorderLayout());

		outputArea = new TextArea();
		outputArea.setEditable(false);

		add(outputArea, "Center");
		inputField = new TextField();
		add(inputField, "South");

		inputField.addActionListener(new InputListener());
	}

	public void addMessage(String msg) {
		outputArea.append(msg);
	}
	
	public void connect(String host, int port) {
		try {
	
			hostAddress = new InetSocketAddress(host, port);
			client = SocketChannel.open(hostAddress);
	
		} catch (Exception e) {
			System.err.println("입출력 에러입니다.");
			e.printStackTrace();
			System.exit(1);
		}

	}

	public void disconnect() {
		try {
			client.close();
		
		} catch (IOException e) {
		}
	}

	public void run() {
		try {
			while (true) {
				ByteBuffer buffer = ByteBuffer.allocate(256);
				client.read(buffer);
				String output = new String(buffer.array()).trim();

				addMessage(output + "\n");
				buffer.clear();
			}
		} catch (IOException e) {
			disconnect();
		}
	}

	public static void main(String[] args) throws IOException {

		ChatClient mf = new ChatClient("자바 채팅 클라이언트");

		mf.pack();
		mf.setSize(500, 300);
		mf.setVisible(true);

		mf.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		mf.connect("127.0.0.1", 9999);

		Thread thread = new Thread(mf);
		thread.start();

	}

	class InputListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			String input = inputField.getText();
			inputField.setText("");
			try {
				byte[] message = new String(input).getBytes();
				ByteBuffer buffer = ByteBuffer.wrap(message);
				client.write(buffer);
		
				buffer.clear();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

}