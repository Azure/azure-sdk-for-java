// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common Configuration for Cosmos DB Kafka sink connector.
 */
public class CosmosSinkConfig extends KafkaCosmosConfig {
    private static final String SINK_CONFIG_PREFIX = "kafka.connect.cosmos.sink.";

    // error tolerance
    public static final String TOLERANCE_ON_ERROR_CONFIG = SINK_CONFIG_PREFIX + "errors.tolerance";
    public static final String TOLERANCE_ON_ERROR_DOC =
        "Error tolerance level after exhausting all retries. 'None' for fail on error. 'All' for log and continue";
    public static final String TOLERANCE_ON_ERROR_DISPLAY = "Error tolerance level.";
    public static final String DEFAULT_TOLERANCE_ON_ERROR = ToleranceOnErrorLevel.NONE.getName();

    // sink bulk config
    public static final String BULK_ENABLED_CONF = SINK_CONFIG_PREFIX + "bulk.enabled";
    private static final String BULK_ENABLED_DOC =
        "Flag to indicate whether Cosmos DB bulk mode is enabled for Sink connector. By default it is true.";
    private static final String BULK_ENABLED_DISPLAY = "enable bulk mode.";
    private static final boolean DEFAULT_BULK_ENABLED = true;

    // TODO[Public Preview]: Add other write config, for example patch, bulkUpdate
    public static final String BULK_MAX_CONCURRENT_PARTITIONS_CONF = SINK_CONFIG_PREFIX + "bulk.maxConcurrentCosmosPartitions";
    private static final String BULK_MAX_CONCURRENT_PARTITIONS_DOC =
        "Cosmos DB Item Write Max Concurrent Cosmos Partitions."
            + " If not specified it will be determined based on the number of the container's physical partitions -"
            + " which would indicate every Spark partition is expected to have data from all Cosmos physical partitions."
            + " If specified it indicates from at most how many Cosmos Physical Partitions each Spark partition contains"
            + " data. So this config can be used to make bulk processing more efficient when input data in Spark has been"
            + " repartitioned to balance to how many Cosmos partitions each Spark partition needs to write. This is mainly"
            + " useful for very large containers (with hundreds of physical partitions).";
    private static final String BULK_MAX_CONCURRENT_PARTITIONS_DISPLAY = "Cosmos DB Item Write Max Concurrent Cosmos Partitions.";
    private static final int DEFAULT_BULK_MAX_CONCURRENT_PARTITIONS = -1;

    public static final String BULK_INITIAL_BATCH_SIZE_CONF = SINK_CONFIG_PREFIX + "bulk.initialBatchSize";
    private static final String BULK_INITIAL_BATCH_SIZE_DOC =
        "Cosmos DB initial bulk micro batch size - a micro batch will be flushed to the backend "
            + "when the number of documents enqueued exceeds this size - or the target payload size is met. The micro batch "
            + "size is getting automatically tuned based on the throttling rate. By default the "
            + "initial micro batch size is 1. Reduce this when you want to avoid that the first few requests consume "
            + "too many RUs.";
    private static final String BULK_INITIAL_BATCH_SIZE_DISPLAY = "Cosmos DB initial bulk micro batch size.";
    private static final int DEFAULT_BULK_INITIAL_BATCH_SIZE = 1; // start with small value to avoid initial RU spike

    // write strategy
    public static final String WRITE_STRATEGY_CONF = SINK_CONFIG_PREFIX + "write.strategy";
    private static final String WRITE_STRATEGY_DOC = "Cosmos DB Item write Strategy: `ItemOverwrite` (using upsert), `ItemAppend` (using create, "
        + "ignore pre-existing items i.e., Conflicts), `ItemDelete` (deletes based on id/pk of data frame), "
        + "`ItemDeleteIfNotModified` (deletes based on id/pk of data frame if etag hasn't changed since collecting "
        + "id/pk), `ItemOverwriteIfNotModified` (using create if etag is empty, update/replace with etag pre-condition "
        + "otherwise, if document was updated the pre-condition failure is ignored)";
    private static final String WRITE_STRATEGY_DISPLAY = "Cosmos DB Item write Strategy.";
    private static final String DEFAULT_WRITE_STRATEGY = ItemWriteStrategy.ITEM_OVERWRITE.getName();

    // max retry
    public static final String MAX_RETRY_COUNT_CONF = SINK_CONFIG_PREFIX + "maxRetryCount";
    private static final String MAX_RETRY_COUNT_DOC =
        "Cosmos DB max retry attempts on write failures for Sink connector. By default, the connector will retry on transient write errors for up to 10 times.";
    private static final String MAX_RETRY_COUNT_DISPLAY = "Cosmos DB max retry attempts on write failures for Sink connector.";
    private static final int DEFAULT_MAX_RETRY_COUNT = 10;

