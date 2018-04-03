package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static br.com.xyinc.dyndata.service.EntityManagementService.SEQ_COLLECTION_LASTID_FIELD_NAME;
import static br.com.xyinc.dyndata.service.EntityManagementService.SEQ_COLLECTION_NAME;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

@Service
public class MongoService {

    private String dbUrl  = "localhost";
    private int    dbPort = 27017;
    private String dbName = "dyndata";

    @Autowired
    private EntityManagementService entityManagementService;

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String newUrl) {
        dbUrl = newUrl;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int newPort) {
        dbPort = newPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String newName) {
        dbName = newName;
    }

    /**
     * Testa a conexão e certifica a criação dos índices necessários para o sistema
     */
    public void testConnection() {
        try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
            client.getAddress();
            MongoDatabase   db           = client.getDatabase(dbName);
            MongoCollection confCol      = db.getCollection(entityManagementService.getConfigurationCollection().getCollectionName());
            IndexOptions    indexOptions = new IndexOptions();
            indexOptions.unique(true);
            confCol.createIndex(fields(include(EntityManagementService.URI_FIELD_NAME)), indexOptions);

            MongoCollection seqCol = db.getCollection(SEQ_COLLECTION_NAME);
            seqCol.createIndex(fields(include(EntityManagementService.URI_FIELD_NAME)), indexOptions);
        }
    }


    public List<Document> callFind(String collectionName, Bson query, Bson fields) {
        try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
            MongoDatabase             db         = client.getDatabase(dbName);
            MongoCollection<Document> collection = db.getCollection(collectionName);
            FindIterable<Document>    it;
            if (query == null) {
                it = collection.find();
            } else {
                it = collection.find(query);
            }
            if (fields != null) {
                it = it.projection(fields);
            }
            List<Document> result = new ArrayList<>();
            for (Document doc : it) {
                result.add(doc);
            }
            return result;
        }
    }

    public void callDeleteOne(String collectionName, Bson condition) {
        try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
            MongoDatabase   db         = client.getDatabase(dbName);
            MongoCollection collection = db.getCollection(collectionName);
            collection.deleteOne(condition);
        }
    }

    @SuppressWarnings("unchecked")
    public long getNextSequence(EntityDescriptor descriptor) {
        long nextNumber;
        try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
            MongoDatabase   db         = client.getDatabase(dbName);
            MongoCollection collection = db.getCollection(SEQ_COLLECTION_NAME);
            Bson            condition  = eq(EntityManagementService.URI_FIELD_NAME, descriptor.getUriName());
            FindIterable    x          = collection.find(condition);
            Document        lastIdLine = (Document) x.first();
            if (lastIdLine == null) {
                nextNumber = 1L;
                lastIdLine = new Document();
                lastIdLine.put(EntityManagementService.URI_FIELD_NAME, descriptor.getUriName());
            } else {
                nextNumber = ((Number) lastIdLine.get(SEQ_COLLECTION_LASTID_FIELD_NAME)).longValue() + 1;
            }
            lastIdLine.put(SEQ_COLLECTION_LASTID_FIELD_NAME, nextNumber);
            UpdateOptions uo = new UpdateOptions();
            uo.upsert(true);
            collection.replaceOne(condition, lastIdLine, uo);
        }
        return nextNumber;
    }

    @SuppressWarnings("unchecked")
    public void callReplaceOne(String collectionName, Document document, Bson condition) {
        try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
            MongoDatabase   db         = client.getDatabase(dbName);
            MongoCollection collection = db.getCollection(collectionName);
            UpdateOptions   uo         = new UpdateOptions();
            uo.upsert(true);
            collection.replaceOne(condition, document, uo);
        }
    }
}
