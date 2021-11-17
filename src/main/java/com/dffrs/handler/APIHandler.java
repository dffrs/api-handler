package com.dffrs.handler;

import com.dffrs.util.APIConfigurationReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Deque;
import java.util.Map;

public final class APIHandler {
    private static APIHandler instance;
    private static final String CONF_FILE = "TEMP";
    private APIConfigurationReader reader;
    private Map<String, String> configurations;

    private void initReader() {
        reader = APIConfigurationReader.getInstance(CONF_FILE);
        try {
            configurations = reader.getConfigurations();
        } catch (FileNotFoundException e) {
            reader = null;
            configurations = null;
            instance = null;
        }
    }

    private APIHandler() {
        initReader();
    }

    public String getAPIParameterBy(String option) {
        if (option == null)
            throw new IllegalArgumentException("ERROR: Option can not be empty.\n");

        //It may be a NULL value, if configurations Map does not contain the option passed as argument.
        return configurations.get(option);
    }

    //TODO: TEST
    public void makeAPIRequest(Deque<String> paramaters, Deque<String> values) throws UnirestException {
        String host = getAPIParameterBy("host");
        String api_host = getAPIParameterBy("rapid_api_host");
        String api_key = getAPIParameterBy("rapid_api_key");
        String header = getAPIParameterBy("header");
        String endpoint = getAPIParameterBy("endpoint");
        String query = prepareQuery(paramaters, values);

        HttpResponse<JsonNode> request = Unirest.get(host+"/"+endpoint+"?"+query)
                .header("x-rapidapi-host", header)
                .header("x-rapidapi-key", api_key)
                .asJson();
    }

    //TODO: TEST
    public String prepareQuery(Deque<String> params, Deque<String> values) {
        if (params.isEmpty() || values.isEmpty())
            throw new IllegalArgumentException();
        if (params.size() != values.size())
            throw new IllegalStateException();
        StringBuilder finalQuery = new StringBuilder();

        while(!params.isEmpty()) {
            try {
                finalQuery.append(encodeString(params.pop(), values.pop()));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                break;
            }
        }

        return finalQuery.toString();
    }

    //TODO: TEST
    private String encodeString(String param, String value) throws UnsupportedEncodingException {
        String charset = "UTF-8";
        return String.format(param+"=%value", URLEncoder.encode(value, charset));
    }

    public static APIHandler getInstance(){
        if (instance == null)
            instance = new APIHandler();
        return instance;
    }
}
