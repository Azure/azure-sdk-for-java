// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

public abstract class TestBase {

    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    public String getConnectionString() {
        return CONNECTION_STRING;
    }

    public boolean isTestConfigurationSet() {
        return CONNECTION_STRING != null;
    }
}
