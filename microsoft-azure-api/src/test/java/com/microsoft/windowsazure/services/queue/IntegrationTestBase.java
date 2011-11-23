package com.microsoft.windowsazure.services.queue;

import com.microsoft.windowsazure.services.core.Configuration;

public abstract class IntegrationTestBase {
    protected static Configuration createConfiguration() {
        Configuration config = Configuration.getInstance();
        return config;
    }
}
