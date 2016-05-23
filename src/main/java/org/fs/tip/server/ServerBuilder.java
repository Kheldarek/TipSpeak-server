package org.fs.tip.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fs.tip.server.utils.Channel;

public class ServerBuilder {
	public static Server build() throws IOException {
		int tcpPort = 45932;
		ExecutorService service = Executors.newCachedThreadPool();
		List<Channel> channels = new ArrayList<>();
		channels.add(new Channel(45933, "Test", 65000, service));
		Server server = new Server(tcpPort, channels, service);
		return server;
	}
}
