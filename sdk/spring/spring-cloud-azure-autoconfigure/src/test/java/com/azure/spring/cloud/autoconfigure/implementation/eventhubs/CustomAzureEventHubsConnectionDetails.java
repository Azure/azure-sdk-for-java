// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails;

public class CustomAzureEventHubsConnectionDetails implements AzureEventHubsConnectionDetails {

    static final String CONNECTION_STRING = "Endpoint=sb://connection-detail-namespace.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=test-key;EntityPath=test-eventhub";

    @Override
    public String getConnectionString() {
        return CONNECTION_STRING;
    }
}
