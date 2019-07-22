package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Message;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * Bridges the gap between Message in DB and Message - converts BasicDBObject to Message and vice versa.
 */
public class MessageBridge implements Bridge<Message> {
    private UserDao userDao;

    MessageBridge() {
        this.userDao = new UserDao();
    }

    MessageBridge(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Message toObject(BasicDBObject basicDBObject) {
        Message msg = new Message(
                basicDBObject.getDate(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY),
                getUserById(basicDBObject.getObjectId(MESSAGE_SENDER_ID_KEY)),
                getUserById(basicDBObject.getObjectId(MESSAGE_RECEIVER_ID_KEY)),
                basicDBObject.getString(MESSAGE_MESSAGE_BODY_KEY),
                Boolean.valueOf(basicDBObject.getString(MESSAGE_DELETED_KEY)));
        msg.setId(basicDBObject.getObjectId(MESSAGE_MESSAGE_ID_KEY));
        return msg;
    }

    private User getUserById(ObjectId objectId) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_ID_KEY, objectId);
        List<User> users = userDao.get(criteria);
        if (users.size() == 1) {
            return users.get(0);
        } else {
            // Log here about no users
            return null;
        }
    }

    @Override
    public List<Message> toObjects(BasicDBList basicDBList) {
        List<Message> messages = new ArrayList<>();
        basicDBList.forEach(messageDBObject -> messages.add(toObject((BasicDBObject) messageDBObject)));
        return messages;
    }

    @Override
    public BasicDBObject toDBObject(Message object) {
        ObjectId receiverId = null;
        User receiver = object.getReceiver();

        if (receiver != null) {
            receiverId = receiver.getId();
        }
        return new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, object.getId())
                .append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, object.getTimestampSent())
                .append(MESSAGE_SENDER_ID_KEY, object.getSender().getId())
                .append(MESSAGE_RECEIVER_ID_KEY, receiverId)
                .append(MESSAGE_MESSAGE_BODY_KEY, object.getMessageBody())
                .append(MESSAGE_DELETED_KEY, object.isDeleted());
    }

    /**
     * Converts Message object list to Mongo database objects
     * @param objects Messages to be converted
     * @return Mongo database objects converted from Message object list
     */
    @Override
    public BasicDBList toDBObjects(List<Message> objects) {
        BasicDBList messageDBList = new BasicDBList();
        objects.forEach(message -> messageDBList.add(toDBObject(message)));
        return messageDBList;
    }
}
