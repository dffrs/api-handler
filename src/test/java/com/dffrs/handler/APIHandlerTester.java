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
    URL APIWithNoParameters = getClass().getResource("/testFileAPIWithNoParameters.txt");
    URL APIWithParameters = getClass().getResource("/testFileAPIWithParameters.txt");

    APIHandler handler;
    String testFilePathWithParameters = "";
    String testFilePathWithNoParameters = "";

    @Before
    public void init() throws URISyntaxException {
        testFilePathWithNoParameters = APIWithNoParameters.toURI().getPath();
        testFilePathWithParameters = APIWithParameters.toURI().getPath();

        // Default: API With no Parameters
        handler = APIHandler.getInstance(testFilePathWithNoParameters);
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
        List<String> valuesList = List.of("P0001");
        HttpResponse<JsonNode> response;

        response = handler.makeAPIRequest(new APIHandler.Request(null, valuesList));

        Assert.assertNotNull(response);
    }

    @Test
    public void makeAPICallWithNoParametersWithCacheTest() throws UnirestException {
        List<String> valuesList = List.of("P0001");
        HttpResponse<JsonNode> response;
        HttpResponse<JsonNode> response2;
        HttpResponse<JsonNode> response3;

        // First call
        response = handler.makeAPIRequest(new APIHandler.Request(null, valuesList));

        // Second call
        response2 = handler.makeAPIRequest(new APIHandler.Request(null, valuesList));

        // Third call
        response3 = handler.makeAPIRequest(new APIHandler.Request(null, List.of("P0002")));

        Assert.assertEquals(response, response2);
        Assert.assertNotEquals(response, response3);

    }

    @Test
    public void makeAPICallWithParametersWithCacheTest() throws UnirestException {
        HttpResponse<JsonNode> response;
        HttpResponse<JsonNode> response2;
        handler = APIHandler.getInstance(testFilePathWithParameters);

        response = handler.makeAPIRequest(new APIHandler.Request(List.of("q"), List.of("Kendrick Lamar")));
        Assert.assertEquals(200, response.getStatus());

        response2 = handler.makeAPIRequest(new APIHandler.Request(List.of("q"), List.of("Kendrick Lamar")));

        Assert.assertEquals(response, response2);
    }
}
