package com.microsoft.windowsazure.services.queue;

import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;

import com.microsoft.windowsazure.services.core.Configuration;

public abstract class IntegrationTestBase {
    protected static Configuration createConfiguration() {
        Configuration config = new Configuration();
        Map<String, String> env = System.getenv();

        // Storage emulator support
        //setConfigValue(config, env, QueueConfiguration.ACCOUNT_NAME, "devstoreaccount1");
        //setConfigValue(config, env, QueueConfiguration.ACCOUNT_KEY, "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==");
        //setConfigValue(config, env, QueueConfiguration.URL, "http://127.0.0.1:10001/devstoreaccount1");

        // Storage account support
        setConfigValue(config, env, QueueConfiguration.ACCOUNT_NAME, "xxx");
        setConfigValue(config, env, QueueConfiguration.ACCOUNT_KEY, "xxx");
        setConfigValue(config, env, QueueConfiguration.URL, "http://xxx.queue.core.windows.net");

        return config;
    }

    private static void setConfigValue(Configuration config, Map<String, String> props, String key, String defaultValue) {
        String value = props.get(key);
        if (value == null)
            value = defaultValue;

        config.setProperty(key, value);
    }

    @BeforeClass
    public static void initializeSystem() {
        System.out.println("initialize");
    }

    @Before
    public void initialize() throws Exception {
        System.out.println("initialize");
    }
}
