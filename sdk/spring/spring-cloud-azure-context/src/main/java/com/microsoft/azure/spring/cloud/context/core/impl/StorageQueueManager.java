/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import com.microsoft.azure.spring.cloud.context.core.util.Memoizer;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.function.Function;

public class StorageQueueManager extends AzureManager<CloudQueue, Tuple<CloudStorageAccount, String>> {

    public StorageQueueManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(Tuple<CloudStorageAccount, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return CloudQueue.class.getSimpleName();
    }

    @Override
    public CloudQueue internalGet(Tuple<CloudStorageAccount, String> key) {
        CloudQueueClient queueClient = key.getFirst().createCloudQueueClient();

        try {
            CloudQueue cloudQueue = queueClient.getQueueReference(key.getSecond());
            if (!cloudQueue.exists()) {
                return null;
            }
            return cloudQueue;
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException("Failed to build queue client", e);
        }
    }

    @Override
    public CloudQueue internalCreate(Tuple<CloudStorageAccount, String> key) {
        CloudQueueClient queueClient = key.getFirst().createCloudQueueClient();

        try {
            CloudQueue cloudQueue = queueClient.getQueueReference(key.getSecond());
            cloudQueue.create();
            return cloudQueue;
        } catch (URISyntaxException | StorageException e) {
            throw new RuntimeException("Failed to build queue client", e);
        }
    }
}
