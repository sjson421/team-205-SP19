package edu.northeastern.ccs.im.communications;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for FlexibleMessage
 * it implements strategy patterns and accepts visitor to visit
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class FlexibleMessageTest {
    private FlexibleMessage broadcastMessage;
    private IReceiver mockedReceiver;
    private IMessageStrategy strategy;

    /**
     * Set up for Receiver and Srategy mocked object
     */
    @Before
    public void setUp() {
        broadcastMessage = new FlexibleMessage(MessageType.TO_GROUP, "foo", "bar", new HashMap<>());
        mockedReceiver = mock(IReceiver.class);
        strategy = mock(IMessageStrategy.class);
    }

    /**
     * accept should ask visitor to visit this instance.
     */
    @Test
    public void test_accept_thenVisit() {
        // Arrange

        // Act
        broadcastMessage.accept(mockedReceiver);
        // Assert

        verify(mockedReceiver).visit(broadcastMessage);
    }

    /**
     *
     */
    @Test
    public void test_setStrategy_runForAssignedStrategy() {
        // Arrange
        broadcastMessage.setStrategy(strategy);

        // Act
        broadcastMessage.getText();

        // Assert
        verify(strategy).getOutputText("bar");
    }

    /**
     * all constructors should work
     */
    @Test
    public void test_super_allSuperConstructorShouldWork() {
        // Arrange
        // Act
        FlexibleMessage message2 = new FlexibleMessage(MessageType.TO_GROUP, "foo", "bar", new HashMap<>());

        // Assert
        assertNotNull(message2);
    }
}
