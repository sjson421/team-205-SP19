package edu.northeastern.ccs.im.dao;

/**
 * Database constants for ease of use
 */
public class Constants {

    private Constants() {}

    /**
     * MongoConnector Value Names
     */
    static final String PROD = "prod";
    static final String CLOUD = "cloud";
    static final String LOCAL = "local";

    /**
     * Properties from dao.properties name scheme
     */
    static final String MODE_PROP = "db.mode";
    static final String DB_URL_PROP = "db.url";
    static final String DB_USER_PROP = "db.user";
    static final String DB_AUTH_PROP = "db.password";
    static final String DB_HOSTS_PROP = "db.hosts";
    static final String DB_REPLICASET_PROP = "db.replicaSet";
    static final String DB_AUTH_SOURCE = "db.authSource";
    static final String DB_DBNAME_PROP = "db.dbName";

    /**
     * USER Collection Attribute Names
     */
    public static final String USER_COLLECTION_NAME = "user";
    public static final String USER_USER_ID_KEY = "user_id";
    public static final String USER_USER_NAME_KEY = "user_name";
    public static final String USER_USER_PW_HASH_KEY = "pw_hash";
    public static final String USER_USER_SALT_KEY = "salt";
    public static final String USER_USER_LOGINS_KEY = "logins";
    public static final String USER_USER_LOGOUTS_KEY = "logouts";
    public static final String USER_USER_PUBLIC_KEY_KEY = "public_key";

    /**
     * GROUP Collection Attribute Names
     */
    public static final String GROUP_COLLECTION_NAME = "group";
    public static final String GROUP_GROUP_ID_KEY = "group_id";
    public static final String GROUP_GROUP_NAME_KEY = "group_name";
    public static final String GROUP_GROUP_ADMINS_KEY = "administrators";
    public static final String GROUP_GROUP_USERS_KEY = "members";

    /**
     * INVITATION Collection Attribute Names
     */
    public static final String INVITATION_COLLECTION_NAME = "invitation";
    public static final String INVITATION_INVITATION_ID_KEY = "invite_id";
    public static final String INVITATION_INVITER_ID_KEY = "inviter_id";
    public static final String INVITATION_INVITEE_ID_KEY = "invitee_id";
    public static final String INVITATION_GROUP_ID_KEY = "group_id";
    public static final String INVITATION_MODERATOR_APPROVAL_NEEDED_KEY = "needs_approval";
    public static final String INVITATION_APPROVED_BY_KEY = "approver_id";

    /**
     * MESSAGE Collection Attribute Names
     */
    public static final String MESSAGE_COLLECTION_NAME = "message";
    public static final String MESSAGE_MESSAGE_ID_KEY = "message_id";
    public static final String MESSAGE_MESSAGE_TIMESTAMP_SENT_KEY = "timestamp_sent";
    public static final String MESSAGE_SENDER_ID_KEY = "sender_id";
    public static final String MESSAGE_RECEIVER_ID_KEY = "receiver_id";
    public static final String MESSAGE_MESSAGE_BODY_KEY = "message_body";
    public static final String MESSAGE_DELETED_KEY = "message_deleted";
}
