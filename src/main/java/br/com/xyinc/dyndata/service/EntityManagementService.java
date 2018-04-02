package br.com.xyinc.dyndata.service;

import br.com.xyinc.dyndata.model.EntityDescriptor;
import br.com.xyinc.dyndata.model.FieldDescriptor;
import org.apache.commons.lang3.ArrayUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * Serviço de gerenciamento de novas entidades.
 */
@Service
public class EntityManagementService {

    public final static  String       SEQ_COLLECTION_NAME              = "seq.collections";
    public final static  String       SEQ_COLLECTION_LASTID_FIELD_NAME = "lastId";
    public final static  String       DEFAULT_COLLECTION_PREFIX        = "dyn.";
    final static         String       URI_FIELD_NAME                   = "uriName";
    final static         String       ENTITY_FIELD_NAME                = "entityName";
    final static         String       KEYS_FIELD_NAME                  = "keys";
    final static         String       SEQ_FIELD_NAME                   = "sequenceField";
    final static         String       FIELDS_FIELD_NAME                = "fields";
    private final static String       COLLECTION_FIELD_NAME            = "collectionName";
    private final static List<String> ALLOWED_SEQ_FIELD_TYPES          = Arrays.asList(FieldTypeService.DefaultFieldTypes.INTEGER, FieldTypeService.DefaultFieldTypes.LONG);
    private static EntityDescriptor _configurationCollection;

    @Autowired
    private MongoService     mongoService;
    @Autowired
    private FieldTypeService fieldTypeService;

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
            FieldDescriptor documentsFieldDescriptor = new FieldDescriptor("documentFields", FieldTypeService.DefaultFieldTypes.DOCUMENT_ARR, true);
            List<FieldDescriptor> fieldFieldsDescriptor = Arrays.asList(
                    new FieldDescriptor("fieldName", FieldTypeService.DefaultFieldTypes.STRING, false),
                    new FieldDescriptor("fieldType", FieldTypeService.DefaultFieldTypes.STRING, false, null, new ArrayList<>(fieldTypeService.getAllowedTypes())),
                    new FieldDescriptor("nullable", FieldTypeService.DefaultFieldTypes.BOOLEAN, true),
                    new FieldDescriptor("allowedValues", FieldTypeService.DefaultFieldTypes.STRING_ARR, true),
                    new FieldDescriptor("min", FieldTypeService.DefaultFieldTypes.DECIMAL, true),
                    new FieldDescriptor("max", FieldTypeService.DefaultFieldTypes.DECIMAL, true),
                    new FieldDescriptor("minLength", FieldTypeService.DefaultFieldTypes.INTEGER, true),
                    new FieldDescriptor("maxLength", FieldTypeService.DefaultFieldTypes.INTEGER, true),
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
        return mongoService.query(getConfigurationCollection(), null);
    }

    /**
     * Obtém entidade com o nome de URI especificado
     *
     * @param uriName URI da entidade a procurar
     * @return Descritor da entidade pesquisada
     */
    public Optional<Document> findEntity(String uriName) {
        return mongoService.query(getConfigurationCollection(), eq(URI_FIELD_NAME, uriName)).stream().findFirst();
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
        if (data.get(ENTITY_FIELD_NAME) == null) {
            throw new IllegalArgumentException("Nome da entidade não pode ser nulo!");
        }
        String uriName    = data.get(URI_FIELD_NAME).toString().trim();
        String entityName = data.get(ENTITY_FIELD_NAME).toString().trim();
        if (uriName.isEmpty()) {
            throw new IllegalArgumentException("URI da entidade não pode ser vazio!");
        }
        if (!uriName.matches("^[0-9a-zA-Z._]{1,100}$")) {
            throw new IllegalArgumentException("URI da entidade deve conter apenas letras, '.' e '_'! De 1 a 100 caracteres. ");
        }
        if (entityName.isEmpty()) {
            throw new IllegalArgumentException("Nome da entidade não pode ser vazio!");
        }

        if (data.get(KEYS_FIELD_NAME) == null) {
            throw new IllegalArgumentException("Lista de chaves não pode ser nula!");
        } else if (!List.class.isAssignableFrom(data.get(KEYS_FIELD_NAME).getClass())) {
            throw new IllegalArgumentException("Lista de chaves deve ser um array!");
        }
        if (data.get(FIELDS_FIELD_NAME) == null) {
            throw new IllegalArgumentException("Lista de campos não pode ser nulo!");
        } else if (!List.class.isAssignableFrom(data.get(FIELDS_FIELD_NAME).getClass())) {
            throw new IllegalArgumentException("Lista de campos deve ser um array!");
        }
        List<String>              keys        = (List<String>) data.get(KEYS_FIELD_NAME);
        List<Map<String, Object>> fields      = (List) data.get(FIELDS_FIELD_NAME);
        List<String>              fieldsNames = fields.stream().map(x -> "" + (x).get("fieldName")).collect(Collectors.toList());
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("Lista de chaves não pode ser vazia!");
        }
        if (!fieldsNames.containsAll(keys)) {
            throw new IllegalArgumentException("Todas as chaves declaradas no campo 'keys' devem estar presentes na lista de campos! Keys: " + String.join(", ", ArrayUtils.toStringArray(keys.toArray())));
        }

        data.put(COLLECTION_FIELD_NAME, DEFAULT_COLLECTION_PREFIX + uriName);
        data.put(URI_FIELD_NAME, uriName);
        data.put(ENTITY_FIELD_NAME, entityName);

        if (data.get(SEQ_FIELD_NAME) != null) {
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

        mongoService.replaceOne(getConfigurationCollection(), data);
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
        Map<String, Object> data = new HashMap<>();
        data.put(URI_FIELD_NAME, uriName.trim());
        mongoService.deleteOne(getConfigurationCollection(), data);
    }
}
