package com.dffrs.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class APIConfigurationReaderTester {
    URL testFile = getClass().getResource("/testFile.txt");
    URL testFileWithMissingParameters = getClass().getResource("/testFileWithMissingParameters.txt");
    URL testFileEmpty = getClass().getResource("/testFileEmpty.txt");

    APIConfigurationReader reader;
    String normalPath = "";
    String missingParametersPath = "";
    String emptyParametersPath = "";
    String nonExistingPath = "someFileThatDoesNotExist.txt";

    @Before
    public void init() throws URISyntaxException {
        normalPath = testFile.toURI().getPath();
        missingParametersPath = testFileWithMissingParameters.toURI().getPath();
        emptyParametersPath = testFileEmpty.toURI().getPath();

        reader = APIConfigurationReader.getInstance(normalPath);
    }

    @Test
    public void getConfigurationParametersLoadConfirmationTest() {
        for (Object co : APIConfigurationReader.getConfigurationsParameters().values())
            Assert.assertNotNull(co);
    }

    @Test
    public void getConfigurationsTest() throws FileNotFoundException {
        Map<String, String> aux = reader.getConfigurations();
        Assert.assertFalse(aux.isEmpty());
    }

    @Test
    public void getConfigurationsWithMissingParametersTest() throws FileNotFoundException {
        reader = APIConfigurationReader.getInstance(missingParametersPath);
        Map<String, String> auxWithNULLValues = reader.getConfigurations();
        Assert.assertTrue(auxWithNULLValues.size() != APIConfigurationReader.getConfigurationsParameters().size());
    }

    @Test(expected = IllegalStateException.class)
    public void getConfigurationsWithEmptyFileTest() throws FileNotFoundException {
        reader = APIConfigurationReader.getInstance(emptyParametersPath);
        Map<String, String> aux = reader.getConfigurations();
    }

    @Test(expected = FileNotFoundException.class)
    public void getConfigurationWithNonExistingFileTest() throws FileNotFoundException {
        reader = APIConfigurationReader.getInstance(nonExistingPath);
        Map<String, String> aux = reader.getConfigurations();
    }

    @Test(expected = IllegalArgumentException.class)
    public void getConfigurationWithEmptyPathTest() {
        reader = APIConfigurationReader.getInstance("");
    }
}
