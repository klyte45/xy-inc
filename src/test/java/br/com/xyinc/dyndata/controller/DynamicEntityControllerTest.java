package br.com.xyinc.dyndata.controller;

import br.com.xyinc.dyndata.service.DynamicEntityService;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class DynamicEntityControllerTest {

    @Mock
    private DynamicEntityService dynamicEntityService;

    @InjectMocks
    @Resource
    private DynamicEntityController dynamicEntityController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(dynamicEntityService.get(anyString(), any())).thenReturn(Optional.empty());
    }

    @Test
    public void listEntities() {
        assert "[ ]".equals(dynamicEntityController.list("XX"));
    }


    @Test
    public void getEntityByName() {
        assert dynamicEntityController.getById("XXX", "id").getStatusCodeValue() == 404;

        when(dynamicEntityService.get(any(), any())).thenReturn(Optional.of(new Document()));
        assert dynamicEntityController.getById("XXX", "id").getStatusCodeValue() == 200;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addEntity() {
        assert dynamicEntityController.create("XXX", new HashMap<>()).getStatusCodeValue() == 201;

        doThrow(IllegalArgumentException.class).when(dynamicEntityService).create(anyString(), anyMap());
        assert dynamicEntityController.create("XXX", new HashMap<>()).getStatusCodeValue() == 400;

        doThrow(IllegalStateException.class).when(dynamicEntityService).create(anyString(), anyMap());
        assert dynamicEntityController.create("XXX", new HashMap<>()).getStatusCodeValue() == 500;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void editEntity() {
        assert dynamicEntityController.update("XXX", new HashMap<>(), "id").getStatusCodeValue() == 204;

        doThrow(IllegalArgumentException.class).when(dynamicEntityService).update(anyString(), anyMap(), anyList());
        assert dynamicEntityController.update("XXX", new HashMap<>(), "id").getStatusCodeValue() == 400;

        doThrow(IllegalStateException.class).when(dynamicEntityService).update(anyString(), anyMap(), anyList());
        assert dynamicEntityController.update("XXX", new HashMap<>(), "id").getStatusCodeValue() == 500;
    }

    @Test
    public void deleteEntity() {
        assert dynamicEntityController.delete("", "id").getStatusCodeValue() == 204;

        doThrow(IllegalArgumentException.class).when(dynamicEntityService).delete(anyString(), any());
        assert dynamicEntityController.delete("", "id").getStatusCodeValue() == 400;

        doThrow(IllegalStateException.class).when(dynamicEntityService).delete(anyString(), any());
        assert dynamicEntityController.delete("", "id").getStatusCodeValue() == 500;
    }
}