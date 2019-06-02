// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import org.junit.Assume;
import org.junit.BeforeClass;

public abstract class TestBase {

    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    @BeforeClass
    public static void skipIfNotConfigured() {
        Assume.assumeTrue(isTestConfigurationSet());
    }

    public String getConnectionString() {
        return CONNECTION_STRING;
    }

    private static boolean isTestConfigurationSet() {
        return CONNECTION_STRING != null;
    }
}
