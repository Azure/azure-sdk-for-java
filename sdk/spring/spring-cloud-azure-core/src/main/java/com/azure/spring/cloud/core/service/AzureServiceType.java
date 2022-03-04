// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.service;

import com.azure.spring.cloud.core.connectionstring.ConnectionStringProvider;

/**
 * Describes an Azure service type. This is only used for cases when build an instance of type like
 *  {@link ConnectionStringProvider}.
 */
public final class AzureServiceType {

    public static final ServiceBus SERVICE_BUS = new ServiceBus();
    public static final EventHubs EVENT_HUBS = new EventHubs();
    public static final StorageBlob STORAGE_BLOB = new StorageBlob();
    public static final StorageFileShare STORAGE_FILE_SHARE = new StorageFileShare();
    public static final StorageQueue STORAGE_QUEUE = new StorageQueue();
    public static final AppConfiguration APP_CONFIGURATION = new AppConfiguration();

    private AzureServiceType() {

    }

    /**
     * The Service Bus service.
     */
    public static class ServiceBus {

    }

    /**
     * The Event Hub service.
     */
    public static class EventHubs {

    }

    /**
     * The Storage Blob service.
     */
    public static class StorageBlob {

    }

    /**
     * The Storage File Share service.
     */
    public static class StorageFileShare {

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
