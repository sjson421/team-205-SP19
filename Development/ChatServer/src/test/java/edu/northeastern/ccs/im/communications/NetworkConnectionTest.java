package edu.northeastern.ccs.im.communications;

import edu.northeastern.ccs.im.LogAppenderResource;
import org.apache.logging.log4j.LogManager;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import static edu.northeastern.ccs.im.TestConstants.SRC_NAME;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * mockito cannot deal with final method
 * it need a little special configuration to make it work
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class NetworkConnectionTest {
    private SocketChannel mockChannel;
    private NetworkConnection connection;

    /**
     * Set up default mocked object
     */
    @Before
    public void setUp() {
        mockChannel = mock(SocketChannel.class);
        connection = new NetworkConnection(mockChannel);
    }

    @Rule
    public LogAppenderResource appender = new LogAppenderResource(LogManager.getLogger(NetworkConnection.class));

    /**
     * test a successful constructing
     *
     * @throws IOException for configureBlocking
     */
    @Test
    public void test_networkConnection_success() throws IOException {
        // Arrange

        // Act

        // Assert
        verify(mockChannel).configureBlocking(false);
        verify(mockChannel).register(any(), eq(SelectionKey.OP_READ));
    }

    /**
     * test when exception is thrown
     *
     * @throws IOException for configureBlocking
     */
    @Test(expected = AssertionError.class)
    public void test_networkConnection_getIOException_throwsAssertionError() throws IOException {
        // Arrange
        when(mockChannel.configureBlocking(false)).thenThrow(new IOException("foo"));
        connection = new NetworkConnection(mockChannel);

        // Act

        // Assert
        assertTrue(appender.getOutput().contains("foo"));
    }

    /**
     * test close
     *
     * @throws IOException for close
     */
    @Test
    public void test_close_success() throws IOException {
        // Arrange

        // Act
        connection.close();

        // Assert
        verify(mockChannel).close();
    }

    /**
     * Test when IOException is thrown
     *
     * @throws IOException for close
     */
    @Test(expected = AssertionError.class)
    public void test_close_getIOException_throwsAssertionError() throws IOException {
        // Arrange
        doThrow(new IOException("foo")).when(mockChannel).close();
        connection = new NetworkConnection(mockChannel);

        // Act
        connection.close();

        // Assert
        assertTrue(appender.getOutput().contains("foo"));
    }

    /**
     * test for wrapper remains
     */
    @Test
    public void test_sendMessage_withMsg_success() throws IOException {
        // Arrange

        // Act
        connection.sendMessage(Message.makeQuitMessage(SRC_NAME));

        // Assert
        verify(mockChannel, times(100)).write((ByteBuffer) any());
    }

    /**
     * test exception
     */
    @Test
    public void test_sendMessage_withIOException_resultIsFalse() throws IOException {
        // Arrange
        when(mockChannel.write((ByteBuffer) any())).thenThrow(new IOException("foo"));

        // Act
        boolean result = connection.sendMessage(Message.makeQuitMessage(SRC_NAME));

        // Assert
        assertFalse(result);
    }

    /**
     * Test iterator
     */
    @Test(expected = AssertionError.class)
    public void test_iterator_NotReadable_throwsException() throws IOException {
        // Arrange
        Iterator<Message> messageIterator = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);

        SelectionKey mockedKey = mock(SelectionKey.class);

        connection.setSelector(mockedSelector);
        connection.setSelectionKey(mockedKey);

        // Act
        boolean result = messageIterator.hasNext();

        // Assert will not come here
        assertFalse(result);
        fail();
    }

    @Test
    public void test_iterator_resultTrue() throws IOException {
        // Arrange
        Iterator<Message> messageIterator = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);

        CharsetDecoder decoder = mock(CharsetDecoder.class);

        when(decoder.decode(any())).
                thenReturn(CharBuffer.wrap("{\"pw\":\"name\",\"msg_type\":\"HLO\",\"sender_name\":\"user\"}".toCharArray()));

        SelectionKey mockedKey = mock(SelectionKey.class);
        when(mockedKey.isReadable()).thenReturn(true);

        connection.setSelector(mockedSelector);
        connection.setSelectionKey(mockedKey);
        connection.setDecoder(decoder);

        // Act
        boolean result = messageIterator.hasNext();

        // Assert will not come here
        assertTrue(result);
    }

    /**
     * read throws exception
     *
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public void test_iterator_withException_throwException() throws IOException {
        // Arrange
        when(mockChannel.read((ByteBuffer) any())).thenThrow(new IOException("foo"));

        Iterator<Message> messageIterator = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);

        SelectionKey mockedKey = mock(SelectionKey.class);
        when(mockedKey.isReadable()).thenReturn(true);

        connection.setSelector(mockedSelector);
        connection.setSelectionKey(mockedKey);

        // Act
        boolean result = messageIterator.hasNext();

        // Assert will not come here
        assertTrue(result);
    }

    /**
     * throw buffer two times, get a HLO
     *
     * @throws IOException
     */
    @Test
    public void test_throughBuffer_success() throws IOException {
        NetworkConnection.MessageIterator messageIterator = (NetworkConnection.MessageIterator) connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);

        SelectionKey mockedKey = mock(SelectionKey.class);
        when(mockedKey.isReadable()).thenReturn(true);

        CharBuffer buffer = mock(CharBuffer.class);
        when(buffer.subSequence(anyInt(), anyInt())).thenReturn(buffer);
        given(buffer.limit()).willReturn(100).willReturn(10).willReturn(1);

        when(buffer.position()).thenReturn(0);
        when(buffer.get(0)).thenReturn('1');
        when(buffer.toString()).thenReturn("{\"pw\":\"name\",\"msg_type\":\"HLO\",\"sender_name\":\"user\"}");

        connection.setMessages(new ArrayDeque<>());

        messageIterator.throughBuffer(buffer);
    }

    /**
     * no message, then throw exception
     *
     * @throws IOException
     */
    @Test(expected = NoSuchElementException.class)
    public void test_iterator_next_withNoElements_Exception() throws IOException {
        // Arrange
        Iterator<Message> messageIterator = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);

        SelectionKey mockedKey = mock(SelectionKey.class);
        when(mockedKey.isReadable()).thenReturn(true);

        connection.setSelector(mockedSelector);
        connection.setSelectionKey(mockedKey);

        // Act
        messageIterator.next();

        // Expect Exception to be thrown
    }

    /**
     * when there is message, then able to return it
     *
     * @throws IOException for select can throw exception
     */
    @Test
    public void test_iterator_next_withSomeElements_success() throws IOException {
        // Arrange
        Iterator<Message> messageIterator = connection.iterator();
        Selector mockedSelector = mock(Selector.class);
        when(mockedSelector.selectNow()).thenReturn(1);

        SelectionKey mockedKey = mock(SelectionKey.class);
        when(mockedKey.isReadable()).thenReturn(true);

        connection.setSelector(mockedSelector);
        connection.setSelectionKey(mockedKey);

        Queue<Message> messages = new ArrayDeque<>();
        Message expectedMessage = Message.makeSystemMessage("Hi");
        ((ArrayDeque<Message>) messages).addFirst(expectedMessage);
        connection.setMessages(messages);

        // Act
        Message message = messageIterator.next();

        // Assert
        assertEquals(expectedMessage, message);
    }

    @Test
    public void test_iterator_hasNext_true() {
        Iterator<Message> messageIterator = connection.iterator();
        Queue mockMessages = mock(Queue.class);
        when(mockMessages.isEmpty()).thenReturn(false);
        connection.setMessages(mockMessages);

        boolean result = messageIterator.hasNext();

        assertTrue(result);
    }
}
