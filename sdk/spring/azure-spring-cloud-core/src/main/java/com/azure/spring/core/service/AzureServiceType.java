// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.service;

/**
 * Describes an Azure service type.
 */
public final class AzureServiceType {

    public static final ServiceBus SERVICE_BUS = new ServiceBus();
    public static final EventHub EVENT_HUB = new EventHub();
    public static final StorageBlob STORAGE_BLOB = new StorageBlob();
    public static final StorageQueue STORAGE_QUEUE = new StorageQueue();
    public static final AppConfiguration APP_CONFIGURATION = new AppConfiguration();

    /**
     * The Service Bus service.
     */
    public static class ServiceBus {

    }

    /**
     * The Event Hub service.
     */
    public static class EventHub {

    }

    /**
     * The Storage Blob service.
     */
    public static class StorageBlob {

    }

    /**
     * The Storage Queue service.
     */
    public static class StorageQueue {

    }

    /**
     * The App Configuration service.
     */
    public static class AppConfiguration {

    }
}
