package org.fs.tip.server.utils.protocol;

/**
 * Created by Fifi on 2016-05-31.
 */
public class SIP {
    public static final String HELLO_CLIENT = "HELLO!:%s:%s";
    public static final String DENIED = "DENIED!";
    public static final String HELLO_SERVER = "HELLO!";
    public static final String READY = "READY!";
    public static final String LIST_RECEIVED = "LIST_RECEIVED!";
    public static final String JOIN = "JOIN:%s";
    public static final String CHANGE_CHANNEL = "CHANGE_CHANNEL:%s";
    public static final String BYE = "BYE_BYE!";

    public static String[] getUserNicknameAndPasswordHash(String message) {
        String[] splitMessage = message.split(":");
        if(splitMessage.length != 3
                && !"HELLO!".equals(splitMessage[0])
                && "".equals(splitMessage[1])
                && "".equals(splitMessage[2])) {
            return null;
        }
        return splitMessage;
    }

    public static String[] getNewChannelName(String message) {
        String[] splitMessage = message.split(":");
        if(splitMessage.length != 2
                && !"CHANGE_CHANNEL".equals(splitMessage[0])
                && "".equals(splitMessage[1])) {
            return null;
        }
        return splitMessage;
    }
}
