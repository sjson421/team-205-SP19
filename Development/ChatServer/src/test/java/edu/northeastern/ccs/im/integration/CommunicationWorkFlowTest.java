package edu.northeastern.ccs.im.integration;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.UserService;
import org.junit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Ignore
public class CommunicationWorkFlowTest {
    private WorkflowTest worker1;
    private WorkflowTest worker2;
    private WorkflowTest worker3;
    private UserService userService;
    private GroupService groupService;
    private User user1;
    private User user2;
    private User user3;
    private Group group1;


    /**
     * Set up users, groups
     */
    @Before
    public void setUp() {
        worker1 = new WorkflowTest();
        worker2 = new WorkflowTest();
        worker3 = new WorkflowTest();
        userService = new UserService();
        groupService = new GroupService();
        Assert.assertTrue(userService.createUser("jiangyi1", "pw1", "publicKey1"));
        Assert.assertTrue(userService.createUser("jiangyi2", "pw2", "publicKey2"));
        Assert.assertTrue(userService.createUser("jiangyi3", "pw3", "publicKey3"));

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "jiangyi1");
        user1 = userService.getUsersByMap(criteria).get(0);

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "jiangyi2");
        user2 = userService.getUsersByMap(criteria).get(0);

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "jiangyi3");
        user3 = userService.getUsersByMap(criteria).get(0);

        List<User> admins = new ArrayList<>();
        admins.add(user1);
        Assert.assertTrue(groupService.createGroup("jiangyiGroup1", admins));
        criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, "jiangyiGroup1");
        group1 = groupService.getGroupsByMap(criteria).get(0);
        groupService.addUserToAGroup(user2, group1);
        groupService.updateGroup(group1);

        worker1.setClientRunnableInitalized(true);
        worker1.setClientRunnableUser(user1);
        worker2.setClientRunnableInitalized(true);
        worker2.setClientRunnableUser(user2);
        worker3.setClientRunnableInitalized(true);
        worker3.setClientRunnableUser(user3);
    }

    /**
     * test broadcast message
     * all other users receive it
     */
    @Test
    public void testBroadcast() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "BCT");
        map.put("sender_name", "jiangyi1");
        map.put("text", "fooText");
        Message bctMessage = Message.makeBroadcastMessage("jiangyi1", map);

        worker1.queueIncomingMessages(bctMessage);

        worker1.run();
    }

    /**
     * test group message, user1 and user2 are in group1,
     * so onky user2 get messages
     */
    @Test
    public void testSendToGroup() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "TO_GROUP");
        map.put("sender_name", "jiangyi1");
        map.put("group_name", "jiangyiGroup1");
        map.put("text", "group Text");
        Message groupMessage = Message.makeGroupMessage("jiangyi1", null, map);

        worker1.queueIncomingMessages(groupMessage);

        worker1.run();
    }

    /**
     * send to another user
     */
    @Test
    public void testSendToUser() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "USER");
        map.put("sender_name", "jiangyi1");
        map.put("recipient_name", "jiangyi2");
        map.put("text", "user Text");
        map.put("encrypted", "true");

        Message groupMessage = Message.makeDirectMessage("jiangyi1", null, map);

        worker1.queueIncomingMessages(groupMessage);

        worker1.run();
    }

    /**
     * must delete group first
     */
    @After
    public void tearDown() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, "jiangyiGroup1");
        group1 = groupService.getGroupsByMap(criteria).get(0);
        Assert.assertTrue(groupService.deleteGroup(group1));


        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "jiangyi2");
        user2 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(user2));

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "jiangyi3");
        user3 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(user3));

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "jiangyi1");
        user1 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(user1));
    }
}
