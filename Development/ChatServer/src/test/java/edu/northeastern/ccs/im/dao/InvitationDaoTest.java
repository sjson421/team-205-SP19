package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.INVITATION_COLLECTION_NAME;
import static edu.northeastern.ccs.im.dao.Constants.INVITATION_INVITATION_ID_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvitationDaoTest {
    private InvitationDao invitationDao;
    private GroupDao groupDao;
    private UserDao userDao;

    private MongoConnector mongoConnector;

    private MongoClient internalMongoClient;
    private MongoServer internalMongoServer;

    @Before
    public void setUp() {
        internalMongoServer = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = internalMongoServer.bind();
        internalMongoClient = new MongoClient(new ServerAddress(serverAddress));

        mongoConnector = new MongoConnector(internalMongoClient, internalMongoClient.getDatabase("testDB"));
        DaoUtils.setFakeMongoConnector(mongoConnector);
        userDao = new UserDao();
        groupDao = new GroupDao();
        invitationDao = new InvitationDao();
    }

    @Test
    public void testInvitationCRUD() {
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        userDao.create(user);
        userDao.create(user2);

        List<User> admin = new ArrayList<>();
        admin.add(user);

        List<User> members = new ArrayList<>();
        members.add(user);

        Group group = new Group("AGroup", admin);
        group.setUsers(members);
        groupDao.create(group);

        List<Invitation> invitations = invitationDao.get(null);
        Assert.assertEquals(0, invitations.size());

        Invitation invitation = new Invitation(user, user2, group, true, null);
        Assert.assertTrue(invitationDao.create(invitation));

        // Cannot create the same invitation again (same invitation_id)
        Assert.assertFalse(invitationDao.create(invitation));

        invitations = invitationDao.get(null);
        Assert.assertEquals(1, invitations.size());

        invitation.setNeedsModeratorApproval(false);
        Assert.assertTrue(invitationDao.set(invitation));

        Map<String, Object> updateInviteCriteria = new HashMap<>();
        updateInviteCriteria.put(INVITATION_INVITATION_ID_KEY, invitation.getId());
        Assert.assertEquals(
                invitation.getNeedsModeratorApproval(),
                invitationDao.get(updateInviteCriteria).get(0).getNeedsModeratorApproval());

        invitations = invitationDao.get(null);
        Assert.assertEquals(1, invitations.size());

        groupDao.delete(group);

        invitations = invitationDao.get(null);
        Assert.assertEquals(1, invitations.size());
        Assert.assertNull(invitations.get(0).getGroup());

        Assert.assertTrue(invitationDao.delete(invitation));
        invitations = invitationDao.get(null);
        Assert.assertEquals(0, invitations.size());

        // Cannot delete invitation that doesn't exist
        Assert.assertFalse(invitationDao.delete(invitation));

        groupDao.delete(group);
        userDao.delete(user);
        userDao.delete(user2);
    }

    @Test
    public void testCreateInviteMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        InvitationDao createInvitationDao = new InvitationDao();

        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        List<User> admin = new ArrayList<>();
        admin.add(user);

        Group group = new Group("group", admin);

        Invitation invitation = new Invitation(user, user2, group, false, null);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(INVITATION_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);

        // Case 1 - False Path (no WriteException, for some reason count not updated)
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0));
        Assert.assertFalse(createInvitationDao.create(invitation));

        // Case 2- True Path (no WriteException, )
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0)).thenReturn(new Long(1));
        Assert.assertTrue(createInvitationDao.create(invitation));
    }

    /**
     * Separate test to mock inviteCollection to test all branches of set()
     */
    @Test
    public void testSetInviteMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        UpdateResult mockUpdateResult = mock(UpdateResult.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        InvitationDao setInvitationDao = new InvitationDao();

        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        List<User> admin = new ArrayList<>();
        admin.add(user);

        Group group = new Group("group", admin);

        Invitation invitation = new Invitation(user, user2, group, false, null);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(INVITATION_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);
        when(mockMongoCollection.replaceOne(any(), any())).thenReturn(mockUpdateResult);

        // Case 1 - Branch first condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(false);
        Assert.assertFalse(setInvitationDao.set(invitation));

        // Case 2 - Branch second condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(0));
        Assert.assertFalse(setInvitationDao.set(invitation));

        // Case 3 - Both conditions in branch pass
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(1));
        Assert.assertTrue(setInvitationDao.set(invitation));
    }

    @After
    public void tearDown() {
        this.mongoConnector.closeConnection();
        this.internalMongoClient.close();
        this.internalMongoServer.shutdownNow();
        this.internalMongoServer.shutdown();
    }
}
