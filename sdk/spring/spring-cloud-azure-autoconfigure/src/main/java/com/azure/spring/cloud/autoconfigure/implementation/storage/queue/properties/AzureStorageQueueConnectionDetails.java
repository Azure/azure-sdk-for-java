// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties;

import org.springframework.boot.autoconfigure.service.connection.ConnectionDetails;

public interface AzureStorageQueueConnectionDetails extends ConnectionDetails {

    String getConnectionString();

    String getEndpoint();

}
