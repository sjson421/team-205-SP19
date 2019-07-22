package edu.northeastern.ccs.im.dao;

/**
 * A Factory to create the appropriate DAO based on the provided DAO type.
 */
public class DaoFactory {
    private DaoFactory() {
    }

    /**
     * Creates a DAO based on given DAO Type
     * @param daoType the DAO type enum
     * @return an appropriate DAO, if there exists one for the dao type, or an exception otherwise
     */
    public static Dao createDao(DAO_TYPE daoType) {
        if (daoType == DAO_TYPE.USER) {
            return new UserDao();
        } else if (daoType == DAO_TYPE.GROUP) {
            return new GroupDao();
        } else if (daoType == DAO_TYPE.INVITATION) {
            return new InvitationDao();
        } else if (daoType == DAO_TYPE.MESSAGE) {
            return new MessageDao();
        } else {
            throw new IllegalArgumentException("dao type " + daoType + " has no valid" +
                    "Dao associated with it!");
        }
    }
}