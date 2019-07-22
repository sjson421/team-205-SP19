package edu.northeastern.ccs.im.integration;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.ccs.im.communications.Message;
import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.UserService;

@Ignore
public class InvitationWorkflowTest {

    private WorkflowTest workflow1;
    private WorkflowTest workflow2;
    private WorkflowTest workflow3;
    private UserService userService;
    private GroupService groupService;
    private InvitationService invitationService;
    private User user1;
    private User user2;
    private User user3;
    private Group group1;
    private String inviteID1;
    private String inviteID2;


    /**
     * Set up users, groups  
     */
    @Before
    public void setUp() {
        workflow1 = new WorkflowTest();
        workflow2 = new WorkflowTest();
        workflow3 = new WorkflowTest();
        userService = new UserService();
        groupService = new GroupService();
        invitationService = new InvitationService();

        Assert.assertTrue(userService.createUser("emma1", "pw1", "emmaKey1"));
        Assert.assertTrue(userService.createUser("emma2", "pw2", "emmaKey2"));
        Assert.assertTrue(userService.createUser("emma3", "pw3", "emmaKey3"));

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "emma1");
        user1 = userService.getUsersByMap(criteria).get(0);
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "emma2");
        user2 = userService.getUsersByMap(criteria).get(0);
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "emma3");
        user3 = userService.getUsersByMap(criteria).get(0);

        List<User> admins = new ArrayList<>();
        admins.add(user1);
        Assert.assertTrue(groupService.createGroup("emmaGroup1", admins));
        criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, "emmaGroup1");
        group1 = groupService.getGroupsByMap(criteria).get(0);

        inviteID1 = invitationService.createInvitation(user1, user2, group1, true, null);
        inviteID2 = invitationService.createInvitation(null, user3, group1, true, null);


        workflow1.setClientRunnableInitalized(true);
        workflow1.setClientRunnableUser(user1);
        workflow2.setClientRunnableInitalized(true);
        workflow2.setClientRunnableUser(user2);
        workflow3.setClientRunnableInitalized(true);
        workflow3.setClientRunnableUser(user3);
    }

    /**
     * Test a user can create a group.
     */
    @Test
    public void testCreateGroup() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "CRG");
        map.put("sender_name", "emma1");
        map.put("group_name", "emmaGroup2");
        map.put("text", "create a group");

        Message groupMessage = Message.makeCreateGroupMessage("emma1", map);
        workflow1.queueIncomingMessages(groupMessage);
        workflow1.run();
    }

    /**
     * Test a user can request to join a group.
     */
    @Test
    public void testJoin() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "INVITE");
        map.put("sender_name", "emma2");
        map.put("invite_status", "CREATED");
        map.put("group_name", "emmaGroup1");
        map.put("invitee", "emma2");
        map.put("invitor", null);
        map.put("text", "request to join in a group");

        Message joinMessage = Message.makeInvitationMessage("emma2", null, map);
        workflow2.queueIncomingMessages(joinMessage);
        workflow2.run();
    }

    /**
     * Test a user can invite another user to join in the group.
     */
    @Test
    public void testInvite() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "INVITE");
        map.put("sender_name", "emma2");
        map.put("invite_status", "CREATED");
        map.put("group_name", "emmaGroup1");
        map.put("invitee", "emma3");
        map.put("invitor", "emma2");
        map.put("text", "invite one to join a group");

        Message inviteMessage = Message.makeInvitationMessage("emma2", null, map);
        workflow2.queueIncomingMessages(inviteMessage);
        workflow2.run();
    }

    /**
     * Test an admin can approve an invitation.
     */
    @Test
    public void testApproveInvite() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "INVITE");
        map.put("sender_name", "emma1");
        map.put("invite_status", "APPROVED");
        map.put("invite_id", inviteID1);
        map.put("text", "approve someone to join group");

        Message inviteMessage = Message.makeInvitationMessage("emma1", null, map);
        workflow1.queueIncomingMessages(inviteMessage);
        workflow1.run();
    }

    /**
     * Test an admin can deny an invitation.
     */
    @Test
    public void testDenyInvite() {
        Map<String, String> map = new HashMap<>();
        map.put("msg_type", "INVITE");
        map.put("sender_name", "emma1");
        map.put("invite_status", "DENIED");
        map.put("invite_id", inviteID2);
        map.put("text", "deny someone to join group");

        Message inviteMessage = Message.makeInvitationMessage("emma1", null, map);
        workflow1.queueIncomingMessages(inviteMessage);
        workflow1.run();
    }

    /**
     * must delete group first
     */
    @After
    public void tearDown() {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, "emmaGroup1");
        group1 = groupService.getGroupsByMap(criteria).get(0);
        Assert.assertTrue(groupService.deleteGroup(group1));

        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "emma2");
        user2 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(user2));
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "emma3");
        user3 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(user3));
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, "emma1");
        user1 = userService.getUsersByMap(criteria).get(0);
        Assert.assertTrue(userService.deleteUser(user1));
    }
}
