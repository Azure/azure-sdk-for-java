// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.service;

import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;

/**
 * Describes an Azure service type. This is only used for cases when build an instance of type like
 *  {@link ServiceConnectionStringProvider}.
 */
public final class AzureServiceType {

    /**
     * The Service Bus service.
     */
    public static final ServiceBus SERVICE_BUS = new ServiceBus();

    /**
     * The Event Hub service.
     */
    public static final EventHubs EVENT_HUBS = new EventHubs();

    /**
     * The Storage Blob service.
     */
    public static final StorageBlob STORAGE_BLOB = new StorageBlob();

    /**
     * The Storage File Share service.
     */
    public static final StorageFileShare STORAGE_FILE_SHARE = new StorageFileShare();

    /**
     * The Storage Queue service.
     */
    public static final StorageQueue STORAGE_QUEUE = new StorageQueue();

    /**
     * The App Configuration service.
     */
    public static final AppConfiguration APP_CONFIGURATION = new AppConfiguration();

    private AzureServiceType() {

    }

    /**
     * The Service Bus service.
     */
    public static final class ServiceBus {

        private ServiceBus() {

        }
    }

    /**
     * The Event Hub service.
     */
    public static final class EventHubs {

        private EventHubs() {

        }

    }

    /**
     * The Storage Blob service.
     */
    public static final class StorageBlob {

        private StorageBlob() {

        }
    }

    /**
     * The Storage File Share service.
     */
    public static final class StorageFileShare {

        private StorageFileShare() {

        }

    }

    /**
     * The Storage Queue service.
     */
    public static final class StorageQueue {

        private StorageQueue() {

        }

    }

    /**
     * The App Configuration service.
     */
    public static final class AppConfiguration {

        private AppConfiguration() {

        }

    }
}
