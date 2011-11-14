package com.microsoft.windowsazure.common;

import java.util.HashMap;
import java.util.Map;

import com.sun.jersey.api.client.config.ClientConfig;

public class Configuration {

    private static Configuration instance;
    Map<String, Object> properties;
    Builder builder;

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
        if (instance == null)
            instance = Configuration.load();
        return instance;
    }

    public static void setInstance(Configuration instance) {
        Configuration.instance = instance;
    }

    public static Configuration load() {
        // TODO - load from somewhere
        return new Configuration();
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
