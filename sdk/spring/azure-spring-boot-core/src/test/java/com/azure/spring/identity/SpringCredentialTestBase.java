// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Properties;

public class SpringCredentialTestBase {

    StandardEnvironment buildEnvironment(Properties properties) {
        StandardEnvironment environment = new StandardEnvironment();
        final MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new PropertiesPropertySource("test", properties));

        return environment;
    }

    static class PropertiesBuilder {

        private final Properties properties = new Properties();
        private String prefix = "";

        public PropertiesBuilder prefix(String prefix) {
            if (prefix != null) {
                this.prefix = prefix;
            }
            return this;
        }

        public PropertiesBuilder property(String key, String value) {
            properties.put(prefix + key, value);
            return this;
        }

        public Properties build() {
            return properties;
        }

    }

}
