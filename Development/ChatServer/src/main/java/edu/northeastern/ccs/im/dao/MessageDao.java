package edu.northeastern.ccs.im.dao;

import com.mongodb.BasicDBObject;
import edu.northeastern.ccs.im.models.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.northeastern.ccs.im.dao.Constants.*;

/**
 * A MessageDao is a Data Access Object that abstracts the connection to the database pertaining to Message info.
 */
public class MessageDao implements Dao<Message>{
    private MessageBridge messageBridge;

    public MessageDao() {
        this.messageBridge = new MessageBridge();
    }
    @Override
    public List<Message> get(Map<String, Object> criteria) {
        List<BasicDBObject> messageDBObjects = DaoUtils.getDBObjectsFromDBMatchingCriteria(
                MESSAGE_COLLECTION_NAME, criteria);

        return messageDBObjects
                .stream()
                .map(messageDbObject -> messageBridge.toObject(messageDbObject))
                .collect(Collectors.toList());
    }

    @Override
    public boolean set(Message changedVersion) {
        BasicDBObject messageAsDBObject = messageBridge.toDBObject(changedVersion);

        return DaoUtils.updateDBObjectInDB(
                MESSAGE_COLLECTION_NAME, MESSAGE_MESSAGE_ID_KEY,
                changedVersion.getId(), messageAsDBObject, MESSAGE_COLLECTION_NAME);
    }

    @Override
    public boolean create(Message object) {
        BasicDBObject messageAsDBObject = messageBridge.toDBObject(object);

        DaoUtils.createIndexInDBIfEmpty(MESSAGE_COLLECTION_NAME, MESSAGE_MESSAGE_ID_KEY);
        return DaoUtils.insertDBOjectInDB(MESSAGE_COLLECTION_NAME, messageAsDBObject, MESSAGE_COLLECTION_NAME);
    }

    @Override
    public boolean delete(Message object) {
        BasicDBObject messageAsDBObject = messageBridge.toDBObject(object);

        return DaoUtils.deleteObjectFromDB(MESSAGE_COLLECTION_NAME, messageAsDBObject);
    }
}