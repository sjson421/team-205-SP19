package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests model Invitation
 *
 * @author Jay Son
 */
public class InvitationTest {
    /**
     * Invitation to be tested
     */
    Invitation invitation;

    /**
     * Initializes invitation to test
     */
    @Before
    public void initialize() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        User inviter = new User("inviter", null, null, null, null, null);
        inviter.setId(id);
        User invitee = new User("invitee", null, null, null, null, null);
        invitee.setId(id);
        Group group = new Group("my group", null);
        group.setId(id);

        invitation = new Invitation(inviter, invitee, group, false, null);
        invitation.setId(id);
    }

    /**
     * Tests all methods of Invitation
     */
    @Test
    public void test() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        Group testGroup = new Group("not my group", null);
        testGroup.setId(id);
        User test = new User("test", null, null, null, null, null);
        test.setId(id);
        assertEquals("5399aba6e4b0ae375bfdca88", invitation.getId().toString());
        invitation.setInviter(test);
        assertEquals(test, invitation.getInviter());
        invitation.setInvitee(test);
        assertEquals(test, invitation.getInvitee());
        invitation.setGroup(testGroup);
        assertEquals(testGroup, invitation.getGroup());
        invitation.setNeedsModeratorApproval(true);
        assertEquals(true, invitation.getNeedsModeratorApproval());
        invitation.setApprovingModerator(test);
        assertEquals(test, invitation.getApprovingModerator());
        assertEquals("(5399aba6e4b0ae375bfdca88, (5399aba6e4b0ae375bfdca88, test, null, null, null, null), "
                + "(5399aba6e4b0ae375bfdca88, test, null, null, null, null), (5399aba6e4b0ae375bfdca88, "
                + "not my group, null, []), true, (5399aba6e4b0ae375bfdca88, test, null, null, null, null))", invitation.toString());
    }

    @Test
    public void testInvitationStatus() {
        ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
        Group testGroup = new Group("not my group", null);
        testGroup.setId(id);
        User test = new User("test", null, null, null, null, null);
        test.setId(id);
        assertEquals("5399aba6e4b0ae375bfdca88", invitation.getId().toString());
        invitation.setInvitationStatus(InvitationStatus.CREATED);
        assertEquals(InvitationStatus.CREATED, invitation.getInvitationStatus());

        invitation.setInvitationStatus(InvitationStatus.PENDING);
        assertEquals(InvitationStatus.PENDING, invitation.getInvitationStatus());

        invitation.setInvitationStatus(InvitationStatus.APPROVED);
        assertEquals(InvitationStatus.APPROVED, invitation.getInvitationStatus());

        invitation.setInvitationStatus(InvitationStatus.DENIED);
        assertEquals(InvitationStatus.DENIED, invitation.getInvitationStatus());

        // test check invitation is processed
        assertEquals(true, invitation.isInvitationProcessed());
        invitation.setInvitationStatus(InvitationStatus.CREATED);
        assertEquals(false, invitation.isInvitationProcessed());
    }
}