    // database name
    private static final String DATABASE_NAME_CONF = SINK_CONFIG_PREFIX + "database.name";
    private static final String DATABASE_NAME_CONF_DOC = "Cosmos DB database name.";
    private static final String DATABASE_NAME_CONF_DISPLAY = "Cosmos DB database name.";

    // container topic map
    public static final String CONTAINERS_TOPIC_MAP_CONF = SINK_CONFIG_PREFIX + "containers.topicMap";
    private static final String CONTAINERS_TOPIC_MAP_DOC =
        "A comma delimited list of Kafka topics mapped to Cosmos containers. For example: topic1#con1,topic2#con2.";
    private static final String CONTAINERS_TOPIC_MAP_DISPLAY = "Topic-Container map";

    // TODO[Public preview]: re-examine idStrategy implementation
    // id.strategy
    public static final String ID_STRATEGY_CONF = SINK_CONFIG_PREFIX + "id.strategy";
    public static final String ID_STRATEGY_DOC =
        "A strategy used to populate the document with an ``id``. Valid strategies are: "
            + "``TemplateStrategy``, ``FullKeyStrategy``, ``KafkaMetadataStrategy``, "
            + "``ProvidedInKeyStrategy``, ``ProvidedInValueStrategy``. Configuration "
            + "properties prefixed with``id.strategy`` are passed through to the strategy. For "
            + "example, when using ``id.strategy=TemplateStrategy`` , "
            + "the property ``id.strategy.template`` is passed through to the template strategy "
            + "and used to specify the template string to be used in constructing the ``id``.";
    public static final String ID_STRATEGY_DISPLAY = "ID Strategy";
    public static final String DEFAULT_ID_STRATEGY = IdStrategies.PROVIDED_IN_VALUE_STRATEGY.getName();

    // TODO[Public Preview] Verify whether compression need to happen in connector

    private final CosmosSinkWriteConfig writeConfig;
    private final CosmosSinkContainersConfig containersConfig;
    private final IdStrategies idStrategy;

    public CosmosSinkConfig(Map<String, ?> parsedConfig) {
        this(getConfigDef(), parsedConfig);
    }

    public CosmosSinkConfig(ConfigDef config, Map<String, ?> parsedConfig) {
        super(config, parsedConfig);
        this.writeConfig = this.parseWriteConfig();
        this.containersConfig = this.parseContainersConfig();
        this.idStrategy = this.parseIdStrategy();
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = KafkaCosmosConfig.getConfigDef();

        defineWriteConfig(configDef);
        defineContainersConfig(configDef);
        defineIdStrategyConfig(configDef);
        return configDef;
    }

    private static void defineWriteConfig(ConfigDef configDef) {
        final String writeConfigGroupName = "Write config";
        int writeConfigGroupOrder = 0;
        configDef
            .define(
                BULK_ENABLED_CONF,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_BULK_ENABLED,
                ConfigDef.Importance.MEDIUM,
                BULK_ENABLED_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                BULK_ENABLED_DISPLAY
            )
            .define(
                BULK_MAX_CONCURRENT_PARTITIONS_CONF,
                ConfigDef.Type.INT,
                DEFAULT_BULK_MAX_CONCURRENT_PARTITIONS,
                ConfigDef.Importance.LOW,
                BULK_MAX_CONCURRENT_PARTITIONS_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                BULK_MAX_CONCURRENT_PARTITIONS_DISPLAY
            )
            .define(
                BULK_INITIAL_BATCH_SIZE_CONF,
                ConfigDef.Type.INT,
                DEFAULT_BULK_INITIAL_BATCH_SIZE,
                ConfigDef.Importance.MEDIUM,
                BULK_INITIAL_BATCH_SIZE_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                BULK_INITIAL_BATCH_SIZE_DISPLAY
            )
            .define(
                WRITE_STRATEGY_CONF,
                ConfigDef.Type.STRING,
                DEFAULT_WRITE_STRATEGY,
                new ItemWriteStrategyValidator(),
                ConfigDef.Importance.HIGH,
                WRITE_STRATEGY_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.LONG,
                WRITE_STRATEGY_DISPLAY
            )
            .define(
                MAX_RETRY_COUNT_CONF,
                ConfigDef.Type.INT,
                DEFAULT_MAX_RETRY_COUNT,
                ConfigDef.Importance.MEDIUM,
                MAX_RETRY_COUNT_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                MAX_RETRY_COUNT_DISPLAY
            )
            .define(
                TOLERANCE_ON_ERROR_CONFIG,
                ConfigDef.Type.STRING,
                DEFAULT_TOLERANCE_ON_ERROR,
                ConfigDef.Importance.HIGH,
                TOLERANCE_ON_ERROR_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                TOLERANCE_ON_ERROR_DISPLAY
            );
    }

