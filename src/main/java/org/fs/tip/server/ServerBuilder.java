package org.fs.tip.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fs.tip.server.utils.Channel;
import org.fs.tip.server.utils.Client;

public class ServerBuilder {
	public static Server build() throws IOException {
		int tcpPort = 45932;
		ExecutorService service = Executors.newCachedThreadPool();
		ConcurrentHashMap<Channel, ArrayList<Client>> channels = new ConcurrentHashMap<>();
		channels.put(new Channel(45933, "Default", 65000, service), new ArrayList<>());
		Server server = new Server(tcpPort, channels, service);
		return server;
	}
}
