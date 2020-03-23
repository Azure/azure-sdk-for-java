// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

public class ServiceBusSenderClient {
    private final ServiceBusSenderAsyncClient asyncClient;

    ServiceBusSenderClient(ServiceBusSenderAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }
}
