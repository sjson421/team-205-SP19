package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;

/**
 * Model for Invitation
 * @author Jay Son
 *
 */
public class Invitation {
	/**
	 * Invitation id
	 */
	private ObjectId id;
	/**
	 * User who is inviting from the group
	 */
	private User inviter;
	/**
	 * User who is to be invited to the group
	 */
	private User invitee;
	/**
	 * Group that the user is being invited to
	 */
	private Group group;
	/**
	 * Whether this invitation requires an administrator to be sent out
	 */
	private Boolean needsModeratorApproval;
    /**
     * Nullable. Administrator to approve this invitation's sending.
     */
    private User approvingModerator;

    //Emma
    private InvitationStatus status;

	/**
	 * Initializes Invitation
	 * @param inviter User who is inviting from the group
	 * @param invitee User who is to be invited to the group
	 * @param group Group that the user is being invited to
	 * @param needsModeratorApproval Whether this invitation requires an administrator to be sent out
	 * @param approvingModerator Nullable. Administrator to approve this invitation's sending.
	 */
	public Invitation(User inviter, User invitee, Group group, Boolean needsModeratorApproval,
			User approvingModerator) {
		super();
		this.id = new ObjectId();
		this.inviter = inviter;
		this.invitee = invitee;
		this.group = group;
		this.needsModeratorApproval = needsModeratorApproval;
		this.approvingModerator = approvingModerator;
		//Emma
		this.status = InvitationStatus.CREATED;
	}

	/**
	 * @return invitation id
	 */
	public ObjectId getId() {
		return id;
	}

	/**
	 * @param id invitation id to set
	 */
	public void setId(ObjectId id) {
		this.id = id;
	}

	/**
	 * @return inviter
	 */
	public User getInviter() {
		return inviter;
	}

	/**
	 * @param inviter inviter
	 */
	public void setInviter(User inviter) {
		this.inviter = inviter;
	}

	/**
	 * @return user getting invite
	 */
	public User getInvitee() {
		return invitee;
	}

	/**
	 * @param invitee user who should get the invite
	 */
	public void setInvitee(User invitee) {
		this.invitee = invitee;
	}

	/**
	 * @return Group for invitation
	 */
	public Group getGroup() {
		return group;
	}

	/**
	 * @param group Group for invitation
	 */
	public void setGroup(Group group) {
		this.group = group;
	}

	/**
	 * @return Whether this invitation requires an administrator to be sent out
	 */
	public Boolean getNeedsModeratorApproval() {
		return needsModeratorApproval;
	}

	/**
	 * @param needsModeratorApproval Whether this invitation requires an administrator to be sent out
	 */
	public void setNeedsModeratorApproval(Boolean needsModeratorApproval) {
		this.needsModeratorApproval = needsModeratorApproval;
	}

	/**
	 * @return Nullable. Administrator to approve this invitation's sending.
	 */
	public User getApprovingModerator() {
		return approvingModerator;
	}

	/**
	 * @param approvingModerator Nullable. Administrator to approve this invitation's sending.
	 */
	public void setApprovingModerator(User approvingModerator) {
		this.approvingModerator = approvingModerator;
	}
	/**
	 * Returns all fields of invitation as a string
	 */
	public String toString() {
        return String.format("(%s, %s, %s, %s, %s, %s)", this.id, this.inviter, this.invitee, 
        		this.group, this.needsModeratorApproval, this.approvingModerator);
	}

	/**
	 * Returns the invite status.
	 * @return invite status
	 */
	//Emma
	public InvitationStatus getInvitationStatus() {
		return status;
	}

	/**
	 * Change the invitation status.
	 * @param newStatus the new status of invitation
	 */
	//Emma
	public void setInvitationStatus(InvitationStatus newStatus) {
		this.status = newStatus;
	}

	/**
	 * Check whether the invitation has been approved/denied or not.
	 * @return true if the invitation status is approve or denied. Otherwise return false
	 */
	//Emma
	public boolean isInvitationProcessed() {
		return this.status == InvitationStatus.APPROVED || this.status == InvitationStatus.DENIED;
	}
}
