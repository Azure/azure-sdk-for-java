// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.kafka.connect.implementation.sink.CosmosSinkTask;
import com.azure.cosmos.kafka.connect.implementation.source.CosmosSourceConfig;
import com.azure.cosmos.kafka.connect.implementation.source.IMetadataReader;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataKafkaStorageManager;
import com.azure.cosmos.kafka.connect.implementation.source.MetadataMonitorThread;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.kafka.connect.sink.SinkTaskContext;
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

    public static void setCosmosClient(CosmosSourceConnector sourceConnector, CosmosAsyncClient cosmosAsyncClient) {
        set(sourceConnector, cosmosAsyncClient,"cosmosClient");
    }

    public static void setCosmosSourceConfig(CosmosSourceConnector sourceConnector, CosmosSourceConfig sourceConfig) {
        set(sourceConnector, sourceConfig,"config");
    }

    public static void setMetadataReader(
        CosmosSourceConnector sourceConnector,
        IMetadataReader metadataReader) {
        set(sourceConnector, metadataReader,"metadataReader");
    }

    public static void setKafkaOffsetStorageReader(
        CosmosSourceConnector sourceConnector,
        MetadataKafkaStorageManager kafkaOffsetStorageReader) {
        set(sourceConnector, kafkaOffsetStorageReader,"kafkaOffsetStorageReader");
    }

    public static void setMetadataMonitorThread(
        CosmosSourceConnector sourceConnector,
        MetadataMonitorThread metadataMonitorThread) {
        set(sourceConnector, metadataMonitorThread,"monitorThread");
    }

    public static CosmosAsyncClient getCosmosClient(CosmosSourceConnector sourceConnector) {
        return get(sourceConnector,"cosmosClient");
    }

    public static MetadataKafkaStorageManager getKafkaOffsetStorageReader(CosmosSourceConnector sourceConnector) {
        return get(sourceConnector,"kafkaOffsetStorageReader");
    }

    public static OffsetStorageReader getOffsetStorageReader(MetadataKafkaStorageManager sourceOffsetStorageReader) {
        return get(sourceOffsetStorageReader,"offsetStorageReader");
    }

    public static void setSinkTaskContext(CosmosSinkTask sinkTask, SinkTaskContext sinkTaskContext) {
        set(sinkTask, sinkTaskContext, "context");
    }

    public static CosmosAsyncClient getSinkTaskCosmosClient(CosmosSinkTask sinkTask) {
        return get(sinkTask,"cosmosClient");
    }

    public static void setConnectorName(CosmosSourceConnector sourceConnector, String connectorName) {
        set(sourceConnector, connectorName,"connectorName");
    }

    public static void setConnectorName(CosmosSinkConnector sinkConnector, String connectorName) {
        set(sinkConnector, connectorName,"connectorName");
    }
}
