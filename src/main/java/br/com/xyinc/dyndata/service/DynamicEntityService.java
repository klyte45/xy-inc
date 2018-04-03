package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Serviço de gerenciamento de novas entidades.
 */
@Service
public class DynamicEntityService {

    @Autowired
    private EntityOperationService  entityOperationService;
    @Autowired
    private EntityManagementService entityManagementService;

    private EntityDescriptor getEntityDescriptor(String entityUri) {
        EntityDescriptor descriptor = entityManagementService.findEntityDescriptor(entityUri);
        if (descriptor == null) {
            throw new IllegalArgumentException("Entidade não cadastrada!");
        }
        return descriptor;
    }

    public List<Document> list(String entityUri) {
        return entityOperationService.query(getEntityDescriptor(entityUri), null);
    }

    public Optional<Document> get(String entityUri, List<String> id) {
        return entityOperationService.get(getEntityDescriptor(entityUri), id);
    }

    public void create(String entityUri, Map<String, Object> data) {
        EntityDescriptor   descriptor = getEntityDescriptor(entityUri);
        Optional<Document> current    = entityOperationService.query(descriptor, entityOperationService.getKeysFromEntity(descriptor, data)).stream().findFirst();
        if (current.isPresent()) {
            throw new IllegalArgumentException("Chave de entidade já cadastrada!");
        }
        entityOperationService.replaceOne(descriptor, data);
    }

    public void update(String entityUri, Map<String, Object> data, List<String> id) {
        EntityDescriptor   descriptor = getEntityDescriptor(entityUri);
        Optional<Document> current    = entityOperationService.query(descriptor, entityOperationService.getKeysFromEntity(descriptor, id)).stream().findFirst();
        if (!current.isPresent()) {
            throw new IllegalArgumentException("Item não encontrado!");
        }
        for (String key : descriptor.getKeys()) {
            data.put(key, current.get().get(key));
        }
        entityOperationService.replaceOne(getEntityDescriptor(entityUri), data);
    }

    public void delete(String entityUri, List<String> id) {
        EntityDescriptor   descriptor = getEntityDescriptor(entityUri);
        Optional<Document> current    = entityOperationService.query(descriptor, entityOperationService.getKeysFromEntity(descriptor, id)).stream().findFirst();
        if (!current.isPresent()) {
            throw new IllegalArgumentException("Item não encontrado!");
        }
        entityOperationService.deleteOne(getEntityDescriptor(entityUri), id);
    }
}
