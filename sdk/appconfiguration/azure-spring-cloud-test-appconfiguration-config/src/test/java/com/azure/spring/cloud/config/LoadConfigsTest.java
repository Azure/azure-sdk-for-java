package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.azure.spring.cloud.config.app.DummyApp;
import com.azure.spring.test.AppRunner;

public class LoadConfigsTest {

    private final Logger log = LoggerFactory.getLogger(LoadConfigsTest.class);

    private static final String STORE_NAME = System.getenv("STORE_NAME");


    @Test
    public void sampleTest() {
        try (AppRunner app = new AppRunner(DummyApp.class)) {
            app.property("spring.cloud.azure.appconfiguration.stores[0].endpoint", STORE_NAME);
            app.property("config.message", "Test");
            app.start();
            assertEquals("Test",app.getProperty("config.message"));
        }
    }
}
