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
        ArrayList<Client> clients1 = new ArrayList<>();
        ArrayList<Client> clients2 = new ArrayList<>();
        ArrayList<Client> clients3 = new ArrayList<>();
        ArrayList<Client> clients4 = new ArrayList<>();
        ArrayList<Client> clients5 = new ArrayList<>();
		ConcurrentHashMap<Channel, ArrayList<Client>> channels = new ConcurrentHashMap<>();
		channels.put(new Channel(45933, "Default", 65000, service, clients1), clients1);
        channels.put(new Channel(45934, "Pierwszy", 65000, service, clients2), clients2);
        channels.put(new Channel(45935, "Drugi", 65000, service, clients3), clients3);
        channels.put(new Channel(45936, "Trzeci", 65000, service, clients4), clients4);
        channels.put(new Channel(45937, "Czwarty", 65000, service, clients5), clients5);
		Server server = new Server(tcpPort, channels, service);
		return server;
	}
}
