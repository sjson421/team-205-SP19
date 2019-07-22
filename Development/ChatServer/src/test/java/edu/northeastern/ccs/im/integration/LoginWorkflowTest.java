package edu.northeastern.ccs.im.integration;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.UserService;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;

@Ignore
public class LoginWorkflowTest {
    private UserService userService;
    private User createdUser;
    private User createdUser2;
    private User createdUser3;
    private String username = "raghav1";
    private String username2 = "raghav2";
    private String username3 = "raghav3";

    @Before
    public void setUp() {
        userService = new UserService();

        Assert.assertTrue(userService.createUser(username, "pwd", "publicKey"));
        Assert.assertTrue(userService.createUser(username2, "pwd2", "publicKey"));
        Assert.assertTrue(userService.createUser(username3, "pwd3", "publicKey"));

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        createdUser = userService.getUsersByMap(criteria).get(0);

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        createdUser2 = userService.getUsersByMap(criteria).get(0);

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username3);
        createdUser3 = userService.getUsersByMap(criteria).get(0);
    }

    @Test
    public void testLogin() {
        testLogin1SingleLogin();
        testLogin2MultipleLogin();
    }

    private void testLogin1SingleLogin() {
        WorkflowTest workflowTest = new WorkflowTest();
        workflowTest.setClientRunnableInitalized(false);

        Message incomingLoginMessage = Message.makeLoginMessage(username, "pwd");
        Message incomingQuitCommand = Message.makeQuitMessage(username);
        workflowTest.queueIncomingMessages(incomingLoginMessage, incomingQuitCommand);

        // will do the login
        workflowTest.run();
        // will start receiving messages 
        workflowTest.run();
    }

    private void testLogin2MultipleLogin() {
        WorkflowTest workflowTest = new WorkflowTest();
        WorkflowTest workflowTest2 = new WorkflowTest();
        workflowTest.setClientRunnableInitalized(false);
        workflowTest2.setClientRunnableInitalized(false);

        Message incomingLoginMessage = Message.makeLoginMessage(username2, "pwd2");
        Message incomingQuitCommand = Message.makeQuitMessage(username2);
        Message incomingLoginMessage2 = Message.makeLoginMessage(username3, "pwd3");
        Message incomingQuitCommand2 = Message.makeQuitMessage(username3);

        workflowTest.queueIncomingMessages(incomingLoginMessage, incomingQuitCommand);
        workflowTest2.queueIncomingMessages(incomingLoginMessage2, incomingQuitCommand2);

        // will do the login
        workflowTest.run();
        // will start receiving messages
        workflowTest.run();

        // will do the login
        workflowTest2.run();
        // will start receiving messages 
        workflowTest2.run();
    }

    @After
    public void tearDown() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        createdUser = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(createdUser));

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        createdUser2 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(createdUser2));

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username3);
        createdUser3 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(createdUser3));
    }
}
