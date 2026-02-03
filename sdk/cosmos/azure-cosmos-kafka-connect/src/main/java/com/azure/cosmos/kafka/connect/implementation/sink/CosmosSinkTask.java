// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCache;
import com.azure.cosmos.kafka.connect.implementation.CosmosClientCacheItem;
import com.azure.cosmos.kafka.connect.implementation.CosmosThroughputControlHelper;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConstants;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class CosmosSinkTask extends SinkTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosSinkTask.class);
    private CosmosSinkTaskConfig sinkTaskConfig;
    private CosmosClientCacheItem cosmosClientItem;
    private CosmosClientCacheItem throughputControlClientItem;
    private SinkRecordTransformer sinkRecordTransformer;
    private IWriter cosmosWriter;

    private long lastLogTimeMs = System.currentTimeMillis();
    private final Map<String, Long> totalWrittenRecordsPerContainer = new ConcurrentHashMap<>();

    @Override
    public String version() {
        return KafkaCosmosConstants.CURRENT_VERSION;
    }

    @Override
    public void start(Map<String, String> props) {
        LOGGER.info("Starting the kafka cosmos sink task");

        try {
            this.sinkTaskConfig = new CosmosSinkTaskConfig(props);
            this.cosmosClientItem =
                CosmosClientCache.getCosmosClient(
                    this.sinkTaskConfig.getAccountConfig(),
                    this.sinkTaskConfig.getTaskId(),
                    this.sinkTaskConfig.getClientMetadataCachesSnapshot());
            LOGGER.info("The taskId is " + this.sinkTaskConfig.getTaskId());
            this.throughputControlClientItem = this.getThroughputControlCosmosClient();
            this.sinkRecordTransformer = new SinkRecordTransformer(this.sinkTaskConfig);

            if (this.sinkTaskConfig.getWriteConfig().isBulkEnabled()) {
                this.cosmosWriter =
                    new CosmosBulkWriter(
                        this.sinkTaskConfig.getWriteConfig(),
                        this.sinkTaskConfig.getThroughputControlConfig(),
                        this.context.errantRecordReporter());
            } else {
                this.cosmosWriter =
                    new CosmosPointWriter(
                        this.sinkTaskConfig.getWriteConfig(),
                        this.sinkTaskConfig.getThroughputControlConfig(),
                        context.errantRecordReporter());
            }
        } catch (Throwable e) {
            LOGGER.warn("Error occurred while starting the kafka sink task", e);
            this.cleanup();

            throw e;
        }
    }

    private CosmosClientCacheItem getThroughputControlCosmosClient() {
        if (this.sinkTaskConfig.getThroughputControlConfig().isThroughputControlEnabled()
            && this.sinkTaskConfig.getThroughputControlConfig().getThroughputControlAccountConfig() != null) {
            // throughput control is using a different database account config
            return CosmosClientCache.getCosmosClient(
                this.sinkTaskConfig.getThroughputControlConfig().getThroughputControlAccountConfig(),
                this.sinkTaskConfig.getTaskId(),
                this.sinkTaskConfig.getThroughputControlClientMetadataCachesSnapshot());
        } else {
            return this.cosmosClientItem;
        }
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        if (records == null || records.isEmpty()) {
            LOGGER.debug("No records to be written");
            return;
        }

        LOGGER.debug("Sending {} records to be written", records.size());

        // group by container
        Map<String, List<SinkRecord>> recordsByContainer =
            records.stream().collect(
                    Collectors.groupingBy(
                            record -> this.sinkTaskConfig
                                        .getContainersConfig()
                                        .getTopicToContainerMap()
                                        .getOrDefault(record.topic(), StringUtils.EMPTY)));

        if (recordsByContainer.containsKey(StringUtils.EMPTY)) {
            throw new IllegalStateException("There is no container defined for topics " + recordsByContainer.get(StringUtils.EMPTY));
        }

        for (Map.Entry<String, List<SinkRecord>> entry : recordsByContainer.entrySet()) {
            String containerName = entry.getKey();
            CosmosAsyncContainer container =
                this.cosmosClientItem
                    .getClient()
                    .getDatabase(this.sinkTaskConfig.getContainersConfig().getDatabaseName())
                    .getContainer(containerName);

            CosmosThroughputControlHelper
                .tryEnableThroughputControl(
                    container,
                    this.throughputControlClientItem.getClient(),
                    this.sinkTaskConfig.getThroughputControlConfig());

            // transform sink records, for example populating id
            List<SinkRecord> transformedRecords = sinkRecordTransformer.transform(containerName, entry.getValue());
            this.cosmosWriter.write(container, transformedRecords);

            totalWrittenRecordsPerContainer.merge(containerName, (long) entry.getValue().size(), Long::sum);
        }

        logWrittenRecordCount();
    }

    private void cleanup() {
        LOGGER.info("Cleaning up CosmosSinkTask");

        if (this.throughputControlClientItem != null && this.throughputControlClientItem != this.cosmosClientItem) {
            LOGGER.debug("Releasing throughput control cosmos client");
            CosmosClientCache.releaseCosmosClient(this.throughputControlClientItem.getClientConfig());
            this.throughputControlClientItem = null;
        }

        if (this.cosmosClientItem != null) {
            LOGGER.debug("Releasing cosmos client");
            CosmosClientCache.releaseCosmosClient(this.cosmosClientItem.getClientConfig());
            this.cosmosClientItem = null;
        }
    }

    private void logWrittenRecordCount() {
        long currentTime = System.currentTimeMillis();
        long durationInMs = currentTime - lastLogTimeMs;
        if (durationInMs >= CosmosSinkTaskConfig.LOG_INTERVAL_MS) {
            // Log accumulated counts for writes per container
            for (Map.Entry<String, Long> entry : totalWrittenRecordsPerContainer.entrySet()) {
                LOGGER.info(
                    "Total {} records written to container {}, durationInMs {}, taskId {}",
                    entry.getValue(),
                    entry.getKey(),
                    durationInMs,
                    this.sinkTaskConfig.getTaskId()
                );
            }

            // Reset counts and update last log time
            totalWrittenRecordsPerContainer.clear();
            lastLogTimeMs = currentTime;
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping Kafka CosmosDB sink task");
        this.cleanup();
    }
}
