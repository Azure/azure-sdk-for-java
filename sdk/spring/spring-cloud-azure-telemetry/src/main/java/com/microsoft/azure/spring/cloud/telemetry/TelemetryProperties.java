/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.telemetry;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("telemetry")
public class TelemetryProperties {

    private String instrumentationKey;

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }
}
