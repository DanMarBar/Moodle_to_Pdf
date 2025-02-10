package com.github.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesService {

    private static final Properties PROPERTIES = new Properties();
    private static final PropertiesService propertiesService = new PropertiesService();

    private PropertiesService() {
        loadProperties();
    }

    public static PropertiesService getInstance() {
        return propertiesService;
    }

    public String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    private void loadProperties() {
        try (InputStream input = PropertiesService.class.getClassLoader()
                .getResourceAsStream("config/personal.properties")) {
            PROPERTIES.load(input);
            System.out.println("Properties cargadas correctamente!");

        } catch (IOException e) {
            throw new RuntimeException("No se ha podido leer el archivo de config.properties " +
                    "donde se encuentra la informacion necesaria");
        }
    }
}
