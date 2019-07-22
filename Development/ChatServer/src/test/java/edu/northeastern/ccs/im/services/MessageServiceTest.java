package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.dao.MessageDao;
import edu.northeastern.ccs.im.models.Message;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageServiceTest {
    MessageService messageService;
    /**
     * Mocked UserDao for usage in above service
     */
    MessageDao messageDao = mock(MessageDao.class);

    /**
     * Initializes test service with mocked DAO
     */
    @Before
    public void initialize() {
        messageService = new MessageService();
        messageService.setMessageDao(messageDao);
    }

    /**
     * Tests create method in userService
     */
    @Test
    public void testCreate() {
        Date date = new Date();
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        Message msg = new Message(date, user, user2, "hello", false);
        Message msg2 = new Message(date, user, user2, "goodbye", false);
        List<Message> messages = new ArrayList<>();
        messages.add(msg);
        messages.add(msg2);

        when(messageDao.get(null)).thenReturn(messages);
        when(messageDao.create(isA(Message.class))).thenReturn(true);
        assertTrue(messageService.createMessage(date, user, user2, "hello"));
        when(messageDao.create(isA(Message.class))).thenReturn(false);
        assertFalse(messageService.createMessage(date, user, user2, "goodbye"));
    }

    /**
     * Tests getAllUsers method in userService
     */
    @Test
    public void testGetAllMessages() {
        Date date = new Date();
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        messageService.createMessage(date, user, user2, "1");
        messageService.createMessage(date, user, user2, "2");
        messageService.createMessage(date, user, user2, "3");
        List<Message> mock = new ArrayList<>();
        mock.add(new Message(date, user, user2, "1", false));
        mock.add(new Message(date, user, user2, "2", false));
        mock.add(new Message(date, user, user2, "3", false));
        when(messageDao.get(anyMap())).thenReturn(mock);
        assertEquals(3, messageService.getAllMessages().size());
    }

    /**
     * Tests getUserByMap method in userService
     */
    @Test
    public void testGetMessagesByMap() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        Date date = new Date();
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        Message msg = new Message(date, user, user2, "hello", false);
        msg.setId(id);
        messageDao.create(msg);
        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put(Constants.USER_USER_ID_KEY, id);

        List<Message> mock = new ArrayList<>();
        mock.add(msg);
        when(messageDao.get(searchMap)).thenReturn(mock);
        assertEquals(mock, messageService.getMessagesByMap(searchMap));
    }

    /**
     * Tests updateUser method in userService
     */
    @Test
    public void testUpdateMessage() {
        ObjectId id = new ObjectId(1000, 1);
        ObjectId id2 = new ObjectId(1000, 2);
        Date date = new Date();
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        Message msg = new Message(date, user, user2, "hello", false);
        Message msg2 = new Message(date, user, user2, "goodbye", false);
        msg.setId(id);
        msg2.setId(id2);
        when(messageDao.set(msg2)).thenReturn(true);
        assertTrue(messageService.updateMessage(msg2));
        when(messageDao.set(msg2)).thenReturn(false);
        assertFalse(messageService.updateMessage(msg2));
    }

    /**
     * Tests deleteUser method in userService
     */
    @Test
    public void testDeleteMessage() {
        ObjectId id = new ObjectId(1000, 1);
        Date date = new Date();
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        Message msg = new Message(date, user, user2, "hello", false);
        messageDao.create(msg);
        messageService.createMessage(date, user, user2, "hello");
        when(messageDao.set(isA(Message.class))).thenReturn(true);
        assertTrue(messageService.deleteMessage(msg));
        when(messageDao.set(isA(Message.class))).thenReturn(false);
        assertFalse(messageService.deleteMessage(msg));
    }
}
