package org.fs.tip.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.fs.tip.server.utils.Channel;

public class Server implements Runnable {
	private ServerSocket server;
	private volatile boolean isRunning = true;
	private List<Channel> channels;
	private StringBuilder channelListMessage;
	private ExecutorService exec;
	
	public Server(int serverPort, List<Channel> channels, ExecutorService listenerService) throws IOException {
		server = new ServerSocket(serverPort);
		this.channels = channels;
		channelListMessage = new StringBuilder();
		this.exec = listenerService;
	}

	@Override
	public void run() {
		channels.forEach(channel -> {
			exec.execute(channel);
		});
		while(isRunning) {
			Socket client = null;
			try {
				client = server.accept();
				createChannelLisitMessage();
				sendChannelListLength(client);
				if(new String(getUserConfirmation(client)).equals("READY")) {
					sendChannelList(client);
					getUserChoose(client);
				}
			} catch(IOException ioe) {
				System.err.println("Error while working");
			}
		}
	}

	private void createChannelLisitMessage() {
		channels.forEach(channel -> {channelListMessage.append(channel.getPort()).append("=").append(channel.getName()).append("\n");});
	}

	private void sendChannelListLength(Socket client) throws IOException {
		if(client != null) {
			try(OutputStream clientStream = client.getOutputStream()) {
				byte[] length = ByteBuffer.allocate(4).putInt(channelListMessage.toString().getBytes().length).array();
				clientStream.write(length);
			}
		}
	}

	private byte[] getUserConfirmation(Socket client) throws IOException {
		byte[] confirmation = new byte[0];
		confirmation[0] = 0;
		if(client != null) {
			try(InputStream clientStream = client.getInputStream()) {
				confirmation = new byte[5];
				clientStream.read(confirmation);
			}
		}
		return confirmation;
	}

	private void sendChannelList(Socket client) throws IOException {
		if(client != null) {
			try(OutputStream clientStream = client.getOutputStream()) {
				clientStream.write(channelListMessage.toString().getBytes());
			}
		}
		
	}
	
	private void getUserChoose(Socket client) throws IOException {
		if(client != null) {
			try(InputStream clientStream = client.getInputStream()) {
				//Here Piotr send me information about channel and I add him to picked channel.
			}
		}
	}
	
	public synchronized void openChannel(int port, String channelName, int bufferSize) {
		Channel channel = new Channel(port, channelName, bufferSize, exec);
		channels.add(channel);
		exec.execute(channel);
	}
	
	public synchronized void closeChannel(int port) {
		channels.remove(port + "");
	}

	public synchronized void stopServer() {
		isRunning = false;
		channels.forEach(channel -> {channel.shutdown();});
		try {
			server.close();
		} catch(IOException ioe) {
			System.err.println("Error while closing server.");
		}
		exec.shutdown();
	}
	
}
