package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * Bridges the gap between Invitation in DB and Invitation - converts BasicDBObject to Invitation and vice versa.
 */
public class InvitationBridge implements Bridge<Invitation> {

    private UserDao userDao;
    private GroupDao groupDao;

    InvitationBridge() {
        this.userDao = new UserDao();
        this.groupDao = new GroupDao();
    }

    InvitationBridge(UserDao userDao, GroupDao groupDao) {
        this.userDao = userDao;
        this.groupDao = groupDao;
    }

    @Override
    public Invitation toObject(BasicDBObject basicDBObject) {
        Invitation invitation = new Invitation(
                getUserById(basicDBObject.getObjectId(INVITATION_INVITER_ID_KEY, null)),
                getUserById(basicDBObject.getObjectId(INVITATION_INVITEE_ID_KEY, null)),
                getGroupId(basicDBObject.getObjectId(INVITATION_GROUP_ID_KEY, null)),
                basicDBObject.getBoolean(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY),
                getUserById((basicDBObject.getObjectId(INVITATION_APPROVED_BY_KEY, null))));
        invitation.setId(basicDBObject.getObjectId(INVITATION_INVITATION_ID_KEY));
        return invitation;
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

    private Group getGroupId(ObjectId objectId) {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(Constants.GROUP_GROUP_ID_KEY, objectId);
        List<Group> groups = groupDao.get(criteria);
        if (groups.size() == 1) {
            return groups.get(0);
        } else {
            // Log here about no users

            return null;
        }
    }

    @Override
    public List<Invitation> toObjects(BasicDBList basicDBList) {
        List<Invitation> invitations = new ArrayList<>();
        basicDBList.forEach(invitationDBObject -> invitations.add(toObject((BasicDBObject) invitationDBObject)));
        return invitations;
    }

    @Override
    public BasicDBObject toDBObject(Invitation object) {
        BasicDBObject invitationDBObject = new BasicDBObject(INVITATION_INVITATION_ID_KEY, object.getId())
                .append(INVITATION_INVITEE_ID_KEY, object.getInvitee().getId())
                .append(INVITATION_MODERATOR_APPROVAL_NEEDED_KEY, object.getNeedsModeratorApproval())
                .append(INVITATION_GROUP_ID_KEY, object.getGroup().getId());

        if (object.getInviter() != null) {
            invitationDBObject.append(INVITATION_INVITER_ID_KEY, object.getInviter().getId());
        }

        if (object.getApprovingModerator() != null) {
            invitationDBObject.append(INVITATION_APPROVED_BY_KEY, object.getApprovingModerator().getId());
        }
        return invitationDBObject;
    }

    @Override
    public BasicDBList toDBObjects(List<Invitation> objects) {
        BasicDBList invitationDBList = new BasicDBList();
        objects.forEach(invitation -> invitationDBList.add(toDBObject(invitation)));
        return invitationDBList;
    }
}