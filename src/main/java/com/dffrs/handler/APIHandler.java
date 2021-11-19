package com.dffrs.handler;

import com.dffrs.util.APIConfigurationReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class APIHandler {
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static APIHandler instance;
    private static String CONF_FILE;
    private Map<String, String> configurations;

    static {
        try {
            CONF_FILE = Objects.requireNonNull(APIHandler.class.getResource("/configFile.txt")).toURI().getPath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            throw new NullPointerException("ERROR: Configuration Option file not found.\n");
        }
    }

    public static class Request {

        private final String query;

        public Request(final List<String> params, final List<String> values) {

            // This option means the were no params
            if (params == null) {
                // TODO: For now, this only utilizes the first element inside values's List.
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
                query = query.concat( encodeString(params.get(i), values.get(i)) + "&");
            }
            return query.substring(0, query.length() - 1);
        }

        private String encodeString(String param, String value)  {
            // Check to see if parameters, also, need to be encoded.
            // They dont...
            return param +"="+(URLEncoder.encode(value, CHARSET));
        }
    }

    private void initReader() {
        try {
            configurations = APIConfigurationReader.getInstance(CONF_FILE).getConfigurations();
        } catch (FileNotFoundException e) {
            configurations = null;
            instance = null;
        }
    }

    private APIHandler() {
        initReader();
    }

    /**
     * Public method to return an API's parameter.
     * It uses {@link #configurations} HashMap as reference to return the value.
     * NOTE: It may return a NULL Reference, if the option is not found.
     *
     * @param option String to search for.
     * @return String representing the parameter's value.
     */
    public String getAPIParameterBy(String option) {
        if (option == null)
            throw new IllegalArgumentException("ERROR: Option can not be empty.\n");

        //It may be a NULL value, if configurations Map does not contain the option passed as argument.
        return configurations.get(option);
    }

    public HttpResponse<JsonNode> makeAPIRequest(APIHandler.Request r) throws UnirestException {
        // https://car-code.p.rapidapi.com/obd2/P0001
        String apiCall = getAPIParameterBy("host")+"/"+getAPIParameterBy("endpoint")+"/"
                + r.getQuery();

        return Unirest.get(apiCall)
                .header(getAPIParameterBy("header"), getAPIParameterBy("rapid_api_host"))
                .header(getAPIParameterBy("header1"), getAPIParameterBy("rapid_api_key"))
                .asJson();
    }

    public static APIHandler getInstance(){
        if (instance == null)
            instance = new APIHandler();
        return instance;
    }
}
