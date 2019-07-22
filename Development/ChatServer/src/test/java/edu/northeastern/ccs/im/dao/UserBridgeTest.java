package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.User;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * Tests that ensure Users are converted properly into BasicDBObjects and vice versa
 */
public class UserBridgeTest {

    private UserBridge userBridge;

    @Before
    public void initialize() {
        this.userBridge = new UserBridge();
    }

    @Test
    public void testToObject() {
        ObjectId id = new ObjectId(1000, 1);
        BasicDBObject userDBObject = new BasicDBObject(USER_USER_NAME_KEY, "Alice");
        userDBObject.append(USER_USER_ID_KEY, id);

        User user = new User("Alice", null, null, null, null, null);
        user.setId(id);

        Assert.assertEquals(user.toString(), userBridge.toObject(userDBObject).toString());
    }

    @Test
    public void testToObjects() {
        ObjectId id = new ObjectId(1000, 1);
        ObjectId id2 = new ObjectId(1000, 2);

        BasicDBObject userDBObject = new BasicDBObject(USER_USER_NAME_KEY, "Alice");
        userDBObject.append(USER_USER_ID_KEY, id);
        BasicDBObject userDBObject2 = new BasicDBObject(USER_USER_NAME_KEY, "Bob");
        userDBObject2.append(USER_USER_ID_KEY, id2);

        BasicDBList userDBList = new BasicDBList();
        userDBList.add(userDBObject);
        userDBList.add(userDBObject2);

        User user = new User("Alice", null, null, null, null, null);
        user.setId(id);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(id2);

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        Assert.assertEquals(users.toString(), userBridge.toObjects(userDBList).toString());
    }

    @Test
    public void testToDBObject() {
        ObjectId id = new ObjectId(1000, 1);
        BasicDBObject userDBObject = new BasicDBObject(USER_USER_ID_KEY, id);
        userDBObject.append(USER_USER_NAME_KEY, "Alice");
        userDBObject.append(USER_USER_PW_HASH_KEY, null);
        userDBObject.append(USER_USER_SALT_KEY, null);
        userDBObject.append(USER_USER_PUBLIC_KEY_KEY, null);
        userDBObject.append(USER_USER_LOGINS_KEY, null);
        userDBObject.append(USER_USER_LOGOUTS_KEY, null);

        User user = new User("Alice", null, null, null, null, null);
        user.setId(id);

        Assert.assertEquals(userDBObject, userBridge.toDBObject(user));
    }

    @Test
    public void testToDBObjects() {
        ObjectId id = new ObjectId(1000, 1);
        ObjectId id2 = new ObjectId(1000, 2);

        BasicDBObject userDBObject = new BasicDBObject(USER_USER_NAME_KEY, "Alice");
        userDBObject.append(USER_USER_ID_KEY, id);
        userDBObject.append(USER_USER_PW_HASH_KEY, null);
        userDBObject.append(USER_USER_SALT_KEY, null);
        userDBObject.append(USER_USER_PUBLIC_KEY_KEY, null);
        userDBObject.append(USER_USER_LOGINS_KEY, null);
        userDBObject.append(USER_USER_LOGOUTS_KEY, null);
        BasicDBObject userDBObject2 = new BasicDBObject(USER_USER_NAME_KEY, "Bob");
        userDBObject2.append(USER_USER_ID_KEY, id2);
        userDBObject2.append(USER_USER_PW_HASH_KEY, null);
        userDBObject2.append(USER_USER_SALT_KEY, null);
        userDBObject2.append(USER_USER_LOGINS_KEY, null);
        userDBObject2.append(USER_USER_PUBLIC_KEY_KEY, null);
        userDBObject2.append(USER_USER_LOGOUTS_KEY, null);

        BasicDBList userDBList = new BasicDBList();
        userDBList.add(userDBObject);
        userDBList.add(userDBObject2);

        User user = new User("Alice", null, null, null, null, null);
        user.setId(id);
        User user2 = new User("Bob", null, null, null, null, null);
        user2.setId(id2);

        List<User> users = new ArrayList<>();
        users.add(user);
        users.add(user2);

        Assert.assertEquals(userDBList, userBridge.toDBObjects(users));
    }

}
