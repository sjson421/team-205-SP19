package edu.northeastern.ccs.im.communications;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test MessageType enum
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class MessageTypeTest {
    /**
     * Test enum has right abbreviation
     */
    @Test
    public void test_toString_returnRightAbbreviation() {
        Assert.assertEquals("HLO", MessageType.HELLO.toString());
        Assert.assertEquals("BYE", MessageType.QUIT.toString());
        Assert.assertEquals("BCT", MessageType.BROADCAST.toString());
        Assert.assertEquals("GROUP", MessageType.TO_GROUP.toString());
        Assert.assertEquals("USER", MessageType.TO_USER.toString());
        Assert.assertEquals("SYS", MessageType.SYSTEM.toString());
        Assert.assertEquals("REGISTER", MessageType.REGISTER.toString());
        Assert.assertEquals("CRG", MessageType.CREATE_GROUP.toString());
        Assert.assertEquals("PUBLIC_KEY", MessageType.PUBLIC_KEY.toString());
        Assert.assertEquals("GET_QUEUE", MessageType.GET_QUEUE.toString());
        Assert.assertEquals("INVITE", MessageType.INVITE.toString());
        Assert.assertEquals("DELETE_MSG", MessageType.DELETE_MSG.toString());
    }
}