    private static void defineContainersConfig(ConfigDef configDef) {
        final String containersGroupName = "Containers";
        int containersGroupOrder = 0;

        configDef
            .define(
                DATABASE_NAME_CONF,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                NON_EMPTY_STRING,
                ConfigDef.Importance.HIGH,
                DATABASE_NAME_CONF_DOC,
                containersGroupName,
                containersGroupOrder++,
                ConfigDef.Width.LONG,
                DATABASE_NAME_CONF_DISPLAY
            )
            .define(
                CONTAINERS_TOPIC_MAP_CONF,
                ConfigDef.Type.STRING,
                ConfigDef.NO_DEFAULT_VALUE,
                new ContainersTopicMapValidator(),
                ConfigDef.Importance.MEDIUM,
                CONTAINERS_TOPIC_MAP_DOC,
                containersGroupName,
                containersGroupOrder++,
                ConfigDef.Width.LONG,
                CONTAINERS_TOPIC_MAP_DISPLAY
            );
    }

    private static void defineIdStrategyConfig(ConfigDef configDef) {
        final String idStrategyConfigGroupName = "ID Strategy";
        int idStrategyConfigGroupOrder = 0;
        configDef
            .define(
                ID_STRATEGY_CONF,
                ConfigDef.Type.STRING,
                DEFAULT_ID_STRATEGY,
                ConfigDef.Importance.HIGH,
                ID_STRATEGY_DOC,
                idStrategyConfigGroupName,
                idStrategyConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                ID_STRATEGY_DISPLAY);
    }

    private CosmosSinkWriteConfig parseWriteConfig() {
        boolean bulkEnabled = this.getBoolean(BULK_ENABLED_CONF);
        int bulkMaxConcurrentCosmosPartitions = this.getInt(BULK_MAX_CONCURRENT_PARTITIONS_CONF);
        int bulkInitialBatchSize = this.getInt(BULK_INITIAL_BATCH_SIZE_CONF);
        ItemWriteStrategy writeStrategy = this.parseItemWriteStrategy();
        int maxRetryCount = this.getInt(MAX_RETRY_COUNT_CONF);
        ToleranceOnErrorLevel toleranceOnErrorLevel = this.parseToleranceOnErrorLevel();

        return new CosmosSinkWriteConfig(
            bulkEnabled,
            bulkMaxConcurrentCosmosPartitions,
            bulkInitialBatchSize,
            writeStrategy,
            maxRetryCount,
            toleranceOnErrorLevel);
    }

    private CosmosSinkContainersConfig parseContainersConfig() {
        String databaseName = this.getString(DATABASE_NAME_CONF);
        Map<String, String> topicToContainerMap = this.getTopicToContainerMap();

        return new CosmosSinkContainersConfig(databaseName, topicToContainerMap);
    }

    private Map<String, String> getTopicToContainerMap() {
        List<String> containersTopicMapList = convertToList(this.getString(CONTAINERS_TOPIC_MAP_CONF));
        return containersTopicMapList
            .stream()
            .map(containerTopicMapString -> containerTopicMapString.split("#"))
            .collect(
                Collectors.toMap(
                    containerTopicMapArray -> containerTopicMapArray[0],
                    containerTopicMapArray -> containerTopicMapArray[1]));
    }

    private ItemWriteStrategy parseItemWriteStrategy() {
        return ItemWriteStrategy.fromName(this.getString(WRITE_STRATEGY_CONF));
    }

    private ToleranceOnErrorLevel parseToleranceOnErrorLevel() {
        return ToleranceOnErrorLevel.fromName(this.getString(TOLERANCE_ON_ERROR_CONFIG));
    }

    private IdStrategies parseIdStrategy() {
        return IdStrategies.fromName(this.getString(ID_STRATEGY_CONF));
    }

    public CosmosSinkWriteConfig getWriteConfig() {
        return writeConfig;
    }

    public CosmosSinkContainersConfig getContainersConfig() {
        return containersConfig;
    }

    public IdStrategies getIdStrategy() {
        return idStrategy;
    }

    public static class ItemWriteStrategyValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String itemWriteStrategyString = (String) o;
            if (StringUtils.isEmpty(itemWriteStrategyString)) {
                throw new ConfigException(name, o, "WriteStrategy can not be empty or null");
            }

            ItemWriteStrategy itemWriteStrategy = ItemWriteStrategy.fromName(itemWriteStrategyString);
            if (itemWriteStrategy == null) {
                throw new ConfigException(name, o, "Invalid ItemWriteStrategy. Allowed values " + ItemWriteStrategy.values());
            }
        }

        @Override
        public String toString() {
            return "ItemWriteStrategy. Only allow " + ItemWriteStrategy.values();
        }
    }
}
