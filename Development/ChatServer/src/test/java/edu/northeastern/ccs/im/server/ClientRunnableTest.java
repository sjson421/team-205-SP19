package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.LogAppenderResource;
import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.communications.NetworkConnection;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.MessageService;
import edu.northeastern.ccs.im.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;

import static edu.northeastern.ccs.im.TestConstants.SRC_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * ClientRunnable need to mock NetworkConnection
 * Mocking will spend more time
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class ClientRunnableTest {
    private NetworkConnection connection;
    private ScheduledFuture future;
    private ClientRunnable clientRunnable;
    private GroupService mockedGroupService;
    private UserService mockedUserService;
    private MessageService mockedMessageService;
    //Emma
    private InvitationService mockedInvitationService;

    @Rule
    public LogAppenderResource appenderForClientRunnable = new LogAppenderResource(LogManager.getLogger(ClientRunnable.class));

    @Rule
    public LogAppenderResource appenderForPrattle = new LogAppenderResource(LogManager.getLogger(Prattle.class));

    /**
     * set up mocked connection
     */
    @Before
    public void setUp() {
        connection = mock(NetworkConnection.class);
        future = mock(ScheduledFuture.class);
        clientRunnable = new ClientRunnable(connection);

        clientRunnable.setFuture(future);

        mockedGroupService = mock(GroupService.class);
        mockedUserService = mock(UserService.class);
        mockedMessageService = mock(MessageService.class);
        mockedInvitationService = mock(InvitationService.class);

        clientRunnable.setUserService(mockedUserService);
        clientRunnable.setGroupService(mockedGroupService);
        clientRunnable.setMsgService(mockedMessageService);
        clientRunnable.setInviteService(mockedInvitationService);
    }

    /**
     * will not initialize without run method is called
     */
    @Test
    public void test_ClientRunnable_stateNotSet() {
        // Arrange
        Message message = Message.makeBroadcastMessage("fooUser", getBroadCastMsgInfo());

        // Act
        clientRunnable.enqueueMessage(message);

        // Assert
        assertFalse(clientRunnable.isInitialized());
    }

    /**
     * test for no message, not initialize
     */
    @Test
    public void test_run_withoutMessage_willNotInitialize() {
        // Arrange
        when(connection.iterator()).thenReturn(getMockedIterator());

        // Act
        clientRunnable.run();

        // Assert
        assertFalse(clientRunnable.isInitialized());
    }

    /**
     * null user name will not initialize
     */
    @Test
    public void test_run_withNullNameInMsg_willNotInitialize() {
        // Arrange
        Iterator<Message> iterator = getMockedIterator();
        when(iterator.hasNext()).thenReturn(true).thenReturn(false);
        when(iterator.next()).thenReturn(Message.makeLoginMessage(null, "foo"));
        when(connection.iterator()).thenReturn(iterator);

        // Act
        clientRunnable.run();

        // Assert
        assertFalse(clientRunnable.isInitialized());
    }

    @Test
    public void test_run_withRegisterMessage_notInitialze() {
        // Arrange
        Iterator<Message> iterator = getMockedIteratorForInitialize();
        when(connection.iterator()).thenReturn(iterator);
        Message mockedMessage = mock(Message.class);
        when(iterator.next()).thenReturn(mockedMessage);
        Map<String, String> map = new HashMap<>();
        map.put("pw", "fooPW");
        map.put("user_name", "fooUser");
        map.put("public_key", "fooKey");
        when(mockedMessage.isRegisterMessage()).thenReturn(true);
        when(mockedMessage.getMsgToInfo()).thenReturn(map);

        when(mockedUserService.createUser(anyString(), anyString(), anyString())).thenReturn(true);


        // Act
        clientRunnable.run();

        // Assert
        assertFalse(clientRunnable.isInitialized());
        verify(mockedUserService).createUser("fooUser", "fooPW", "fooKey");
    }

    @Test
    public void test_run_withRegisterMessage_Exist_createFail() {
        // Arrange
        Iterator<Message> iterator = getMockedIteratorForInitialize();
        when(connection.iterator()).thenReturn(iterator);
        Message mockedMessage = mock(Message.class);
        when(iterator.next()).thenReturn(mockedMessage);
        Map<String, String> map = new HashMap<>();
        map.put("pw", "fooPW");
        map.put("user_name", "fooUser");
        map.put("public_key", "fooKey");
        when(mockedMessage.isRegisterMessage()).thenReturn(true);
        when(mockedMessage.getMsgToInfo()).thenReturn(map);

        when(mockedUserService.createUser(anyString(), anyString(), anyString())).thenReturn(false);


        // Act
        clientRunnable.run();

        // Assert
        assertFalse(clientRunnable.isInitialized());
        verify(mockedUserService).createUser("fooUser", "fooPW", "fooKey");
        verify(connection).sendMessage(any());
    }

    /**
     * test for valid message, initialize with right name and id
     */
    @Test
    public void test_run_withValidMessage_rightPassword_initialize() {
        // Arrange
        Iterator<Message> iterator = getMockedIteratorForInitialize();
        when(connection.iterator()).thenReturn(iterator);

        Message mockedMessage = mock(Message.class);
        when(iterator.next()).thenReturn(mockedMessage);
        when(mockedMessage.isRegisterMessage()).thenReturn(false);
        Map<String, String> map = new HashMap<>();
        map.put("pw", "pw1");
        map.put("user_name", "srcName");
        when(mockedMessage.getMsgToInfo()).thenReturn(map);

        List<User> users = new ArrayList<>();
        users.add(new User("srcName", "RQbC9TFqYMkG8VUIuf8hEA==", "sLZYheXbMxK8DG7+JR3xnQ==", null, null, null));
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        assertTrue(clientRunnable.isInitialized());
        assertEquals(SRC_NAME, clientRunnable.getName());
    }

    /**
     * test for valid message, not initialize with wrong pw
     */
    @Test
    public void test_run_withValidMessage_wrongPassword_Fail() {
        // Arrange
        Iterator<Message> iterator = getMockedIteratorForInitialize();
        when(connection.iterator()).thenReturn(iterator);

        Message mockedMessage = mock(Message.class);
        when(iterator.next()).thenReturn(mockedMessage);
        when(mockedMessage.isRegisterMessage()).thenReturn(false);
        Map<String, String> map = new HashMap<>();
        map.put("pw", "pw1");
        map.put("user_name", "srcName");
        when(mockedMessage.getMsgToInfo()).thenReturn(map);

        List<User> users = new ArrayList<>();
        users.add(new User("srcName", "bar", "sLZYheXbMxK8DG7+JR3xnQ==", null, null, null));
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        assertFalse(clientRunnable.isInitialized());
    }

    /**
     * test for invalid message
     */
    @Test
    public void test_run_withInvalidMessage_notInitialize() {
        // Arrange
        Iterator<Message> iterator = getMockedIterator();
        when(connection.iterator()).thenReturn(iterator);

        // Act
        clientRunnable.run();

        // Assert
        assertFalse(clientRunnable.isInitialized());
    }

    /**
     * if it is initialized, it should handle message.
     */
    @Test
    public void test_run_alreadyInitializedNotValidMsg_reject() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn("bar");
        when(msg.isBroadcastMessage()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        verify(msg, times(0)).isBroadcastMessage();
    }

    /**
     * terminate msg which will say BYE and terminate it
     */
    @Test
    public void test_run_terminateMsg_terminate() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);
        clientRunnable.setName("srcName");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(true);
        when(msg.terminate()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        assertTrue(appenderForClientRunnable.getOutput()
                .contains("QUIT"));
        verify(future).cancel(false);
    }


    /**
     * if it is initialized, it should handle message.
     */
    @Test
    public void test_run_ValidMsg_broadcast() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        verify(msg).isBroadcastMessage();
    }

    /**
     * This is the test for send to group and no user is in group
     */
    @Test
    public void test_run_sendGroupMessage_notInGroup() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        verify(msg).isBroadcastMessage();
    }

    /**
     * This is the test for send to group and no user is in group
     */
    @Test
    public void test_run_sendGroupMessage_noSuchGroup_fail() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Map<String, String> map = new HashMap<>();
        map.put("group_name", "fooGroup");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        when(mockedGroupService.getGroupsByMap(anyMap())).thenReturn(new ArrayList<>());

        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        verify(mockedGroupService, times(0)).containsUser(any(), any());
    }

    @Test
    public void test_run_sendGroupMessage_noInGroup_fail() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Map<String, String> map = new HashMap<>();
        map.put("group_name", "fooGroup");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);
        List<Group> groups = new ArrayList<>();
        groups.add(null);
        when(mockedGroupService.getGroupsByMap(anyMap())).thenReturn(groups);
        when(mockedGroupService.containsUser(any(), any())).thenReturn(false);

        // Act
        clientRunnable.run();

        // Assert
        verify(mockedGroupService, times(1)).containsUser(any(), any());
    }

    @Test
    public void test_run_sendGroupMessage_success() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Map<String, String> map = new HashMap<>();
        map.put("group_name", "fooGroup");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);
        List<Group> groups = new ArrayList<>();
        groups.add(null);
        when(mockedGroupService.getGroupsByMap(anyMap())).thenReturn(groups);
        when(mockedGroupService.containsUser(any(), any())).thenReturn(true);

        // Act
        clientRunnable.run();

        // Assert
        verify(mockedGroupService, times(1)).containsUser(any(), any());
    }


    @Test
    public void test_run_sendDirectMessage_noSuchUser() {
        // Arrange
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        User sender = new User("srcName", null, null, null, null, null);
        User receiver = new User("srcName2", null, null, null, null, null);

        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(sender);
        clientRunnable.setWaitingList(mockedWaitingList);

        Map<String, String> map = new HashMap<>();
        map.put("recipient_name", "srcName2");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isToUser()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        Message errorMsg = Message.makeSystemMessage("There is no such user: srcName2");

        // no user with the username exists
        List<User> userList = new ArrayList<>();
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(userList);

        // Verify that client runnable enqueues a no such user message
        // this message will get added to waitingList and in handleOutgoingMessage
        // client runnable will remove the message from the queue
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(errorMsg);

        // Act
        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
    }

    @Test
    public void test_run_createGroup() {
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        User sender = new User(SRC_NAME, null, null, null, null, null);

        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(sender);
        clientRunnable.setWaitingList(mockedWaitingList);

        Map<String, String> map = new HashMap<>();
        map.put("group_name", "group_foo");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(true);
        when(msg.isToUser()).thenReturn(false);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);


        // Mocks the user in checkForInitialization() setUsername, but also creates admin list
        List<User> users = new ArrayList<>();
        users.add(sender);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // After initialization, process the message
        Group group = new Group("group_foo", users);

        when(mockedGroupService.createGroup("group_foo", users)).thenReturn(true);

        // Verify that client runnable enqueues the delete success message
        // this message will get added to waitingList and in handleOutgoingMessage
        // client runnable will remove the message from the queue
        Message successMessage = Message.makeSystemMessage("Group was successfully created: group_foo");
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(successMessage);

        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
    }

    @Test
    public void test_run_createGroup_fail() {
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        User sender = new User(SRC_NAME, null, null, null, null, null);

        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(sender);
        clientRunnable.setWaitingList(mockedWaitingList);

        Map<String, String> map = new HashMap<>();
        map.put("group_name", "group_foo");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(true);
        when(msg.isToUser()).thenReturn(false);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);


        // Mocks the user in checkForInitialization() setUsername, but also creates admin list
        List<User> users = new ArrayList<>();
        users.add(sender);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // After initialization, process the message
        Group group = new Group("group_foo", users);

        when(mockedGroupService.createGroup("group_foo", users)).thenReturn(false);

        // Verify that client runnable enqueues the delete success message
        // this message will get added to waitingList and in handleOutgoingMessage
        // client runnable will remove the message from the queue
        Message successMessage = Message.makeSystemMessage("Could not create group: group_foo");
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(successMessage);

        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
    }

    @Test
    public void test_run_deleteMessage_success() {
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        User sender = new User(SRC_NAME, null, null, null, null, null);

        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(sender);
        clientRunnable.setWaitingList(mockedWaitingList);

        ObjectId fakeMessageId = new ObjectId();
        Map<String, String> map = new HashMap<>();
        map.put("message_id", fakeMessageId.toHexString());

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(false);
        when(msg.isToUser()).thenReturn(false);
        when(msg.isDeleteMessage()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        edu.northeastern.ccs.im.models.Message message = mock(edu.northeastern.ccs.im.models.Message.class);
        List<edu.northeastern.ccs.im.models.Message> messageList = new ArrayList<>();
        messageList.add(message);

        // Mocks the user in checkForInitialization() setUsername, but also creates admin list
        List<User> users = new ArrayList<>();
        users.add(sender);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // After initialization, process the message
        when(mockedMessageService.getMessagesByMap(anyMap())).thenReturn(messageList);
        // no need to mock when messageList.isEmpty() since its a real list
        when(mockedMessageService.deleteMessage(message)).thenReturn(true);

        // Verify that client runnable enqueues the delete success message
        // this message will get added to waitingList and in handleOutgoingMessage
        // client runnable will remove the message from the queue
        Message successMessage = Message.makeSystemMessage("Deleted message: " + fakeMessageId.toHexString());
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(successMessage);

        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
    }

    @Test
    public void test_run_deleteMessage_fail_no_message() {
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        User sender = new User(SRC_NAME, null, null, null, null, null);

        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(sender);
        clientRunnable.setWaitingList(mockedWaitingList);

        ObjectId fakeMessageId = new ObjectId();
        Map<String, String> map = new HashMap<>();
        map.put("message_id", fakeMessageId.toHexString());

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(false);
        when(msg.isToUser()).thenReturn(false);
        when(msg.isDeleteMessage()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        List<edu.northeastern.ccs.im.models.Message> emptyMessageList = new ArrayList<>();

        // Mocks the user in checkForInitialization() setUsername, but also creates admin list
        List<User> users = new ArrayList<>();
        users.add(sender);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // After initialization, process the message
        when(mockedMessageService.getMessagesByMap(anyMap())).thenReturn(emptyMessageList);

        // Verify that client runnable enqueues the delete success message
        // this message will get added to waitingList and in handleOutgoingMessage
        // client runnable will remove the message from the queue
        Message errorMessage = Message.makeSystemMessage("There is no such message: " + fakeMessageId.toHexString());
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(errorMessage);

        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
    }

    @Test
    public void test_run_deleteMessage_fail() {
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        User sender = new User(SRC_NAME, null, null, null, null, null);

        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(sender);
        clientRunnable.setWaitingList(mockedWaitingList);

        ObjectId fakeMessageId = new ObjectId();
        Map<String, String> map = new HashMap<>();
        map.put("message_id", fakeMessageId.toHexString());

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(false);
        when(msg.isToUser()).thenReturn(false);
        when(msg.isDeleteMessage()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        edu.northeastern.ccs.im.models.Message message = mock(edu.northeastern.ccs.im.models.Message.class);
        List<edu.northeastern.ccs.im.models.Message> messageList = new ArrayList<>();
        messageList.add(message);

        // Mocks the user in checkForInitialization() setUsername, but also creates admin list
        List<User> users = new ArrayList<>();
        users.add(sender);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // After initialization, process the message
        when(mockedMessageService.getMessagesByMap(anyMap())).thenReturn(messageList);
        // no need to mock when messageList.isEmpty() since its a real list
        when(mockedMessageService.deleteMessage(message)).thenReturn(false);

        // Verify that client runnable enqueues the delete success message
        // this message will get added to waitingList and in handleOutgoingMessage
        // client runnable will remove the message from the queue
        Message errorMessage = Message.makeSystemMessage("Could not delete message: " + fakeMessageId.toHexString());
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(errorMessage);

        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
    }

    //Emma
    @Test
    public void test_send_invitation_to_admin() {
        // Arrange
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        //when(connection.iterator()).thenReturn(iteratorForInitialize).thenReturn(iteratorForIncomingMessage);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        User currUser = new User("srcName", null, null, null, null, null);
        clientRunnable.setUser(currUser);
        clientRunnable.setWaitingList(mockedWaitingList);

        // Mocks the message that iterator gets when calling next()
        Message msg = mock(Message.class);
        // how the message will look like
        Map<String, String> map = new HashMap<>();
        map.put("group_name", "group_name");
        map.put("invite_status", "CREATED"); // create the invitation
        map.put("invitee", "cindy");
        map.put("invitor", "srcName");
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        when(msg.getName()).thenReturn(SRC_NAME);
        when(msg.isInvitation()).thenReturn(true);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(false);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);


        User user = new User("alice", null, null, null, null, null);
        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUserByName(any())).thenReturn(user);

        User userb = new User("srcName", null, null, null, null, null);
        List<User> admins = new ArrayList<>();
        admins.add(userb);
        admins.add(currUser);
        Group group = mock(Group.class);//new Group("group_name", admins);
        List<Group> groupList = new ArrayList<>();
        groupList.add(group);
        when(mockedGroupService.getGroupByName(any())).thenReturn(group);
        when(group.getUsers()).thenReturn(admins);
        when(group.getGroupName()).thenReturn("group_name");

        Message successMessage = Message.makeSystemMessage("Current user is not the admin of group group_name");
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(successMessage);

        clientRunnable.run();

        // Assert
        verify(mockedWaitingList, times(1)).remove();
    }

    @Test
    public void test_request_to_join_send_msg() {
        // Arrange
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        //when(connection.iterator()).thenReturn(iteratorForInitialize).thenReturn(iteratorForIncomingMessage);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(new User("srcName", null, null, null, null, null));

        // Mocks the message that iterator gets when calling next()
        Message msg = mock(Message.class);
        // how the message will look like
        Map<String, String> map = new HashMap<>();
        map.put("group_name", "group_name");
        map.put("invite_status", "CREATED"); // create the invitation
        map.put("invitee", "alice");
        map.put("invitor", null);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        when(msg.getName()).thenReturn(SRC_NAME);
        when(msg.isInvitation()).thenReturn(true);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(false);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        User user = new User("alice", null, null, null, null, null);
        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUserByName(any())).thenReturn(user);

        User userb = new User("alice", null, null, null, null, null);
        List<User> admins = new ArrayList<>();
        users.add(userb);
        Group group = new Group("group_name", admins);
        List<Group> groupList = new ArrayList<>();
        groupList.add(group);
        when(mockedGroupService.getGroupByName(any())).thenReturn(group);

        clientRunnable.run();

        // Assert
        verify(mockedInvitationService, times(1)).createInvitation(any(), any(), any(), any(), any());
        assertNull(msg.getMsgToInfo().get("invitor"));
    }


    @Test
    public void test_approve_invitation() {
        ConcurrentLinkedQueue<Message> mockedWaitingList = mock(ConcurrentLinkedQueue.class);
        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        User currUser = new User("srcName", null, null, null, null, null);
        clientRunnable.setUser(currUser);
        clientRunnable.setUserService(mockedUserService);
        clientRunnable.setWaitingList(mockedWaitingList);

        // Mocks the message that iterator gets when calling next()
        Message msg = mock(Message.class);
        // how the message will look like
        Map<String, String> map = new HashMap<>();
        map.put("group_name", "group_name");
        map.put("invite_status", "APPROVED"); // create the invitation
        map.put("invitee", "alice");
        map.put("invitor", null);
        map.put("invite_id", "5cacd9775f21e33243a29d22");
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        when(msg.getName()).thenReturn(SRC_NAME);
        when(msg.isInvitation()).thenReturn(true);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isToGroup()).thenReturn(false);
        when(msg.isCreateGroupMessage()).thenReturn(false);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        User user = new User("alice", null, null, null, null, null);
        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUserByName(any())).thenReturn(user);

        User userb = new User("alice", null, null, null, null, null);
        List<User> admins = new ArrayList<>();
        //admins.add(currUser);
        admins.add(userb);
        Group group = mock(Group.class);
        List<Group> groupList = new ArrayList<>();
        groupList.add(group);
        when(mockedGroupService.getGroupByName(any())).thenReturn(group);
        when(group.getAdministrators()).thenReturn(admins);

        Invitation invite = mock(Invitation.class);
        List<Invitation> inviteList = new ArrayList<>();
        inviteList.add(invite);
        when(invite.getGroup()).thenReturn(group);
        when(invite.getInvitee()).thenReturn(user);
        when(invite.getInviter()).thenReturn(userb);
        when(mockedInvitationService.getInvitationByID(any())).thenReturn(invite);

        Message successMessage = Message.makeSystemMessage("You have processed invite 5cacd9775f21e33243a29d22");
        when(mockedWaitingList.isEmpty()).thenReturn(false).thenReturn(true);
        when(mockedWaitingList.remove()).thenReturn(successMessage);

        clientRunnable.run();

        // All we must do is then test that waitingList.remove() is called once
        verify(mockedWaitingList, times(1)).remove();
        //verify(mockedInvitationService, times(1)).updateInvitationToNewStatus(any(), any());
    }

    /**
     * It will be hard to test terminate triggered by timer for now
     * Still good to test terminate method
     * <p>
     */
    @Test
    public void test_terminateClient_success() {
        // Arrange

        // Act
        clientRunnable.terminateClient();

        // Assert
        // it is because there is no this client actually
        assertTrue(appenderForPrattle.getOutput().contains("Could not find a thread that I tried to remove!"));
        verify(future).cancel(false);
    }

    /**
     * when time is behind, then terminate
     */
    @Test
    public void test_timeBehind_terminate() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        List<User> users = new ArrayList<>();
        users.add(user);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);
        ClientTimer mockedClientTimer = mock(ClientTimer.class);
        when(mockedClientTimer.isBehind()).thenReturn(true);
        clientRunnable.setTimer(mockedClientTimer);

        // Act
        clientRunnable.run();

        verify(msg).terminate();
        assertEquals("srcName", clientRunnable.getUser().getUsername());
    }

    /**
     * Tests whether message is correctly stored upon message sending
     */
    @Test
    public void testStoreMessage() {
        assertFalse(clientRunnable.storeMessage("barUser", "sending"));
        List<User> users = new ArrayList<>();
        User sender = new User("fooUser", null, null, null, null, null);
        User receiver = new User("barUser", null, null, null, null, null);
        users.add(receiver);
        clientRunnable.setUser(sender);
        when(mockedUserService.getUsersByMap(isA(Map.class))).thenReturn(users);
        when(mockedMessageService.createMessage(isA(Date.class), eq(sender), eq(receiver), eq("sending"))).thenReturn(true);
        assertTrue(clientRunnable.storeMessage("barUser", "sending"));
    }

    /**
     * Tests whether user is able to receive all message queues from logout time
     */
    @Test
    public void testMessageQueueing() {
        // Arrange
        List<Date> logins = new ArrayList<>();
        logins.add(new Date(0));

        List<Date> logouts = new ArrayList<>();
        logouts.add(new Date(10000));
        User sender = new User("sender", null, null, null, logins, logouts);
        User receiver = new User("srcName", null, null, null, logins, logouts);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(receiver);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isGetQueueMessage()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        clientRunnable.setUser(receiver);

        edu.northeastern.ccs.im.models.Message messageBefore = new edu.northeastern.ccs.im.models.Message(new Date(2000),
                sender, receiver, "Before logout message", false);
        edu.northeastern.ccs.im.models.Message message1 = new edu.northeastern.ccs.im.models.Message(new Date(15000),
                sender, receiver, "After logout message", false);
        edu.northeastern.ccs.im.models.Message message2 = new edu.northeastern.ccs.im.models.Message(new Date(30000),
                sender, receiver, "Later after logout message", false);
        edu.northeastern.ccs.im.models.Message message3 = new edu.northeastern.ccs.im.models.Message(new Date(30000),
                sender, receiver, "Even later after logout message", false);

        List<edu.northeastern.ccs.im.models.Message> messageQueue = new ArrayList<>();
        messageQueue.add(messageBefore);
        messageQueue.add(message1);
        messageQueue.add(message2);
        messageQueue.add(message3);
        when(mockedMessageService.getMessagesByMap(isA(Map.class))).thenReturn(messageQueue);

        // Act
        clientRunnable.run();

        //Two messages sent for "Getting queued messages..." and "All queued messages sent!" system messages.
        //Three messages for queued messages since last logout. Total: 5 messages sent.
        verify(connection, times(5)).sendMessage(any());
    }

    /**
     * Tests whether user is able to receive all message queues when it's his or her first time logging in
     */
    @Test
    public void testMessageQueueing_NoLogout() {
        // Arrange
        List<Date> logins = new ArrayList<>();
        logins.add(new Date(0));

        List<Date> logouts = new ArrayList<>();
        User sender = new User("sender", null, null, null, logins, logouts);
        User receiver = new User("srcName", null, null, null, logins, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(receiver);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isGetQueueMessage()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        clientRunnable.setUser(receiver);

        edu.northeastern.ccs.im.models.Message messageBefore = new edu.northeastern.ccs.im.models.Message(new Date(2000),
                sender, receiver, "Some message", false);
        edu.northeastern.ccs.im.models.Message message1 = new edu.northeastern.ccs.im.models.Message(new Date(15000),
                sender, receiver, "After message", false);
        edu.northeastern.ccs.im.models.Message message2 = new edu.northeastern.ccs.im.models.Message(new Date(30000),
                sender, receiver, "Later message", false);
        edu.northeastern.ccs.im.models.Message message3 = new edu.northeastern.ccs.im.models.Message(new Date(30000),
                sender, receiver, "Even later message", false);

        List<edu.northeastern.ccs.im.models.Message> messageQueue = new ArrayList<>();
        messageQueue.add(messageBefore);
        messageQueue.add(message1);
        messageQueue.add(message2);
        messageQueue.add(message3);
        when(mockedMessageService.getMessagesByMap(isA(Map.class))).thenReturn(messageQueue);

        // Act
        clientRunnable.run();

        //Two messages sent for "Getting queued messages..." and "All queued messages sent!" system messages.
        //Four messages for queued messages since last logout. Total: 5 messages sent.
        verify(connection, times(6)).sendMessage(any());
    }

    /**
     * Tests whether user is able to receive all messages
     */
    @Test
    public void testMessageHistory() {
        // Arrange
        List<Date> logins = new ArrayList<>();
        logins.add(new Date(0));

        List<Date> logouts = new ArrayList<>();
        logouts.add(new Date(10000));
        User sender = new User("sender", null, null, null, logins, logouts);
        User receiver = new User("srcName", null, null, null, logins, logouts);

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(receiver);

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isBroadcastMessage()).thenReturn(false);
        when(msg.isGetMessageHistory()).thenReturn(true);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);

        clientRunnable.setUser(receiver);

        edu.northeastern.ccs.im.models.Message messageBefore = new edu.northeastern.ccs.im.models.Message(new Date(2000),
                sender, receiver, "Before logout message", false);
        edu.northeastern.ccs.im.models.Message message1 = new edu.northeastern.ccs.im.models.Message(new Date(15000),
                sender, receiver, "After logout message", false);
        edu.northeastern.ccs.im.models.Message message2 = new edu.northeastern.ccs.im.models.Message(new Date(30000),
                sender, receiver, "Later after logout message", false);
        edu.northeastern.ccs.im.models.Message message3 = new edu.northeastern.ccs.im.models.Message(new Date(30000),
                sender, receiver, "Even later after logout message", false);

        List<edu.northeastern.ccs.im.models.Message> history = new ArrayList<>();
        history.add(messageBefore);
        history.add(message1);
        history.add(message2);
        history.add(message3);
        when(mockedMessageService.getMessagesByMap(isA(Map.class))).thenReturn(history);

        // Act
        clientRunnable.run();

        //Two messages sent for system messages.
        //Four messages for all messages. Total: 6 messages sent.
        verify(connection, times(6)).sendMessage(any());
    }

    @Test
    public void test_run_getPublicKey_success() {
        // Arrange
        User user = new User("srcName", null, null, null, null, null);
        Iterator<Message> iteratorForInitialize = getMockedIteratorForInitialize();

        Iterator<Message> iteratorForIncomingMessage = getMockedIterator();
        when(iteratorForIncomingMessage.hasNext()).thenReturn(true).thenReturn(false);
        when(connection.iterator()).thenReturn(iteratorForIncomingMessage);
        clientRunnable.setInitialized(true);
        clientRunnable.setUser(user);

        Map<String, String> map = new HashMap<>();
        map.put("recipient_name", "foo");

        Message msg = mock(Message.class);
        when(msg.getName()).thenReturn(SRC_NAME).thenReturn(SRC_NAME);
        when(msg.isGetPublicKey()).thenReturn(true);
        when(msg.getMsgToInfo()).thenReturn(map);
        when(iteratorForIncomingMessage.next()).thenReturn(msg);
        List<User> users = new ArrayList<>();
        User publicKeyOwner = mock(User.class);
        users.add(publicKeyOwner);
        when(mockedUserService.getUsersByMap(anyMap())).thenReturn(users);

        // Act
        clientRunnable.run();

        // Assert
        verify(publicKeyOwner).getPublicKey();
    }

    /**
     * get a mocked iterator for initialize
     *
     * @return a mocked iterator
     */
    private Iterator<Message> getMockedIteratorForInitialize() {
        Iterator<Message> iteratorForInitialize = mock(Iterator.class);
        when(iteratorForInitialize.hasNext()).thenReturn(true).thenReturn(false);
        when(iteratorForInitialize.next()).thenReturn(Message.makeBroadcastMessage(SRC_NAME, getBroadCastMsgInfo()));

        return iteratorForInitialize;
    }

    private Map<String, String> getBroadCastMsgInfo() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "BCT");
        map.put("sender_name", SRC_NAME);
        map.put("text", "fooText");

        return map;
    }

    /**
     * get a mocked iterator
     *
     * @return a mocked iterator
     */
    private Iterator<Message> getMockedIterator() {
        return mock(Iterator.class);
    }
}
