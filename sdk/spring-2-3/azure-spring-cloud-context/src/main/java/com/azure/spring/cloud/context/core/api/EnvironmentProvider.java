// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.api;

import com.azure.core.management.AzureEnvironment;

/**
 * Interface to provide the {@link AzureEnvironment}
 *
 * @author Warren Zhu
 */
public interface EnvironmentProvider {

    AzureEnvironment getEnvironment();

}
