package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.northeastern.ccs.im.dao.Constants.USER_COLLECTION_NAME;
import static edu.northeastern.ccs.im.dao.Constants.USER_USER_ID_KEY;
import static edu.northeastern.ccs.im.dao.Constants.USER_USER_NAME_KEY;

/**
 * A UserDao is a Data Access Object that abstracts the connection to the database pertaining to User info.
 */
public class UserDao implements Dao<User> {
    private UserBridge userBridge;

    public UserDao() {
        this.userBridge = new UserBridge();
    }

    @Override
    public List<User> get(Map<String, Object> criteria) {
        List<BasicDBObject> usersDBObjects = DaoUtils.getDBObjectsFromDBMatchingCriteria(
                USER_COLLECTION_NAME, criteria);

        return usersDBObjects
                .stream()
                .map(userDbObject -> userBridge.toObject(userDbObject))
                .collect(Collectors.toList());
    }

    @Override
    public boolean set(User changedVersion) {
        BasicDBObject userAsDBOject = userBridge.toDBObject(changedVersion);

        return DaoUtils.updateDBObjectInDB(
                USER_COLLECTION_NAME, USER_USER_ID_KEY,
                changedVersion.getId(), userAsDBOject, USER_COLLECTION_NAME);
    }

    @Override
    public boolean create(User object) {
        BasicDBObject userAsDBOject = userBridge.toDBObject(object);

        DaoUtils.createIndexInDBIfEmpty(USER_COLLECTION_NAME, USER_USER_ID_KEY, USER_USER_NAME_KEY);
        return DaoUtils.insertDBOjectInDB(USER_COLLECTION_NAME, userAsDBOject, USER_COLLECTION_NAME);
    }

    @Override
    public boolean delete(User object) {
        BasicDBObject userAsDBOject = userBridge.toDBObject(object);

        return DaoUtils.deleteObjectFromDB(USER_COLLECTION_NAME, userAsDBOject);
    }
}