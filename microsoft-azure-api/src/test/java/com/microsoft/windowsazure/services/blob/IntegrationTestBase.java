package com.microsoft.windowsazure.services.blob;

import com.microsoft.windowsazure.services.core.Configuration;

public abstract class IntegrationTestBase {
    protected static Configuration createConfiguration() {
        return Configuration.getInstance();
    }
}
