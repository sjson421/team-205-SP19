package edu.northeastern.ccs.im.communications;

/**
 * Enumeration for the different types of messages.
 *
 * @author Maria Jump
 */
public enum MessageType {
    /**
     * Message sent by the user attempting to login using a specified username.
     */
    HELLO("HLO"),
    /**
     * Message sent by the user to start the logging out process and sent by the
     * server once the logout process completes.
     */
    QUIT("BYE"),
    /**
     * Message whose contents is broadcast to all connected users.
     */
    BROADCAST("BCT"),
    /**
     * Message whose contents is sent to some group
     */
    TO_GROUP("GROUP"),

    /**
     * Message whose contents are sent to another user
     */
    TO_USER("USER"),

    SYSTEM("SYS"),
    /**
     * Register
     */
    REGISTER("REGISTER"),

    /**
     * Message sent by user to create group
     */
    CREATE_GROUP("CRG"),

    /**
     * Message sent by user to get receiver's public key
     */
    PUBLIC_KEY("PUBLIC_KEY"),

    /**
     * Message sent by user to get all queued messages since logout
     */
    GET_QUEUE("GET_QUEUE"),

    /**
     * Message sent by user to get all message history
     */
    GET_HISTORY("GET_HISTORY"),

    /**
     * Invitation
     */
    INVITE("INVITE"),

    /**
     * Message to delete a message
     */
    DELETE_MSG("DELETE_MSG");

    /**
     * Store the short name of this message type.
     */
    private String abbreviation;

    /**
     * Define the message type and specify its short name.
     *
     * @param abbrev Short name of this message type, as a String.
     */
    private MessageType(String abbrev) {
        abbreviation = abbrev;
    }

    /**
     * Return a representation of this Message as a String.
     *
     * @return Three letter abbreviation for this type of message.
     */
    @Override
    public String toString() {
        return abbreviation;
    }
}
