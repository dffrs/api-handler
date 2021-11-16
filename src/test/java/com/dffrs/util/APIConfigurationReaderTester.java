package com.dffrs.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

public class APIConfigurationReaderTester {
    URL is = getClass().getResource("/testFile.txt");
    APIConfigurationReader reader;
    String path = "";

    @Before
    public void init() throws URISyntaxException {
        path = is.toURI().getPath();
        reader = APIConfigurationReader.getInstance(path);
    }

    @Test
    public void getConfigurationParametersTest(){
        for(Object co : reader.getGlobalConfigurations())
            Assert.assertTrue(reader.getConfigurationsParameters().containsValue(co));
    }

    @Test
    public void getConfigurationsTest(){
        Map<String, String> aux = reader.getConfigurations();
        Assert.assertFalse(aux.isEmpty());
    }
}
