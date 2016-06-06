package org.fs.tip.server.utils;

import java.net.InetAddress;

public class Client {
	private int portNumber;
	private InetAddress ip;
	private String nickname;
	
	public Client(int portNumber, InetAddress ip, String nickname) {
		this.portNumber = portNumber;
		this.ip = ip;
		this.nickname = nickname;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public InetAddress getIp() {
		return ip;
	}

	public String getNickname() {
		return nickname;
	}
}
