/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Configuration {

    /**
     * Property name for socket connection timeout used by services created with this configuration.
     */
    public static final String PROPERTY_CONNECT_TIMEOUT = "com.microsoft.windowsazure.services.core.Configuration.connectTimeout";

    /**
     * Property name for socket read timeout used by services created with this configuration.
     */
    public static final String PROPERTY_READ_TIMEOUT = "com.microsoft.windowsazure.services.core.Configuration.readTimeout";

    /**
     * Property name to control if HTTP logging to console is on or off. If property is set, logging is on, regardless
     * of value.
     */
    public static final String PROPERTY_LOG_HTTP_REQUESTS = "com.microsoft.windowsazure.services.core.Configuration.logHttpRequests";

    private static Configuration instance;
    Map<String, Object> properties;
    Builder builder;

    static Log log = LogFactory.getLog(Configuration.class);

    public Configuration() {
        this.properties = new HashMap<String, Object>();
        this.builder = DefaultBuilder.create();
    }

    public Configuration(Builder builder) {
        this.properties = new HashMap<String, Object>();
        this.builder = builder;
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

        InputStream stream = Configuration.class.getClassLoader().getResourceAsStream(
                "META-INF/com.microsoft.windowsazure.properties");
        if (stream != null) {
            Properties properties = new Properties();
            properties.load(stream);
            for (Object key : properties.keySet()) {
                config.setProperty(key.toString(), properties.get(key));
            }
        }

        return config;
    }

    public <T> T create(Class<T> service) {
        return builder.build("", service, service, properties);
    }

    public <T> T create(String profile, Class<T> service) {
        return builder.build(profile, service, service, properties);
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

    public Map<String, Object> getProperties() {
        return properties;
    }
}
