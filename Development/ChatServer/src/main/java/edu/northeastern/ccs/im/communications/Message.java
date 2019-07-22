package edu.northeastern.ccs.im.communications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Each instance of this class represents a single transmission by our IM
 * clients.
 * <p>
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class Message {
    /**
     * keyword for text
     */
    private static final String TEXT_KEY = "text";
    private static final String MSG_TYPE_KEY = "msg_type";
    private static final String SENDER_NAME_KEY = "sender_name";
    private static final String SYSTEM = "SYSTEM";
    private static final String PUBLIC_KEY = "PUBLIC_KEY";

    /**
     * The handle of the message.
     */
    private MessageType msgType;

    /**
     * The first argument used in the message. This will be the sender's identifier.
     */
    private String msgSender;

    /**
     * The second argument used in the message.
     */
    private String msgText;

    private Map<String, String> msgToInfo;

    /**
     * @param handle    Handle for the type of message being created.
     * @param srcName   Name of the individual sending this message
     * @param text      Text of the instant message
     * @param msgToInfo send msg to group or user
     */
    protected Message(MessageType handle, String srcName, String text, Map<String, String> msgToInfo) {
        msgType = handle;
        msgSender = srcName;
        msgText = text;
        this.msgToInfo = msgToInfo;
    }

    /**
     * Create a new message to continue the logout process.
     *
     * @param myName The name of the client that sent the quit message.
     * @return Instance of Message that specifies the process is logging out.
     */
    public static Message makeQuitMessage(String myName) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put(MSG_TYPE_KEY, "QUIT");
        return new Message(MessageType.QUIT, myName, null, msgInfo);
    }

    /**
     * Create a new message broadcasting an announcement to the world.
     *
     * @param myName Name of the sender of this very important missive.
     * @return Instance of Message that transmits text to all logged in users.
     */
    public static Message makeBroadcastMessage(String myName, Map<String, String> msgToInfo) {
        return new Message(MessageType.BROADCAST, myName, null, msgToInfo);
    }

    /**
     * Create a new message creating a group.
     *
     * @param myName    Name of the sender of this very important missive.
     * @param msgToInfo group creation details
     * @return Instance of Message that transmits text to all logged in users.
     */
    public static Message makeCreateGroupMessage(String myName, Map<String, String> msgToInfo) {
        return new Message(MessageType.CREATE_GROUP, myName, null, msgToInfo);
    }

    public static Message makeGetKeyMessage(String myName, Map<String, String> msgToInfo) {
        return new Message(MessageType.PUBLIC_KEY, myName, null, msgToInfo);
    }

    /**
     * Create a message that can send to a group
     *
     * @param myName    Name of the sender of this very important missive.
     * @param text      Text of the message that will be sent to all users
     * @param msgToInfo get group id
     * @returnInstance of Message that transmits text to all logged in users of such group.
     */
    public static Message makeGroupMessage(String myName, String text, Map<String, String> msgToInfo) {
        return new Message(MessageType.TO_GROUP, myName, text, msgToInfo);
    }

    /**
     * Create a message that can send to a single user
     *
     * @param myName    Name of the sender of this very important missive.
     * @param text      Text of the message that will be sent to its receiver
     * @param msgToInfo User ID of receiver
     * @return          Instance of message to be transmitted to user
     */
    public static Message makeDirectMessage(String myName, String text, Map<String, String> msgToInfo) {
        return new Message(MessageType.TO_USER, myName, text, msgToInfo);
    }

    /**
     * Create a message for getting the user's queued messages from last logout
     *
     * @param myName    Name of the sender to request message queue
     * @return          Instance of message for getting queue
     */
    public static Message makeGetQueueMessage(String myName) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put(MSG_TYPE_KEY, "GET_QUEUE");
        return new Message(MessageType.GET_QUEUE, myName, null, msgInfo);
    }

    /**
     * Create a message for getting the user's message history
     *
     * @param myName    Name of the sender to request message queue
     * @return          Instance of message for getting queue
     */
    public static Message makeGetHistoryMessage(String myName) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put(MSG_TYPE_KEY, "GET_HISTORY");
        return new Message(MessageType.GET_HISTORY, myName, null, msgInfo);
    }

    /**
     * Create a message for registration
     *
     * @param userName user name that one wants to register
     * @param pw       password that one wants to set
     * @return Message contains the information we need.
     */
    public static Message makeRegisterMessage(String userName, String pw, String publicKey) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put("user_name", userName);
        msgInfo.put("pw", pw);
        msgInfo.put("public_key", publicKey);
        return new Message(MessageType.REGISTER, userName, null, msgInfo);
    }

    public static Message makeInvitationMessage(String myName, String text, Map<String, String> msgToInfo) {
        return new Message(MessageType.INVITE, myName, text, msgToInfo);
    }

    public static Message makeDeleteMessageMessage(String myName, Map<String, String> msgToInfo) {
        return new Message(MessageType.DELETE_MSG, myName, null, msgToInfo);
    }

    /**
     * Given a handle, name and text, return the appropriate message instance or an
     * instance from a subclass of message.
     * <p>
     * The necessity of using if else does not exist anymore
     * it will be the first thing to refactor when feature is done.
     *
     * @param json contains all information that we need.
     * @return Instance of Message (or its subclasses) representing the handle,
     * name, & text.
     */
    protected static Message makeMessage(String json) {
        HashMap<String, String> payload;
        try {
            payload = new ObjectMapper().readValue(json, new TypeReference<HashMap<String, String>>() {
            });
        } catch (IOException e) {
            return null;
        }

        String msgType = payload.get(MSG_TYPE_KEY);
        String senderName = payload.get(SENDER_NAME_KEY);

        if (msgType.equals("REGISTER")) {
            return makeRegisterMessage(senderName, payload.get("pw"), payload.get("public_key"));
        }

        if (msgType.equals("HLO")) {
            return makeLoginMessage(senderName, payload.get("pw"));
        }

        // quit
        if (msgType.equals("BYE")) {
            return makeQuitMessage(senderName);
        }

        if (msgType.equals("TO_GROUP")) {
            return makeGroupMessage(senderName, null, payload);
        }

        if (msgType.equals(PUBLIC_KEY)) {
            return makeGetKeyMessage(senderName, payload);
        }

        if (msgType.equals("BCT")) {
            return makeBroadcastMessage(senderName, payload);
        }

        if (msgType.equals("INVITE")) {
            return makeInvitationMessage(senderName, null, payload);
        }

        if (msgType.equals("CRG")) {
            return makeCreateGroupMessage(senderName, payload);
        }

        if (msgType.equals(MessageType.TO_USER.toString())) {
            return makeDirectMessage(senderName, payload.get("text"), payload);
        }

        if (msgType.equals("GET_QUEUE")) {
            return makeGetQueueMessage(senderName);
        }

        if (msgType.equals("GET_HISTORY")) {
            return makeGetHistoryMessage(senderName);
        }

        if (msgType.equals(MessageType.DELETE_MSG.toString())) {
            return makeDeleteMessageMessage(senderName, payload);
        }
        return null;
    }

    /**
     * Sending system message when system needs to notificate others
     *
     * @param text text to return to one who needs to get notification
     * @return System message
     */
    public static Message makeSystemMessage(String text) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put(TEXT_KEY, text);
        String system = SYSTEM;
        msgInfo.put(MSG_TYPE_KEY, system);
        msgInfo.put(SENDER_NAME_KEY, system);
        return new Message(MessageType.SYSTEM, system, null, msgInfo);
    }

    public static Message makeReturnKeyMessage(String name, String key) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put(MSG_TYPE_KEY, PUBLIC_KEY);
        msgInfo.put(SENDER_NAME_KEY, SYSTEM);
        msgInfo.put(PUBLIC_KEY, key);
        msgInfo.put("KEY_OWNER", name);

        return new Message(MessageType.SYSTEM, SYSTEM, null, msgInfo);
    }

    /**
     * Make message contains information about login
     *
     * @param userName user that one wants to login as
     * @param pw       password that one provides
     * @return login Message
     */
    public static Message makeLoginMessage(String userName, String pw) {
        Map<String, String> msgInfo = new HashMap<>();
        msgInfo.put("user_name", userName);
        msgInfo.put("pw", pw);
        return new Message(MessageType.HELLO, userName, null, msgInfo);
    }

    /**
     * Return the name of the sender of this message.
     *
     * @return String specifying the name of the message originator.
     */
    public String getName() {
        return msgSender;
    }

    /**
     * Return the text of this message.
     *
     * @return String equal to the text sent by this message.
     */
    public String getText() {
        return msgText == null ? getMsgToInfo().get(TEXT_KEY) : msgText;
    }

    /**
     * Return the map of to message info
     *
     * @return to info for a group or user
     */
    public Map<String, String> getMsgToInfo() {
        return msgToInfo;
    }

    /**
     * Determine if this message is broadcasting text to everyone.
     *
     * @return True if the message is a broadcast message; false otherwise.
     */
    public boolean isBroadcastMessage() {
        return (msgType == MessageType.BROADCAST);
    }

    /**
     * etermine if this message is Register
     *
     * @return True if the message is a Register message; false otherwise.
     */
    public boolean isRegisterMessage() {
        return msgType == MessageType.REGISTER;
    }

    /**
     * Determine if this message is creating a group.
     *
     * @return True if the message is a create group message; false otherwise.
     */
    public boolean isCreateGroupMessage() {
        return (msgType == MessageType.CREATE_GROUP);
    }

    /**
     * Determine if the message is a request to get all queued messages
     *
     * @return True if the message is a request to get all queued messages
     */
    public boolean isGetQueueMessage() {
        return (msgType == MessageType.GET_QUEUE);
    }

    /**
     * Determine if the message is a request to get message history
     *
     * @return True if the message is a request to get all message history
     */
    public boolean isGetMessageHistory() {
        return (msgType == MessageType.GET_HISTORY);
    }

    /**
     * Determine if this message is broadcasting text to some group.
     *
     * @return True if the message is a to group message; false otherwise.
     */
    public boolean isToGroup() {
        return (msgType == MessageType.TO_GROUP);
    }

    /**
     * Determine if this message is broadcasting text to some user.
     *
     * @return True if the message is to user message; false otherwise.
     */
    public boolean isToUser() {
        return (msgType == MessageType.TO_USER);
    }

    /**
     * Determine if this message is an invitation message.
     *
     * @return True if the message is an invitation
     */
    //Emma
    public boolean isInvitation() {
        return (msgType == MessageType.INVITE);
    }


    /**
     * Determine if this message is to get publickey
     *
     * @return True if the message to get public key
     */
    public boolean isGetPublicKey() {
        return msgType == MessageType.PUBLIC_KEY;
    }

    /**
     * Determibe if message is a delete message.
     * @return True if it is a "delete message" message, false otherwise
     */
    public boolean isDeleteMessage() {
        return (msgType == MessageType.DELETE_MSG);
    }

    /**
     * Determine if this message is a message signing off from the IM server.
     *
     * @return True if the message is sent when signing off; false otherwise
     */
    public boolean terminate() {
        return msgType == MessageType.QUIT;
    }

    /**
     * Representation of this message as a String. This begins with the message
     * handle and then contains the length (as an integer) and the value of the next
     * two arguments.
     *
     * @return Representation of this message as a String.
     */
    @Override
    public String toString() {
        return getJSONString(msgToInfo);
    }


    /**
     * get payload's json string
     *
     * @param payload
     * @return string of json
     */
    @SuppressWarnings("squid:S00112") // True, we could have customized exception, but now there is no need
    private static String getJSONString(Map<String, String> payload) {
        try {
            return new ObjectMapper().writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Something went wrong when constructing your message.");
        }
    }
}
