package edu.northeastern.ccs.im.dao;


import com.mongodb.BasicDBObject;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.result.UpdateResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DaoUtils extracts a lot of the common functionality that exists in every Dao
 */
public class DaoUtils {

    private static MongoConnector fakeMongoConnector;

    private DaoUtils() {}

    private static Logger logger = LogManager.getLogger();

    /**
     * For the given collection, creates an index that enforces uniqueness on the given keys. No two documents
     * in the collection will have the same value for these keys. Does this only if the collection is empty.
     * @param collectionName the collection's name
     * @param keys the keys to ensure uniqueness on
     */
    static void createIndexInDBIfEmpty(String collectionName, String... keys) {
        MongoConnector connector = openConnection();
        MongoCollection<BasicDBObject> mongoCollection = getCollection(connector, collectionName);
        long collectionSize = mongoCollection.countDocuments();
        if (collectionSize == 0) {
            createIndexInDB(mongoCollection, keys);
        }
        closeConnection(connector);
    }

    /**
     * Inserts the given database object as a new record into the given collection using the given mongo connection.
     * @param collectionName the mongo collection to create the document in
     * @param dbObject the BasicDBObject to insert in the DB
     * @param entityName the entity name of the collection type, needed for logging
     * @return true if the insert was successful, false otherwise
     */
    static boolean insertDBOjectInDB(String collectionName,
                                     BasicDBObject dbObject, String entityName) {
        MongoConnector connector = openConnection();
        MongoCollection<BasicDBObject> mongoCollection = getCollection(connector, collectionName);
        try {
            long collectionSize = mongoCollection.countDocuments();
            mongoCollection.insertOne(dbObject);
            long newCollectionSize = mongoCollection.countDocuments();
            closeConnection(connector);
            return newCollectionSize == collectionSize + 1;
        } catch (MongoWriteException e) {
            logger.error("Could not create " + entityName + " in Database");
            logger.error(e.getMessage());
            closeConnection(connector);
            return false;
        }
    }

    /**
     * For the given collection, return a list of DBObjects matching a certain criteria.
     * @param collectionName the mongo collection to look in
     * @param criteria the criteria to match each document/DBObject with
     * @return a list of BasicDBObjects with all matching documents
     */
    static List<BasicDBObject> getDBObjectsFromDBMatchingCriteria(String collectionName, Map<String, Object> criteria) {
        MongoConnector connector = openConnection();
        MongoCollection<BasicDBObject> mongoCollection = getCollection(connector, collectionName);

        List<BasicDBObject> dbObjects = new ArrayList<>();

        if (criteria == null) {
            criteria = new HashMap<>();
        }
        try (MongoCursor<BasicDBObject> cursor = mongoCollection.find(new BasicDBObject(criteria)).iterator()) {
            while (cursor.hasNext()) {
                dbObjects.add(cursor.next());
            }
        }
        closeConnection(connector);
        return dbObjects;
    }

    /**
     * For the given mongo collection, updates the object whose value for the given objectIdKey is the given object id
     * with the updated version of the database object.
     * @param collectionName the mongo collection with the the object to update
     * @param dbObjectIdKeyName the name of the key for documents in the given collection whose value is an object id
     * @param dbObjectId the object id of the document to update
     * @param messageAsDBObject the updated version of the document
     * @param entityName the entity name of the collection type, needed for logging
     * @return true if the update was successful, false otherwise
     */
    static boolean updateDBObjectInDB(String collectionName, String dbObjectIdKeyName,
                                      ObjectId dbObjectId, BasicDBObject messageAsDBObject, String entityName) {
        MongoConnector connector = openConnection();
        MongoCollection<BasicDBObject> mongoCollection = getCollection(connector, collectionName);

        try {
            UpdateResult updateResult = mongoCollection.replaceOne(
                    new BasicDBObject(dbObjectIdKeyName, dbObjectId),
                    new BasicDBObject(messageAsDBObject));
            closeConnection(connector);
            return updateResult.isModifiedCountAvailable() && updateResult.getModifiedCount() == 1;
        } catch (MongoWriteException e) {
            logger.error("Could not update " + entityName + " in Database");
            logger.error(e.getMessage());
            closeConnection(connector);
            return false;
        }
    }

    /**
     * For the given mongo collection, deletes the given DB object.
     * @param collectionName the mongo collection to delete the object from
     * @param messageAsDBObject the BasicDBObject to delete from the DB
     * @return true if the delete was successful, false otherwise
     */
    static boolean deleteObjectFromDB(String collectionName, BasicDBObject messageAsDBObject) {
        MongoConnector connector = openConnection();
        MongoCollection<BasicDBObject> mongoCollection = getCollection(connector, collectionName);

        long msgCollectionSize = mongoCollection.countDocuments();
        mongoCollection.findOneAndDelete(messageAsDBObject);
        long newCollectionSize = mongoCollection.countDocuments();

        closeConnection(connector);
        return newCollectionSize == msgCollectionSize - 1;
    }

    /**
     * For the given collection, creates an index that enforces uniqueness on the given keys. No two documents
     * in the collection will have the same value for these keys.
     * @param mongoCollection the mongo collection to create the indexes in
     * @param keys the key names for each document that must have unique values
     */
    private static void createIndexInDB(MongoCollection<BasicDBObject> mongoCollection, String... keys) {
        for (String key : keys) {
            mongoCollection.createIndex(new Document(key, 1), new IndexOptions().unique(true));
        }
    }

    /**
     * Sets the mongo connector to be the given one. Allows us to fake once.
     * @param fakeMongoConnector the fake mongo connector to use
     */
    static void setFakeMongoConnector(MongoConnector fakeMongoConnector) {
        DaoUtils.fakeMongoConnector = fakeMongoConnector;
    }

    /**
     * Gets a new MongoConnector instance, unless a fake has been provided.
     * @return New MongoConnector, if fake is null. Otherwise, returns fake.
     */
    private static MongoConnector openConnection() {
        if (fakeMongoConnector != null) {
            return fakeMongoConnector;
        }
        return new MongoConnector();
    }

    /**
     * Closes the connection with Mongo.
     * @param mongoConnector the MongoConnector representing the MongoConnector
     */
    private static void closeConnection(MongoConnector mongoConnector) {
        mongoConnector.closeConnection();
    }

    private static MongoCollection<BasicDBObject> getCollection(MongoConnector mongoConnector, String collectionName) {
        return mongoConnector.getMongoDatabase().getCollection(collectionName, BasicDBObject.class);
    }
}
