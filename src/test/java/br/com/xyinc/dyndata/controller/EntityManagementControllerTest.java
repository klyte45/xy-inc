package br.com.xyinc.dyndata.controller;

import br.com.xyinc.dyndata.service.EntityManagementService;
import org.bson.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

public class EntityManagementControllerTest {

    @Mock
    private EntityManagementService entityManagementService;

    @InjectMocks
    @Resource
    private EntityManagementController entityManagementController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(entityManagementService.findEntity(anyString())).thenReturn(Optional.empty());
    }

    @Test
    public void listEntities() {
        assert "[ ]".equals(entityManagementController.listEntities());
    }


    @Test
    public void getEntityByName() {
        assert entityManagementController.getEntityByName("").getStatusCodeValue() == 404;

        when(entityManagementService.findEntity("aaa")).thenReturn(Optional.of(new Document()));
        assert entityManagementController.getEntityByName("aaa").getStatusCodeValue() == 200;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addEntity() {
        assert entityManagementController.addEntity(new HashMap<>()).getStatusCodeValue() == 201;

        doThrow(IllegalArgumentException.class).when(entityManagementService).createEntity(anyMap());
        assert entityManagementController.addEntity(new HashMap<>()).getStatusCodeValue() == 400;

        doThrow(IllegalStateException.class).when(entityManagementService).createEntity(anyMap());
        assert entityManagementController.addEntity(new HashMap<>()).getStatusCodeValue() == 500;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void editEntity() {
        assert entityManagementController.editEntity(new HashMap<>(), "").getStatusCodeValue() == 201;

        doThrow(IllegalArgumentException.class).when(entityManagementService).updateEntity(anyMap(), anyString());
        assert entityManagementController.editEntity(new HashMap<>(), "").getStatusCodeValue() == 400;

        doThrow(IllegalStateException.class).when(entityManagementService).updateEntity(anyMap(), anyString());
        assert entityManagementController.editEntity(new HashMap<>(), "").getStatusCodeValue() == 500;
    }

    @Test
    public void deleteEntity() {
        assert entityManagementController.deleteEntity("").getStatusCodeValue() == 201;

        doThrow(IllegalArgumentException.class).when(entityManagementService).deleteEntity(anyString());
        assert entityManagementController.deleteEntity("").getStatusCodeValue() == 400;

        doThrow(IllegalStateException.class).when(entityManagementService).deleteEntity(anyString());
        assert entityManagementController.deleteEntity("").getStatusCodeValue() == 500;
    }
}