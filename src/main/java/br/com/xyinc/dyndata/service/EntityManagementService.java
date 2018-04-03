package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.exception.EntityKeyException;
import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * Serviço de gerenciamento de novas entidades.
 */
@Service
public class EntityManagementService {

    public final static  String       REGEX_ENTITY                     = "^[0-9a-zA-Z._]{1,100}$";
    final static         String       SEQ_COLLECTION_NAME              = "seq.collections";
    public final static  String       DEFAULT_COLLECTION_PREFIX        = "dyn.";
    final static         String       URI_FIELD_NAME                   = "uriName";
    final static         String       ENTITY_FIELD_NAME                = "entityName";
    final static         String       KEYS_FIELD_NAME                  = "keys";
    final static         String       SEQ_FIELD_NAME                   = "sequenceField";
    final static         String       FIELDS_FIELD_NAME                = "fields";
    private final static String       COLLECTION_FIELD_NAME            = "collectionName";
    final static         String       SEQ_COLLECTION_LASTID_FIELD_NAME = "lastId";
    private final static List<String> ALLOWED_SEQ_FIELD_TYPES          = Arrays.asList(FieldTypeService.DefaultFieldTypes.INTEGER, FieldTypeService.DefaultFieldTypes.LONG);
    private final static List<String> ALLOWED_KEY_FIELD_TYPES          = Arrays.asList(
            FieldTypeService.DefaultFieldTypes.INTEGER,
            FieldTypeService.DefaultFieldTypes.LONG,
            FieldTypeService.DefaultFieldTypes.STRING,
            FieldTypeService.DefaultFieldTypes.BOOLEAN,
            FieldTypeService.DefaultFieldTypes.TIMESTAMP
    );
    private static EntityDescriptor _configurationCollection;

    @Autowired
    private EntityOperationService entityOperationService;
    @Autowired
    private FieldTypeService       fieldTypeService;

    /**
     * Instancia o descritor que representa a coleção dos descritores de entidades.
     *
     * @return Descritor da coleção dos descritores de entidades
     */
    public EntityDescriptor getConfigurationCollection() {
        if (_configurationCollection == null) {
            _configurationCollection = new EntityDescriptor();
            _configurationCollection.setUriName("@collectionDescriptor");
            _configurationCollection.setKeys(Collections.singletonList(URI_FIELD_NAME));

            FieldDescriptor nullableFieldDescriptor = new FieldDescriptor("nullable", FieldTypeService.DefaultFieldTypes.BOOLEAN, false);
            nullableFieldDescriptor.setDefaultValue("true");

            FieldDescriptor documentsFieldDescriptor = new FieldDescriptor("documentFields", FieldTypeService.DefaultFieldTypes.DOCUMENT_ARR, true);

            List<FieldDescriptor> fieldFieldsDescriptor = Arrays.asList(
                    new FieldDescriptor("fieldName", FieldTypeService.DefaultFieldTypes.STRING, false),
                    new FieldDescriptor("fieldType", FieldTypeService.DefaultFieldTypes.STRING, false, null, new ArrayList<>(fieldTypeService.getAllowedTypes())),
                    new FieldDescriptor("nullable", FieldTypeService.DefaultFieldTypes.BOOLEAN, true),
                    nullableFieldDescriptor,
                    new FieldDescriptor("allowedValues", FieldTypeService.DefaultFieldTypes.STRING_ARR, true),
                    new FieldDescriptor("min", FieldTypeService.DefaultFieldTypes.DECIMAL, true),
                    new FieldDescriptor("max", FieldTypeService.DefaultFieldTypes.DECIMAL, true),
                    new FieldDescriptor("minLength", FieldTypeService.DefaultFieldTypes.INTEGER, true),
                    new FieldDescriptor("maxLength", FieldTypeService.DefaultFieldTypes.INTEGER, true),
                    new FieldDescriptor("defaultValue", FieldTypeService.DefaultFieldTypes.STRING, true),
                    documentsFieldDescriptor
            );

            documentsFieldDescriptor.setDocumentFields(fieldFieldsDescriptor);

            FieldDescriptor keyFieldDescriptor = new FieldDescriptor(KEYS_FIELD_NAME, FieldTypeService.DefaultFieldTypes.STRING_ARR, false);
            keyFieldDescriptor.setMinLength(1);

            _configurationCollection.setFields(Arrays.asList(
                    new FieldDescriptor(URI_FIELD_NAME, FieldTypeService.DefaultFieldTypes.STRING, false),
                    new FieldDescriptor(ENTITY_FIELD_NAME, FieldTypeService.DefaultFieldTypes.STRING, false),
                    new FieldDescriptor(SEQ_FIELD_NAME, FieldTypeService.DefaultFieldTypes.STRING, true),
                    keyFieldDescriptor,
                    new FieldDescriptor(FIELDS_FIELD_NAME, FieldTypeService.DefaultFieldTypes.DOCUMENT_ARR, false, fieldFieldsDescriptor)
            ));
        }
        return _configurationCollection;
    }

