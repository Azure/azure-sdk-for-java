package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.services.core.Configuration;

public abstract class IntegrationTestBase {
    protected static Configuration createConfiguration() {
        return Configuration.getInstance();
    }

    protected static boolean isRunningWithEmulator(Configuration config) {
        String accountName = "devstoreaccount1";
        String accountKey = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

        return accountName.equals(config.getProperty(QueueConfiguration.ACCOUNT_NAME))
                && accountKey.equals(config.getProperty(QueueConfiguration.ACCOUNT_KEY));
    }
}
