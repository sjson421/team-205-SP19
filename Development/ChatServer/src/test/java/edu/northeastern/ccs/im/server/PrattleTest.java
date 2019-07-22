package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * I am concern about testing main, because it is not integration test
 * How ever, it I have to do it for now, some better practice should be taken
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class PrattleTest {

    @Test
    public void test_runLoop_notReady_pass() throws IOException {
        ServerSocketChannel mockChannel = mock(ServerSocketChannel.class);
        Selector selector = mock(Selector.class);
        ScheduledExecutorService threadPool = mock(ScheduledExecutorService.class);

        Prattle.runLoop(selector, mockChannel, threadPool);
    }

    /**
     * No key acceptable then assertion Error
     *
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public void test_runLoop_keyError_exception() throws IOException {
        Prattle.startServer();

        ServerSocketChannel mockChannel = mock(ServerSocketChannel.class);
        Selector selector = mock(Selector.class);
        ScheduledExecutorService threadPool = mock(ScheduledExecutorService.class);

        when(selector.select(ServerConstants.DELAY_IN_MS)).thenReturn(1).thenAnswer((Answer<Integer>) invocationOnMock -> {
            Prattle.stopServer();
            return 0;
        });
        Set<SelectionKey> selectionKeys = new HashSet<>();
        SelectionKey key = mock(SelectionKey.class);
        selectionKeys.add(key);

        when(selector.selectedKeys()).thenReturn(selectionKeys);

        Prattle.runLoop(selector, mockChannel, threadPool);

        // Expect the exception to be thrown
    }

    /**
     * socket not equal then assertion Error
     *
     * @throws IOException
     */
    @Test(expected = AssertionError.class)
    public void test_runLoop_notEqualToSocket_exception() throws IOException {
        Prattle.startServer();

        ServerSocketChannel mockChannel = mock(ServerSocketChannel.class);
        Selector selector = mock(Selector.class);
        ScheduledExecutorService threadPool = mock(ScheduledExecutorService.class);

        when(selector.select(ServerConstants.DELAY_IN_MS)).thenReturn(1).thenAnswer((Answer<Integer>) invocationOnMock -> {
            Prattle.stopServer();
            return 0;
        });
        Set<SelectionKey> selectionKeys = new HashSet<>();
        SelectionKey key = mock(SelectionKey.class);
        selectionKeys.add(key);

        when(selector.selectedKeys()).thenReturn(selectionKeys);
        when(key.isAcceptable()).thenReturn(true);

        Prattle.runLoop(selector, mockChannel, threadPool);

        // Expect the exception to be thrown
    }

    /**
     * createClientThread when success
     *
     * @throws IOException
     */
    @Test
    public void test_runLoop_Valid_success() throws IOException {
        Prattle.startServer();

        ServerSocketChannel mockChannel = mock(ServerSocketChannel.class);
        Selector selector = mock(Selector.class);
        ScheduledExecutorService threadPool = mock(ScheduledExecutorService.class);

        when(selector.select(ServerConstants.DELAY_IN_MS)).thenReturn(1).thenAnswer((Answer<Integer>) invocationOnMock -> {
            Prattle.stopServer();
            return 0;
        });
        Set<SelectionKey> selectionKeys = new HashSet<>();
        SelectionKey key = mock(SelectionKey.class);
        selectionKeys.add(key);

        when(selector.selectedKeys()).thenReturn(selectionKeys);
        when(key.isAcceptable()).thenReturn(true);
        when(key.channel()).thenReturn(mockChannel);

        Prattle.runLoop(selector, mockChannel, threadPool);

        // Assert
        verify(mockChannel).accept();
    }

    /**
     * test for isReady
     */
    @Test
    public void test_isReady_controlledByStopAndStart() {
        Prattle.startServer();
        assertTrue(Prattle.getReady());
        Prattle.stopServer();
        assertFalse(Prattle.getReady());
    }

    /**
     * Remember it needs test run serially
     * which is junit's default
     */
    @Test
    public void test_broadcastMessage_enqueMessage() {
        // Arrange
        ConcurrentLinkedQueue<ClientRunnable> list = new ConcurrentLinkedQueue<>();
        ClientRunnable runnable1 = mock(ClientRunnable.class);
        ClientRunnable runnable2 = mock(ClientRunnable.class);
        Message message = mock(Message.class);
        when(runnable1.isInitialized()).thenReturn(false);
        when(runnable2.isInitialized()).thenReturn(true);
        list.add(runnable1);
        list.add(runnable2);
        Prattle.setActive(list);

        // Act
        Prattle.broadcastMessage(message);

        // Assert
        verify(runnable1).isInitialized();
        verify(runnable2).isInitialized();
        verify(runnable2).enqueueMessage(message);
    }

    @Test
    public void test_sendGroupMessage_enqueMessage() {
        // Arrange
        ConcurrentLinkedQueue<ClientRunnable> list = new ConcurrentLinkedQueue<>();
        ClientRunnable runnable1 = mock(ClientRunnable.class);
        ClientRunnable runnable2 = mock(ClientRunnable.class);
        ClientRunnable runnable3 = mock(ClientRunnable.class);
        GroupService groupService = mock(GroupService.class);
        Prattle.setGroupService(groupService);
        when(groupService.containsUser(any(), any())).thenReturn(false, true);
        Message message = mock(Message.class);
        when(runnable1.isInitialized()).thenReturn(false);
        when(runnable2.isInitialized()).thenReturn(true);
        when(runnable3.isInitialized()).thenReturn(true);
        list.add(runnable1);
        list.add(runnable2);
        list.add(runnable3);
        Prattle.setActive(list);

        // Act
        Prattle.sendToGroup(message, null);

        // Assert
        verify(runnable1).isInitialized();
        verify(runnable2).isInitialized();
        verify(groupService, times(2)).containsUser(any(), any());
    }

    @Test
    public void test_sendDirectMessage_enqueMessage() {
        // Arrange

        User sender = new User("sender", null, null, null, null, null);
        User receiver = new User("receiver", null, null, null, null, null);
        User other = new User("irrelevant", null, null, null, null, null);
        ConcurrentLinkedQueue<ClientRunnable> list = new ConcurrentLinkedQueue<>();
        ClientRunnable senderRunnable = mock(ClientRunnable.class);
        ClientRunnable receiverRunnable = mock(ClientRunnable.class);
        ClientRunnable otherRunnable = mock(ClientRunnable.class);

        Message message = mock(Message.class);
        when(senderRunnable.isInitialized()).thenReturn(true);
        when(receiverRunnable.isInitialized()).thenReturn(true);
        when(otherRunnable.isInitialized()).thenReturn(false);

        when(senderRunnable.getUser()).thenReturn(sender);
        when(receiverRunnable.getUser()).thenReturn(receiver);
        when(otherRunnable.getUser()).thenReturn(other);

        list.add(senderRunnable);
        list.add(receiverRunnable);
        list.add(otherRunnable);
        Prattle.setActive(list);

        // Act
        Prattle.sendToUser(message, receiver);

        // Assert
        verify(senderRunnable).isInitialized();
        verify(receiverRunnable).isInitialized();
        verify(otherRunnable).isInitialized();

        verify(receiverRunnable).enqueueMessage(message);
    }
}
