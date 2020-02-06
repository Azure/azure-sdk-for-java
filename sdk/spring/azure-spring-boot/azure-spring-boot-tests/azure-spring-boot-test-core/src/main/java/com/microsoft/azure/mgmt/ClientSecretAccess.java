/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.mgmt;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.credentials.AzureTokenCredentials;

import java.util.Map;

public class ClientSecretAccess implements Access {
    
    private static final String TENANT = "AZURE_TENANT";
    private static final String SUBSCRIPTION = "AZURE_SUBSCRIPTION";
    private static final String CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    
    private String tenant;
    private String subscription;
    
    private String clientId;
    private String clientSecret;
    
    public static ClientSecretAccess load() {
        return load(System.getenv());
    }
    
    public static ClientSecretAccess load(Map<String, String> props) {
        final String tenant = props.get(TENANT);
        final String subscription = props.get(SUBSCRIPTION);
        final String clientId = props.get(CLIENT_ID);
        final String clientSecret = props.get(CLIENT_SECRET);
        
        assertNotEmpty(tenant, TENANT);
        assertNotEmpty(subscription, SUBSCRIPTION);
        assertNotEmpty(clientId, CLIENT_ID);
        assertNotEmpty(clientSecret, CLIENT_SECRET);
        
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

    public String tenant() {
        return tenant;
    }
    
    @Override
    public String subscription() {
        return subscription;
    }
    
    public String clientId() {
        return clientId;
    }
    
    public String clientSecret() {
        return clientSecret;
    }
    
    @Override
    public String servicePrincipal() {
        return clientId;
    }
}
