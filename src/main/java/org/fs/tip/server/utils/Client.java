package org.fs.tip.server.utils;

import java.net.InetAddress;

public class Client {
	private int portNumber;
	private InetAddress ip;
	
	public Client(int portNumber, InetAddress ip) {
		this.portNumber = portNumber;
		this.ip = ip;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public InetAddress getIp() {
		return ip;
	}
		
}
