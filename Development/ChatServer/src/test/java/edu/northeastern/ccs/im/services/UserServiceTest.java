package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.dao.UserDao;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for InvitationService
 *
 * @author Jay Son
 */
@SuppressWarnings("squid:S00100") // testing naming convention is different
public class UserServiceTest {
    /**
     * UserService to test
     */
    UserService userService;
    /**
     * Mocked UserDao for usage in above service
     */
    UserDao userDao = mock(UserDao.class);

    /**
     * Initializes test service with mocked DAO
     */
    @Before
    public void initialize() {
        userService = new UserService();
        userService.setUserDao(userDao);
    }

    /**
     * Tests create method in userService
     */
    @Test
    public void testCreate() {
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        when(userDao.get(null)).thenReturn(users);
        when(userDao.create(isA(User.class))).thenReturn(true);
        assertTrue(userService.createUser("tim"));
        when(userDao.create(isA(User.class))).thenReturn(false);
        assertFalse(userService.createUser("9184721"));
    }

    @Test
    public void test_createUser_withUserNameAndPw_notExist_success() {
        // Arrange
        String userName = "foo";
        String pw = "bar";
        String publicKey = "baz";
        when(userDao.create(any())).thenReturn(true);

        // Act
        boolean result = userService.createUser(userName, pw, publicKey);

        // Assert
        assertTrue(result);
    }

    @Test
    public void test_createUser_withUserNameAndPw_tExist_fail() {
        // Arrange
        String userName = "foo";
        String pw = "bar";
        String publicKey = "baz";
        when(userDao.create(any())).thenReturn(false);

        // Act
        boolean result = userService.createUser(userName, pw, publicKey);

        // Assert
        assertFalse(result);
    }

    /**
     * Tests getAllUsers method in userService
     */
    @Test
    public void testGetAllUsers() {
        userService.createUser("alice");
        userService.createUser("bob");
        userService.createUser("charlie");
        List<User> usersMock = new ArrayList<>();
        usersMock.add(new User("alice", null, null, null, null, null));
        usersMock.add(new User("bob", null, null, null, null, null));
        usersMock.add(new User("charlie", null, null, null, null, null));
        when(userDao.get(null)).thenReturn(usersMock);
        assertEquals(3, userService.getAllUsers().size());
    }

    /**
     * Tests getUserByMap method in userService
     */
    @Test
    public void testGetUserByMap() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        User user = new User("david", null, null, null, null, null);
        user.setId(id);
        userDao.create(user);
        Map<String, Object> searchMap = new HashMap<>();
        searchMap.put(Constants.USER_USER_ID_KEY, id);

        List<User> usersMock = new ArrayList<>();
        usersMock.add(user);
        when(userDao.get(searchMap)).thenReturn(usersMock);
        assertEquals(usersMock, userService.getUsersByMap(searchMap));
    }

    /**
     * Tests updateUser method in userService
     */
    @Test
    public void testUpdateUser() {
        User initial = new User("john", null, null, null, null, null);
        initial.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
        User updated = new User("jonathan", null, null, null, null, null);
        updated.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
        when(userDao.set(isA(User.class))).thenReturn(true);
        assertTrue(userService.updateUser(updated));
        when(userDao.set(isA(User.class))).thenReturn(false);
        assertFalse(userService.updateUser(updated));
    }

    /**
     * Tests deleteUser method in userService
     */
    @Test
    public void testDeleteUser() {
        User user = new User("alice", null, null, null, null, null);
        userDao.create(user);
        userService.createUser("bob");
        when(userDao.delete(isA(User.class))).thenReturn(true);
        assertTrue(userService.deleteUser(user));
        when(userDao.delete(isA(User.class))).thenReturn(false);
        assertFalse(userService.deleteUser(user));
    }

    /**
     * Tests getUserByName method in userService
     */
    @Test
    public void testGetGroupByName() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        User user = new User("alice", null, null, null, null, null);
        user.setId(id);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        when(userService.getUsersByMap(anyMap())).thenReturn(userList);
        assertEquals("5399aba6e4b0ae375bfdca88",
                userService.getUserByName("alice").getId().toString());
    }
}
