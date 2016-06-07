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
    private String lastCommand = "";
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
            //work();
            procced();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*catch (InterruptedException e) {
            workingFlag = false;
        }*/
    }

    public void procced() throws IOException {
        if(!isHelloCorrect(getLineFromClient())) {
            writeToClient("DENNIED!\r");
            return;
        }
        writeToClient("HELLO!");
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

    public void work() throws IOException, InterruptedException {
        System.out.println("Checking connection from " + clientSocket.getLocalPort());
        String hello = getHelloMessage();
        System.out.println(hello);
        if(isHelloCorrect(hello)) {
            sendHelloMessage();
            System.out.println("Sent HELLO!");
            System.out.println("Waiting for clientSocket response from " + clientSocket.getLocalPort());
            if (getUserConfirmation().equals("READY!")) {
                System.out.println("Sending channel list to clientSocket  from " + clientSocket.getLocalPort());
                createChannelListMessage();
                sendChannelList();
                System.out.println("Sent channel list to " + clientSocket.getLocalPort());
                System.out.println("Waiting for ack...");
                if ("LIST_RECEIVED!".equals(getUserChoose())) {
                    System.out.println("Joining " + clientSocket.getLocalPort() + "to default channel");
                    Channel defaultChannel = getDefaultChannel();
                    joinToDefaultChannel(defaultChannel);
                    sendChannelDataToClient(defaultChannel);
                    System.out.println(clientSocket.getLocalPort() + " joined to default channel");
                    actualizeList();
                    System.out.println("Waiting for ready!");
                    String message = getLineFromClient();
                    if("READY!".equals(message)) {
                        createChannelListMessage();
                        sendChannelList();
                        System.out.println("LIST IS SENT!");
                        if("LIST_RECEIVED!".equals(getLineFromClient())) {

                        }
                    }
                    while(workingFlag) {
                        Thread.sleep(2000);
                        System.out.println("Reading change_CHANNEL");
                        String changeMessage = getLineFromClient();
                        System.out.println("Already read: " + changeMessage);
                        String[] changeArray = changeMessage.split(":");
                        if(changeArray[0].equals("CHANGE_CHANNEL")) {
                            Channel channel = getChannel(changeArray[1]);
                            channels.get(client.getChannel()).remove(client);
                            client.setChannel(channel);
                            channels.get(client.getChannel()).add(client);
                            System.out.println("Sending JOIN!");
                            sendChannelDataToClient(channel);
                            System.out.println("Sent JOIN!");
                            System.out.println("Waiting for ready!");
                            String message1 = getLineFromClient();
                            if("READY!".equals(message1)) {
                                createChannelListMessage();
                                sendChannelList();
                                System.out.println("READY IS DONE!");
                                if("LIST_RECEIVED!".equals(getLineFromClient())) {

                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            System.out.println("Dienied " + clientSocket.getLocalPort() + "!");
            writeToClient("DENNIED!\r");
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

    public String getHelloMessage() throws IOException {
        String helloMessage = getLineFromClient();
        return helloMessage;
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
        channelListMessage = new StringBuilder();
        channels.forEachKey(1, channel -> {System.out.println(channel);channelListMessage.append(channel).append("\n");});
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
