/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The base for storage manager tests.
 */
public abstract class GraphRbacManagementTestBase {
    protected static GraphRbacManager graphRbacManager;

    protected static void createClients() {
//        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
//                System.getenv("client-id"),
//                System.getenv("domain"),
//                System.getenv("secret"),
//                "https://graph.windows.net",
//                null);
        UserTokenCredentials credentials = new UserTokenCredentials(
                "1950a258-227b-4e31-a9cf-717495945fc2",
                System.getenv("domain"),
                System.getenv("username"),
                System.getenv("password"),
                "https://graph.windows.net",
                "https://graph.windows.net",
                AzureEnvironment.AZURE
        );

        graphRbacManager = GraphRbacManager
                .configure()
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .authenticate(credentials, "myorganization");
    }
}
