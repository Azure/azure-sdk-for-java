// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test;

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

    public <T> T getBean(Class<T> type) {
        return getApp().getBean(type);
    }

    public ApplicationContext parent() {
        return getApp().getParent();
    }

    public <T> Map<String, T> getParentBeans(Class<T> type) {
        return parent().getBeansOfType(type);
    }

    public String getProperty(String key) {
        return getApp().getEnvironment().getProperty(key);
    }

    public int port() {
        return getApp().getEnvironment().getProperty("server.port", Integer.class, -1);
    }

    public String root() {
        final String protocol = tlsEnabled() ? "https" : "http";
        return String.format("%s://localhost:%d/", protocol, port());
    }

    private boolean tlsEnabled() {
        return getApp().getEnvironment().getProperty("server.ssl.enabled", Boolean.class, false);
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
