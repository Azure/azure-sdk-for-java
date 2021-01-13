// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.management;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;

import java.util.Map;

public class ClientSecretAccess implements Access {

    private static final String SPRING_TENANT_ID = "SPRING_TENANT_ID";
    private static final String SPRING_SUBSCRIPTION_ID = "SPRING_SUBSCRIPTION_ID";
    private static final String SPRING_CLIENT_ID = "SPRING_CLIENT_ID";
    private static final String SPRING_CLIENT_SECRET = "SPRING_CLIENT_SECRET";

    private final String tenant;
    private final String subscription;
    private final String clientId;
    private final String clientSecret;

    public static ClientSecretAccess load() {
        return load(System.getenv());
    }

    private static ClientSecretAccess load(Map<String, String> props) {
        final String tenant = props.get(SPRING_TENANT_ID);
        final String subscription = props.get(SPRING_SUBSCRIPTION_ID);
        final String clientId = props.get(SPRING_CLIENT_ID);
        final String clientSecret = props.get(SPRING_CLIENT_SECRET);

        assertNotEmpty(tenant, SPRING_TENANT_ID);
        assertNotEmpty(subscription, SPRING_SUBSCRIPTION_ID);
        assertNotEmpty(clientId, SPRING_CLIENT_ID);
        assertNotEmpty(clientSecret, SPRING_CLIENT_SECRET);

        return new ClientSecretAccess(tenant, subscription, clientId, clientSecret);
    }

    private static void assertNotEmpty(String text, String key) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s is not set!", key));
        }
    }

    public ClientSecretAccess(String tenantId, String subscriptionId, String clientId, String clientSecret) {
        this.tenant = tenantId;
        this.subscription = subscriptionId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public AzureTokenCredentials credentials() {
        return new ApplicationTokenCredentials(
            clientId,
            tenant,
            clientSecret,
            AzureEnvironment.AZURE);
    }

    @Override
    public String subscription() {
        return subscription;
    }

    @Override
    public String tenantId() {
        return tenant;
    }

    public String clientId() {
        return clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }

}
