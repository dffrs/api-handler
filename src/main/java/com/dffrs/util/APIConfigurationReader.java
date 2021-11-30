package com.dffrs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.PatternSyntaxException;

public final class APIConfigurationReader {

    /**
     * String representing the delimiter that should be used, when passing values to Configuration Options
     * fields, inside the Configuration File.
     * <p>
     * ex: host -> "some value here"
     */
    public static final String DELIMITER = "->";
    /**
     * Object representing {@link APIConfigurationReader} instance.
     */
    private static APIConfigurationReader instance;

    /**
     * String used to specify the path to the Configuration Options file.
     */
    private final String pathToConfFile;

    /**
     * Class's static attribute to keep loaded all the {@link ConfigurationOptions} values.
     * Used to compare with the values read from the Configuration Options file.
     * (Check {@link #getConfigurations()} method).
     */
    private static Map<String, ConfigurationOptions> confParameter;

    static {
        APIConfigurationReader.confParameter = new HashMap<>();
        loadConfigurationParameters();
    }

    /**
     * Private Enumerator to list all possible configuration options, that could be specified
     * inside an API configuration file.
     * If, later on, for some reason, new options need to be added, make sure it is defined here, first.
     * <p>
     * Each Configuration Option has an id, which is used to get identified when {@link #getConfigurations()}
     * is called.
     */
    private enum ConfigurationOptions {
        HOST("host"),
        RAPID_API_HOST("rapid_api_host"),
        RAPID_API_KEY("rapid_api_key"),
        HEADER("header"),
        ENDPOINT("endpoint");

        private final String confIdentifier;

        ConfigurationOptions(String id) {
            this.confIdentifier = id;
        }

        public String getConfIdentifier() {
            return confIdentifier;
        }
    }

    /**
     * Construct an {@link APIConfigurationReader} instance.
     *
     * @param pathToConfFile String to pass the path to the API configuration file.
     * @throws IllegalArgumentException When passed an empty string.
     */
    private APIConfigurationReader(String pathToConfFile) {
        if (pathToConfFile.isEmpty())
            throw new IllegalArgumentException("ERROR: Path to Configuration File does not exist.(Empty Argument)\n");
        this.pathToConfFile = pathToConfFile;
    }

    /**
     * Private procedure to load all configuration options, i.e. #ConfigurationOptions.values(),
     * and map them to {@link #confParameter}.
     */
    private static void loadConfigurationParameters() {
        for (ConfigurationOptions co : ConfigurationOptions.values()) {
            APIConfigurationReader.confParameter.put(co.getConfIdentifier(), co);
        }
    }

    /**
     * Public Static Factory method to instantiate an {@link #APIConfigurationReader(String)} object.
     *
     * @param pathToConfFile String to pass the path to the API configuration file.
     * @return #APIConfigurationReader instance.
     */
    public static APIConfigurationReader getInstance(String pathToConfFile) {
        if (instance == null || !instance.pathToConfFile.equals(pathToConfFile))
            instance = new APIConfigurationReader(pathToConfFile);
        return instance;
    }

    /**
     * Public static method to return all configuration parameters, i.e. Configuration options
     * and their ids.
     *
     * @return HashMap structure with all configuration parameters loaded. (See {@link #loadConfigurationParameters()}).
     */
    public static Map<String, ConfigurationOptions> getConfigurationsParameters() {
        return APIConfigurationReader.confParameter;
    }

    /**
     * Public method to return the current Configuration Options file
     * being used.
     *
     * @return String representing the path.
     */
    public String getFilePath() {
        return pathToConfFile;
    }

    /**
     * Public method that is responsible to load API configuration options through the specified file path,
     * given when {@link APIConfigurationReader} instance was created.
     * It compares each element read to {@link #confParameter} entries. Everytime it fails to map
     * the String read to an entry, it adds a NULL reference, i.e.
     * <p>
     *           |-(String matched) --------> ({@link #confParameter} entry equivalent)
     *      map -|-(String that failed) ----> null
     *          (...)
     *
     * @return HashMap structure containing every #ConfigurationOptions found.
     * @throws FileNotFoundException Whenever the Configuration Options file was not found.
     */
    public Map<String, String> getConfigurations() throws FileNotFoundException, PatternSyntaxException,
            ArrayIndexOutOfBoundsException, IllegalStateException {
        Map<String, String> aux = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(this.pathToConfFile))) {
            String temp;
            String[] array;
            int i = 1;
            while (scanner.hasNext()) {
                temp = scanner.nextLine().replaceAll(" ", "");
                if (!temp.isEmpty()) {
                    array = temp.split(DELIMITER);
                    if (APIConfigurationReader.confParameter.containsKey(array[0])) {
                        // Temp solution. If map already contains one, add another with "name"+1.
                        if (aux.containsKey(array[0])) {
                            aux.put(array[0] + i, array[1]);
                            i++;
                        } else {
                            aux.put(array[0], array[1]);
                        }
                    } else {
                        // Everytime a configuration option is not recognised inside the file, it will be
                        // associated with a NULL Reference
                        aux.put(array[0], null);
                    }
                }
            }
        }

        if (aux.isEmpty())
            throw new IllegalStateException();
        return aux;
    }
}