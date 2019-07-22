package edu.northeastern.ccs.im.utils;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("squid:S00100") // testing naming convention is different
public class PasswordHelperTest {
    @Test
    public void test_getSalt_getRandomSalt() {
        byte[] salt = PasswordHelper.getSalt();
        assertEquals(16, salt.length);
    }

    @Test
    public void test_getPasswordHashString_withStringAndSalt_success() {
        String pw = "foo";
        byte[] salt = PasswordHelper.getSalt();

        String hash = PasswordHelper.getPasswordHashString(pw, salt);

        assertTrue(hash.length() > 0);
    }
}
