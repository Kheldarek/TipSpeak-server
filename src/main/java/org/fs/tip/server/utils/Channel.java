package org.fs.tip.server.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fs.tip.server.task.Multicaster;

public class Channel implements Runnable {
	private int bufferLength;
	private int port;
	private String name;
	private ArrayList<Client> clients;
	private boolean isListining;
	private ExecutorService exec;

	public Channel() {
		this(0, "" ,0, Executors.newCachedThreadPool(), new ArrayList<>());
	}
	
	public Channel(int port, String name, int bufferLength, ExecutorService exec, ArrayList<Client> clients) {
		this.exec = exec;
		this.bufferLength = bufferLength;
		this.port = port;
		this.name = name;
		this.clients = clients;
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

	private void openSocket() throws IOException {
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

    private void println(Client client) {
        System.out.println("Channel " + name + ": " + client.getNickname());
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

    public String getSimpleString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(port);
        return sb.toString();
    }

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(name).append("=").append(port).append(":");
		for (Client client : clients) {
			sb.append(client.getNickname()).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
