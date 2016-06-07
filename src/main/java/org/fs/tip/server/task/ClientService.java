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
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Filip Sochal on 2016-05-31.
 */
public class ClientService implements Runnable {
    private StringBuilder channelListMessage;
    private final Socket clientSocket;
    private ConcurrentHashMap<Channel, ArrayList<Client>> channels;
    private boolean isClientNicknameFree = true;
    private String nickname = "";
    private boolean workingFlag = true;
    private Client client;

    public ClientService(Socket client, ConcurrentHashMap<Channel, ArrayList<Client>> channels) {
        this.clientSocket = client;
        channelListMessage = new StringBuilder();
        this.channels = channels;
    }

    @Override
    public void run() {
        try {
            proceed();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void proceed() throws IOException {
        if(!isHelloCorrect(getLineFromClient())) {
            writeToClient("DENIED!\r");
            return;
        }
        writeToClient("HELLO!\r");
        while(workingFlag) {
            String message = getLineFromClient();
            String[] splittedMessage = message.split(":");
            switch (splittedMessage[0]) {
                case "READY!": {
                    createChannelListMessage();
                    sendChannelList();
                    break;
                }
                case "LIST_RECEIVED!": {
                    joinToDefaultIfIsntInAny();
                    break;
                }
                case "CHANGE_CHANNEL": {
                    changeChannel(splittedMessage[1]);
                    break;
                }
                default: {
                    System.out.println("Unknown command!");
                    break;
                }
            }
        }
    }

    private void changeChannel(String channelName) throws IOException {
        Channel channel = getChannel(channelName);
        channels.get(client.getChannel()).remove(client);
        client.setChannel(channel);
        channels.get(client.getChannel()).add(client);
        System.out.println("Sending JOIN!");
        sendChannelDataToClient(channel);
        System.out.println("Sent JOIN!");
        actualizeList();
    }

    private void joinToDefaultIfIsntInAny() throws IOException {
        if(client == null) {
            Channel defaultChannel = getDefaultChannel();
            joinToDefaultChannel(defaultChannel);
            sendChannelDataToClient(defaultChannel);
            actualizeList();
        }
    }

    private boolean isHelloCorrect(String hello) {
        String[] splitted = hello.split(":");
        if(splitted.length != 3) {
            return false;
        }
        if(!splitted[0].equals("HELLO!")) {
            return false;
        }
        isClientNicknameFree = true;
        channels.forEach((channel, clients) -> {
            if(clients.stream().filter(client -> client.getNickname().equals(splitted[1])).count()!= 0) {
                isClientNicknameFree = false;
            }
        });
        if(!isClientNicknameFree) {
            return false;
        }
        if(!splitted[2].equals("q")) {
            return false;
        }
        nickname = splitted[1];
        return true;
    }

    private void joinToDefaultChannel(Channel defaultChannel) {
        ArrayList<Client> list = channels.get(defaultChannel);
        client = new Client(clientSocket.getLocalPort(), clientSocket.getInetAddress(), nickname, clientSocket, defaultChannel);
        list.add(client);
    }

    private void actualizeList() {
        channels.forEach((channel, clients) -> clients.forEach(client -> {
            try {
                if(client != this.client)
                    writeToClient(client.getSocket(), "ACTUALIZE!\r");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    private void sendChannelDataToClient(Channel defaultChannel) throws IOException {
        writeToClient("JOIN:" + defaultChannel.getSimpleString() + "\r");
    }

    private void createChannelListMessage() {
        channelListMessage = new StringBuilder();
        channelListMessage.append("LIST:");
        channels.forEachKey(1, channel -> {System.out.println(channel);channelListMessage.append(channel).append("|");});
        channelListMessage.append("\r");
    }

    private void sendChannelList() throws IOException {
        writeToClient(channelListMessage.toString());
    }

    private String getLineFromClient() throws IOException {
        String result = "";
        if(clientSocket != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            System.out.println("Reading...");
            result = reader.readLine();
        }
        return result;
    }

    private void writeToClient(Socket client, String message) throws IOException {
        if(client != null) {
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
            writer.write(message);
            writer.flush();
        }
    }

    private void writeToClient(String message) throws IOException {
        if(clientSocket != null) {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.write(message);
            writer.flush();
        }
    }

    public Channel getDefaultChannel() {
        return getChannel("Default");
    }

    public Channel getChannel(String name) {
        Enumeration<Channel> keys = channels.keys();
        Channel defaultChannel = new Channel();
        boolean searchFlag = true;
        while(searchFlag && keys.hasMoreElements()) {
            defaultChannel = keys.nextElement();
            if(defaultChannel.getName().equals(name)) {
                searchFlag = false;
            }
        }
        return defaultChannel;
    }
}
