package br.com.xyinc.dyndata;

import br.com.xyinc.dyndata.service.MongoService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.annotation.Resource;

import static org.mockito.Mockito.*;

public class CommandLineProcessorTest {

    @Mock
    private MongoService mongoService;

    @InjectMocks
    @Resource
    private CommandLineProcessor commandLineProcessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(mongoService).testConnection();
        doCallRealMethod().when(mongoService).setDbName(any(String.class));
        doCallRealMethod().when(mongoService).setDbUrl(any(String.class));
        doCallRealMethod().when(mongoService).setDbPort(any(Integer.class));
        when(mongoService.getDbName()).thenCallRealMethod();
        when(mongoService.getDbUrl()).thenCallRealMethod();
        when(mongoService.getDbPort()).thenCallRealMethod();
    }

    @Test
    public void run_noArgs() {
        commandLineProcessor.run();
    }

    @Test
    public void run_validArgs() {
        final int validPort = 8080;
        final String hostName = "teste.host";
        final String dbName = "testName";
        commandLineProcessor.run("--dbport=" + validPort, "--dburl=" + hostName, "--dbname=" + dbName);
        assert dbName.equals(mongoService.getDbName());
        assert hostName.equals(mongoService.getDbUrl());
        assert validPort == mongoService.getDbPort();
    }


    @Test(expected = IllegalArgumentException.class)
    public void run_invalidPortString() {
        commandLineProcessor.run("--dbport=AHSghagdhasdha");
    }

    @Test(expected = IllegalArgumentException.class)
    public void run_invalidPortOutOfRange() {
        commandLineProcessor.run("--dbport=70000");
    }
}