/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.api;

import com.microsoft.azure.AzureEnvironment;

/**
 * Interface to provide the {@link AzureEnvironment}
 *
 * @author Warren Zhu
 */
public interface EnvironmentProvider {
    AzureEnvironment getEnvironment();
}
