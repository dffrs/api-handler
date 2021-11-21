package com.dffrs.handler;

import com.dffrs.memory.inMemoryCache.LRUCache;
import com.dffrs.util.APIConfigurationReader;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

public final class APIHandler {
    /**
     * Charset used when encoding.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    /**
     * Instance of {@link APIHandler}, based on Singleton Code Design.
     */
    private static APIHandler instance;

    /**
     * Map used to keep all the API Configuration Options loaded in memory.
     * Used in {@link #getAPIParameterBy(String)} method.
     */
    private Map<String, String> configurations;
    /**
     * In memory type of cache to stored recently API call requests.
     * Used in {@link #makeAPIRequest(Request)} method.
     */
    private static final LRUCache<String, HttpResponse<JsonNode>> cache;

    /**
     * Integer to define the number of occurrences the {@link #cache} should keep.
     */
    private static final int INITIAL_VALUE_FOR_CACHE = 5;

    /**
     * {@link APIConfigurationReader} instance. Useful here: {@link #getInstance(String)}.
     */
    private static APIConfigurationReader reader;

    static {
        cache = new LRUCache<>(INITIAL_VALUE_FOR_CACHE);
    }

    /**
     * Nested class responsible for creating an encoded UTF-8 URL query, used whenever an API Call is made.
     */
    public static class Request {
        /**
         * String representing the final query, after being formatted based on UTF-8 Standard.
         */
        private final String query;

        /**
         * Constructor responsible to create a Request instance. It behaves different if it is desired to
         * specify the params to URL query.
         * If params is a NULL Reference, it, only, encodes the values's first element, otherwise it encodes
         * all params and values elements.
         *
         * @param params List containing all params needed for the URL.
         * @param values List containing all the elements to match the params's.
         */
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

        /**
         * Public method responsible to return the final URL query.
         *
         * @return String representing the query.
         */
        public String getQuery() {
            return query;
        }

        /**
         * Private method, used in {@link #Request(List, List)}, to create and prepare the URL query.
         *
         * @param params List containing all params needed for the URL.
         * @param values List containing all the elements to match the params's.
         * @return String representing the final and encoded query, ready to be used.
         *
         * @throws NullPointerException everytime the Values List is a NULL Reference.
         * @throws IllegalArgumentException everytime the Params List's and Values List's sizes are different.
         */
        private String prepareQuery(List<String> params, List<String> values) throws NullPointerException,
                IllegalStateException {

            if (values == null) {
                throw new NullPointerException();
            }

            if (params.size() != values.size()) {
                throw new IllegalStateException();
            }

            // This is necessary for API with param=value URL queries
            String query = "?";
            for (int i = 0; i != params.size(); i++) {
                query = query.concat(encodeString(params.get(i), values.get(i)) + "&");
            }
            return query.substring(0, query.length() - 1);
        }

        /**
         * Private method to do the actual URL query encoding. Used inside {@link #prepareQuery(List, List)}.
         *
         * @param param String representing the param to match the future encoded value.
         * @param value String representing the value to encode.
         * @return String with param and value encoded.
         */
        private String encodeString(String param, String value) {
            // Check to see if parameters, also, need to be encoded.
            // They dont...
            return param + "=" + (URLEncoder.encode(value, CHARSET));
        }
    }

    /**
     * Private method responsible to initiate {@link #configurations}, based on
     * APIConfigurationReader.getConfigurations() map.
     * Used in {@link #APIHandler()}.
     */
    private void initReader() {
        try {
            configurations = reader.getConfigurations();

        } catch (FileNotFoundException | PatternSyntaxException | ArrayIndexOutOfBoundsException |
                 IllegalStateException e) {
            String message;
            if (e.getClass().equals(FileNotFoundException.class))
                message = "ERROR: Configuration Options File was not found. " +
                        "Check " + reader.getFilePath();
            else if (e.getClass().equals(ArrayIndexOutOfBoundsException.class))
                message = "ERROR: Configuration Options File have parameters with empty values. " +
                        "Check " + reader.getFilePath();
                else if (e.getClass().equals(IllegalStateException.class))
                    message = "ERROR: Configuration Options not detected inside specified file. " +
                            "Check " + reader.getFilePath();
                    else
                        message = "ERROR: Configuration Options File uses the wrong delimiter. " +
                                "Check " +APIConfigurationReader.DELIMITER + " and " + reader.getFilePath();

            System.err.println(message + "\n\n" + e.getClass()+": "+e.getMessage());
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

    /**
     * Public method used to make an API Call, based on the {@link #configurations} values, and
     * APIHandler.Request's query.
     * It caches the responses, for responsiveness and efficiency sake.
     *
     * @param request APIHandler.Request's instance to retrieve the URL query.
     * @return HttpResponse object, after the call was made.
     * @throws UnirestException If the call was unsuccessful.
     */
    public HttpResponse<JsonNode> makeAPIRequest(APIHandler.Request request) throws UnirestException {
        // https://car-code.p.rapidapi.com/obd2/P0001
        String apiCall = getAPIParameterBy("endpoint") + "/"
                + request.getQuery();

        HttpResponse<JsonNode> r = cache.get(apiCall);
        if (r == null) { // This means the cache has no record of that request.
            r = Unirest.get(getAPIParameterBy("host") + "/" + apiCall)
                    .header(getAPIParameterBy("header"), getAPIParameterBy("rapid_api_host"))
                    .header(getAPIParameterBy("header1"), getAPIParameterBy("rapid_api_key"))
                    .asJson();
            cache.addElement(apiCall, r);
        }
        return r;
    }

    /**
     * Public Static Factory method to instantiate an {@link APIHandler} object.
     *
     * @param pathToFile String representing the path to the Configuration Options File.
     *                   Will be used on a APIConfigurationReader's instance.
     * @return #APIConfigurationReader instance.
     */
    public static APIHandler getInstance(String pathToFile) {
        if (instance == null || !reader.getFilePath().equals(pathToFile)) {
            reader = APIConfigurationReader.getInstance(pathToFile);
            instance = new APIHandler();
        }
        return instance;
    }
}
