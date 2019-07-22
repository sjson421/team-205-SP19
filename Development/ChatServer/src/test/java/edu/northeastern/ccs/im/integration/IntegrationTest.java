package edu.northeastern.ccs.im.integration;

import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.InvitationStatus;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.services.GroupService;
import edu.northeastern.ccs.im.services.InvitationService;
import edu.northeastern.ccs.im.services.MessageService;
import edu.northeastern.ccs.im.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

/**
 * This is a simple integration test suite. These tests will not be run when running
 * `mvn clean install` or `mvn clean test`. They will be ignored. These tests should be
 * instead run these locally against test database in Atlas.
 */
@Ignore
public class IntegrationTest {
    private static Logger logger = LogManager.getLogger();

    private UserService userService = new UserService();
    private GroupService groupService = new GroupService();
    private InvitationService invitationService = new InvitationService();
    private MessageService messageService = new MessageService();

    @Test
    public void testUserCRUDWorkflow() {
        String username = "user123";
        String username2 = "user1234";

        logger.info("Creating a user with username '" + username + "' in the database...");
        Assert.assertTrue(userService.createUser(username));
        logger.info("Created a user with username '\" + username + \"' in the database. SUCCESS.");

        logger.info("Retrieving all users in the database...");
        logger.info(userService.getAllUsers());
        logger.info("Retrieved all users in the database. SUCCESS");

        logger.info("Retrieving all users in the database with username '" + username + "'. There should only be one.");
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        List<User> users = userService.getUsersByMap(criteria);
        Assert.assertEquals(1, users.size());
        logger.info("Retrieved all users in the database with username '" + username + "'. There was only be one. SUCCESS.");

        logger.info("Changing user '" + username + "' username to '" + username2 + "'...");
        User user = users.get(0);
        user.setUsername(username2);
        Assert.assertTrue(userService.updateUser(user));
        logger.info("Changed user '" + username + "' username to '" + username2 + "'. SUCCESS.");


        logger.info("Confirming no user exists with username '" + username + "'...");
        users = userService.getUsersByMap(criteria);
        Assert.assertEquals(0, users.size());
        logger.info("Confirmed no user exists with username '" + username + "'. SUCCESS.");

        logger.info("Confirming one user exists with username '" + username2 + "'...");
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        users = userService.getUsersByMap(criteria);
        Assert.assertEquals(1, users.size());
        logger.info("Confirmed one user exists with username '" + username2 + "'. SUCCESS.");

        logger.info("Deleting user...");
        userService.deleteUser(user);
        users = userService.getUsersByMap(criteria);
        Assert.assertEquals(0, users.size());
        logger.info("Deleted user. SUCCESS.");
    }