    /**
     * Obtém todas as entidades cadastradas
     *
     * @return Entidades cadastradas
     */
    public List<Document> listEntities() {
        return entityOperationService.query(getConfigurationCollection(), null);
    }

    /**
     * Obtém entidade com o nome de URI especificado
     *
     * @param uriName URI da entidade a procurar
     * @return Descritor da entidade pesquisada
     */
    public Optional<Document> findEntity(String uriName) {
        return entityOperationService.query(getConfigurationCollection(), eq(URI_FIELD_NAME, uriName)).stream().findFirst();
    }

    /**
     * Obtém o descritor da entidade com o nome de URI especificado
     *
     * @param entityUri URI da entidade a procurar
     * @return Descritor da entidade pesquisada
     */
    public EntityDescriptor findEntityDescriptor(String entityUri) {
        Optional<Document> document = findEntity(entityUri);
        if (!document.isPresent()) {
            throw new IllegalArgumentException("URI de entidade inválida!");
        }
        String       serializedDocument = JSON.serialize(document.get());
        ObjectMapper mapper             = new ObjectMapper();
        try {
            return mapper.readerFor(EntityDescriptor.class).readValue(serializedDocument);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }

    /**
     * Remove entidade baseado na URI
     *
     * @param uriName URI da entidade a ser apagada
     */
    public void deleteEntity(String uriName) {
        if (!findEntity(uriName.trim()).isPresent()) {
            throw new IllegalArgumentException("Entidade não encontrada!");
        }
        entityOperationService.deleteOne(getConfigurationCollection(), Collections.singletonList(uriName));
    }

    /**
     * Cadastra entidade a partir dos dados
     *
     * @param data Dados do descritor da nova entidade
     */
    @SuppressWarnings("unchecked")
    public void createEntity(Map<String, Object> data) {
        if (data.get(URI_FIELD_NAME) == null) {
            throw new IllegalArgumentException("URI da entidade não pode ser nula!");
        }
        String uriName = data.get(URI_FIELD_NAME).toString().trim();
        if (findEntity(uriName).isPresent()) {
            throw new IllegalArgumentException("Entidade já existe!");
        }

        saveEntity(data);
    }

    /**
     * Atualiza entidade a partir dos dados
     *
     * @param data    Dados do descritor da entidade
     * @param uriName URI da entidade a ser modificada
     */
    public void updateEntity(Map<String, Object> data, String uriName) {
        Optional<Document> current = findEntity(uriName.trim());
        if (!current.isPresent()) {
            throw new IllegalArgumentException("Entidade não encontrada!");
        }
        data.put(URI_FIELD_NAME, uriName.trim());
        data.put(SEQ_FIELD_NAME, current.get().get(SEQ_FIELD_NAME));
        data.put(KEYS_FIELD_NAME, current.get().get(KEYS_FIELD_NAME));
        saveEntity(data);
    }

    @SuppressWarnings("unchecked")
    private void saveEntity(Map<String, Object> data) {
        String uriName    = uriValidation(data);
        String entityName = ensureNotNullOrEmpty(data, ENTITY_FIELD_NAME, "Nome da entidade não pode ser nulo!", "Nome da entidade não pode ser vazio!");

        fieldsCheck(data);

        data.put(COLLECTION_FIELD_NAME, DEFAULT_COLLECTION_PREFIX + uriName);
        data.put(URI_FIELD_NAME, uriName);
        data.put(ENTITY_FIELD_NAME, entityName);

        entityOperationService.replaceOne(getConfigurationCollection(), data);
    }

    @SuppressWarnings("unchecked")
    private void fieldsCheck(Map<String, Object> data) {
        ensureList(data, KEYS_FIELD_NAME, "Lista de chaves não pode ser nula!", "Lista de chaves deve ser um array!");
        ensureList(data, FIELDS_FIELD_NAME, "Lista de campos não pode ser nulo!", "Lista de campos deve ser um array!");

        List<String>              keys        = ((List<String>) data.get(KEYS_FIELD_NAME)).stream().distinct().collect(Collectors.toList());
        List<Map<String, Object>> fields      = (List) data.get(FIELDS_FIELD_NAME);
        List<String>              fieldsNames = fields.stream().map(x -> "" + (x).get("fieldName")).collect(Collectors.toList());

        keysCheck(keys, fields, fieldsNames);

        if (data.get(SEQ_FIELD_NAME) != null) {
            sequenceFieldValidation(data, fields, fieldsNames);
        }
    }

    private void keysCheck(List<String> keys, List<Map<String, Object>> fields, List<String> fieldsNames) {
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Lista de chaves não pode ser vazia!");
        }
        if (!fieldsNames.containsAll(keys)) {
            throw new IllegalArgumentException("Todas as chaves declaradas no campo 'keys' devem estar presentes na lista de campos! Keys: " + String.join(", ", ArrayUtils.toStringArray(keys.toArray())));
        }
        if (fields.size() != fields.stream().distinct().collect(Collectors.toList()).size()) {
            throw new IllegalArgumentException("Todos os campos devem ter nomes distintos entre si!");
        }

        for (Map<String, Object> field : fields) {
            if (keys.contains(field.get("fieldName").toString())) {
                String fieldType = field.getOrDefault("fieldType", "").toString();
                if (!ALLOWED_KEY_FIELD_TYPES.contains(fieldType)) {
                    throw new EntityKeyException(String.format("O campo chave '%s' deve ser do tipo Boolean, String, Timestamp, Integer ou Long! Encontrado: %s", field.get("fieldName"), fieldType));
                }
            }
        }
    }

