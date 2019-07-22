package edu.northeastern.ccs.im.dao;

import java.util.List;
import java.util.Map;

/**
 * A Dao is a Data Acess Object that abstracts the connection to the database.
 *
 * @param <T> The type of dao (should be a valid type, i.e., a class in the pojo package)
 */
public interface Dao<T> {

    /**
     * Gets a list of T matching some criteria
     *
     * @param criteria - a map of attrribute names to their values
     * @return List of T
     */
    List<T> get(Map<String, Object> criteria);

    /**
     * Update a document in the database that T represents, where the given object has the changes needed.
     *
     * @param changedVersion the changed version of the object of type T
     * @return true if the update was successful, false otherwise
     */
    boolean set(T changedVersion);

    /**
     * Creates a new document representing the given object of type T.
     *
     * @param object the given object to create a document for in the database
     * @return true if the create was successful, false otherwise
     */
    boolean create(T object);

    /**
     * Deletes a document in the database that T represents
     *
     * @param object the object of type T to delete from database
     * @return true if the delete was successful, false otherwise
     */
    boolean delete(T object);
}