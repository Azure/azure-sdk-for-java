// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface AzureEventHubsConnectionDetails extends ConnectionDetails {

    String getConnectionString();

}
