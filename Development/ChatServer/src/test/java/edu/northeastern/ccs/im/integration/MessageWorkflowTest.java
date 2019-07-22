package edu.northeastern.ccs.im.integration;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.dao.MessageDao;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.MessageService;
import edu.northeastern.ccs.im.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.types.ObjectId;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.*;

import static edu.northeastern.ccs.im.dao.Constants.*;
import static junit.framework.TestCase.assertTrue;

/**
 * Tests the functions of getting the message queue, getting the message history, and deleting a message
 */
@Ignore
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MessageWorkflowTest {
    private WorkflowTest workflowTest;
    private static Logger logger;
    private UserService userService;
    private MessageDao messageDao;
    private MessageService messageService;
    private User alice;
    private User bob;
    private ObjectId idOne;
    private ObjectId idTwo;
    private ObjectId idThree;
    edu.northeastern.ccs.im.models.Message before;
    edu.northeastern.ccs.im.models.Message after;
    edu.northeastern.ccs.im.models.Message later;

    private static final String MSG_TYPE = "msg_type";
    private static final String MESSAGE_ID = "message_id";
    private static final String SENDER_NAME = "sender_name";

    /**
     * Basic setup for users and messages
     */
    @Before
    public void initialize() {
        logger = LogManager.getLogger();
        workflowTest = new WorkflowTest();
        userService = new UserService();
        messageDao = new MessageDao();
        idOne = new ObjectId();
        idTwo = new ObjectId();
        idThree = new ObjectId();

        //users
        assertTrue(userService.createUser("jay1", "pwd", "publicKey"));
        assertTrue(userService.createUser("jay2", "pwd", "publicKey"));

        Map<String, Object> aliceMap = new HashMap<>();
        aliceMap.put(USER_USER_NAME_KEY, "jay1");
        alice = userService.getUsersByMap(aliceMap).get(0);

        Map<String, Object> bobMap = new HashMap<>();
        bobMap.put(USER_USER_NAME_KEY, "jay2");
        bob = userService.getUsersByMap(bobMap).get(0);

        List<Date> logouts = new ArrayList<>();
        logouts.add(new Date(100));
        alice.setLogouts(logouts);
        userService.updateUser(alice);

        //messages
        before = new edu.northeastern.ccs.im.models.Message(new Date(50), bob, alice, "Before logout", false);
        after = new edu.northeastern.ccs.im.models.Message(new Date(150), bob, alice, "After logout", false);
        later = new edu.northeastern.ccs.im.models.Message(new Date(200), bob, alice, "Later after logout", false);

        before.setId(idOne);
        after.setId(idTwo);
        later.setId(idThree);

        messageDao.create(before);
        messageDao.create(after);
        messageDao.create(later);

        workflowTest.setClientRunnableInitalized(true);
        workflowTest.setClientRunnableUser(alice);

        logger.info("Initialization of users and messages complete!");
    }

    /**
     * Soft deletes message, keeping it in the database but setting client visibility to false
     */
    @Test
    public void testA_MessageDelete() {
        Map<String, String> map = new HashMap<>();
        map.put(MSG_TYPE, "DELETE_MSG");
        map.put(SENDER_NAME, "jay1");
        map.put(MESSAGE_ID, idTwo.toHexString());

        Message deleteHello = Message.makeDeleteMessageMessage("jay1", map);
        workflowTest.queueIncomingMessages(deleteHello);
        workflowTest.run();
        logger.info("The message saying \"After logout\" is now soft deleted!");
        after.setDeleted(true);
    }

    /**
     * Gets the message queue after logout, excluding the soft deleted messages.
     */
    @Test
    public void testB_MessageQueue() {
        Message getQueue = Message.makeGetQueueMessage("jay1");
        workflowTest.queueIncomingMessages(getQueue);
        workflowTest.run();
        //Twice for system messages, two for messages sent after logout.
        logger.info("The message queue of two messages has been successfully sent!");
    }

    /**
     * Gets the message history, excluding the soft deleted messages.
     */
    @Test
    public void testC_MessageHistory() {
        Message getHistory = Message.makeGetHistoryMessage("jay1");
        workflowTest.queueIncomingMessages(getHistory);
        workflowTest.run();
        //Twice for system messages, three times for all visible messages
        logger.info("The message history of three messages has been successfully sent!");
    }

    @After
    public void tearDown() {
        assertTrue(userService.deleteUser(alice));
        assertTrue(userService.deleteUser(bob));
        assertTrue(messageDao.delete(before));
        assertTrue(messageDao.delete(after));
        assertTrue(messageDao.delete(later));
    }
}
