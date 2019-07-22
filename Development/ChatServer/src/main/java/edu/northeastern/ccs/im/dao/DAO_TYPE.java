package edu.northeastern.ccs.im.dao;

/**
 * Different DAO_Types. Each one refers to a different type of entity. Used when instantiating Daos from other layers.
 */
public enum DAO_TYPE {
    USER,
    GROUP,
    INVITATION,
    MESSAGE
}