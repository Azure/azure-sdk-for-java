// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.test.utils;

import com.azure.core.management.provider.DelayProvider;

import java.time.Duration;

/**
 * Class helps thread sleep in tests.
 */
public class TestDelayProvider implements DelayProvider {

    private final boolean isLiveMode;

    /**
     * Constructor of TestDelayProvider
     *
     * @param isLiveMode the boolean flag for test mode
     */
    public TestDelayProvider(boolean isLiveMode) {
        this.isLiveMode = isLiveMode;
    }

    @Override
    public Duration getDelayDuration(Duration delay) {
        return isLiveMode ? delay : Duration.ofSeconds(1);
    }
}
