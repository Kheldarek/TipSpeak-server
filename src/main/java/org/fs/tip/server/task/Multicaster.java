package org.fs.tip.server.task;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.List;

import org.fs.tip.server.utils.Client;

public class Multicaster implements Runnable {
	private List<Client> clients;
	private DatagramPacket receivedPacket;
	
	public Multicaster(List<Client> clients, DatagramPacket packet) {
		this.clients = clients;
		this.receivedPacket = packet;
	}

	@Override
	public void run() {
		for(Client client : clients) {
			if(!client.getIp().equals(receivedPacket.getAddress())) {
				try {
					sendData(client);
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void sendData(Client client) throws IOException {
		try(DatagramSocket datagramSocket = new DatagramSocket()){
			byte[] buffer = receivedPacket.getData();
			DatagramPacket toSendPacket = new DatagramPacket(buffer, buffer.length, client.getIp(), client.getPortNumber());
			datagramSocket.send(toSendPacket);
		}
	}

}
