// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

class LiveMetrics {

    static boolean isEnabled(ConfigProperties configProperties) {
        return configProperties.getBoolean("applicationinsights.live.metrics.enabled", true);
    }
}
