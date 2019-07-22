package edu.northeastern.ccs.im.communications;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class NaiveMessageReceiverTest {
    private IReceiver receiver;
    private Message mockedMessage;

    @Before
    public void setUp() {
        receiver = new NaiveMessageReceiver();
        mockedMessage = mock(FlexibleMessage.class);
    }

    /**
     * Currently, visitor have no functionality
     * It should be added when we implement something like filter
     */
    @Test
    public void test_naiveMessageReceiver_doNothing() {
        receiver.visit(mockedMessage);
    }
}
