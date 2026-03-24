// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface AzureServiceBusConnectionDetails extends ConnectionDetails {

    String getConnectionString();

}
