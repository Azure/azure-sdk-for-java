/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerregistry;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;

import java.util.Map;

/**
 * Response containing the login credentials for a container registry.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_4_0)
public interface RegistryCredentials {
    /**
     * The admin user access key names and values which can be used to login into the container registry.
     *
     * @return the admin user access keys
     */
    Map<AccessKeyType, String> accessKeys();

    /**
     * Get the username value which can be used to login into the container registry.
     *
     * @return the username value
     */
    String username();
}
