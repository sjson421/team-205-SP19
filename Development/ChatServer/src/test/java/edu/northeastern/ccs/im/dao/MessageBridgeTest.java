package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Message;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static edu.northeastern.ccs.im.dao.Constants.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageBridgeTest {
    private MessageBridge msgBridge;
    private UserDao userDao = mock(UserDao.class);

    @Before
    public void initialize() {
        this.msgBridge = new MessageBridge(userDao);
    }

    /**
     * Tests conversion of database object to Message object
     */
    @Test
    public void testToObject() {
        ObjectId id = new ObjectId(1000, 1);
        Date date = new Date();
        BasicDBObject msgDBObject = new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, date);
        msgDBObject.append(MESSAGE_SENDER_ID_KEY, id);
        msgDBObject.append(MESSAGE_RECEIVER_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_BODY_KEY, "hello");
        msgDBObject.append(MESSAGE_DELETED_KEY, false);

        User user = new User("alice", null, null, null, null, null);
        user.setId(id);
        User receiver = new User("charlie", null, null, null, null, null);
        receiver.setId(id);
        Message msg = new Message(date, user, receiver, "hello", false);
        msg.setId(id);

        List<User> users = new ArrayList<>();
        users.add(user);
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, id);
        when(this.userDao.get(criteria)).thenReturn(users);

        Assert.assertEquals(msg.toString(), msgBridge.toObject(msgDBObject).toString());

        List<User> empty = new ArrayList<>();
        when(this.userDao.get(criteria)).thenReturn(empty);
        msg.setSender(null);
        Assert.assertEquals(msg.toString(), msgBridge.toObject(msgDBObject).toString());
    }

    /**
     * Tests conversion of database objects to Message objects
     */
    @Test
    public void testToObjects() {
        ObjectId id = new ObjectId(1000, 1);
        ObjectId id2 = new ObjectId(1000, 2);

        BasicDBObject msgDBObject = new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, new Date());
        msgDBObject.append(MESSAGE_SENDER_ID_KEY, id);
        msgDBObject.append(MESSAGE_RECEIVER_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_BODY_KEY, "hello");
        msgDBObject.append(MESSAGE_DELETED_KEY, false);
        BasicDBObject msgDBObject2 = new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, id2);
        msgDBObject2.append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, new Date());
        msgDBObject2.append(MESSAGE_SENDER_ID_KEY, id2);
        msgDBObject.append(MESSAGE_RECEIVER_ID_KEY, id2);
        msgDBObject2.append(MESSAGE_MESSAGE_BODY_KEY, "goodbye");
        msgDBObject2.append(MESSAGE_DELETED_KEY, false);

        BasicDBList msgDBList = new BasicDBList();
        msgDBList.add(msgDBObject);
        msgDBList.add(msgDBObject2);

        User user = new User("alice", null, null, null, null, null);
        User charlie = new User("charlie", null, null, null, null, null);
        user.setId(id);
        Message msg = new Message(new Date(), user, charlie, "hello", false);
        msg.setId(id);
        User user2 = new User("bob", null, null, null, null, null);
        user2.setId(id);
        Message msg2 = new Message(new Date(), user2, charlie, "goodbye", false);
        msg2.setId(id2);

        List<Message> messages = new ArrayList<>();
        messages.add(msg);
        messages.add(msg2);

        List<User> users = new ArrayList<>();
        users.add(user);
        List<User> users2 = new ArrayList<>();
        users2.add(user2);

        Map<String, Object> criteria = new HashMap<>();
        Map<String, Object> criteria2 = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, id);
        criteria2.put(USER_USER_ID_KEY, id2);
        when(this.userDao.get(criteria)).thenReturn(users);
        when(this.userDao.get(criteria2)).thenReturn(users2);

        Assert.assertEquals(messages.toString(), msgBridge.toObjects(msgDBList).toString());
    }

    /**
     * Tests conversion of Message object to database object
     */
    @Test
    public void testToDBObject() {
        ObjectId id = new ObjectId(1000, 1);
        Date date = new Date();
        BasicDBObject msgDBObject = new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, date);
        msgDBObject.append(MESSAGE_SENDER_ID_KEY, id);
        msgDBObject.append(MESSAGE_RECEIVER_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_BODY_KEY, "hello");
        msgDBObject.append(MESSAGE_DELETED_KEY, false);

        User user = new User("alice", null, null, null, null, null);
        user.setId(id);
        User bob = new User("bob", null, null, null, null, null);
        bob.setId(id);
        Message msg = new Message(date, user, bob, "hello", false);
        msg.setId(id);

        Assert.assertEquals(msgDBObject, msgBridge.toDBObject(msg));
    }

    /**
     * Tests conversion of Message objects to database objects
     */
    @Test
    public void testToDBObjects() {
        ObjectId id = new ObjectId(1000, 1);
        ObjectId id2 = new ObjectId(1000, 2);
        Date date = new Date();

        BasicDBObject msgDBObject = new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, id);
        msgDBObject.append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, date);
        msgDBObject.append(MESSAGE_SENDER_ID_KEY, id);
        msgDBObject.append(MESSAGE_RECEIVER_ID_KEY, id2);
        msgDBObject.append(MESSAGE_MESSAGE_BODY_KEY, "hello");
        msgDBObject.append(MESSAGE_DELETED_KEY, false);

        BasicDBObject msgDBObject2 = new BasicDBObject(MESSAGE_MESSAGE_ID_KEY, id2);
        msgDBObject2.append(MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY, date);
        msgDBObject2.append(MESSAGE_SENDER_ID_KEY, id2);
        msgDBObject2.append(MESSAGE_RECEIVER_ID_KEY, id);
        msgDBObject2.append(MESSAGE_MESSAGE_BODY_KEY, "goodbye");
        msgDBObject2.append(MESSAGE_DELETED_KEY, false);

        BasicDBList msgDBList = new BasicDBList();
        msgDBList.add(msgDBObject);
        msgDBList.add(msgDBObject2);

        User user = new User("alice", null, null, null, null, null);
        user.setId(id);
        User user2 = new User("bob", null, null, null, null, null);
        user2.setId(id2);
        Message msg = new Message(date, user, user2, "hello", false);
        msg.setId(id);
        Message msg2 = new Message(date, user2, user, "goodbye", false);
        msg2.setId(id2);

        List<Message> messages = new ArrayList<>();
        messages.add(msg);
        messages.add(msg2);

        Assert.assertEquals(msgDBList, msgBridge.toDBObjects(messages));
    }
}
