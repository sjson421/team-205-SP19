package edu.northeastern.ccs.im.server;

import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test timer
 */
public class ClientTimerTest {
    private static final long TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS = 18000000;
    private static final long TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS = 600000;

    /**
     * test timer after initialization
     */
    @Test
    public void testUpdateAfterInitialization() {
        ClientTimer ct = new ClientTimer();
        GregorianCalendar now = new GregorianCalendar();

        ct.updateAfterInitialization();

        assertTrue(ct.getCalendar().getTimeInMillis() >= now.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_INITIAL_IN_MS);
    }

    /**
     * test after some activity, still timer after activity
     */
    @Test
    public void testUpdateAfterActivity() {
        ClientTimer ct = new ClientTimer();
        GregorianCalendar now = new GregorianCalendar();

        ct.updateAfterActivity();

        assertTrue(ct.getCalendar().getTimeInMillis() >= now.getTimeInMillis() + TERMINATE_AFTER_INACTIVE_BUT_LOGGEDIN_IN_MS);
    }

    /**
     * set a calendar and test when it is left behind
     */
    @Test
    public void testIsBehind() {
        ClientTimer ct = new ClientTimer();
        // should return false for a newly created ClientTimer
        assertFalse(ct.isBehind());

        GregorianCalendar calendar = ct.getCalendar();
        calendar.add(GregorianCalendar.HOUR, -5);
        ct.setCalendar(calendar);
        // should return true for a ClientTimer that is past 5 hours without activity
        assertTrue(ct.isBehind());
    }
}
