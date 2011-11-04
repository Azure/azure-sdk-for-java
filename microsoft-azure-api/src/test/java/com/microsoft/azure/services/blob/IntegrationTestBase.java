package com.microsoft.azure.services.blob;

import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.azure.configuration.Configuration;

public abstract class IntegrationTestBase {
    protected Configuration createConfiguration() {
        Configuration config = new Configuration();
        config.setProperty(BlobConfiguration.ACCOUNT_NAME, "xxx");
        config.setProperty(BlobConfiguration.ACCOUNT_KEY, "xxx");
        config.setProperty(BlobConfiguration.URL, "http://xxx.blob.core.windows.net");

        // when mock running
        // config.setProperty("serviceBus.uri", "http://localhost:8086");
        // config.setProperty("wrapClient.uri",
        // "http://localhost:8081/WRAPv0.9");

        return config;
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
