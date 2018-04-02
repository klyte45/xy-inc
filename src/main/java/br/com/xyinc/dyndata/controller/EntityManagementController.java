package br.com.xyinc.dyndata.controller;

import br.com.xyinc.dyndata.model.ErrorModel;
import br.com.xyinc.dyndata.service.EntityManagementService;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class EntityManagementController {

    @Autowired
    private EntityManagementService entityManagementService;

    @RequestMapping(value = "/configuration/entity", method = RequestMethod.GET)
    public String listEntities() {
        return JSON.serialize(entityManagementService.listEntities());
    }

    @RequestMapping(value = "/configuration/entity/{uriName}", method = RequestMethod.GET)
    public ResponseEntity<String> getEntityByName(@PathVariable("uriName") String uriName) {
        Optional<Document> result = entityManagementService.findEntity(uriName);
        return result.map(document -> new ResponseEntity<>(JSON.serialize(document), HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(value = "/configuration/entity", method = RequestMethod.POST)
    public ResponseEntity addEntity(@RequestBody Map<String, Object> newEntity) {
        try {
            entityManagementService.createEntity(newEntity);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/configuration/entity/{uriName}", method = RequestMethod.PUT)
    public ResponseEntity editEntity(@RequestBody Map<String, Object> alterEntity, @PathVariable("uriName") String uriName) {
        try {
            entityManagementService.updateEntity(alterEntity, uriName);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/configuration/entity/{uriName}", method = RequestMethod.DELETE)
    public ResponseEntity deleteEntity(@PathVariable("uriName") String uriName) {
        try {
            entityManagementService.deleteEntity(uriName);
            return new ResponseEntity(HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return new ResponseEntity<>(new ErrorModel(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
