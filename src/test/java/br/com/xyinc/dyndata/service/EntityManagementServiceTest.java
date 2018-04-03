package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.exception.EntityKeyException;
import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static br.com.xyinc.dyndata.service.EntityManagementService.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class EntityManagementServiceTest {

    @Mock
    private EntityOperationService entityOperationService;
    @Mock
    private FieldTypeService       fieldTypeService;

    @InjectMocks
    @Resource
    private EntityManagementService entityManagementService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(fieldTypeService.getAllowedTypes()).thenCallRealMethod();
        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(new ArrayList<>());
    }

    private void mockToValidEntityDescriptor() {
        Document doc = new Document();
        doc.put(URI_FIELD_NAME, "AAAA");
        doc.put(ENTITY_FIELD_NAME, "BBBB");
        doc.put(SEQ_FIELD_NAME, "CCCC");
        doc.put(FIELDS_FIELD_NAME, new ArrayList());
        doc.put(KEYS_FIELD_NAME, Collections.singletonList("DDDD"));

        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(Collections.singletonList(doc));
    }

    private void mockToInvalidEntityDescriptor() {
        Document doc = new Document();
        doc.put("ASDASD", "AAAA");
        doc.put("ASDadsa", "BBBB");
        doc.put("afsdfasde", "CCCC");
        doc.put("adasda", new ArrayList());
        doc.put("afdadfsd", Collections.singletonList("DDDD"));

        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(Collections.singletonList(doc));
    }

    @Test
    public void findEntityDescriptor_OK() {
        mockToValidEntityDescriptor();
        EntityDescriptor ed = entityManagementService.findEntityDescriptor("XXXX");
        assert "AAAA".equals(ed.getUriName());
        assert "BBBB".equals(ed.getEntityName());
        assert "CCCC".equals(ed.getSequenceField());
        assert ed.getFields().isEmpty();
        assert ed.getKeys().size() == 1;
        assert "DDDD".equals(ed.getKeys().get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void findEntityDescriptor_Null() {
        entityManagementService.findEntityDescriptor("XXXX");
    }

    @Test(expected = IllegalStateException.class)
    public void findEntityDescriptor_Exception() {
        mockToInvalidEntityDescriptor();
        entityManagementService.findEntityDescriptor("XXXX");
    }

    @Test
    public void listEntities() {
        entityManagementService.listEntities();
    }

    @Test
    public void createEntity_OK() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Any Entity");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorLong());
        data.put(SEQ_FIELD_NAME, "teste");

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_SeqField_NotInEntity() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Teste");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());
        data.put(SEQ_FIELD_NAME, "XXXX");

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_SeqField_NotNumber() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Teste");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());
        data.put(SEQ_FIELD_NAME, "teste");

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NullEntity() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, null);
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_EmptyEntity() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "       ");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NullUri() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, null);
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_EmptyUri() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "");
        data.put(ENTITY_FIELD_NAME, "Aaa");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NonStandardUri() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "ôçaeaqeqw231+§");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_AlreadyExists() {
        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(Collections.singletonList(new Document()));
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NullKeys() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, null);
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NullFields() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, null);

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_EmptyKeys() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, new ArrayList());
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NonValidKey() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("testasse"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = EntityKeyException.class)
    public void createEntity_NonValidTypeKey() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("testeDoc"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorStringArr());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NonListKeys() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, "");
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_NonListFields() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, "");

        entityManagementService.createEntity(data);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEntity_RepeatedFields() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "adasder");
        data.put(ENTITY_FIELD_NAME, "AAA");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorRepeatedString());

        entityManagementService.createEntity(data);
    }

    @Test
    public void updateEntity_OK() {
        Document prevDoc = new Document();
        prevDoc.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));

        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(Collections.singletonList(prevDoc));
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Any Entity");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.updateEntity(data, "aAA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateEntity_NotFound() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Any Entity");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.updateEntity(data, "aAA");
    }


    @Test
    public void deleteEntity_OK() {
        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(Collections.singletonList(new Document()));
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Any Entity");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.deleteEntity("aAA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteEntity_NotFound() {
        HashMap<String, Object> data = new HashMap<>();

        data.put(URI_FIELD_NAME, "any");
        data.put(ENTITY_FIELD_NAME, "Any Entity");
        data.put(KEYS_FIELD_NAME, Collections.singletonList("teste"));
        data.put(FIELDS_FIELD_NAME, getFieldDescriptorString());

        entityManagementService.deleteEntity("aAA");
    }


    private List<Map<String, Object>> getFieldDescriptorString() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING, false);
        strFieldDescriptor.setMinLength(10);
        strFieldDescriptor.setMaxLength(20);
        ObjectMapper mapper      = new ObjectMapper();
        TypeFactory  typeFactory = mapper.getTypeFactory();
        MapType      mapType     = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        try {
            return Collections.singletonList(mapper.readValue(mapper.writeValueAsString(strFieldDescriptor), mapType));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Map<String, Object>> getFieldDescriptorRepeatedString() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.STRING, false);
        strFieldDescriptor.setMinLength(10);
        strFieldDescriptor.setMaxLength(20);
        ObjectMapper mapper      = new ObjectMapper();
        TypeFactory  typeFactory = mapper.getTypeFactory();
        MapType      mapType     = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        try {
            return Arrays.asList(mapper.readValue(mapper.writeValueAsString(strFieldDescriptor), mapType), mapper.readValue(mapper.writeValueAsString(strFieldDescriptor), mapType));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Map<String, Object>> getFieldDescriptorStringArr() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("testeDoc", FieldTypeService.DefaultFieldTypes.STRING_ARR, false);
        strFieldDescriptor.setMinLength(10);
        strFieldDescriptor.setMaxLength(20);
        ObjectMapper mapper      = new ObjectMapper();
        TypeFactory  typeFactory = mapper.getTypeFactory();
        MapType      mapType     = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        try {
            return Collections.singletonList(mapper.readValue(mapper.writeValueAsString(strFieldDescriptor), mapType));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Map<String, Object>> getFieldDescriptorLong() {
        FieldDescriptor strFieldDescriptor = new FieldDescriptor("teste", FieldTypeService.DefaultFieldTypes.LONG, false);
        strFieldDescriptor.setMinLength(10);
        strFieldDescriptor.setMaxLength(20);
        ObjectMapper mapper      = new ObjectMapper();
        TypeFactory  typeFactory = mapper.getTypeFactory();
        MapType      mapType     = typeFactory.constructMapType(HashMap.class, String.class, Object.class);
        try {
            return Collections.singletonList(mapper.readValue(mapper.writeValueAsString(strFieldDescriptor), mapType));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}