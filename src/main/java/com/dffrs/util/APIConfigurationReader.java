package com.dffrs.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public final class APIConfigurationReader {

    private static final String DELIMITER = "->";
    private static APIConfigurationReader instance;
    private final String pathToConfFile;
    private Map<String, ConfigurationOptions> confParameter;

    private enum ConfigurationOptions {
        HOST("host"),
        RAPID_API_HOST("rapid_api_host"),
        RAPID_API_KEY("rapid_api_key"),
        HEADER("header"),
        ENDPOINT("endpoint");

        private String confIdentifier;

        private ConfigurationOptions(String id) {
            this.confIdentifier = id;
        }

        public String getConfIdentifier() {
            return confIdentifier;
        }
    }

    private APIConfigurationReader(String pathToConfFile) {
        if (pathToConfFile.isEmpty())
            throw new IllegalArgumentException("ERROR: Path to Configuration File does not exist.(Empty Argument)\n");
        this.pathToConfFile = pathToConfFile;
        this.confParameter = new HashMap<String, ConfigurationOptions>();

        // Load configuration options.
        loadConfigurationParameters();
    }

    private void loadConfigurationParameters() {
        for (ConfigurationOptions co : ConfigurationOptions.values()) {
            this.confParameter.put(co.getConfIdentifier(), co);
        }
    }

    public static APIConfigurationReader getInstance(String pathToConfFile) {
        if (instance == null)
            instance = new APIConfigurationReader(pathToConfFile);
        return instance;
    }

    public Map<String, String> getConfigurations() {
        Map<String, String> aux = new HashMap<String, String>();
        try (Scanner scanner = new Scanner(new File(this.pathToConfFile))) {
            String temp = "";
            String[] array;
            while (scanner.hasNext()) {
                temp = scanner.nextLine();
                array = temp.split(DELIMITER);
                if (confParameter.containsKey(array[0])) {
                    aux.put(array[0], array[1]);
                } else {
                    // Everytime a configuration option is not recognised inside the file, it will be
                    // associated with a NULL Reference
                    aux.put(array[0], null);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (aux.isEmpty())
            throw new IllegalStateException("ERROR: Configuration Options not detected inside specified file.\n");
        return aux;
    }
}