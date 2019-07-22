package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests model User
 *
 * @author Jay Son
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class UserTest {
    /**
     * User to test
     */
    User user;
    List<Date> dates;

    /**
     * Initializes user to test
     */
    @Before
    public void initialize() {
        user = new User("alice", null, null, null, dates, dates);

        Date date = new Date();
        dates = new ArrayList<Date>();
        dates.add(date);
    }

    /**
     * Test for all methods of User
     */
    @Test
    public void test() {
        assertEquals("alice", user.getUsername());
        user.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
        assertEquals("5399aba6e4b0ae375bfdca88", user.getId().toString());
        user.setUsername("bob");
        assertEquals("bob", user.getUsername());

        List<Date> newDates = new ArrayList<>();
        Date newDate = new Date();
        newDates.add(newDate);

        user.setPwHash("pass");
        assertEquals("pass", user.getPwHash());
        user.setSalt("key");
        assertEquals("key", user.getSalt());

        user.setLogins(newDates);
        assertEquals(newDates, user.getLogins());
        user.setLogouts(newDates);
        assertEquals(newDates, user.getLogouts());

        Date addDate = new Date();
        newDates.add(addDate);
        user.addLogin(addDate);
        assertEquals(newDates, user.getLogins());
        user.addLogout(addDate);
        assertEquals(newDates, user.getLogouts());

        assertEquals("(5399aba6e4b0ae375bfdca88, bob, pass, " +
                "key, " + newDates + ", " + newDates + ")", user.toString());

        user.setPublicKey("foo");
        assertEquals("foo", user.getPublicKey());


        user.setLogins(null);
        user.setLogouts(null);
        user.addLogin(addDate);
        assertEquals(addDate, user.getLogins().get(0));
        user.addLogout(addDate);
        assertEquals(addDate, user.getLogouts().get(0));
    }

    @Test
    public void test_equals_null_false() {
        User user = new User("foo", "bar", "baz", null, null, null);
        assertFalse(user.equals(null));
    }

    @Test
    public void test_equals_differentClass_false() {
        User user = new User("foo", "bar", "baz", null, null, null);
        assertFalse(user.equals(new Object()));
    }

    @Test
    public void test_hashCode_equalShouldSameHashCode() {
        User user = new User("foo", "bar", "baz", null, null, null);
        User user2 = new User("foo", "bar", "baz", null, null, null);
        ObjectId id = new ObjectId();
        user.setId(id);
        user2.setId(id);

        assertTrue(user.hashCode() == user2.hashCode());
    }
}
