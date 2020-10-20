// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

/**
 * POJO describes Service Bus connection info
 */
public class ServiceBusKey {
    private final String host;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    ServiceBusKey(String host, String sharedAccessKeyName, String sharedAccessKey) {
        this.host = host;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKey;
    }

    public String getHost() {
        return host;
    }

    public String getSharedAccessKeyName() {
        return sharedAccessKeyName;
    }

    public String getSharedAccessKey() {
        return sharedAccessKey;
    }

}
