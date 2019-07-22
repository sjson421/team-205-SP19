package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.dao.Dao;
import edu.northeastern.ccs.im.dao.GroupDao;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.GROUP_GROUP_NAME_KEY;

/**
 * Service for performing database operations on GroupDao with model Group
 *
 * @author Jay Son
 */
public class GroupService {
    /**
     * Associated Group DAO for performing database operations
     */
    Dao<Group> groupDao;

    /**
     * Creates a new Group DAO on the service
     */
    public GroupService() {
        groupDao = new GroupDao();
    }

    /**
     * Sets associated DAO
     *
     * @param groupDao Group DAO for performing database operations
     */
    public void setGroupDao(Dao<Group> groupDao) {
        this.groupDao = groupDao;
    }

    /**
     * Creates Group on the database
     *
     * @param groupName      Name of group to create
     * @param administrators Initial administrators for the group
     * @return Success status of group creation by group DAO
     */
    public Boolean createGroup(String groupName, List<User> administrators) {
        Group group = new Group(groupName, administrators);
        return groupDao.create(group);
    }

    /**
     * Gets all groups in the database
     *
     * @return All groups stored on the database
     */
    public List<Group> getAllGroups() {
        return groupDao.get(null);
    }

    /**
     * Gets groups on the database based on a map of
     * (<Search criteria key>, <value to search by>)
     *
     * @param searchMap Search criteria as a key, value pair
     * @return All groups matching the criteria given by searchMap
     */
    public List<Group> getGroupsByMap(Map<String, Object> searchMap) {
        return groupDao.get(searchMap);
    }

    /**
     * Updates Group with the same ID as updatedGroup
     *
     * @param updatedGroup Updated Group
     * @return Success status of group update by group DAO
     */
    public Boolean updateGroup(Group updatedGroup) {
        return groupDao.set(updatedGroup);
    }

    /**
     * Deletes Group from database
     *
     * @param group Group to be deleted
     * @return Success status of group deletion by group DAO
     */
    public boolean deleteGroup(Group group) {
        return groupDao.delete(group);
    }

    /**
     * Returns whether the given group contains the given user
     *
     * @param group Group that may or may not contain the user
     * @param user  User who may or may not be in the group
     * @return Boolean for whether the user exists in the group
     */
    public boolean containsUser(Group group, User user) {
        return group.getUsers().contains(user) || group.getAdministrators().contains(user);
    }

    /**
     * Get a list of admin ppl of a group.
     * @param group a given group
     * @return admins of group
     */
    public List<User> getAdmin(Group group) {
        return group.getAdministrators();
    }

    /**
     * Add user to the given group.
     * @param invitee person who got invited
     * @param group given group
     */
    public void addUserToAGroup(User invitee, Group group) {
        group.addUser(invitee);
    }

    /**
     * This is a helper method. Retrieve a group by its group name.
     *
     * @param groupName the given input for group name
     * @return a group that has given name
     */
    public Group getGroupByName(String groupName) {
        Map<String, Object> map = new HashMap<>();
        map.put(GROUP_GROUP_NAME_KEY, groupName);
        List<Group> groups = getGroupsByMap(map);

        if (groups.isEmpty()) {
            return null;
        }
        return groups.get(0);
    }
}
