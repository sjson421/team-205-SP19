package edu.northeastern.ccs.im.services;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;

import edu.northeastern.ccs.im.dao.Constants;
import edu.northeastern.ccs.im.dao.InvitationDao;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.User;

/**
 * Unit tests for InvitationService
 * @author Jay Son
 *
 */
public class InvitationServiceTest {
	/**
	 * InvitationService to test
	 */
	InvitationService invitationService;
	/**
	 * Mocked InvitationDao for usage in above service
	 */
	InvitationDao invitationDao = mock(InvitationDao.class);

	/**
	 * Initializes test service with mocked DAO
	 */
	@Before
	public void initialize() {
		invitationService = new InvitationService();
		invitationService.setInvitationDao(invitationDao);
	}

	/**
	 * Tests create method in invitationService
	 */
	@Test
	public void testCreate() {
		when(invitationDao.create(isA(Invitation.class))).thenReturn(true);
		assertNotNull(invitationService.createInvitation(null, null, null, false, null));
		when(invitationDao.create(isA(Invitation.class))).thenReturn(false);
		assertNull(invitationService.createInvitation(null, null, null, false, null));
	}

	/**
	 * Tests getAllInvitations method in invitationService
	 */
	@Test
	public void testGetAllInvitations() {
		invitationService.createInvitation(null, null, null, false, null);
		invitationService.createInvitation(null, null, null, false, null);
		invitationService.createInvitation(null, null, null, false, null);
		List<Invitation> mock = new ArrayList<>();
		mock.add(new Invitation(null, null, null, false, null));
		mock.add(new Invitation(null, null, null, false, null));
		mock.add(new Invitation(null, null, null, false, null));
		when(invitationDao.get(null)).thenReturn(mock);
		assertEquals(3, invitationService.getAllInvitations().size());
	}
	/**
	 * Tests getInvitationByMap method in invitationService
	 */
	@Test
	public void testGetInvitationByMap() {
		ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
		Invitation invitation = new Invitation(null, null, null, false, null);
		invitation.setId(id);
		invitationDao.create(invitation);
		Map<String, Object> searchMap = new HashMap<String, Object>();
		searchMap.put(Constants.INVITATION_INVITATION_ID_KEY, id);
		List<Invitation> mock = new ArrayList<>();
		mock.add(invitation);
		when(invitationDao.get(searchMap)).thenReturn(mock);
		assertEquals(mock, invitationService.getInvitationsByMap(searchMap));
	}
	/**
	 * Tests updateInvitation method in invitationService
	 */
	@Test
	public void testUpdateInvitation() {
		Invitation initial = new Invitation(null, null, null, false, null);
		initial.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
		Invitation updated = new Invitation(null, null, null, true, null);
		updated.setId(new ObjectId("5399aba6e4b0ae375bfdca88"));
		when(invitationDao.set(isA(Invitation.class))).thenReturn(true);
		assertTrue(invitationService.updateInvitation(updated));
		when(invitationDao.set(isA(Invitation.class))).thenReturn(false);
		assertFalse(invitationService.updateInvitation(updated));
	}

	/**
	 * Tests deleteInvitation method in invitationService
	 */
	@Test
	public void testDeleteInvitation() {
		Invitation invitation = new Invitation(null, null, null, false, null);
		invitationDao.create(invitation);
		invitationService.createInvitation(null, null, null, true, null);
		when(invitationDao.delete(isA(Invitation.class))).thenReturn(true);
		assertTrue(invitationService.deleteInvitation(invitation));
		when(invitationDao.delete(isA(Invitation.class))).thenReturn(false);
		assertFalse(invitationService.deleteInvitation(invitation));
	}

	/**
	 * Tests approveRejectGroupInvite method in invitationService
	 */
	@Test
	public void testGroupInvite() {
		Group reject = new Group("group reject", null);
		Invitation rejectInvite = new Invitation(null, null, reject, false, null);
		assertFalse(invitationService.approveRejectGroupInvite(null, reject, rejectInvite, false));
		
		Group accept = new Group("group accept", null);
		Invitation acceptInvite = new Invitation(null, null, accept, false, null);
		assertTrue(invitationService.approveRejectGroupInvite(null, accept, acceptInvite, true));
	}


	/**
	 * Tests getGroupByID method in groupService
	 */
	@Test
	public void testGetGroupByID() {
		ObjectId id = new ObjectId("5399aba6e4b0ae375bfdca88");
		Invitation invite = new Invitation(new User("alice", null, null, null, null, null),
						null, null, null, null);
		invite.setId(id);
		List<Invitation> inviteList = new ArrayList<>();
		inviteList.add(invite);
		when(invitationService.getInvitationsByMap(anyMap())).thenReturn(inviteList);
		assertEquals("alice",
						invitationService.getInvitationByID("5399aba6e4b0ae375bfdca88").getInviter().getUsername().toString());
	}
}
