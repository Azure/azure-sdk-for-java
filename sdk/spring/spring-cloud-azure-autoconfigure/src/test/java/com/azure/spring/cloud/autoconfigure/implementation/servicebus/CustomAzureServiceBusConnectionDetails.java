// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails;

import static com.azure.spring.cloud.autoconfigure.implementation.util.TestServiceBusUtils.CONNECTION_STRING_FORMAT;

public class CustomAzureServiceBusConnectionDetails implements AzureServiceBusConnectionDetails {

    static final String CONNECTION_STRING = String.format(CONNECTION_STRING_FORMAT, "connection-detail-namespace");

    @Override
    public String getConnectionString() {
        return CONNECTION_STRING;
    }
}
