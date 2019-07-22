package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.dao.GroupDao;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GroupService
 *
 * @author Jay Son
 */
public class GroupServiceTest {
    /**
     * GroupService to test
     */
    GroupService groupService;
    /**
     * Mocked GroupDao for usage in above service
     */
    GroupDao groupDao = mock(GroupDao.class);

    /**
     * Initializes test service with mocked DAO
     */
    @Before
    public void initialize() {
        groupService = new GroupService();
        groupService.setGroupDao(groupDao);
    }

    /**
     * tests create in groupService
     */
    @Test
    public void testCreate() {
        when(groupDao.create(isA(Group.class))).thenReturn(true);
        assertTrue(groupService.createGroup("bonk", null));
        when(groupDao.create(isA(Group.class))).thenReturn(false);
        assertFalse(groupService.createGroup("bonk", null));
    }

    /**
     * Tests getAllGroups method in groupService
     */
    @Test
    public void testGetAllGroups() {
        groupService.createGroup("alice", null);
        groupService.createGroup("bob", null);
        groupService.createGroup("charlie", null);
        List<Group> mock = new ArrayList<>();
        mock.add(new Group("one", null));
        mock.add(new Group("two", null));
        mock.add(new Group("three", null));
        when(groupDao.get(null)).thenReturn(mock);
        assertEquals(3, groupService.getAllGroups().size());
    }

    /**
     * Tests getGroupByMap method in groupService
     */
    @Test
    public void testGetGroupByMap() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        Group group = new Group("group", null);
        group.setId(id);
        groupDao.create(group);
        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put(Constants.GROUP_GROUP_ID_KEY, id);
        List<Group> mock = new ArrayList<>();
        mock.add(group);
        when(groupDao.get(searchMap)).thenReturn(mock);
        assertEquals(mock, groupService.getGroupsByMap(searchMap));
    }

    /**
     * Tests updateGroup method in groupService
     */
    @Test
    public void testUpdateGroup() {
        Group initial = new Group("best group", null);
        initial.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
        Group updated = new Group("updated group", null);
        updated.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
        when(groupDao.set(isA(Group.class))).thenReturn(true);
        assertTrue(groupService.updateGroup(updated));
        when(groupDao.set(isA(Group.class))).thenReturn(false);
        assertFalse(groupService.updateGroup(updated));
    }

    /**
     * Tests deleteGroup method in groupService
     */
    @Test
    public void testDeleteGroup() {
        Group group = new Group("group", null);
        groupDao.create(group);
        groupService.createGroup("group 2", null);
        when(groupDao.delete(isA(Group.class))).thenReturn(true);
        assertTrue(groupService.deleteGroup(group));
        when(groupDao.delete(isA(Group.class))).thenReturn(false);
        assertFalse(groupService.deleteGroup(group));
    }

    /**
     * Tests containsUser method in groupService
     */
    @Test
    public void testContainsUser() {
        Group group = new Group("group", new ArrayList<>());
        User user1 = new User("alice", null, null, null,null, null);
        User user2 = new User("bob", null, null, null,null, null);

        group.addUser(user1);

        assertTrue(groupService.containsUser(group, user1));
        assertFalse(groupService.containsUser(group, user2));
    }

    /**
     * Tests getGroupByName method in groupService
     */
    @Test
    public void testGetGroupByName() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        Group group = new Group("group", null);
        group.setId(id);
        List<Group> groupList = new ArrayList<>();
        groupList.add(group);
        when(groupService.getGroupsByMap(anyMap())).thenReturn(groupList);
        assertEquals("5399aba6e4b0ae375bfdca88",
                groupService.getGroupByName("group").getId().toString());
    }
}
