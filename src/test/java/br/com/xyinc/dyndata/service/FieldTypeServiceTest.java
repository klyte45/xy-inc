package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.FieldType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;
import java.math.BigInteger;

public class FieldTypeServiceTest {

    @InjectMocks
    @Resource
    private FieldTypeService fieldTypeService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getType_checkBasicRegistered() {
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.STRING) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.BOOLEAN) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.DECIMAL) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.INTEGER) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.LONG) != null;
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.TIMESTAMP) != null;
    }

    @Test
    public void registerType_checkOverride() {
        FieldType<BigInteger> fieldType = new FieldType<>(BigInteger.class);
        fieldTypeService.registerType(FieldTypeService.DefaultFieldTypes.INTEGER, fieldType);
        assert fieldTypeService.getType(FieldTypeService.DefaultFieldTypes.INTEGER) == fieldType;
    }


    @Test
    public void registerType_checkAdd() {
        FieldType<BigInteger> fieldType = new FieldType<>(BigInteger.class);
        String newTypeName = "BigInteger";
        fieldTypeService.registerType(newTypeName, fieldType);
        assert fieldTypeService.getType(newTypeName) == fieldType;
    }

    @Test
    public void getType() {
    }
}