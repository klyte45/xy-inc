package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import com.mongodb.MongoClient;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EntityOperationServiceTest {


    @Mock
    private MongoService     mongoService;
    @Mock
    private FieldTypeService fieldTypeService;

    @InjectMocks
    @Resource
    private EntityOperationService entityOperationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(fieldTypeService.toBson(any(), any(), any(), any())).thenCallRealMethod();
        when(fieldTypeService.getType(any())).thenCallRealMethod();
    }

    private EntityDescriptor getSampleEntityDescriptor() {
        EntityDescriptor ed = new EntityDescriptor();
        ed.setKeys(Arrays.asList("B", "A"));
        ed.setFields(Arrays.asList(new FieldDescriptor("A", "Integer", false), new FieldDescriptor("B", "String", false)));
        return ed;
    }

    @Test
    public void query() {
        entityOperationService.query(getSampleEntityDescriptor(), null);
    }

    @Test
    public void getKeysFromEntity_List() {
        Bson         condition      = entityOperationService.getKeysFromEntity(getSampleEntityDescriptor(), Arrays.asList("15", "20"));
        BsonDocument asBsonDocument = condition.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        Assert.assertEquals("{ \"B\" : \"15\", \"A\" : 20 }", asBsonDocument.toJson());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getKeysFromEntity_List_WrongSize() {
        entityOperationService.getKeysFromEntity(getSampleEntityDescriptor(), Collections.singletonList("15"));
    }

    @Test
    public void getKeysFromEntity_Map() {
        Map<String, Object> values = new HashMap<>();
        values.put("A", 15);
        values.put("B", "SSS");
        Bson         condition      = entityOperationService.getKeysFromEntity(getSampleEntityDescriptor(), values);
        BsonDocument asBsonDocument = condition.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        Assert.assertEquals("{ \"B\" : \"SSS\", \"A\" : 15 }", asBsonDocument.toJson());
    }

    @Test
    public void getFieldsFromEntity() {
        Bson         fieldList      = entityOperationService.getFieldsFromEntity(getSampleEntityDescriptor());
        BsonDocument asBsonDocument = fieldList.toBsonDocument(BsonDocument.class, MongoClient.getDefaultCodecRegistry());
        Assert.assertEquals("{ \"A\" : 1, \"B\" : 1, \"_id\" : 0 }", asBsonDocument.toJson());
    }

    @Test
    public void get() {
        entityOperationService.get(getSampleEntityDescriptor(), Arrays.asList("15", "20"));
    }

    @Test(expected = IllegalStateException.class)
    public void get_entityWithNoKey() {
        EntityDescriptor ed = getSampleEntityDescriptor();
        ed.setKeys(new ArrayList<>());
        entityOperationService.get(ed, Arrays.asList("15", "20"));
    }

    @Test
    public void replaceOne() {
        entityOperationService.replaceOne(getSampleEntityDescriptor(), new HashMap<>());
    }

    @Test(expected = IllegalStateException.class)
    public void replaceOne_entityWithNoKey() {
        EntityDescriptor ed = getSampleEntityDescriptor();
        ed.setKeys(new ArrayList<>());
        entityOperationService.replaceOne(ed, new HashMap<>());
    }

    @Test
    public void deleteOne() {
        entityOperationService.deleteOne(getSampleEntityDescriptor(), Arrays.asList("15", "20"));
    }

    @Test(expected = IllegalStateException.class)
    public void deleteOne_entityWithNoKey() {
        EntityDescriptor ed = getSampleEntityDescriptor();
        ed.setKeys(new ArrayList<>());
        entityOperationService.deleteOne(ed, Arrays.asList("15", "20"));
    }
}