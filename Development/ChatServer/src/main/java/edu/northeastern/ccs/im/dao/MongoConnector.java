package edu.northeastern.ccs.im.dao;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Objects;
import java.util.Properties;

/**
 * MongoConnector wraps the functionality of MongoClient, and allows us to provide a default configuration based
 * on properties file, which we use connect to the appropriate MongoInstance. The Dao layer will use this object
 * to connect to Mongo.
 */
class MongoConnector {

    private static Logger logger = LogManager.getLogger();
    private MongoClient mongo = null;
    private MongoDatabase mongoDatabase = null;

    private boolean fakeProvided;

    MongoConnector() {
        fakeProvided = false;
        setUp();
    }

    /**
     * This constructor allows for testing the Dao layer by passing a faked (or mocked) MongoClient and MongoDatabase.
     * @param mongoClient the fake or mocked MongoClient
     * @param mongoDatabase the fake or mocked MongoDatabase
     */
    MongoConnector(MongoClient mongoClient, MongoDatabase mongoDatabase) {
        fakeProvided = true;
        this.mongoDatabase = mongoDatabase;
        this.mongo = mongoClient;
    }

    /**
     * The only method accessible outside is this - we can get the appropriate Database to connect to with either
     * default or faked properties.
     * @return the MongoDatabase object
     */
    MongoDatabase getMongoDatabase() {
        if (mongo == null) {
            loadProperties();
            setUp();
        }
        return mongoDatabase;
    }

    private Properties loadProperties() {
        try {
            Properties properties = new Properties();
            properties.load(MongoConnector.class.getClassLoader().getResourceAsStream("dao.properties"));
            return properties;
        } catch (IOException e) {
            logger.error("Could not load properties");
            logger.error(e.getMessage());
            return null;
        }
    }

    private MongoClientURI getMongoClientURI() {
        Properties properties = loadProperties();
        Objects.requireNonNull(properties);

        // In production, our machine will have the mode as 'prod' in the system environment variable.
        String dbConnectionMode = System.getenv("PRATTLE_RUNTIME_ENV");

        if (dbConnectionMode == null) {
            dbConnectionMode = properties.getProperty(Constants.MODE_PROP, Constants.CLOUD);
        }

        if (dbConnectionMode.equals(Constants.CLOUD) || dbConnectionMode.equals(Constants.PROD)) {
            String dbUrl = properties.getProperty(dbConnectionMode + "." + Constants.DB_URL_PROP);
            String dbUser = properties.getProperty(dbConnectionMode + "." + Constants.DB_USER_PROP);
            String dbPassword = properties.getProperty(dbConnectionMode + "." + Constants.DB_AUTH_PROP);
            String hosts = properties.getProperty(dbConnectionMode + "." + Constants.DB_HOSTS_PROP);
            String replicaSet = properties.getProperty(dbConnectionMode + "." + Constants.DB_REPLICASET_PROP);
            String authSource = properties.getProperty(dbConnectionMode + "." + Constants.DB_AUTH_SOURCE);

            Objects.requireNonNull(dbUrl);
            Objects.requireNonNull(dbUser);
            Objects.requireNonNull(dbPassword);
            Objects.requireNonNull(hosts);
            Objects.requireNonNull(replicaSet);
            Objects.requireNonNull(authSource);

            return new MongoClientURI(String.format(dbUrl, dbUser, dbPassword, hosts, replicaSet, authSource));
        } else if (dbConnectionMode.equals(Constants.LOCAL)) {
            String dbUrl = properties.getProperty(dbConnectionMode + "." + Constants.DB_URL_PROP);
            String hosts = properties.getProperty(dbConnectionMode + "." + Constants.DB_HOSTS_PROP);

            Objects.requireNonNull(dbUrl);
            Objects.requireNonNull(hosts);

            return new MongoClientURI(String.format(dbUrl, hosts));
        } else {
            logger.warn("Unknown option for connection mode specified in " + Constants.MODE_PROP + ". Assuming local" +
                    "mongo exists.");

            String dbUrl = properties.getProperty(Constants.LOCAL + "." + Constants.DB_URL_PROP);
            String hosts = properties.getProperty(Constants.LOCAL + "." + Constants.DB_HOSTS_PROP);

            Objects.requireNonNull(dbUrl);
            Objects.requireNonNull(hosts);

            return new MongoClientURI(String.format(dbUrl, hosts));
        }
    }

    private String getDatabaseName() {
        Properties properties = loadProperties();
        Objects.requireNonNull(properties);

        String dbConnectionMode = System.getenv("PRATTLE_RUNTIME_ENV");

        if (dbConnectionMode == null) {
            dbConnectionMode = properties.getProperty(Constants.MODE_PROP, Constants.CLOUD);
        }

        if (dbConnectionMode.equals(Constants.CLOUD) ||
                dbConnectionMode.equals(Constants.PROD) ||
                dbConnectionMode.equals(Constants.LOCAL)) {
            String dbName = properties.getProperty(dbConnectionMode + "." + Constants.DB_DBNAME_PROP);
            Objects.requireNonNull(dbName);
            return dbName;
        } else {
            logger.warn("Unknown option for connection mode specified in " + Constants.MODE_PROP + ". Assuming local" +
                    "mongo exists.");

            String dbName = properties.getProperty(Constants.LOCAL + "." + Constants.DB_DBNAME_PROP);
            Objects.requireNonNull(dbName);
            return dbName;
        }
    }

    private void setUp() {
        mongo = new MongoClient(getMongoClientURI());
        mongoDatabase = mongo.getDatabase(getDatabaseName());
    }

    /**
     * Allows for the mongo connection to be closed whenever needed. (Best practice is to close after every transaction)
     */
    void closeConnection() {
        if (mongo != null && !fakeProvided) {
            mongo.close();
            mongo = null;
        }
    }
}