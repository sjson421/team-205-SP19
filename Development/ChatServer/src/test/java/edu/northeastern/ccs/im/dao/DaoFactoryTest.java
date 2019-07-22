package edu.northeastern.ccs.im.dao;

import edu.northeastern.ccs.im.models.Group;
import edu.northeastern.ccs.im.models.Invitation;
import edu.northeastern.ccs.im.models.User;
import org.junit.Assert;
import org.junit.Test;

public class DaoFactoryTest {

    @Test
    public void testCreateDao() {
        Dao<User> userDao = DaoFactory.createDao(DAO_TYPE.USER);
        Assert.assertEquals(userDao.getClass(), UserDao.class);

        Dao<Group> groupDao = DaoFactory.createDao(DAO_TYPE.GROUP);
        Assert.assertEquals(groupDao.getClass(), GroupDao.class);

        Dao<Invitation> invitationDao = DaoFactory.createDao(DAO_TYPE.INVITATION);
        Assert.assertEquals(invitationDao.getClass(), InvitationDao.class);

        Dao<Invitation> messageDao = DaoFactory.createDao(DAO_TYPE.MESSAGE);
        Assert.assertEquals(messageDao.getClass(), MessageDao.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateFail() {
        Dao<User> invitationDao = DaoFactory.createDao(null);
    }
}
