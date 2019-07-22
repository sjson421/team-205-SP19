package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Invitation;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.northeastern.ccs.im.dao.Constants.INVITATION_COLLECTION_NAME;
import static edu.northeastern.ccs.im.dao.Constants.INVITATION_INVITATION_ID_KEY;

/**
 * A InvitationDao is a Data Access Object that abstracts the connection to the database pertaining to Invitation info.
 */
public class InvitationDao implements Dao<Invitation> {
    private Bridge<Invitation> invitationBridge;

    public InvitationDao() {
        this.invitationBridge = new InvitationBridge();
    }

    @Override
    public List<Invitation> get(Map<String, Object> criteria) {
        List<BasicDBObject> invitationDBObjects = DaoUtils.getDBObjectsFromDBMatchingCriteria(
                INVITATION_COLLECTION_NAME, criteria);

        return invitationDBObjects
                .stream()
                .map(invitationDBObject -> invitationBridge.toObject(invitationDBObject))
                .collect(Collectors.toList());
    }

    @Override
    public boolean set(Invitation changedVersion) {
        BasicDBObject invitationAsDBObject = invitationBridge.toDBObject(changedVersion);

        return DaoUtils.updateDBObjectInDB(
                INVITATION_COLLECTION_NAME, INVITATION_INVITATION_ID_KEY,
                changedVersion.getId(), invitationAsDBObject, INVITATION_COLLECTION_NAME);
    }

    @Override
    public boolean create(Invitation object) {
        BasicDBObject invitationAsDBObject = invitationBridge.toDBObject(object);

        DaoUtils.createIndexInDBIfEmpty(INVITATION_COLLECTION_NAME, INVITATION_INVITATION_ID_KEY);
        return DaoUtils.insertDBOjectInDB(INVITATION_COLLECTION_NAME, invitationAsDBObject, INVITATION_COLLECTION_NAME);
    }

    @Override
    public boolean delete(Invitation object) {
        BasicDBObject invitationAsDBObject = invitationBridge.toDBObject(object);

        return DaoUtils.deleteObjectFromDB(INVITATION_COLLECTION_NAME, invitationAsDBObject);
    }
}
