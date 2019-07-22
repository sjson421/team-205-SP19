package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import edu.northeastern.ccs.im.models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.List;

import static edu.northeastern.ccs.im.dao.Constants.USER_COLLECTION_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserDaoTest {
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
    }

    @Test
    public void testUserCRUD() {
        List<User> users = userDao.get(null);
        Assert.assertEquals(0, users.size());

        User user = new User("alice", null, null, null, null, null);
        User user2 = new User("bob", null, null, null, null, null);
        User user3 = new User("bob", null, null, null, null, null);

        Assert.assertTrue(userDao.create(user));
        Assert.assertTrue(userDao.create(user2));
        // Cannot create the same user again
        Assert.assertFalse(userDao.create(user2));
        // username "bob" already used, new user cannot also have username bob
        Assert.assertFalse(userDao.create(user3));

        users = userDao.get(null);
        Assert.assertEquals(2, users.size());

        user.setUsername("NotAlice");
        Assert.assertTrue(userDao.set(user));
        // bob already exists, NotAlice cannot change username to bob
        user.setUsername("bob");
        Assert.assertFalse(userDao.set(user));
        // have to set user object's name back to the right thing
        user.setUsername("NotAlice");

        users = userDao.get(null);
        Assert.assertEquals(2, users.size());
        Assert.assertEquals(user.getUsername(), users.get(0).getUsername());

        users.forEach(aUser -> Assert.assertTrue(userDao.delete(aUser)));

        users = userDao.get(null);
        Assert.assertEquals(0, users.size());

        // Cannot delete user that does not exist
        Assert.assertFalse(userDao.delete(user));

        users = userDao.get(null);
        Assert.assertEquals(0, users.size());
    }

    @Test
    public void testCreateUserMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);
        UserDao createUserDao = new UserDao();

        User user = new User("alice", null, null, null, null, null);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(USER_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);

        // Case 1 - False Path (no WriteException, for some reason count not updated)
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0));
        Assert.assertFalse(createUserDao.create(user));

        // Case 2- True Path (no WriteException, )
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0)).thenReturn(new Long(1));
        Assert.assertTrue(createUserDao.create(user));
    }

    @Test
    public void testSetUserMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        UpdateResult mockUpdateResult = mock(UpdateResult.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        UserDao setUserDao = new UserDao();

        User user = new User("alice", null, null, null, null, null);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(USER_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);
        when(mockMongoCollection.replaceOne(any(), any())).thenReturn(mockUpdateResult);

        // Case 1 - Branch first condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(false);
        Assert.assertFalse(setUserDao.set(user));

        // Case 2 - Branch second condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(0));
        Assert.assertFalse(setUserDao.set(user));

        // Case 3 - Both conditions in branch pass
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(1));
        Assert.assertTrue(setUserDao.set(user));
    }

    @After
    public void tearDown() {
        this.mongoConnector.closeConnection();
        this.internalMongoClient.close();
        this.internalMongoServer.shutdownNow();
        this.internalMongoServer.shutdown();
    }
}
