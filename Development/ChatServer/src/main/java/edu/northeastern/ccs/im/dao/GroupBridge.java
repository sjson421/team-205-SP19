package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * Bridges the gap between Group in DB and Group - converts BasicDBObject to Group and vice versa.
 */
class GroupBridge implements Bridge<Group> {
    private UserDao userDao;

    GroupBridge() {
        this.userDao = new UserDao();
    }

    GroupBridge(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Group toObject(BasicDBObject basicDBObject) {
        Group group = new Group(
                basicDBObject.getString(GROUP_GROUP_NAME_KEY),
                getUsersById((BasicDBList) basicDBObject.get(GROUP_GROUP_ADMINS_KEY)));
        group.setId(basicDBObject.getObjectId(GROUP_GROUP_ID_KEY));
        group.setUsers(getUsersById((BasicDBList) basicDBObject.get(GROUP_GROUP_USERS_KEY)));
        return group;
    }

    private User getUserById(ObjectId objectId) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.USER_USER_ID_KEY, objectId);
        List<User> users = userDao.get(criteria);
        if (users.size() == 1) {
            return users.get(0);
        } else {
            // Log here about no users
            return null;
        }
    }

    private List<User> getUsersById(BasicDBList objectIds) {
        return objectIds.stream().map(objectId -> getUserById((ObjectId) objectId )).collect(Collectors.toList());
    }


    @Override
    public List<Group> toObjects(BasicDBList basicDBList) {
        List<Group> groups = new ArrayList<>();
        basicDBList.forEach(groupDBObject -> groups.add(toObject((BasicDBObject) groupDBObject)));
        return groups;
    }

    @Override
    public BasicDBObject toDBObject(Group object) {
        BasicDBList administratorsIds = new BasicDBList();
        administratorsIds.addAll(object.getAdministrators().stream().map(User::getId).collect(Collectors.toList())) ;
        BasicDBList memberIds = new BasicDBList();
        memberIds.addAll(object.getUsers().stream().map(User::getId).collect(Collectors.toList()));
        return new BasicDBObject(GROUP_GROUP_ID_KEY, object.getId())
                .append(GROUP_GROUP_NAME_KEY, object.getGroupName())
                .append(GROUP_GROUP_ADMINS_KEY, administratorsIds)
                .append(GROUP_GROUP_USERS_KEY, memberIds);
    }

    @Override
    public BasicDBList toDBObjects(List<Group> objects) {
        BasicDBList groupsDBList = new BasicDBList();
        objects.forEach(group -> groupsDBList.add(toDBObject(group)));
        return groupsDBList;
    }
}