// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.utils;

import org.springframework.boot.actuate.health.Status;

/**
 * Util class for Constants
 */
public class Constants {

    public static final String AZURE_KEYVAULT_PROPERTYSOURCE_NAME = "azurekv";

    public static final long DEFAULT_REFRESH_INTERVAL_MS = 1800000L;

    public static final Status NOT_CONFIGURED_STATUS = new Status("Not configured keyVaultPropertySource");
}
