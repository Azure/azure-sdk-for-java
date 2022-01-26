// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.SocketUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AppRunner implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppRunner.class);

    private final Class<?> appClass;
    private final Map<String, String> props;

    private ConfigurableApplicationContext app;

    public AppRunner(Class<?> appClass) {
        this.appClass = appClass;
        props = new LinkedHashMap<>();
    }

    public void property(String key, String value) {
        props.put(key, value);
    }

    public ConfigurableApplicationContext start() {
        if (app == null) {
            final SpringApplicationBuilder builder = new SpringApplicationBuilder(appClass);
            builder.properties("spring.jmx.enabled=false");
            if (!props.containsKey("server.port")){
                builder.properties(String.format("server.port=%d", availableTcpPort()));
            }
            builder.properties(props());
            LOGGER.info("app begin to run.");
            app = builder.build().run();
            LOGGER.info("app running.");
        }
        return app;
    }

    private int availableTcpPort() {
        return SocketUtils.findAvailableTcpPort();
    }

    private String[] props() {
        final List<String> result = new ArrayList<>();

        for (final Map.Entry<String, String> entry : props.entrySet()) {
            result.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
        }

        return result.toArray(new String[0]);
    }

    public void stop() {
        if (app != null) {
            app.close();
            app = null;
        }
    }

    public ApplicationContext parent() {
        return getApp().getParent();
    }

    public String getProperty(String key) {
        return getApp().getEnvironment().getProperty(key);
    }

    @Override
    public void close() {
        stop();
    }

    private ConfigurableApplicationContext getApp() {
        if (app == null) {
            throw new ApplicationContextException("App is not running.");
        }
        return app;
    }
}
