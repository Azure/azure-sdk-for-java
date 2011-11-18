package com.microsoft.windowsazure.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.jersey.api.client.config.ClientConfig;

public class Configuration {

    private static Configuration instance;
    Map<String, Object> properties;
    Builder builder;

    static Log log = LogFactory.getLog(Configuration.class);

    public Configuration() {
        this.properties = new HashMap<String, Object>();
        this.builder = DefaultBuilder.create();
        init();
    }

    public Configuration(Builder builder) {
        this.properties = new HashMap<String, Object>();
        this.builder = builder;
        init();
    }

    private void init() {
        //		DefaultClientConfig clientConfig = new DefaultClientConfig();
        //		clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, true);
        try {
            setProperty("ClientConfig", builder.build("", ClientConfig.class, properties));
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static Configuration getInstance() {
        if (instance == null) {
            try {
                instance = Configuration.load();
            }
            catch (IOException e) {
                log.error("Unable to load META-INF/com.microsoft.windowsazure.properties", e);
                instance = new Configuration();
            }
        }
        return instance;
    }

    public static void setInstance(Configuration instance) {
        Configuration.instance = instance;
    }

    public static Configuration load() throws IOException {
        Configuration config = new Configuration();

        InputStream stream = Configuration.class.getClassLoader().getResourceAsStream("META-INF/com.microsoft.windowsazure.properties");
        if (stream != null) {
            Properties properties = new Properties();
            properties.load(stream);
            for (Object key : properties.keySet()) {
                config.setProperty(key.toString(), properties.get(key));
            }
        }

        return config;
    }

    public <T> T create(Class<T> service) throws Exception {
        return builder.build("", service, properties);
    }

    public <T> T create(String profile, Class<T> service) throws Exception {
        return builder.build(profile, service, properties);
    }

    public Builder getBuilder() {
        return builder;
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

}
