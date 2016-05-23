package org.fs.tip.server.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.fs.tip.server.task.Multicaster;

public class Channel implements Runnable {
	private int bufferLength;
	private int port;
	private String name;
	private List<Client> clients;
	private boolean isListining;
	private ExecutorService exec;
	
	public Channel(int port, String name, int bufferLength, ExecutorService exec) {
		this.exec = exec;
		this.bufferLength = bufferLength;
		this.port = port;
		this.name = name;
		clients = new ArrayList<>(5);
		isListining = true;
	}

	@Override
	public void run() {
		try {
			openSocket();
		} catch(SocketException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void openSocket() throws IOException, SocketException {
		try(DatagramSocket datagramSocket = new DatagramSocket(port)) {
			listen(datagramSocket);
		}
	}

	private void listen(DatagramSocket datagramSocket) throws IOException {
		while(isListining) {
			byte[] buffer = new byte[bufferLength];
			DatagramPacket packet = new DatagramPacket(buffer, bufferLength);
			datagramSocket.receive(packet);
			exec.execute(new Multicaster(clients, packet));
		}
	}
	
	public synchronized void shutdown() {
		isListining = false;
	}
	
	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

}
