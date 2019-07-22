package edu.northeastern.ccs.im.models;

import org.junit.Assert;
import org.junit.Test;

/**
 * This is the test file for InvitationStatus Enum class.
 * @author Emma Qiu
 */

public class InvitationStatusTest {
  /**
   * Test enum has right abbreviation
   */
  @Test
  public void test_toString_returnRightAbbreviation() {
    Assert.assertEquals("CRA", InvitationStatus.CREATED.toString());
    Assert.assertEquals("PEN", InvitationStatus.PENDING.toString());
    Assert.assertEquals("APV", InvitationStatus.APPROVED.toString());
    Assert.assertEquals("DEN", InvitationStatus.DENIED.toString());
  }
}