package edu.northeastern.ccs.im.models;

/**
 * Enumeration for the different status of invitation.
 *
 * @author Emma Qiu
 */
public enum InvitationStatus {

  /**
   * Invitation created
   */
  CREATED("CRA"),

  /**
   * Invitation is pending for any decision
   */
  PENDING("PEN"),

  /**
   * Invitation is approved
   */
  APPROVED("APV"),

  /**
   * Invitation is denied
   */
  DENIED("DEN");

  /**
   * Store the short name for invitation status
   */
  private String abbreviation;

  /**
   * Define the message type and specify its short name.
   * @param abbreviation short name for status
   */
  InvitationStatus(String abbreviation) {
    this.abbreviation = abbreviation;
  }

  @Override
  public String toString() {
    return abbreviation;
  }
}
