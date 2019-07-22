package edu.northeastern.ccs.im.models;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Model for User
 *
 * @author Jay Son
 */
public class User {
    /**
     * user id
     */
    private ObjectId id;
    /**
     * user username
     */
    private String username;

    /**
     * user password
     */
    private String pwHash;

    /**
     * public key for encryption
     */
    private String salt;

    /**
     * public key
     */
    private String publicKey;

    /**
     * Last login time for message queueing
     */
    private List<Date> logins;

    /**
     * Last logout time for message queueing
     */
    private List<Date> logouts;

    /**
     * Initializes user with given username
     *
     * @param username username to give this user
     * @param pwHash  password for this user
     * @param salt     encryption key
     */
    public User(String username, String pwHash, String salt, String publicKey, List<Date> logins, List<Date> logouts) {
        this.id = new ObjectId();
        this.username = username;
        this.pwHash = pwHash;
        this.salt = salt;
        this.logins = logins;
        this.logouts = logouts;
        this.publicKey = publicKey;
    }

    /**
     * Gets user id
     *
     * @return user id
     */
    public ObjectId getId() {
        return id;
    }

    /**
     * Sets user id
     *
     * @param id user id to set
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    /**
     * Gets user username
     *
     * @return user username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set user username
     *
     * @param username user username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return password
     */
    public String getPwHash() {
        return pwHash;
    }

    /**
     * set password
     */
    public void setPwHash(String pwHash) {
        this.pwHash = pwHash;
    }

    /**
     * @return salt
     */
    public String getSalt() {
        return salt;
    }

    /**
     * sets salt
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * get public key
     * @return public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * set public key
     * @param publicKey public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return login list
     */
    public List<Date> getLogins() {
        return logins;
    }

    /**
     * sets login list
     */
    public void setLogins(List<Date> logins) {
        this.logins = logins;
    }

    /**
     * @return logout list
     */
    public List<Date> getLogouts() {
        return logouts;
    }

    /**
     * sets logout list
     */
    public void setLogouts(List<Date> logouts) {
        this.logouts = logouts;
    }

    /**
     * add login time to login list
     */
    public void addLogin(Date lastLogin) {
        if (logins == null) {
            logins = new ArrayList<>();
        }
        logins.add(lastLogin);
    }

    /**
     * add logout time to login list
     */
    public void addLogout(Date lastLogout) {
        if (logouts == null) {
            logouts = new ArrayList<>();
        }
        logouts.add(lastLogout);
    }

    /**
     * returns all fields of this user as a string
     */
    public String toString() {
        return "(" + id + ", " + username + ", " + pwHash + ", " + salt + ", " + logins + ", " + logouts + ")";
    }

    /**
     * check if it equals
     *
     * @param obj obj to compare
     * @return true if it contains same user id
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        User user = (User) obj;
        return user.id.equals(this.id);
    }

    /**
     * use object id's hash code
     *
     * @return object id's hash code
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
