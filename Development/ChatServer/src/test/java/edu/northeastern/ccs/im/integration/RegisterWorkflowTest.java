package edu.northeastern.ccs.im.integration;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.UserService;
import org.junit.*;

import java.util.HashMap;
import java.util.Map;

@Ignore
public class RegisterWorkflowTest {
    private WorkflowTest workflowTest;
    private UserService userService;
    private User createdUser;

    private String username = "raghav_1";

    @Before
    public void setUp() {
        workflowTest = new WorkflowTest();
        userService = new UserService();
    }

    @Test
    public void testRegister() {
        workflowTest.setClientRunnableInitalized(false);

        Message incomingLoginMessage = Message.makeRegisterMessage(
                username, "pwd", "publicKey");
        workflowTest.queueIncomingMessages(incomingLoginMessage);

        // will register
        workflowTest.run();
    }

    @After
    public void tearDown() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        createdUser = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(createdUser));
    }
}
