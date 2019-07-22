package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MessageTest {
    Message message;

    @Before
    public void initialize() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        Date date = new Date();
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);

        message = new Message(date, user, user2, "hello", false);
        message.setId(id);
    }

    /**
     * Tests all methods of Message
     */
    @Test
    public void test() {
        message.setId(new ObjectId("53aaaba6e4b0ae375bfdca88"));
        assertEquals("53aaaba6e4b0ae375bfdca88", message.getId().toString());
        User user = new User("bob", null, null, null, null, null);
        User user2 = new User("charlie", null, null, null, null, null);
        user.setId(new ObjectId("53aaaba6e4b0ae375bfdca88"));
        message.setSender(user);
        assertEquals("bob", message.getSender().getUsername());
        message.setReceiver(user2);
        assertEquals("charlie", message.getReceiver().getUsername());
        Date date = new Date();
        message.setTimestampSent(date);
        assertEquals(date, message.getTimestampSent());
        message.setMessageBody("goodbye");
        assertEquals("goodbye", message.getMessageBody());
        assertEquals("(53aaaba6e4b0ae375bfdca88, " + date + ", (53aaaba6e4b0ae375bfdca88, bob, null, null, null, null), goodbye)", message.toString());
    }
}
