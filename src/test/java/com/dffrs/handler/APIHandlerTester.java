package com.dffrs.handler;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class APIHandlerTester {
    URL testFile = getClass().getResource("/testFile.txt");

    APIHandler handler;
    String testFilePath = "";

    @Before
    public void init() throws URISyntaxException {
        testFilePath = testFile.toURI().getPath();

        handler = APIHandler.getInstance();
    }

    @Test
    public void getAPIParameterByTest() {
        String option = "host";
        String result = handler.getAPIParameterBy(option);

        // NUll means that there were no value saved for the specified option.
        Assert.assertNotNull(result);
    }

    @Test
    public void getQueryWithNoParametersTest() {
        List<String> valuesList = new ArrayList<>();
        valuesList.add("P0001");

        String query = new APIHandler.Request(null, valuesList).getQuery();
        Assert.assertEquals(URLEncoder.encode(valuesList.get(0), StandardCharsets.UTF_8), query);
    }

    @Test
    public void makeAPICallWithNoParametersTest() throws UnirestException {
        List<String> valuesList = List.of("POOO1");
        HttpResponse<JsonNode> response;

        response = handler.makeAPIRequest(new APIHandler.Request(null, valuesList));

        Assert.assertNotNull(response);
    }
}
