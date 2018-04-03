package br.com.xyinc.dyndata.controller;

import br.com.xyinc.dyndata.model.ErrorModel;
import br.com.xyinc.dyndata.service.DynamicEntityService;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static br.com.xyinc.dyndata.service.EntityManagementService.REGEX_ENTITY;


@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class DynamicEntityController {

    @Autowired
    private DynamicEntityService dynamicEntityService;

    @RequestMapping(value = "/{entityUri:" + REGEX_ENTITY + "}", method = RequestMethod.GET)
    public String list(@PathVariable("entityUri") String entityUri) {
        return JSON.serialize(dynamicEntityService.list(entityUri));
    }

    @RequestMapping(value = "/{entityUri:" + REGEX_ENTITY + "}/{id:.+}", method = RequestMethod.GET)
    public ResponseEntity<String> getById(@PathVariable("entityUri") String entityUri, @PathVariable("id") String id) {
        Optional<Document> result = dynamicEntityService.get(entityUri, Arrays.asList(id.split("/")));
        return result.map(document -> new ResponseEntity<>(JSON.serialize(document), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/{entityUri:" + REGEX_ENTITY + "}", method = RequestMethod.POST)
    public ResponseEntity create(@PathVariable("entityUri") String entityUri, @RequestBody Map<String, Object> newEntity) {
        try {
            dynamicEntityService.create(entityUri, newEntity);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{entityUri:" + REGEX_ENTITY + "}/{id:.+}", method = RequestMethod.PUT)
    public ResponseEntity update(@PathVariable("entityUri") String entityUri, @RequestBody Map<String, Object> alterEntity, @PathVariable("id") String id) {
        try {
            dynamicEntityService.update(entityUri, alterEntity, Arrays.asList(id.split("/")));
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{entityUri:" + REGEX_ENTITY + "}/{id:.+}", method = RequestMethod.DELETE)
    public ResponseEntity delete(@PathVariable("entityUri") String entityUri, @PathVariable("id") String id) {
        try {
            dynamicEntityService.delete(entityUri, Arrays.asList(id.split("/")));
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
