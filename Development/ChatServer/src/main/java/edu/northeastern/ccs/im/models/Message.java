package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;

import java.util.Date;

/**
 * Message model
 */
public class Message {
    /**
     * message id
     */
    private ObjectId id;
    /**
     * The time that the message is sent out from server
     */
    private Date timestampSent;
    /**
     * User who sent this message
     */
    private User sender;
    /**
     * User to receive this message
     */
    private User receiver;
    /**
     * Text of message
     */
    private String messageBody;

    /**
     * Whether or not the message is deleted (can be seen). Deleted messages will have this set to true.
     */
    private boolean isDeleted;

    /**
     * Creates new Message along with its corresponding fields
     * @param timestampSent The time that the message is sent out from server
     * @param sender User who sent this message
     * @param receiver User to receive this message
     * @param messageBody Text of message
     * @param isDeleted whether or not the message has been deleted
     */
    public Message(Date timestampSent, User sender, User receiver, String messageBody, boolean isDeleted) {
        this.id = new ObjectId();
        this.timestampSent = timestampSent;
        this.sender = sender;
        this.receiver = receiver;
        this.messageBody = messageBody;
        this.isDeleted = isDeleted;
    }

    /**
     * @return message id
     */
    public ObjectId getId() {
        return id;
    }
    /**
     * sets message id
     */
    public void setId(ObjectId id) {
        this.id = id;
    }
    /**
     * @return message sent time
     */
    public Date getTimestampSent() {
        return timestampSent;
    }
    /**
     * gets message sent time
     */
    public void setTimestampSent(Date timestampSent) {
        this.timestampSent = timestampSent;
    }
    /**
     * @return sender for message
     */
    public User getSender() {
        return sender;
    }
    /**
     * sets sender for message
     */
    public void setSender(User sender) {
        this.sender = sender;
    }
    /**
     * @return receiver for message
     */
    public User getReceiver() {
        return receiver;
    }
    /**
     * sets receiver for message
     */
    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }
    /**
     * @return message text
     */
    public String getMessageBody() {
        return messageBody;
    }
    /**
     * sets message text
     */
    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    /**
     * Whether or not the message has been deleted.
     * @return true if the message has been deleted, false otherwise
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Sets whether or not the message has been deleted.
     * @param isDeleted is deleted or not
     */
    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * @return fields of message as a string
     */
    public String toString() {
        return "(" + id + ", " + timestampSent + ", " + sender + ", " + messageBody + ")";
    }
}
