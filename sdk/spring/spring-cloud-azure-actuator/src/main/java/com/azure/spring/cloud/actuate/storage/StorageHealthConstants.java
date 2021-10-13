// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuate.storage;

import org.springframework.boot.actuate.health.Status;

import java.time.Duration;

/**
 * Azure Storage actuator related constants.
 */
final class StorageHealthConstants {

    static final String URL_FIELD = "URL";

    static final Duration POLL_TIMEOUT = Duration.ofSeconds(2);

    static final Status NOT_CONFIGURED_STATUS = new Status("Not configured");

}
