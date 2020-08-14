// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.storage.actuator;

import java.time.Duration;

import org.springframework.boot.actuate.health.Status;

public final class AzureStorageActuatorConstants {
    static final String URL_FIELD = "URL";
    static final Duration POLL_TIMEOUT = Duration.ofSeconds(2);
    static final Status NOT_CONFIGURED_STATUS = new Status("Not configured");
}
