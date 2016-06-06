package org.fs.tip.server.utils;

import java.net.InetAddress;
import java.net.Socket;

public class Client {
	private int portNumber;
	private InetAddress ip;
	private String nickname;
	private Socket socket;
    private Channel channel;
	
	public Client(int portNumber, InetAddress ip, String nickname, Socket socket, Channel channel) {
		this.portNumber = portNumber;
		this.ip = ip;
		this.nickname = nickname;
		this.socket = socket;
        this.channel = channel;
	}

    public Socket getSocket() {
        return socket;
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

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }
}
