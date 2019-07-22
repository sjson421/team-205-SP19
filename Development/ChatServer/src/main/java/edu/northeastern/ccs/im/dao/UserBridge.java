package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * Bridges the gap between User in DB and User - converts BasicDBObject to User and vice versa
 */
class UserBridge implements Bridge<User> {

    @Override
    public User toObject(BasicDBObject basicDBObject) {
        User user = new User(
                basicDBObject.getString(USER_USER_NAME_KEY),
                basicDBObject.getString(USER_USER_PW_HASH_KEY),
                basicDBObject.getString(USER_USER_SALT_KEY),
                basicDBObject.getString(USER_USER_PUBLIC_KEY_KEY),
                (List<Date>) basicDBObject.get(USER_USER_LOGINS_KEY),
                (List<Date>) basicDBObject.get(USER_USER_LOGOUTS_KEY));
        user.setId(basicDBObject.getObjectId(USER_USER_ID_KEY));

        return user;
    }

    @Override
    public List<User> toObjects(BasicDBList basicDBList) {
        List<User> users = new ArrayList<>();
        basicDBList.forEach(userDBObject -> users.add(toObject((BasicDBObject) userDBObject)));
        return users;
    }

    @Override
    public BasicDBObject toDBObject(User object) {
        return new BasicDBObject(USER_USER_ID_KEY, object.getId())
                .append(USER_USER_NAME_KEY, object.getUsername())
                .append(USER_USER_PW_HASH_KEY, object.getPwHash())
                .append(USER_USER_SALT_KEY, object.getSalt())
                .append(USER_USER_PUBLIC_KEY_KEY, object.getPublicKey())
                .append(USER_USER_LOGINS_KEY, object.getLogins())
                .append(USER_USER_LOGOUTS_KEY, object.getLogouts());
    }

    @Override
    public BasicDBList toDBObjects(List<User> objects) {
        BasicDBList usersDBList = new BasicDBList();
        objects.forEach(user -> usersDBList.add(toDBObject(user)));
        return usersDBList;
    }
}