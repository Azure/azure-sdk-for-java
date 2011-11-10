package com.microsoft.azure.services.blob;

import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.azure.configuration.Configuration;

public abstract class IntegrationTestBase {
    protected Configuration createConfiguration() {
        Configuration config = new Configuration();
        Map<String, String> env = System.getenv();
        setConfigValue(config, env, BlobConfiguration.ACCOUNT_NAME, "xxx");
        setConfigValue(config, env, BlobConfiguration.ACCOUNT_KEY, "xxx");
        setConfigValue(config, env, BlobConfiguration.URL, "http://xxx.blob.core.windows.net");

        // when mock running
        // config.setProperty("serviceBus.uri", "http://localhost:8086");
        // config.setProperty("wrapClient.uri",
        // "http://localhost:8081/WRAPv0.9");

        return config;
    }

    private void setConfigValue(Configuration config, Map<String, String> props, String key, String defaultValue) {
        String value = props.get(key);
        if (value == null)
            value = defaultValue;

        config.setProperty(key, value);
    }

    @BeforeClass
    public static void initializeSystem() {
        System.out.println("initialize");
        // System.setProperty("http.proxyHost", "itgproxy");
        // System.setProperty("http.proxyPort", "80");
        // System.setProperty("http.keepAlive", "false");
    }

    @Before
    public void initialize() throws Exception {
        System.out.println("initialize");
        // System.setProperty("http.proxyHost", "itgproxy");
        // System.setProperty("http.proxyPort", "80");
        // System.setProperty("http.keepAlive", "false");
    }
}
