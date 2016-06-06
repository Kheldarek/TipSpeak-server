package org.fs.tip.server.task;

import org.fs.tip.server.utils.Channel;
import org.fs.tip.server.utils.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Filip Sochal on 2016-05-31.
 */
public class ClientService implements Runnable {
    private StringBuilder channelListMessage;
    private final Socket client;
    private ConcurrentHashMap<Channel, ArrayList<Client>> channels;

    public ClientService(Socket client, ConcurrentHashMap<Channel, ArrayList<Client>> channels) {
        this.client = client;
        channelListMessage = new StringBuilder();
        this.channels = channels;
    }

    @Override
    public void run() {
        try {
            work();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void work() throws IOException {
        System.out.println("Checking connection from " + client.getLocalPort());
        sendHelloMessage();
        System.out.println("Sent HELLO!");
        System.out.println("Waiting for client response from " + client.getLocalPort());
        if(getUserConfirmation().equals("READY!")) {
            System.out.println("Sending channel list to client  from " + client.getLocalPort());
            createChannelListMessage();
            sendChannelList();
            System.out.println("Sent channel list to " + client.getLocalPort());
            System.out.println("Waiting for ack...");
            if("LIST_RECEIVED!".equals(getUserChoose())) {
                System.out.println("Joining " + client.getLocalPort() + "to default channel");
                sendDefaultChannel();
                System.out.println(client.getLocalPort() + " joined to default channel");
            }
        }
    }

    private void sendDefaultChannel() throws IOException {
        Enumeration<Channel> keys = channels.keys();
        Channel defaultChannel = new Channel();
        boolean searchFlag = true;
        while(searchFlag && keys.hasMoreElements()) {
            defaultChannel = keys.nextElement();
            if(defaultChannel.getName().equals("Default")) {
                searchFlag = false;
            }
        }
        writeToClient("JOIN:" + defaultChannel.getSimpleString() + "\r");
    }

    private void sendHelloMessage() throws IOException {
        writeToClient("HELLO!\r");
    }

    private String getUserConfirmation() throws IOException {
        String result = "";
        result = getLineFromClient();
        System.out.println("Received response...");
        return result;
    }

    private void createChannelListMessage() {
        channels.forEachKey(1, channel -> channelListMessage.append(channel).append("\n"));
        channelListMessage.append("@\r");
    }

    private void sendChannelList() throws IOException {
        writeToClient(channelListMessage.toString());
    }

    private String getUserChoose() throws IOException {
        String result = getLineFromClient();
        System.out.println("Received response...");
        return result;
    }

    private String getLineFromClient() throws IOException {
        String result = "";
        if(client != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            System.out.println("Reading...");
            result = reader.readLine();
        }
        return result;
    }

    private void writeToClient(String message) throws IOException {
        if(client != null) {
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
            writer.write(message);
            writer.flush();
        }
    }
}
