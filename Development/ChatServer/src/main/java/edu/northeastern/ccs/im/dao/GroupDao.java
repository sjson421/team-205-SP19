package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Group;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.northeastern.ccs.im.dao.Constants.GROUP_COLLECTION_NAME;
import static edu.northeastern.ccs.im.dao.Constants.GROUP_GROUP_ID_KEY;
import static edu.northeastern.ccs.im.dao.Constants.GROUP_GROUP_NAME_KEY;

/**
 * A GroupDao is a Data Access Object that abstracts the connection to the database pertaining to Group info.
 */
public class GroupDao implements Dao<Group> {
    private Bridge<Group> groupBridge;

    public GroupDao() {
        this.groupBridge = new GroupBridge();
    }

    @Override
    public List<Group> get(Map<String, Object> criteria) {
        List<BasicDBObject> groupDBObjects = DaoUtils.getDBObjectsFromDBMatchingCriteria(GROUP_COLLECTION_NAME, criteria);

        return groupDBObjects
                .stream()
                .map(groupDBObject -> groupBridge.toObject(groupDBObject))
                .collect(Collectors.toList());
    }

    @Override
    public boolean set(Group changedVersion) {
        BasicDBObject groupAsDBObject = groupBridge.toDBObject(changedVersion);

        return DaoUtils.updateDBObjectInDB(
                GROUP_COLLECTION_NAME, GROUP_GROUP_ID_KEY,
                changedVersion.getId(), groupAsDBObject, GROUP_COLLECTION_NAME);
    }

    @Override
    public boolean create(Group object) {
        BasicDBObject groupAsDBObject = groupBridge.toDBObject(object);

        DaoUtils.createIndexInDBIfEmpty(GROUP_COLLECTION_NAME, GROUP_GROUP_ID_KEY, GROUP_GROUP_NAME_KEY);
        return DaoUtils.insertDBOjectInDB(GROUP_COLLECTION_NAME, groupAsDBObject, GROUP_COLLECTION_NAME);
    }

    @Override
    public boolean delete(Group object) {
        BasicDBObject groupAsDBObject = groupBridge.toDBObject(object);

        return DaoUtils.deleteObjectFromDB(GROUP_COLLECTION_NAME, groupAsDBObject);
    }
}