/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.keyvault;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.keyvault.implementation.KeyVaultManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * The base for storage manager tests.
 */
public abstract class KeyVaultManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static GraphRbacManager graphRbacManager;
    protected static KeyVaultManager keyVaultManager;
    protected static ApplicationTokenCredentials credentials;

    protected static void createClients() {
        credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);

        ApplicationTokenCredentials graphCredentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                "https://graph.windows.net/",
                null);

        RestClient restClient = AzureEnvironment.AZURE.newRestClientBuilder()
                .withCredentials(credentials)
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        graphRbacManager = GraphRbacManager
                .authenticate(graphCredentials, System.getenv("domain"));

        keyVaultManager = KeyVaultManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }
}
