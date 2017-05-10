/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.credentials.AzureCliCredentials;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.rest.LogLevel;

/**
 * The base for storage manager tests.
 */
public abstract class GraphRbacManagementTestBase {
    protected static GraphRbacManager graphRbacManager;

    protected static void createClients() throws Exception {
        AzureCliCredentials credentials = AzureCliCredentials.create();

        graphRbacManager = GraphRbacManager
                .configure()
                .withLogLevel(LogLevel.BODY)
                .authenticate(credentials);
    }
}
