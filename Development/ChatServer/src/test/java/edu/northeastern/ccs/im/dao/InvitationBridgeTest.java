package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that ensure Groups are converted properly into BasicDBObjects and vice versa
 */
public class InvitationBridgeTest {

    private InvitationBridge invitationBridge;
    private GroupDao groupDao = mock(GroupDao.class);
    private UserDao userDao = mock(UserDao.class);

    @Before
    public void initialize() {
        this.invitationBridge = new InvitationBridge(userDao, groupDao);
    }

    @Test
    public void testToObject() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);
        ObjectId invitationObjectId = new ObjectId(1000, 4);


        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);

        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBObject inviteDBObject = new BasicDBObject(INVITATION_INVITATION_ID_KEY, invitationObjectId);
        inviteDBObject.append(INVITATION_INVITER_ID_KEY, userObjectId);
        inviteDBObject.append(INVITATION_INVITEE_ID_KEY, userObjectId2);
        inviteDBObject.append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, true);
        inviteDBObject.append(INVITATION_GROUP_ID_KEY, groupObjectId);
        inviteDBObject.append(INVITATION_APPROVED_BY_KEY, userObjectId);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        List<User> members = new ArrayList<>(admin);

        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        Invitation invitation = new Invitation(user1, user2, group, true, user1);
        invitation.setId(invitationObjectId);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        Map<String, Object> groupCriteria = new HashMap<>();
        groupCriteria.put(GROUP_GROUP_ID_KEY, groupObjectId);

        List<Group> groupResult = new ArrayList<>();
        groupResult.add(group);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);
        when(this.groupDao.get(groupCriteria)).thenReturn(groupResult);

        Assert.assertEquals(invitation.toString(), invitationBridge.toObject(inviteDBObject).toString());
    }

    @Test
    public void testToObjects() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);
        ObjectId groupObjectId2 = new ObjectId(1000, 4);
        ObjectId invitationObjectId = new ObjectId(1000, 5);
        ObjectId invitationObjectId2 = new ObjectId(1000, 6);


        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);

        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBObject groupDBObject2 = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group2");
        groupDBObject2.append(GROUP_GROUP_ID_KEY, groupObjectId2);
        groupDBObject2.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject2.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBObject inviteDBObject = new BasicDBObject(INVITATION_INVITATION_ID_KEY, invitationObjectId);
        inviteDBObject.append(INVITATION_INVITER_ID_KEY, userObjectId);
        inviteDBObject.append(INVITATION_INVITEE_ID_KEY, userObjectId2);
        inviteDBObject.append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, true);
        inviteDBObject.append(INVITATION_GROUP_ID_KEY, groupObjectId);
        inviteDBObject.append(INVITATION_APPROVED_BY_KEY, userObjectId);

        BasicDBObject inviteDBObject2 = new BasicDBObject(INVITATION_INVITATION_ID_KEY, invitationObjectId2);
        inviteDBObject2.append(INVITATION_INVITER_ID_KEY, userObjectId);
        inviteDBObject2.append(INVITATION_INVITEE_ID_KEY, userObjectId2);
        inviteDBObject2.append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, true);
        inviteDBObject2.append(INVITATION_GROUP_ID_KEY, groupObjectId2);
        inviteDBObject2.append(INVITATION_APPROVED_BY_KEY, userObjectId);

        BasicDBList inviteDBList = new BasicDBList();
        inviteDBList.add(inviteDBObject);
        inviteDBList.add(inviteDBObject2);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        List<User> members = new ArrayList<>(admin);

        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        Group group2 = new Group("Group2", admin);
        group2.setId(groupObjectId2);
        group2.setUsers(members);

        Invitation invitation = new Invitation(user1, user2, group, true, user1);
        invitation.setId(invitationObjectId);

        Invitation invitation2 = new Invitation(user1, user2, group2, true, user1);
        invitation2.setId(invitationObjectId2);

        List<Invitation> invitations = new ArrayList<>();
        invitations.add(invitation);
        invitations.add(invitation2);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        Map<String, Object> groupCriteria = new HashMap<>();
        groupCriteria.put(GROUP_GROUP_ID_KEY, groupObjectId);
        Map<String, Object> groupCriteria2 = new HashMap<>();
        groupCriteria2.put(GROUP_GROUP_ID_KEY, groupObjectId2);

        List<Group> groupResult = new ArrayList<>();
        groupResult.add(group);
        List<Group> groupResult2 = new ArrayList<>();
        groupResult2.add(group2);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);
        when(this.groupDao.get(groupCriteria)).thenReturn(groupResult);
        when(this.groupDao.get(groupCriteria2)).thenReturn(groupResult2);

        Assert.assertEquals(invitations.toString(), invitationBridge.toObjects(inviteDBList).toString());
    }

    @Test
    public void testToDBObject() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);
        ObjectId invitationObjectId = new ObjectId(1000, 4);


        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);

        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBObject inviteDBObject = new BasicDBObject(INVITATION_INVITATION_ID_KEY, invitationObjectId);
        inviteDBObject.append(INVITATION_INVITER_ID_KEY, userObjectId);
        inviteDBObject.append(INVITATION_INVITEE_ID_KEY, userObjectId2);
        inviteDBObject.append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, true);
        inviteDBObject.append(INVITATION_GROUP_ID_KEY, groupObjectId);
        inviteDBObject.append(INVITATION_APPROVED_BY_KEY, userObjectId);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        List<User> members = new ArrayList<>(admin);

        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        Invitation invitation = new Invitation(user1, user2, group, true, user1);
        invitation.setId(invitationObjectId);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        Map<String, Object> groupCriteria = new HashMap<>();
        groupCriteria.put(GROUP_GROUP_ID_KEY, groupObjectId);

        List<Group> groupResult = new ArrayList<>();
        groupResult.add(group);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);
        when(this.groupDao.get(groupCriteria)).thenReturn(groupResult);

        Assert.assertEquals(inviteDBObject, invitationBridge.toDBObject(invitation));
    }

    @Test
    public void testToDBObjects() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);
        ObjectId groupObjectId2 = new ObjectId(1000, 4);
        ObjectId invitationObjectId = new ObjectId(1000, 5);
        ObjectId invitationObjectId2 = new ObjectId(1000, 6);

        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);

        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBObject groupDBObject2 = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group2");
        groupDBObject2.append(GROUP_GROUP_ID_KEY, groupObjectId2);
        groupDBObject2.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject2.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBObject inviteDBObject = new BasicDBObject(INVITATION_INVITATION_ID_KEY, invitationObjectId);
        inviteDBObject.append(INVITATION_INVITER_ID_KEY, userObjectId);
        inviteDBObject.append(INVITATION_INVITEE_ID_KEY, userObjectId2);
        inviteDBObject.append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, true);
        inviteDBObject.append(INVITATION_GROUP_ID_KEY, groupObjectId);
        inviteDBObject.append(INVITATION_APPROVED_BY_KEY, userObjectId);

        BasicDBObject inviteDBObject2 = new BasicDBObject(INVITATION_INVITATION_ID_KEY, invitationObjectId2);
        inviteDBObject2.append(INVITATION_INVITER_ID_KEY, userObjectId);
        inviteDBObject2.append(INVITATION_INVITEE_ID_KEY, userObjectId2);
        inviteDBObject2.append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, true);
        inviteDBObject2.append(INVITATION_GROUP_ID_KEY, groupObjectId2);
        inviteDBObject2.append(INVITATION_APPROVED_BY_KEY, userObjectId);

        BasicDBList inviteDBList = new BasicDBList();
        inviteDBList.add(inviteDBObject);
        inviteDBList.add(inviteDBObject2);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        List<User> members = new ArrayList<>(admin);

        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        Group group2 = new Group("Group2", admin);
        group2.setId(groupObjectId2);
        group2.setUsers(members);

        Invitation invitation = new Invitation(user1, user2, group, true, user1);
        invitation.setId(invitationObjectId);

        Invitation invitation2 = new Invitation(user1, user2, group2, true, user1);
        invitation2.setId(invitationObjectId2);

        List<Invitation> invitations = new ArrayList<>();
        invitations.add(invitation);
        invitations.add(invitation2);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        Map<String, Object> groupCriteria = new HashMap<>();
        groupCriteria.put(GROUP_GROUP_ID_KEY, groupObjectId);
        Map<String, Object> groupCriteria2 = new HashMap<>();
        groupCriteria2.put(GROUP_GROUP_ID_KEY, groupObjectId2);

        List<Group> groupResult = new ArrayList<>();
        groupResult.add(group);
        List<Group> groupResult2 = new ArrayList<>();
        groupResult2.add(group2);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);
        when(this.groupDao.get(groupCriteria)).thenReturn(groupResult);
        when(this.groupDao.get(groupCriteria2)).thenReturn(groupResult2);

        Assert.assertEquals(inviteDBList, invitationBridge.toDBObjects(invitations));
    }
}
