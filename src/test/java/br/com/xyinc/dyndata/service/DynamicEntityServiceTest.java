package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class DynamicEntityServiceTest {

    @Mock
    private EntityManagementService entityManagementService;
    @Mock
    private EntityOperationService  entityOperationService;

    @InjectMocks
    @Resource
    private DynamicEntityService dynamicEntityService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(entityOperationService.query(any(EntityDescriptor.class), any(Bson.class))).thenReturn(new ArrayList<>());
        when(entityOperationService.get(any(), any())).thenReturn(Optional.empty());
        EntityDescriptor ed = new EntityDescriptor();
        ed.setKeys(Collections.singletonList("A"));
        when(entityManagementService.findEntityDescriptor(any())).thenReturn(ed);
    }


    @Test(expected = IllegalArgumentException.class)
    public void listEntities_InvalidEntity() {
        when(entityManagementService.findEntityDescriptor(any())).thenReturn(null);
        dynamicEntityService.list("XXX");
    }

    @Test
    public void listEntities() {
        assert dynamicEntityService.list("XXX").isEmpty();
    }

    @Test
    public void get() {
        assert !dynamicEntityService.get("XXX", new ArrayList<>()).isPresent();
    }

    @Test
    public void create_OK() {
        dynamicEntityService.create("XXX", new HashMap<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_NOK() {
        when(entityOperationService.query(any(), any())).thenReturn(Collections.singletonList(new Document()));
        dynamicEntityService.create("XXX", new HashMap<>());
    }

    @Test
    public void update_OK() {
        Document current = new Document();
        current.put("A", 15);
        when(entityOperationService.query(any(), any())).thenReturn(Collections.singletonList(current));
        HashMap<String, Object> map = new HashMap<>();
        dynamicEntityService.update("XXX", map, new ArrayList<>());
        assert map.get("A").equals(current.get("A"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_NOK() {
        dynamicEntityService.update("XXX", new HashMap<>(), new ArrayList<>());
    }

    @Test
    public void delete_OK() {
        when(entityOperationService.query(any(), any())).thenReturn(Collections.singletonList(new Document()));
        dynamicEntityService.delete("XXX", new ArrayList<>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void delete_NOK() {
        dynamicEntityService.delete("XXX", new ArrayList<>());
    }

}