/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.core;

import com.azure.management.resources.fluentcore.utils.DelayProvider;

import java.time.Duration;

public class TestDelayProvider extends DelayProvider {
    private boolean isLiveMode;

    public TestDelayProvider(boolean isLiveMode) {
        this.isLiveMode = isLiveMode;
    }

    @Override
    public void sleep(int milliseconds) {
        if (isLiveMode) {
            super.sleep(milliseconds);
        }
    }

    @Override
    public Duration getLroRetryTimeout() {
        return isLiveMode ? super.getLroRetryTimeout() : Duration.ofMillis(1);
    }

    @Override
    public Duration getDelayDuration(Duration delay) {
        return isLiveMode ? delay : Duration.ZERO;
    }
}
