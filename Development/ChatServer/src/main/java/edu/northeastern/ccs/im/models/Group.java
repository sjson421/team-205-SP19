package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for Group
 *
 * @author Jay Son
 */
public class Group {
    /**
     * Group ID
     */
    private ObjectId id;
    /**
     * Group name
     */
    private String groupName;
    /**
     * Administrators for the group
     */
    private List<User> administrators;
    /**
     * Users under this group
     */
    private List<User> users;
    /**
     * Subgroups for this group. Subgroups are also groups.
     */
    private List<Group> subgroups;

    /**
     * Initializes Group
     *
     * @param groupName      Group name
     * @param administrators Initial administrators for the group
     */
    public Group(String groupName, List<User> administrators) {
        super();
        this.id = new ObjectId();
        this.groupName = groupName;
        this.administrators = administrators;
        this.users = new ArrayList<>();
        this.subgroups = new ArrayList<>();
        if (administrators != null) {
          users.addAll(administrators);
        }
    }

    /**
     * @return group id
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * @param id group id to set
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * @return group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * @param groupName group name to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @return group administrators
     */
    public List<User> getAdministrators() {
        return administrators;
    }

    /**
     * @param administrators group administrators to set
     */
    public void setAdministrators(List<User> administrators) {
        this.administrators = administrators;
    }

    /**
     * @return group users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * @param users group users to set
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * @return subgroups for this group
     */
    public List<Group> getSubgroups() {
        return subgroups;
    }

    /**
     * @param subgroups subgroups to set under this group
     */
    public void setSubgroups(List<Group> subgroups) {
        this.subgroups = subgroups;
    }

    /**
     * Adds a user under this group
     *
     * @param user user to add
     * @return Success status of user addition into group
     */
    public Boolean addUser(User user) {
        return this.users.add(user);
    }

    /**
     * Returns all fields as a string
     */
    public String toString() {
        return String.format("(%s, %s, %s, %s)", this.id, this.groupName, this.administrators, this.users);
    }
}
