// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceOffsetStorageReader;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataMonitorThread;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.kafka.connect.storage.OffsetStorageReader;

public class KafkaCosmosReflectionUtils {
    private static <T> void set(Object object, T newValue, String fieldName) {
        try {
            FieldUtils.writeField(object, fieldName, newValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Object object, String fieldName) {
        try {
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCosmosClient(CosmosDBSourceConnector sourceConnector, CosmosAsyncClient cosmosAsyncClient) {
        set(sourceConnector, cosmosAsyncClient,"cosmosClient");
    }

    public static void setCosmosSourceConfig(CosmosDBSourceConnector sourceConnector, CosmosSourceConfig sourceConfig) {
        set(sourceConnector, sourceConfig,"config");
    }

    public static void setOffsetStorageReader(
        CosmosDBSourceConnector sourceConnector,
        CosmosSourceOffsetStorageReader storageReader) {
        set(sourceConnector, storageReader,"offsetStorageReader");
    }

    public static void setMetadataMonitorThread(
        CosmosDBSourceConnector sourceConnector,
        MetadataMonitorThread metadataMonitorThread) {
        set(sourceConnector, metadataMonitorThread,"monitorThread");
    }

    public static CosmosAsyncClient getCosmosClient(CosmosDBSourceConnector sourceConnector) {
        return get(sourceConnector,"cosmosClient");
    }

    public static CosmosSourceOffsetStorageReader getSourceOffsetStorageReader(CosmosDBSourceConnector sourceConnector) {
        return get(sourceConnector,"offsetStorageReader");
    }

    public static OffsetStorageReader getOffsetStorageReader(CosmosSourceOffsetStorageReader sourceOffsetStorageReader) {
        return get(sourceOffsetStorageReader,"offsetStorageReader");
    }
}
