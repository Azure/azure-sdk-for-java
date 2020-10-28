// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.context.core.api;

import java.util.Arrays;

import com.microsoft.azure.AzureEnvironment;

/**
 * Interface to provide the {@link AzureEnvironment}
 *
 * @author Warren Zhu
 */
public interface EnvironmentProvider {

    AzureEnvironment getEnvironment();

    /**
     * @return The Azure environment as defined by the
     *         <code>com.azure.core.management</code> SDK.
     */
    default com.azure.core.management.AzureEnvironment getCoreEnvironment() {
        return com.azure.core.management.AzureEnvironment.knownEnvironments().stream()
                .filter(coreEnvironment -> getEnvironment().managementEndpoint()
                        .equals(coreEnvironment.getManagementEndpoint()))
                .findAny().get();
    }

}
