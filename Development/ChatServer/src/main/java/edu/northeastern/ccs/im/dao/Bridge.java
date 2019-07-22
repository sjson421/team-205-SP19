package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

import java.util.List;

/**
 * Serves as a Bridge between our POJOs and how they are represented in the DB.
 * @param <T> The POJO type
 */
public interface Bridge<T> {
    /**
     * Given an document in the database, represents that object as the appropriate POJO
     * @param basicDBObject the document in DB
     * @return a POJO with the same information
     */
    T toObject(BasicDBObject basicDBObject);

    /**
     * Given a list of documents in the database, represents those objects as the appropriate POJO
     * @param basicDBList the documents in DB
     * @return a List of POJO with the same information
     */
    List<T> toObjects(BasicDBList basicDBList);

    /**
     * Given a POJO, converts it into the appropriate DB representation.
     * @param object the POJO
     * @return the DB object representation
     */
    BasicDBObject toDBObject(T object);

    /**
     * Given a list of POJO, converts it into a list of the appropriate DB representation.
     * @param objects the POJOs
     * @return the DB object representations
     */
    BasicDBList toDBObjects(List<T> objects);
}