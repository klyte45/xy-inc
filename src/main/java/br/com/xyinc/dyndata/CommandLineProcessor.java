package br.com.xyinc.dyndata;

import br.com.xyinc.dyndata.service.MongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

@Service
public class CommandLineProcessor implements CommandLineRunner {

    private static final String[] trueValues = new String[]{"1", "true"};

    @Autowired
    private MongoService mongoService;

    @Override
    public void run(String... args) {
        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length != 2) continue;
            switch (kv[0]) {
                case "--dbport":
                    try {
                        int portNumber = Integer.parseInt(kv[1]);
                        if (portNumber < 1024 || portNumber > 0xFFFF) {
                            throw new IllegalArgumentException("Número da porta do banco de dados deve estar entre 1024 e " + 0xFFFF);
                        } else {
                            mongoService.setDbPort(portNumber);
                        }
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Número de porta do banco de dados inválido: " + kv[1], e);
                    }
                    break;
                case "--dburl":
                    mongoService.setDbUrl(kv[1]);
                    break;
                case "--dbname":
                    mongoService.setDbName(kv[1]);
                    break;
            }
        }
        mongoService.testConnection();
    }
}