package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.UpdateOptions;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static br.com.xyinc.dyndata.service.EntityManagementService.SEQ_COLLECTION_LASTID_FIELD_NAME;
import static br.com.xyinc.dyndata.service.EntityManagementService.SEQ_COLLECTION_NAME;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.*;

@Service
public class MongoService {

    private String dbUrl  = "localhost";
    private int    dbPort = 27017;
    private String dbName = "dyndata";

    @Autowired
    private FieldTypeService        fieldTypeService;
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
            confCol.createIndex(fields(include(EntityManagementService.URI_FIELD_NAME)), indexOptions);
        }
    }

    /**
     * Busca documento baseado no descritor da entidade
     *
     * @param descriptor Descritor de entidade
     * @param query      Query de filtro
     * @return Lista de documentos correspondentes
     */
    public List<Document> query(EntityDescriptor descriptor, Bson query) {
        Bson fields = fields(include(ArrayUtils.toStringArray(descriptor.getFields().stream().map(FieldDescriptor::getFieldName).toArray())), exclude("_id"));
        return query(descriptor.getCollectionName(), query, fields);
    }

    private List<Document> query(String collectionName, Bson query, Bson fields) {
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

    /**
     * Inclui ou substitui um documento BSON baseado no descritor de entidade, usando suas chaves como referência
     *
     * @param descriptor Descritor da entidade
     * @param params     Dados do objeto
     */
    @SuppressWarnings("unchecked")
    public void replaceOne(EntityDescriptor descriptor, Map<String, Object> params) {
        if (descriptor.getKeys().isEmpty()) {
            throw new IllegalStateException("Entidades devem ter chave");
        } else {
            String   collectionName = descriptor.getCollectionName();
            Document document       = fieldTypeService.toDocument(descriptor, params);
            if (descriptor.getSequenceField() != null) {
                if (document.get(descriptor.getSequenceField()) == null) {
                    long nextNumber;
                    try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
                        MongoDatabase          db         = client.getDatabase(dbName);
                        MongoCollection        collection = db.getCollection(SEQ_COLLECTION_NAME);
                        Bson                   condition  = eq(EntityManagementService.URI_FIELD_NAME, descriptor.getUriName());
                        FindIterable<Document> x          = collection.find();
                        Document               lastIdLine = x.first();
                        if (lastIdLine == null) {
                            nextNumber = 1L;
                            lastIdLine = new Document();
                            lastIdLine.put(EntityManagementService.URI_FIELD_NAME, descriptor.getUriName());
                        } else {
                            nextNumber = ((BsonValue) lastIdLine.get(SEQ_COLLECTION_LASTID_FIELD_NAME)).asNumber().longValue() + 1;
                        }
                        document.put(SEQ_COLLECTION_LASTID_FIELD_NAME, nextNumber);
                        UpdateOptions uo = new UpdateOptions();
                        uo.upsert(true);
                        collection.replaceOne(condition, document, uo);
                    }
                    document.put(descriptor.getSequenceField(), nextNumber);
                }
            }
            Bson condition = and(descriptor.getKeys().stream().map(x -> eq(x, params.get(x))).distinct().collect(Collectors.toList()));
            try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
                MongoDatabase   db         = client.getDatabase(dbName);
                MongoCollection collection = db.getCollection(collectionName);
                UpdateOptions   uo         = new UpdateOptions();
                uo.upsert(true);
                collection.replaceOne(condition, document, uo);
            }
        }
    }

    /**
     * Apaga um documento BSON baseado nas chaves descritas no descritor da entidade
     *
     * @param descriptor Descritor da entidade
     * @param params     Referência do objeto a ser apagado
     */
    public void deleteOne(EntityDescriptor descriptor, Map<String, Object> params) {
        String collectionName = descriptor.getCollectionName();
        Bson   condition;
        if (descriptor.getKeys().isEmpty()) {
            throw new IllegalStateException("Entidades devem ter chave");
        } else {
            condition = and(descriptor.getKeys().stream().map(x -> eq(x, params.get(x))).distinct().collect(Collectors.toList()));
        }
        try (MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort))) {
            MongoDatabase   db         = client.getDatabase(dbName);
            MongoCollection collection = db.getCollection(collectionName);
            collection.deleteOne(condition);
        }
    }
}
