package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import edu.northeastern.ccs.im.models.Message;
import edu.northeastern.ccs.im.models.User;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;

import static edu.northeastern.ccs.im.dao.Constants.MESSAGE_COLLECTION_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests MessageDao
 */
public class MessageDaoTest {
    private MessageDao msgDao;
    private UserDao userDao;

    private MongoConnector mongoConnector;

    private MongoClient internalMongoClient;
    private MongoServer internalMongoServer;

    /**
     * Sets up connection
     */
    @Before
    public void setUp() {
        internalMongoServer = new MongoServer(new MemoryBackend());
        InetSocketAddress serverAddress = internalMongoServer.bind();
        internalMongoClient = new MongoClient(new ServerAddress(serverAddress));
        mongoConnector = new MongoConnector(internalMongoClient, internalMongoClient.getDatabase("testDB"));
        DaoUtils.setFakeMongoConnector(mongoConnector);
        userDao = new UserDao();
        msgDao = new MessageDao();
    }

    /**
     * Tests CRUD operations
     */
    @Test
    public void testMessageCRUD() {
        List<Message> messages = msgDao.get(null);
        Assert.assertEquals(0, messages.size());

        User alice = new User("alice", null, null, null, null, null);
        User bob = new User("bob", null, null, null, null, null);
        userDao.create(alice);
        userDao.create(bob);
        Message msg = new Message(new Date(), alice, bob, "Hey there!", false);
        Message msg2 = new Message(new Date(), alice, bob, "Oops!", false);

        Assert.assertTrue(msgDao.create(msg));
        Assert.assertFalse(msgDao.create(msg));
        Assert.assertTrue(msgDao.create(msg2));

        messages = msgDao.get(null);
        Assert.assertEquals(2, messages.size());

        msg.setMessageBody("Goodbye!");
        Assert.assertTrue(msgDao.set(msg));

        messages = msgDao.get(null);
        Assert.assertEquals(2, messages.size());
        Assert.assertEquals(msg.getMessageBody(), messages.get(0).getMessageBody());

        for (Message message : messages) {
            Assert.assertTrue(msgDao.delete(message));
        }
        messages = msgDao.get(null);
        Assert.assertEquals(0, messages.size());

        Assert.assertFalse(msgDao.delete(msg));

        messages = msgDao.get(null);
        Assert.assertEquals(0, messages.size());
    }

    /**
     * Tests message creation
     */
    @Test
    public void testCreateMessageMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        MessageDao createMessageDao = new MessageDao();

        User user = new User("john", null, null, null, null, null);
        User bob = new User("bob", null, null, null, null, null);
        Message msg = new Message(new Date(), user, bob, "hello", false);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(MESSAGE_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);

        // Case 1 - False Path (no WriteException, for some reason count not updated)
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0));
        Assert.assertFalse(createMessageDao.create(msg));

        // Case 2- True Path (no WriteException, )
        when(mockMongoCollection.countDocuments()).thenReturn(new Long(0)).thenReturn(new Long(0)).thenReturn(new Long(1));
        Assert.assertTrue(createMessageDao.create(msg));
    }

    /**
     * Tests message modification
     */
    @Test
    public void testSetMessageMock() {
        MongoConnector mockMongoConnector = mock(MongoConnector.class);
        MongoDatabase mockMongoDatabase = mock(MongoDatabase.class);
        MongoCollection<BasicDBObject> mockMongoCollection = mock(MongoCollection.class);
        UpdateResult mockUpdateResult = mock(UpdateResult.class);
        DaoUtils.setFakeMongoConnector(mockMongoConnector);

        MessageDao setMessageDao = new MessageDao();

        User user = new User("alice", null, null, null, null, null);
        User bob = new User("bob", null, null, null, null, null);
        Message msg = new Message(new Date(), user, bob, "hello", false);

        when(mockMongoConnector.getMongoDatabase()).thenReturn(mockMongoDatabase);
        when(mockMongoDatabase.getCollection(MESSAGE_COLLECTION_NAME, BasicDBObject.class)).thenReturn(mockMongoCollection);
        when(mockMongoCollection.replaceOne(any(), any())).thenReturn(mockUpdateResult);

        // Case 1 - Branch first condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(false);
        Assert.assertFalse(setMessageDao.set(msg));

        // Case 2 - Branch second condition fails
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(0));
        Assert.assertFalse(setMessageDao.set(msg));

        // Case 3 - Both conditions in branch pass
        when(mockUpdateResult.isModifiedCountAvailable()).thenReturn(true);
        when(mockUpdateResult.getModifiedCount()).thenReturn(new Long(1));
        Assert.assertTrue(setMessageDao.set(msg));
    }

    /**
     * Closes connection for this test
     */
    @After
    public void tearDown() {
        this.mongoConnector.closeConnection();
        this.internalMongoClient.close();
        this.internalMongoServer.shutdownNow();
        this.internalMongoServer.shutdown();
    }
}