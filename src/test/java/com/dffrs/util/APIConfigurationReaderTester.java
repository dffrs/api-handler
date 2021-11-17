package com.dffrs.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    public void getConfigurationsTest() {
        Map<String, String> aux = reader.getConfigurations();
        Assert.assertFalse(aux.isEmpty());
    }

    @Test
    public void getConfigurationsWithMissingParametersTest() {
        reader = APIConfigurationReader.getInstance(missingParametersPath);
        Map<String, String> auxWithNULLValues = reader.getConfigurations();
        Assert.assertTrue(auxWithNULLValues.size() != APIConfigurationReader.getConfigurationsParameters().size());
    }

    @Test(expected = IllegalStateException.class)
    public void getConfigurationsWithEmptyFileTest() {
        reader = APIConfigurationReader.getInstance(emptyParametersPath);
        Map<String, String> aux = reader.getConfigurations();
    }
}
