package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.dao.Dao;
import edu.northeastern.ccs.im.dao.MessageDao;
import edu.northeastern.ccs.im.models.Message;
import edu.northeastern.ccs.im.models.User;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.MESSAGE_DELETED_KEY;

/**
 *  Service for performing database operations on MessageDao with model Message
 */
public class MessageService {
    /**
     * DAO associated with this service
     */
    Dao<Message> messageDao;

    /**
     * Sets new DAO for this service
     */
    public MessageService() {
        messageDao = new MessageDao();
    }
    /**
     * Sets DAO for this service
     */
    public void setMessageDao(Dao<Message> messageDao) {
        this.messageDao = messageDao;
    }

    /**
     * Creates a new message according to its model
     * @param timestampSent The time that the message is sent out from server
     * @param sender User who sent this message
     * @param receiver User to receive this message
     * @param messageBody Text of message
     * @return True if message was created on database. False otherwise.
     */
    public Boolean createMessage(Date timestampSent, User sender, User receiver, String messageBody) {
        Message msg = new Message(timestampSent, sender, receiver, messageBody, false);
        return messageDao.create(msg);
    }

    /**
     * Gets all (visible, not deleted) messages
     * @return All messages in database
     */
    public List<Message> getAllMessages() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(MESSAGE_DELETED_KEY, false);

        return messageDao.get(criteria);
    }

    /**
     * Gets (visible, not deleted) messages in databased searched by map
     * @param searchMap Map to search database by.
     * @return Messages found according to the searchMap
     */
    public List<Message> getMessagesByMap(Map<String, Object> searchMap) {
        searchMap.put(MESSAGE_DELETED_KEY, false);
        return messageDao.get(searchMap);
    }

    /**
     * Updates message according to its id
     * @param updatedMessage updated message with the same id as its old counterpart
     * @return Success status of message update on database
     */
    public Boolean updateMessage(Message updatedMessage) {
        return messageDao.set(updatedMessage);
    }

    /**
     * Deletes message according to its id. Does a soft delete (sets the isDelete flag to be false, and updates the
     * field in the DB).
     * @param msg updated message with the same id as its old counterpart
     * @return Success status of message update on database
     */
    public boolean deleteMessage(Message msg) {
        msg.setDeleted(true);
        return messageDao.set(msg);
    }
}
