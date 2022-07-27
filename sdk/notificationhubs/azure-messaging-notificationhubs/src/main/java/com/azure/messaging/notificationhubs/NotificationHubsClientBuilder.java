// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs;

import com.azure.core.client.traits.AzureSasCredentialTrait;
import com.azure.core.client.traits.ConfigurationTrait;
import com.azure.core.client.traits.ConnectionStringTrait;
import com.azure.core.client.traits.TokenCredentialTrait;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.Configuration;

public class NotificationHubsClientBuilder implements
    TokenCredentialTrait<NotificationHubsClientBuilder>,
    ConnectionStringTrait<NotificationHubsClientBuilder>,
    AzureSasCredentialTrait<NotificationHubsClientBuilder>,
    ConfigurationTrait<NotificationHubsClientBuilder> {

    /**
     * Sets the Azure SAS credential for the Notification Hub client.
     * @param azureSasCredential The Azure SAS credential to set for the client.
     * @return The current NotificationHubsClientBuilder instance.
     */
    @Override
    public NotificationHubsClientBuilder credential(AzureSasCredential azureSasCredential) {
        return null;
    }

    /**
     * Sets the configuration for the Notification Hubs client.
     * @param configuration The configuration to set for the client.
     * @return The current NotificationHubsClientBuilder instance.
     */
    @Override
    public NotificationHubsClientBuilder configuration(Configuration configuration) {
        return null;
    }

    /**
     * Sets the connection string for the Notification Hubs client.
     * @param s The connection string to set for the client.
     * @return The current NotificationHubsClientBuilder instance.
     */
    @Override
    public NotificationHubsClientBuilder connectionString(String s) {
        return null;
    }

    /**
     * Sets the token credential for the Notification Hubs client.
     * @param tokenCredential The token credential to set for the client.
     * @return The current NotificationHubsClientBuilder instance.
     */
    @Override
    public NotificationHubsClientBuilder credential(TokenCredential tokenCredential) {
        return null;
    }
}