    private void sequenceFieldValidation(Map<String, Object> data, List<Map<String, Object>> fields, List<String> fieldsNames) {
        String seqField = data.get(SEQ_FIELD_NAME).toString();

        if (!fieldsNames.contains(seqField)) {
            throw new IllegalArgumentException("O campo sequencial declarado deve estar contido na lista de campos!");
        }

        for (Map<String, Object> field : fields) {
            if (seqField.equals(field.get("fieldName"))) {
                String fieldType = field.getOrDefault("fieldType", "").toString();
                if (!ALLOWED_SEQ_FIELD_TYPES.contains(fieldType)) {
                    throw new IllegalArgumentException(String.format("O campo sequencial '%s' deve ser do tipo Integer ou Long! Encontrado: %s", seqField, fieldType));
                }
            }
        }
    }

    private void ensureList(Map<String, Object> data, String fieldName, String nullMessage, String notListMessage) {
        if (data.get(fieldName) == null) {
            throw new IllegalArgumentException(nullMessage);
        } else if (!List.class.isAssignableFrom(data.get(fieldName).getClass())) {
            throw new IllegalArgumentException(notListMessage);
        }
    }

    private String ensureNotNullOrEmpty(Map<String, Object> data, String fieldName, String nullMessage, String emptyMessage) {
        if (data.get(fieldName) == null) {
            throw new IllegalArgumentException(nullMessage);
        }
        String name = data.get(fieldName).toString().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException(emptyMessage);
        }
        return name;
    }

    private String uriValidation(Map<String, Object> data) {
        String uriName = ensureNotNullOrEmpty(data, URI_FIELD_NAME, "URI da entidade não pode ser nulo!", "URI da entidade não pode ser vazio!");
        if (!uriName.matches(REGEX_ENTITY)) {
            throw new IllegalArgumentException("URI da entidade deve conter apenas letras, '.' e '_'! De 1 a 100 caracteres. ");
        }
        return uriName;
    }
}
