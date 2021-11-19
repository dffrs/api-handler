package com.dffrs.handler;

import com.dffrs.util.APIConfigurationReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class APIHandler {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static APIHandler instance;
    private static String CONF_FILE;

    // TODO: Create an internal class to represent the different type of API Requests
    public static class Request {

        private final List<String> params;
        private final List<String> values;
        private String query;

        public Request(List<String> params, List<String> values) {
            this.params = new ArrayList<>();
            this.values = new ArrayList<>();

            // This option means the were no params
            if (params == null) {
                query = String.format(values.get(0), URLEncoder.encode((values.get(0)), CHARSET));
            } else {
                try {
                    query = prepareQuery(params, values);
                } catch (NullPointerException e) {
                    throw new NullPointerException("ERROR: Values's List can not be a NULL REFERENCE.\n");
                } catch (IllegalStateException e) {
                    throw new IllegalStateException("ERROR: Params's size =/= Values's size.\n");
                }
            }
        }

        public String getQuery() {
            return query;
        }

        //TODO: TEST
        private String encodeString(String param, String value)  {
            //TODO: Check to see if parameters, also, need to be encoded.
            // They dont...
            String temp = param +"="+(URLEncoder.encode(value, CHARSET));
            // String temp = (URLEncoder.encode(param +"="+value, CHARSET));
            return temp;
        }

        //TODO: TEST
        private String prepareQuery(List<String> params, List<String> values) throws NullPointerException,
                IllegalStateException{
            
            if (values == null) {
                throw new NullPointerException();
            }

            if (params.size() != values.size()) {
                throw new IllegalStateException();
            }

            String query = "";
            for (int i = 0; i != params.size(); i ++) {
                String tempParams = params.get(i);
                String tempValues = values.get(i);
                query += (encodeString(tempParams, tempValues) + "&");
            }
            return query.substring(0, query.length() - 1);
        }
    }

    static {
        try {
            CONF_FILE = ((URL) APIHandler.class.getResource("/configFile.txt")).toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            throw new NullPointerException("ERROR: Configuration Option file not found.\n");
        }
    }

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
    public HttpResponse<JsonNode> makeAPIRequest(APIHandler.Request r) throws UnirestException {
        String host = getAPIParameterBy("host");
        String api_host = getAPIParameterBy("rapid_api_host");
        String api_key = getAPIParameterBy("rapid_api_key");
        String header = getAPIParameterBy("header");
        String header1 = getAPIParameterBy("header1");
        String endpoint = getAPIParameterBy("endpoint");
        // String query = prepareQuery(paramaters, values);

        //TODO: Hardcode just to test the connection to an API Service
        // https://car-code.p.rapidapi.com/obd2/P0001
        String value = "P0001";
        String apiCall = host+"/"+endpoint+"/"+value;

        HttpResponse<JsonNode> request = Unirest.get(apiCall)
                .header(header, api_host)
                .header(header1, api_key)
                .asJson();
        return request;
    }

    public static APIHandler getInstance(){
        if (instance == null)
            instance = new APIHandler();
        return instance;
    }
}
