package edu.northeastern.ccs.im.services;

import edu.northeastern.ccs.im.dao.Dao;
import edu.northeastern.ccs.im.dao.UserDao;
import edu.northeastern.ccs.im.models.User;
import edu.northeastern.ccs.im.utils.PasswordHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.northeastern.ccs.im.dao.Constants.USER_USER_NAME_KEY;


/**
 * Service for performing database operations on UserDao with model User
 *
 * @author Jay Son
 */
public class UserService {
    /**
     * Associated User DAO for performing database operations
     */
    Dao<User> userDao;

    /**
     * Creates a new User DAO on the service
     */
    public UserService() {
        userDao = new UserDao();
    }

    /**
     * Sets associated DAO
     *
     * @param userDao User DAO for performing database operations
     */
    public void setUserDao(Dao<User> userDao) {
        this.userDao = userDao;
    }

    /**
     * Creates User on the database. Username must be unique in the database to
     * create.
     *
     * @param username Username to be used for user
     * @return Success status of user creation by User DAO
     */
    public Boolean createUser(String username) {
        User user = new User(username, null, null, null, null, null);
        return userDao.create(user);
    }


    public Boolean createUser(String userName, String pw, String publicKey) {
        byte[] salt = PasswordHelper.getSalt();
        String saltString = PasswordHelper.convertByteToString(salt);
        String hash = PasswordHelper.getPasswordHashString(pw, salt);

        User user = new User(userName, hash, saltString, publicKey, null, null);
        return userDao.create(user);
    }

    /**
     * Gets all users on the database
     *
     * @return All users stored on the database
     */
    public List<User> getAllUsers() {
        return userDao.get(null);
    }

    /**
     * Gets users on the database based on a map of (<Search criteria key>, <value
     * to search by>)
     *
     * @param searchMap Search criteria as a key, value pair
     * @return All users matching the criteria given by searchMap
     */
    public List<User> getUsersByMap(Map<String, Object> searchMap) {
        return userDao.get(searchMap);
    }

    /**
     * Updates User with the same ID as updatedUser.
     *
     * @param updatedUser Updated User
     * @return Success status of user update by user DAO
     */
    public Boolean updateUser(User updatedUser) {
        return userDao.set(updatedUser);
    }

    /**
     * Deletes User from database
     *
     * @param user User to be deleted
     * @return Success status of user deletion by user DAO
     */
    public boolean deleteUser(User user) {
        return userDao.delete(user);
    }

    /**
     * This is a helper method. Retrieve a user by its name.
     *
     * @param userName the given input for user name
     * @return user that have the given name
     */
    public User getUserByName(String userName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(USER_USER_NAME_KEY, userName);
        List<User> userList = getUsersByMap(map);

        if (userList.isEmpty()) {
            return null;
        }
        return userList.get(0);
    }
}
