package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.services.core.Configuration;

public abstract class IntegrationTestBase {
    protected static Configuration createConfiguration() {
        Configuration config = Configuration.getInstance();
        OverrideWithEnv(config, QueueConfiguration.ACCOUNT_NAME);
        OverrideWithEnv(config, QueueConfiguration.ACCOUNT_KEY);
        OverrideWithEnv(config, QueueConfiguration.URI);
        return config;
    }

    private static void OverrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }

    protected static boolean isRunningWithEmulator(Configuration config) {
        String accountName = "devstoreaccount1";
        String accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

        return accountName.equals(config.getProperty(QueueConfiguration.ACCOUNT_NAME))
                && accountKey.equals(config.getProperty(QueueConfiguration.ACCOUNT_KEY));
    }
}
