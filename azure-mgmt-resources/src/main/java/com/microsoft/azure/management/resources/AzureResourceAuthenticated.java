/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;


import com.microsoft.azure.CloudException;

import java.io.IOException;

public interface AzureResourceAuthenticated {
    Subscriptions subscriptions() throws IOException, CloudException;
    ResourceGroups resourceGroups() throws IOException, CloudException;
}
