package edu.northeastern.ccs.im.communications;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Test for message
 * For now we support Hello Quit and BroadCast
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class MessageTest {
    /**
     * Test BroadCast message
     */
    @Test
    public void test_makeBroadcastMessage_success() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("text", "bar");
        Message message = Message.makeBroadcastMessage("foo", map);

        // Act

        // Assert
        assertTrue(message.isBroadcastMessage());
        assertEquals("foo", message.getName());
        assertEquals("bar", message.getText());
        assertEquals("{\"text\":\"bar\"}", message.toString());
    }

    @Test
    public void test_makeCreateGroupMessage_success() {
        // Arrange
        Map<String, String> msgToInfo = new HashMap<>();
        msgToInfo.put("group_name", "bar");

        Message message = Message.makeCreateGroupMessage("foo", msgToInfo);
        // Act

        // Assert
        assertTrue(message.isCreateGroupMessage());
        assertEquals("foo", message.getName());
        assertNull(message.getText());
        assertEquals("{\"group_name\":\"bar\"}", message.toString());
        assertEquals("bar", message.getMsgToInfo().get("group_name"));
    }

    @Test
    public void test_makeGroupMessage_success() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("user_name", "foo");
        Message message = Message.makeGroupMessage("userFoo", "fooText", map);

        // Act

        // Assert
        assertTrue(message.isToGroup());
        assertFalse(message.terminate());
        assertEquals("userFoo", message.getName());
        assertEquals("fooText", message.getText());
        assertEquals("{\"user_name\":\"foo\"}", message.toString());
        assertEquals(map, message.getMsgToInfo());
    }

    @Test
    public void test_makeDirectMessage_success() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("recipient_name", "bar");

        Message message = Message.makeDirectMessage("userFoo", "fooText", map);

        // Act

        // Assert
        assertTrue(message.isToUser());
        assertFalse(message.isToGroup());
        assertFalse(message.terminate());
        assertEquals("userFoo", message.getName());
        assertEquals("fooText", message.getText());
        assertEquals("{\"recipient_name\":\"bar\"}", message.toString());
        assertEquals(map, message.getMsgToInfo());
    }

    @Test
    public void test_makeRegisterMessage_success() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("user_name", "userFoo");
        map.put("pw", "pwBar");
        map.put("public_key", "pkBaz");
        Message message = Message.makeRegisterMessage("userFoo", "pwBar", "pkBaz");

        // Act

        // Assert
        assertTrue(message.isRegisterMessage());
        assertFalse(message.terminate());
        assertEquals("userFoo", message.getName());
        assertEquals(map, message.getMsgToInfo());
    }

    @Test
    public void test_makeLoginMessage_success() {
        // Arrange
        Map<String, String> map = new HashMap<>();
        map.put("user_name", "userFoo");
        map.put("pw", "pwBar");
        Message message = Message.makeLoginMessage("userFoo", "pwBar");

        // Act

        // Assert
        assertFalse(message.terminate());
        assertEquals("userFoo", message.getName());
        assertEquals("{\"user_name\":\"userFoo\",\"pw\":\"pwBar\"}", message.toString());
        assertEquals(map, message.getMsgToInfo());
    }

    @Test
    public void test_makeSystemMessage_success() {
        Message message = Message.makeSystemMessage("foo");

        assertFalse(message.isRegisterMessage());
        assertFalse(message.terminate());
        assertEquals("foo", message.getText());
        assertEquals("{\"msg_type\":\"SYSTEM\",\"sender_name\":\"SYSTEM\",\"text\":\"foo\"}", message.toString());
    }

    @Test
    public void test_makeQuitMessage_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"BYE\",\"sender_name\":\"foo\"}");

        assertTrue(message.terminate());
        assertEquals("{\"msg_type\":\"QUIT\"}", message.toString());
    }

    @Test
    public void test_makeToGroupMessage_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"TO_GROUP\",\"sender_name\":\"foo\"}");

        assertFalse(message.terminate());
        assertTrue(message.isToGroup());
    }

    @Test
    public void test_makeNullMessage_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"NONE_OF_THEM\",\"sender_name\":\"foo\"}");

        assertNull(message);
    }

    @Test
    public void test_makeMessage_withInvalidJson_returnNull() {
        Message message = Message.makeMessage("akaca{ =C  AN ");

        assertNull(message);
    }

    @Test
    public void test_makeMessage_withRegister_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"REGISTER\",\"pw\":\"bar\",\"sender_name\":\"foo\"}");

        assertTrue(message.isRegisterMessage());
    }

    @Test
    public void test_makeMessage_withLogin_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"HLO\",\"pw\":\"bar\",\"sender_name\":\"foo\"}");

        assertEquals("bar", message.getMsgToInfo().get("pw"));
        assertEquals("foo", message.getMsgToInfo().get("user_name"));
    }

    @Test
    public void test_makeMessage_withBroadcast_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"BCT\",\"text\":\"hi\",\"sender_name\":\"foo\"}");

        assertTrue(message.isBroadcastMessage());
    }

    @Test
    public void test_makeMessage_withCreateGroup_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"CRG\",\"group_name\":\"foo\"}");

        assertTrue(message.isCreateGroupMessage());
    }

    @Test
    public void test_makeMessage_withDirectMessage_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"USER\",\"text\":\"foo\"}");

        assertTrue(message.isToUser());
    }

    @Test
    public void test_makeReturnKeyMessage_success() {
        Message message = Message.makeReturnKeyMessage("fooUser", "fooKey");

        assertEquals("PUBLIC_KEY", message.getMsgToInfo().get("msg_type"));
        assertEquals("fooKey", message.getMsgToInfo().get("PUBLIC_KEY"));
    }

    @Test
    public void test_makeMessage_withGetPublicKey_success() {
        Message message = Message.makeMessage("{\"msg_type\":\"PUBLIC_KEY\",\"text\":\"foo\"}");

        assertTrue(message.isGetPublicKey());
    }
}
