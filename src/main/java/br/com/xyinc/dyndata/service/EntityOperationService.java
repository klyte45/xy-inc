package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.*;

@Service
public class EntityOperationService {


    @Autowired
    private MongoService     mongoService;
    @Autowired
    private FieldTypeService fieldTypeService;

    /**
     * Busca documento baseado no descritor da entidade
     *
     * @param descriptor Descritor de entidade
     * @param query      Query de filtro
     * @return Lista de documentos correspondentes
     */
    public List<Document> query(EntityDescriptor descriptor, Bson query) {
        Bson fields = getFieldsFromEntity(descriptor);
        return mongoService.callFind(descriptor.getCollectionName(), query, fields);
    }

    /**
     * Obtém o filtro BSON correspondente às chaves da entidade
     *
     * @param descriptor Descritor da entidade
     * @param id         Lista de chaves, na ordem declarada no descritor
     * @return Filtro BSON correspondente às chaves
     */
    public Bson getKeysFromEntity(EntityDescriptor descriptor, List<String> id) {
        List<String> keys         = descriptor.getKeys();
        List<Bson>   list         = new ArrayList<>();
        Set<Bson>    uniqueValues = new HashSet<>();
        if (keys.size() != id.size()) {
            throw new IllegalArgumentException("Tamanho da chave não corresponde a quantidade de valores enviados!");
        }
        for (String key : keys) {
            Bson eq = eq(key, fieldTypeService.toBson(id.get(keys.indexOf(key)), null, fieldTypeService.getType(descriptor.getFieldByName(key).getFieldType()), new ArrayList<>()));
            if (uniqueValues.add(eq)) {
                list.add(eq);
            }
        }
        return and(list);
    }

    /**
     * Obtém o filtro BSON correspondente às chaves da entidade
     *
     * @param descriptor Descritor da entidade
     * @param params     Valores da entidade de onde os valores da chave serão extraídos
     * @return Filtro BSON correspondente às chaves
     */
    public Bson getKeysFromEntity(EntityDescriptor descriptor, Map<String, Object> params) {
        List<Bson> list         = new ArrayList<>();
        Set<Bson>  uniqueValues = new HashSet<>();
        for (String key : descriptor.getKeys()) {
            Bson eq = eq(key, fieldTypeService.toBson(params.get(key), null, fieldTypeService.getType(descriptor.getFieldByName(key).getFieldType()), new ArrayList<>()));
            if (uniqueValues.add(eq)) {
                list.add(eq);
            }
        }
        return and(list);
    }

    /**
     * Obtém item da entidade que corresponda à chave informada
     *
     * @param descriptor Descritor da entidade
     * @param id         Lista de chaves, na ordem declarada no descritor
     * @return Documento BSON correspondente, se exitir (encapsulado em Optional)
     */
    public Optional<Document> get(EntityDescriptor descriptor, List<String> id) {
        if (descriptor.getKeys().isEmpty()) {
            throw new IllegalStateException("Entidades devem ter chave");
        } else {
            Bson condition = getKeysFromEntity(descriptor, id);
            Bson fields    = getFieldsFromEntity(descriptor);
            return mongoService.callFind(descriptor.getCollectionName(), condition, fields).stream().findFirst();
        }
    }

    Bson getFieldsFromEntity(EntityDescriptor descriptor) {
        List<String> fieldListInclude = descriptor.getFields().stream().map(FieldDescriptor::getFieldName).collect(Collectors.toList());
        List<String> fieldListExclude = new ArrayList<>();
        if (!fieldListInclude.contains("_id")) {
            fieldListExclude.add("_id");
        }
        return fields(include(fieldListInclude), exclude(fieldListExclude));
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
            Bson     condition      = getKeysFromEntity(descriptor, params);

            mongoService.callReplaceOne(collectionName, document, condition);
        }
    }


    /**
     * Apaga um documento BSON baseado nas chaves descritas no descritor da entidade
     *
     * @param descriptor Descritor da entidade
     * @param id         Lista de chaves, na ordem declarada no descritor
     */
    public void deleteOne(EntityDescriptor descriptor, List<String> id) {
        String       collectionName = descriptor.getCollectionName();
        Bson         condition;
        List<String> keys           = descriptor.getKeys();
        if (keys.isEmpty()) {
            throw new IllegalStateException("Entidades devem ter chave");
        } else {
            condition = getKeysFromEntity(descriptor, id);
        }
        mongoService.callDeleteOne(collectionName, condition);
    }

}