    // test for creating group
    @Test
    public void testInvitationGroup() {
        String username = "user123";
        String username2 = "useradmin";
        String username3 = "user345";

        logger.info("Creating a user with username '" + username + "' in the database...");
        Assert.assertTrue(userService.createUser(username));
        logger.info("Created a user with username '\" + username + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username2 + "' in the database...");
        Assert.assertTrue(userService.createUser(username2));
        logger.info("Created a user with username '\" + username2 + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username3 + "' in the database...");
        Assert.assertTrue(userService.createUser(username3));
        logger.info("Created a user with username '\" + username3 + \"' in the database. SUCCESS.");

        List<User> adminPPL = new ArrayList<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        User userAdmin = userService.getUsersByMap(criteria).get(0);
        adminPPL.add(userAdmin);

        String group1 = "group1";
        logger.info("Creating a group with name '" + group1 + "' in the database...");
        groupService.createGroup(group1, adminPPL);
        logger.info("Created a group with name '\" + group_name1 + \"' in the database. SUCCESS.");
    }

    // test someone invite other ppl to join. and the invitation is approved
    @Test
    public void testInvitationWorkflow() {
        String username = "user123";
        String username2 = "useradmin";
        String username3 = "user345";

        logger.info("Creating a user with username '" + username + "' in the database...");
        Assert.assertTrue(userService.createUser(username));
        logger.info("Created a user with username '\" + username + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username2 + "' in the database...");
        Assert.assertTrue(userService.createUser(username2));
        logger.info("Created a user with username '\" + username2 + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username3 + "' in the database...");
        Assert.assertTrue(userService.createUser(username3));
        logger.info("Created a user with username '\" + username3 + \"' in the database. SUCCESS.");

        List<User> adminPPL = new ArrayList<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        User userAdmin = userService.getUsersByMap(criteria).get(0);
        adminPPL.add(userAdmin);

        String group1 = "group1";
        logger.info("Creating a group with name '" + group1 + "' in the database...");
        groupService.createGroup(group1, adminPPL);
        logger.info("Created a group with name '\" + group_name1 + \"' in the database. SUCCESS.");

        //retrieve group
        criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, group1);
        Group group = groupService.getGroupsByMap(criteria).get(0);
        //retrieve user
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        User userOne = userService.getUsersByMap(criteria).get(0);
        //retrieve userThree
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username3);
        User userThree = userService.getUsersByMap(criteria).get(0);


        invitationService.createInvitation(userThree, userOne, group, true, null);
        logger.info("Created a invitation for " + userOne.getUsername() + " to join group " + group.getGroupName());
        //retrieve invite
        criteria = new HashMap<>();
        criteria.put("invitee", userOne);
        criteria.put("group_name", group.getGroupName());
        Invitation invite = invitationService.getAllInvitations().get(0);

        invitationService.updateInvitationToNewStatus(invite, InvitationStatus.APPROVED);
    }

    @Test
    public void testInvitationWorkflowReject() {
        String username = "user123";
        String username2 = "useradmin";
        String username3 = "user345";

        logger.info("Creating a user with username '" + username + "' in the database...");
        Assert.assertTrue(userService.createUser(username));
        logger.info("Created a user with username '\" + username + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username2 + "' in the database...");
        Assert.assertTrue(userService.createUser(username2));
        logger.info("Created a user with username '\" + username2 + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username3 + "' in the database...");
        Assert.assertTrue(userService.createUser(username3));
        logger.info("Created a user with username '\" + username3 + \"' in the database. SUCCESS.");

        List<User> adminPPL = new ArrayList<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        User userAdmin = userService.getUsersByMap(criteria).get(0);
        adminPPL.add(userAdmin);

        String group1 = "group1";
        logger.info("Creating a group with name '" + group1 + "' in the database...");
        groupService.createGroup(group1, adminPPL);
        logger.info("Created a group with name '\" + group_name1 + \"' in the database. SUCCESS.");

        //retrieve group
        criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, group1);
        Group group = groupService.getGroupsByMap(criteria).get(0);
        //retrieve user
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        User userOne = userService.getUsersByMap(criteria).get(0);
        //retrieve userThree
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username3);
        User userThree = userService.getUsersByMap(criteria).get(0);


        invitationService.createInvitation(userThree, userOne, group, true, null);
        logger.info("Created a invitation for " + userOne.getUsername() + " to join group " + group.getGroupName());
        //retrieve invite
        criteria = new HashMap<>();
        criteria.put("invitee", userOne);
        criteria.put("group_name", group.getGroupName());
        Invitation invite = invitationService.getAllInvitations().get(0);

        invitationService.updateInvitationToNewStatus(invite, InvitationStatus.DENIED);
    }

    // Integration Test for someone request to join the group without being invited
    @Test
    public void testInvitationWorkflow2() {
        String username = "user123";
        String username2 = "useradmin";

        logger.info("Creating a user with username '" + username + "' in the database...");
        Assert.assertTrue(userService.createUser(username));
        logger.info("Created a user with username '\" + username + \"' in the database. SUCCESS.");

        logger.info("Creating a user with username '" + username2 + "' in the database...");
        Assert.assertTrue(userService.createUser(username2));
        logger.info("Created a user with username '\" + username2 + \"' in the database. SUCCESS.");

        List<User> adminPPL = new ArrayList<>();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username2);
        User userAdmin = userService.getUsersByMap(criteria).get(0);
        adminPPL.add(userAdmin);

        String group1 = "group1";
        logger.info("Creating a group with name '" + group1 + "' in the database...");
        groupService.createGroup(group1, adminPPL);
        logger.info("Created a group with name '\" + group_name1 + \"' in the database. SUCCESS.");

        //retrieve group
        criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_NAME_KEY, group1);
        Group group = groupService.getGroupsByMap(criteria).get(0);
        //retrieve user
        criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_NAME_KEY, username);
        User userOne = userService.getUsersByMap(criteria).get(0);


        invitationService.createInvitation(null, userOne, group, true, null);
        logger.info("Created a invitation for " + userOne.getUsername() + " to join group " + group.getGroupName());
        //retrieve invite
        criteria = new HashMap<>();
        criteria.put("invitee", userOne);
        criteria.put("group_name", group.getGroupName());
        Invitation invite = invitationService.getAllInvitations().get(0);

        invitationService.updateInvitationToNewStatus(invite, InvitationStatus.APPROVED);
    }

    @Test
    public void testMessage() {
        User sender = new User("sender", null, null, null, null, null);
        User receiver = new User("receiver", null, null, null, null, null);
        Assert.assertTrue(messageService.createMessage(new Date(), sender, receiver, "Hello!"));
        logger.info("Message has been created on the database!");
    }
}