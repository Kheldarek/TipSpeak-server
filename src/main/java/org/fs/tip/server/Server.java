package org.fs.tip.server;

import org.fs.tip.server.task.ClientService;
import org.fs.tip.server.utils.Channel;
import org.fs.tip.server.utils.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class Server implements Runnable {
	private ServerSocket server;
	private volatile boolean isRunning = true;
	private ConcurrentHashMap<Channel, ArrayList<Client>> channels;
	private ExecutorService exec;
	
	public Server(int serverPort, ConcurrentHashMap<Channel, ArrayList<Client>> channels, ExecutorService listenerService) throws IOException {
		server = new ServerSocket(serverPort);
		this.channels = channels;
		this.exec = listenerService;
	}

	@Override
	public void run() {
		channels.forEachKey(1 , channel -> exec.execute(channel));
		while(isRunning) {
			Socket client = null;
			try {
				System.out.println("Waiting for client... ");
				client = server.accept();
                exec.execute(new ClientService(client, channels));
			} catch(IOException ioe) {
				System.err.println("Error while working");
                System.err.println(ioe.getMessage());
			}
		}
	}
	
	public synchronized void openChannel(int port, String channelName, int bufferSize) {
        ArrayList<Client> clients = new ArrayList<>();
		Channel channel = new Channel(port, channelName, bufferSize, exec, clients);
		channels.put(channel, clients);
		exec.execute(channel);
	}
	
	public synchronized void closeChannel(int port) {
		channels.remove(port + "");
	}

	public synchronized void stopServer() {
		isRunning = false;
		channels.forEachKey(1, channel -> channel.shutdown());
		try {
			server.close();
		} catch(IOException ioe) {
			System.err.println("Error while closing server.");
		}
		exec.shutdown();
	}
	
}
