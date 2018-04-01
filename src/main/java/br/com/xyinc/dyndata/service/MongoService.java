package br.com.xyinc.dyndata.service;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.stereotype.Service;

@Service
public class MongoService {

    private String dbUrl = "localhost";
    private int dbPort = 27017;
    private String dbName = "dyndata";

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String newUrl) {
        dbUrl = newUrl;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int newPort) {
        dbPort = newPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String newName) {
        dbName = newName;
    }

    public void testConnection() {
        MongoClient client = new MongoClient(new ServerAddress(dbUrl, dbPort));
        client.getAddress();
    }
}
