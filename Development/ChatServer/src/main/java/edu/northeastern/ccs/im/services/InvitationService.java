package edu.northeastern.ccs.im.services;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.ccs.im.dao.Dao;
import edu.northeastern.ccs.im.dao.GroupDao;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.dao.InvitationDao;
import edu.northeastern.ccs.im.models.InvitationStatus;
import edu.northeastern.ccs.im.models.User;

import static edu.northeastern.ccs.im.dao.Constants.INVITATION_INVITATION_ID_KEY;

/**
 * Service for performing database operations on InvitationDao with model
 * Invitation
 * 
 * @author Jay Son
 *
 */
public class InvitationService {
	/**
	 * Associated Invitation DAO for performing database operations
	 */
	Dao<Invitation> invitationDao;
	Dao<Group> groupDao;

	/**
	 * Creates a new Invitation DAO on the service
	 */
	public InvitationService() {
		invitationDao = new InvitationDao();
		groupDao = new GroupDao();
	}

	/**
	 * Sets associated DAO
	 * 
	 * @param invitationDao Invitation DAO for performing database operations
	 */
	public void setInvitationDao(Dao<Invitation> invitationDao) {
		this.invitationDao = invitationDao;
	}

  /**
   * sets group dao
   * @param groupDao group dao
   */
	public void setGroupDao(Dao<Group> groupDao) {
	  this.groupDao = groupDao;
  }

	/**
	 * Creates Invitation on the database
	 * 
	 * @param inviter                User who invited
	 * @param invitee                User to be invited
	 * @param group                  Group that invitee will be invited to
	 * @param needsModeratorApproval Boolean for whether the current invitation
	 *                               needs administrator approval
	 * @param approvedBy             Nullable. Administrator to approve invitation
	 * @return Success status of Invitation creation by Invitation DAO
	 */
	public String createInvitation(User inviter, User invitee, Group group, Boolean needsModeratorApproval,
			User approvedBy) {
		Invitation invitation = new Invitation(inviter, invitee, group, needsModeratorApproval, approvedBy);
		if (!invitationDao.create(invitation)) {
				return null;
		}
		return invitation.getId().toString();
	}

	/**
	 * Gets all invitations on the database
	 * 
	 * @return All Invitations stored in the database
	 */
	public List<Invitation> getAllInvitations() {
		return invitationDao.get(null);
	}

	/**
	 * Gets invitations on the database based on a map of (<Search criteria key>,
	 * <value to search by>)
	 * 
	 * @param searchMap Search criteria as a key, value pair
	 * @return All invitations matching the criteria given by searchMap
	 */
	public List<Invitation> getInvitationsByMap(Map<String, Object> searchMap) {
		return invitationDao.get(searchMap);
	}

	/**
	 * Updates Invitation with the same ID as updatedInvitation.
	 * 
	 * @param updatedInvitation Updated Invitation
	 * @return Success status of Invitation update by Invitation DAO
	 */
	public Boolean updateInvitation(Invitation updatedInvitation) {
		return invitationDao.set(updatedInvitation);
	}

	/**
	 * Deletes Invitation from database
	 * 
	 * @param invitation Invitation to be deleted
	 * @return Success status of Invitation deletion by Invitation DAO
	 */
	public boolean deleteInvitation(Invitation invitation) {
		return invitationDao.delete(invitation);
	}

	/**
	 * Approve or reject invitation from group by user
	 * 
	 * @param user       User who is accepting or reject the invitation
	 * @param group      Group that the user is invited to
	 * @param invitation Invitation sent from a group
	 * @param approved   True is user approves invitation and false if declined
	 * @return If approved, returns success status of the group's addition of the
	 *         user. Returns false if user declines invitation.
	 */
	public boolean approveRejectGroupInvite(User user, Group group, Invitation invitation, Boolean approved) {
		deleteInvitation(invitation);
		if (approved) {
			return group.addUser(user);
		}
		return false;
	}

  /**
   * Update the status of invitation to given status.
   * @param invite the invitation
   * @param status the new status of invitation
   */
	public void updateInvitationToNewStatus(Invitation invite, InvitationStatus status) {

			if (status == InvitationStatus.APPROVED) {
				Group currGroup = invite.getGroup();
				currGroup.addUser(invite.getInvitee());
				groupDao.set(currGroup);
			}
  }

	/**
	 * This is a helper method. Retrieve an invitation by its id.
	 *
	 * @param id the id of a given invitation
	 * @return the invitation that has given id
	 */
	public Invitation getInvitationByID(String id) {
		Map<String, Object> map = new HashMap<>();
		map.put(INVITATION_INVITATION_ID_KEY, new ObjectId(id));
		List<Invitation> invitation = getInvitationsByMap(map);

		if (invitation.isEmpty()) {
			return null;
		}
		return invitation.get(0);
	}
}
