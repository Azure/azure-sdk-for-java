// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.service;


public final class AzureServiceType {

    public static final ServiceBus SERVICE_BUS = new ServiceBus();
    public static final EventHub EVENT_HUB = new EventHub();
    public static final StorageBlob STORAGE_BLOB = new StorageBlob();
    public static final StorageQueue STORAGE_QUEUE = new StorageQueue();
    public static final AppConfiguration APP_CONFIGURATION = new AppConfiguration();

    public static class ServiceBus {

    }

    public static class EventHub {

    }

    public static class StorageBlob {

    }

    public static class StorageQueue {

    }

    public static class AppConfiguration {

    }
}
