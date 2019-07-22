package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests model Group
 *
 * @author Jay Son
 */
public class GroupTest {
    /**
     * Group to be tested
     */
    Group group;
    /**
     * Admins for group
     */
    List<User> admins;
    /**
     * Users/members of group
     */
    List<User> users;
    /**
     * Subgroups for group
     */
    List<Group> subGroups;

    /**
     * Initializes group to test
     */
    @Before
    public void initialize() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        User user1 = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        User user3 = new User("charlie", null, null, null, null, null);
        user1.setId(id);
        user2.setId(id);
        user3.setId(id);
        admins = new ArrayList<>();
        admins.add(user1);
        users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
        users.add(user3);
        Group subGroup1 = new Group("sub1", admins);
        Group subGroup2 = new Group("sub2", admins);
        subGroups = new ArrayList<>();
        subGroups.add(subGroup1);
        subGroups.add(subGroup2);

        group = new Group("Team 205", admins);
    }

    /**
     * Tests all methods
     */
    @Test
    public void test() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        group.setId(id);
        assertEquals("5399aba6e4b0ae375bfdca88", group.getId().toString());
        group.setGroupName("Team 24");
        assertEquals("Team 24", group.getGroupName());

        admins.remove(0);
        User user = new User("dan", null, null, null, null, null);
        user.setId(id);
        admins.add(user);

        group.setAdministrators(admins);
        assertEquals("dan", group.getAdministrators().get(0).getUsername());
        group.setUsers(users);
        assertEquals("charlie", group.getUsers().get(2).getUsername());

        group.setSubgroups(subGroups);
        assertEquals("sub2", group.getSubgroups().get(1).getGroupName());

        group.addUser(user);
        assertEquals("dan", group.getUsers().get(3).getUsername());
        assertEquals("(5399aba6e4b0ae375bfdca88, Team 24, [(5399aba6e4b0ae375bfdca88, dan, null, null, null, null)], "
                + "[(5399aba6e4b0ae375bfdca88, alice, null, null, null, null), (5399aba6e4b0ae375bfdca88, bob, null, null, null, null), "
                + "(5399aba6e4b0ae375bfdca88, charlie, null, null, null, null), (5399aba6e4b0ae375bfdca88, dan, null, null, null, null)])", group.toString());
    }
}
