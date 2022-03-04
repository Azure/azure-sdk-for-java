// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.actuator.storage;

import org.springframework.boot.actuate.health.Status;

/**
 * Azure Storage actuator related constants.
 */
final class StorageHealthConstants {

    static final String URL_FIELD = "URL";

    static final Status NOT_CONFIGURED_STATUS = new Status("Not configured");
}
