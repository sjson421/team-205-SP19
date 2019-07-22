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
import edu.northeastern.ccs.im.models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import static edu.northeastern.ccs.im.dao.Constants.GROUP_COLLECTION_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GroupDaoTest {
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
    }

    @Test
    public void testGroupCRUD() {
        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);

        userDao.create(user);
        userDao.create(user2);

        List<User> admin = new ArrayList<>();
        admin.add(user);
        List<User> members = new ArrayList<>();
        members.add(user);
        members.add(user2);

        Group group = new Group("AGroup", admin);
        group.setUsers(members);

        List<Group> groups = groupDao.get(null);
        Assert.assertEquals(0, groups.size());

        Assert.assertTrue(groupDao.create(group));

        // cannot create the same group again (same group_id)
        Assert.assertFalse(groupDao.create(group));

        groups = groupDao.get(null);
        Assert.assertEquals(1, groups.size());

        // Creating group with same name as group 1, will fail
        Group group2 = new Group("AGroup", admin);
        Assert.assertFalse(groupDao.create(group2));
        groups = groupDao.get(null);
        Assert.assertEquals(1, groups.size());
        // Creating same group now with unique name, will pass
        group2.setGroupName("UniqueGroup");
        Assert.assertTrue(groupDao.create(group2));
        groups = groupDao.get(null);
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(2, groups.get(0).getUsers().size());

        group.setUsers(group.getUsers().subList(0, 1));
        Assert.assertTrue(groupDao.set(group));

        // setting Group 1 name to group 2's name, will fail, confirm and revert
        group.setGroupName(group2.getGroupName());
        Assert.assertFalse(groupDao.set(group));
        group.setGroupName("AGroup");

        groups = groupDao.get(null);
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(1, groups.get(0).getUsers().size());

        userDao.delete(user);

        groups = groupDao.get(null);
        Assert.assertEquals(2, groups.size());
        Assert.assertEquals(1, groups.get(0).getUsers().size());
        Assert.assertNull(groups.get(0).getUsers().get(0));

        Assert.assertTrue(groupDao.delete(group));
        Assert.assertTrue(groupDao.delete(group2));
        // delete will fail the second time, group doesn't exist
        Assert.assertFalse(groupDao.delete(group));

        groups = groupDao.get(null);
        Assert.assertEquals(0, groups.size());

        userDao.delete(user);
        userDao.delete(user2);
    }

    @Test
    public void testCreateGroupMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        GroupDao createGroupDao = new GroupDao();

        User user = new User("alice", null, null, null, null, null);
        List<User> admin = new ArrayList<>();
        admin.add(user);

        Group group = new Group("group", admin);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(GROUP_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);

        // Case 1 - False Path (no WriteException, for some reason count not updated)
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0));
        Assert.assertFalse(createGroupDao.create(group));

        // Case 2- True Path (no WriteException, )
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0)).thenReturn(new Long(1));
        Assert.assertTrue(createGroupDao.create(group));
    }

    @Test
    public void testSetGroupMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        UpdateResult mockUpdateResult = mock(UpdateResult.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        GroupDao setGroupDao = new GroupDao();

        User user = new User("alice", null, null, null, null, null);
        List<User> admin = new ArrayList<>();
        admin.add(user);

        Group group = new Group("group", admin);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(GROUP_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);
        when(mockMongoCollection.replaceOne(any(), any())).thenReturn(mockUpdateResult);

        // Case 1 - Branch first condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(false);
        Assert.assertFalse(setGroupDao.set(group));

        // Case 2 - Branch second condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(0));
        Assert.assertFalse(setGroupDao.set(group));

        // Case 3 - Both conditions in branch pass
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(1));
        Assert.assertTrue(setGroupDao.set(group));
    }

    @After
    public void tearDown() {
        this.mongoConnector.closeConnection();
        this.internalMongoClient.close();
        this.internalMongoServer.shutdownNow();
        this.internalMongoServer.shutdown();
    }
}
