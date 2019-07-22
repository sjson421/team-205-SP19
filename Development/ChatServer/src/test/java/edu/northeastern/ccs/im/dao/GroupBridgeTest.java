package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Group;
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
public class GroupBridgeTest {

    private GroupBridge groupBridge;
    private UserDao userDao = mock(UserDao.class);

    @Before
    public void initialize() {
        this.groupBridge = new GroupBridge(userDao);
    }

    @Test
    public void testToObject() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);

        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);
        adminDBList.add(userObjectId2);

        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        admin.add(user2);
        List<User> members = new ArrayList<>(admin);

        //// Constructing a group POJO with these administrators
        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        //// Mocking what occurs when UserService needs to find users by Id
        //// We need to do this to ensure that GroupBridge can properly construct
        ////  admin list with users
        // First creates search criteria for each id
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);

        Assert.assertEquals(group.toString(), groupBridge.toObject(groupDBObject).toString());
    }

    @Test
    public void testToObjects() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);
        ObjectId groupObjectId2 = new ObjectId(1000, 4);

        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);
        adminDBList.add(userObjectId2);
        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBList adminDBList2 = new BasicDBList();
        adminDBList2.add(userObjectId);
        BasicDBList memberDbList2 = new BasicDBList();
        memberDbList2.addAll(adminDBList2);

        BasicDBObject groupDBObject2 = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group2");
        groupDBObject2.append(GROUP_GROUP_ID_KEY, groupObjectId2);
        groupDBObject2.append(GROUP_GROUP_ADMINS_KEY, adminDBList2);
        groupDBObject2.append(GROUP_GROUP_USERS_KEY, memberDbList2);

        BasicDBList groupDBList = new BasicDBList();
        groupDBList.add(groupDBObject);
        groupDBList.add(groupDBObject2);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        admin.add(user2);
        List<User> members = new ArrayList<>(admin);

        List<User> admin2 = new ArrayList<>();
        admin2.add(user1);
        List<User> members2 = new ArrayList<>(admin2);

        //// Constructing a group POJO with these administrators
        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        Group group2 = new Group("Group2", admin2);
        group2.setId(groupObjectId2);
        group2.setUsers(members2);

        List<Group> groups = new ArrayList<>();
        groups.add(group);
        groups.add(group2);

        //// Mocking what occurs when UserService needs to find users by Id
        //// We need to do this to ensure that GroupBridge can properly construct
        ////  admin list with users
        // First creates search criteria for each id
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);

        Assert.assertEquals(groups.toString(), groupBridge.toObjects(groupDBList).toString());
    }

    @Test
    public void testToDBObject() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);

        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);
        adminDBList.add(userObjectId2);

        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        admin.add(user2);
        List<User> members = new ArrayList<>(admin);

        //// Constructing a group POJO with these administrators
        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        //// Mocking what occurs when UserService needs to find users by Id
        //// We need to do this to ensure that GroupBridge can properly construct
        ////  admin list with users
        // First creates search criteria for each id
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);

        Assert.assertEquals(groupDBObject, groupBridge.toDBObject(group));
    }

    @Test
    public void testToDBObjects() {
        //// Creates 3 ObjectIds - to be shared by BasicDBObjects and Corresponding POJOss
        ObjectId userObjectId = new ObjectId(1000, 1);
        ObjectId userObjectId2 = new ObjectId(1000, 2);
        ObjectId groupObjectId = new ObjectId(1000, 3);
        ObjectId groupObjectId2 = new ObjectId(1000, 4);

        //// Constructing the BasicDBObject for a Group, along with List of Administrator Ids
        BasicDBList adminDBList = new BasicDBList();
        adminDBList.add(userObjectId);
        adminDBList.add(userObjectId2);
        BasicDBList memberDbList = new BasicDBList();
        memberDbList.addAll(adminDBList);

        BasicDBObject groupDBObject = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group");
        groupDBObject.append(GROUP_GROUP_ID_KEY, groupObjectId);
        groupDBObject.append(GROUP_GROUP_ADMINS_KEY, adminDBList);
        groupDBObject.append(GROUP_GROUP_USERS_KEY, memberDbList);

        BasicDBList adminDBList2 = new BasicDBList();
        adminDBList2.add(userObjectId);
        BasicDBList memberDbList2 = new BasicDBList();
        memberDbList2.addAll(adminDBList2);

        BasicDBObject groupDBObject2 = new BasicDBObject(GROUP_GROUP_NAME_KEY, "Group2");
        groupDBObject2.append(GROUP_GROUP_ID_KEY, groupObjectId2);
        groupDBObject2.append(GROUP_GROUP_ADMINS_KEY, adminDBList2);
        groupDBObject2.append(GROUP_GROUP_USERS_KEY, memberDbList2);

        BasicDBList groupDBList = new BasicDBList();
        groupDBList.add(groupDBObject);
        groupDBList.add(groupDBObject2);

        //// Constructing User POJOs that will be admin
        User user1 = new User("Alice", null, null, null, null, null);
        user1.setId(userObjectId);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(userObjectId2);

        List<User> admin = new ArrayList<>();
        admin.add(user1);
        admin.add(user2);
        List<User> members = new ArrayList<>(admin);

        List<User> admin2 = new ArrayList<>();
        admin2.add(user1);
        List<User> members2 = new ArrayList<>(admin2);

        //// Constructing a group POJO with these administrators
        Group group = new Group("Group", admin);
        group.setId(groupObjectId);
        group.setUsers(members);

        Group group2 = new Group("Group2", admin2);
        group2.setId(groupObjectId2);
        group2.setUsers(members2);

        List<Group> groups = new ArrayList<>();
        groups.add(group);
        groups.add(group2);

        //// Mocking what occurs when UserService needs to find users by Id
        //// We need to do this to ensure that GroupBridge can properly construct
        ////  admin list with users
        // First creates search criteria for each id
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(USER_USER_ID_KEY, userObjectId);
        Map<String, Object> criteria2 = new HashMap<>();
        criteria2.put(USER_USER_ID_KEY, userObjectId2);

        // Mocks the result of calling UserService find user by Id
        List<User> userResult1 = new ArrayList<>();
        userResult1.add(user1);
        List<User> userResult2 = new ArrayList<>();
        userResult2.add(user2);

        // Mocks action when UserService attempts to find by id

        when(this.userDao.get(criteria)).thenReturn(userResult1);
        when(this.userDao.get(criteria2)).thenReturn(userResult2);

        Assert.assertEquals(groupDBList, groupBridge.toDBObjects(groups));
    }
}
