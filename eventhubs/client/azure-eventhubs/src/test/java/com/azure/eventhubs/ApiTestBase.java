// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;

abstract class ApiTestBase extends TestBase {
    private static final String EVENT_HUB_CONNECTION_STRING_ENV_NAME = "AZURE_EVENTHUBS_CONNECTION_STRING";
    private static final String CONNECTION_STRING = System.getenv(EVENT_HUB_CONNECTION_STRING_ENV_NAME);

    String getConnectionString() {
        return CONNECTION_STRING;
    }

    @Override
    public TestMode getTestMode() {
        if (super.getTestMode() == TestMode.RECORD) {
            return ImplUtils.isNullOrEmpty(getConnectionString())
                ? TestMode.PLAYBACK
                : TestMode.RECORD;
        }
        return super.getTestMode();
    }
}
