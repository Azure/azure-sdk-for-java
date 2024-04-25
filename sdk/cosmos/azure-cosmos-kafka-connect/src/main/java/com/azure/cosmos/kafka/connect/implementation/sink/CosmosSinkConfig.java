// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.patch.CosmosPatchConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.patch.CosmosPatchJsonPropertyConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.patch.KafkaCosmosPatchOperationType;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Common Configuration for Cosmos DB Kafka sink connector.
 */
public class CosmosSinkConfig extends KafkaCosmosConfig {

    // error tolerance
    public static final String TOLERANCE_ON_ERROR = "azure.cosmos.sink.errors.tolerance.level";
    public static final String TOLERANCE_ON_ERROR_DOC =
        "Error tolerance level after exhausting all retries. 'None' for fail on error. 'All' for log and continue";
    public static final String TOLERANCE_ON_ERROR_DISPLAY = "Error tolerance level.";
    public static final String DEFAULT_TOLERANCE_ON_ERROR = ToleranceOnErrorLevel.NONE.getName();

    // sink bulk config
    public static final String BULK_ENABLED = "azure.cosmos.sink.bulk.enabled";
    private static final String BULK_ENABLED_DOC =
        "Flag to indicate whether Cosmos DB bulk mode is enabled for Sink connector. By default it is true.";
    private static final String BULK_ENABLED_DISPLAY = "enable bulk mode.";
    private static final boolean DEFAULT_BULK_ENABLED = true;
    public static final String BULK_MAX_CONCURRENT_PARTITIONS = "azure.cosmos.sink.bulk.maxConcurrentCosmosPartitions";
    private static final String BULK_MAX_CONCURRENT_PARTITIONS_DOC =
        "Cosmos DB Item Write Max Concurrent Cosmos Partitions."
            + " If not specified it will be determined based on the number of the container's physical partitions -"
            + " which would indicate every batch is expected to have data from all Cosmos physical partitions."
            + " If specified it indicates from at most how many Cosmos Physical Partitions each batch contains data."
            + " So this config can be used to make bulk processing more efficient when input data in each batch has been"
            + " repartitioned to balance to how many Cosmos partitions each batch needs to write. This is mainly"
            + " useful for very large containers (with hundreds of physical partitions).";
    private static final String BULK_MAX_CONCURRENT_PARTITIONS_DISPLAY = "Cosmos DB Item Write Max Concurrent Cosmos Partitions.";
    private static final int DEFAULT_BULK_MAX_CONCURRENT_PARTITIONS = -1;

    public static final String BULK_INITIAL_BATCH_SIZE = "azure.cosmos.sink.bulk.initialBatchSize";
    private static final String BULK_INITIAL_BATCH_SIZE_DOC =
        "Cosmos DB initial bulk micro batch size - a micro batch will be flushed to the backend "
            + "when the number of documents enqueued exceeds this size - or the target payload size is met. The micro batch "
            + "size is getting automatically tuned based on the throttling rate. By default the "
            + "initial micro batch size is 1. Reduce this when you want to avoid that the first few requests consume "
            + "too many RUs.";
    private static final String BULK_INITIAL_BATCH_SIZE_DISPLAY = "Cosmos DB initial bulk micro batch size.";
    private static final int DEFAULT_BULK_INITIAL_BATCH_SIZE = 1; // start with small value to avoid initial RU spike

    // write strategy
    public static final String WRITE_STRATEGY = "azure.cosmos.sink.write.strategy";
    private static final String WRITE_STRATEGY_DOC = "Cosmos DB Item write Strategy: `ItemOverwrite` (using upsert), `ItemAppend` (using create, "
        + "ignore pre-existing items i.e., Conflicts), `ItemDelete` (deletes based on id/pk of data frame), "
        + "`ItemDeleteIfNotModified` (deletes based on id/pk of data frame if etag hasn't changed since collecting "
        + "id/pk), `ItemOverwriteIfNotModified` (using create if etag is empty, update/replace with etag pre-condition "
        + "otherwise, if document was updated the pre-condition failure is ignored), `ItemPatch` (Partial update all documents based on the patch config)";
    private static final String WRITE_STRATEGY_DISPLAY = "Cosmos DB Item write Strategy.";
    private static final String DEFAULT_WRITE_STRATEGY = ItemWriteStrategy.ITEM_OVERWRITE.getName();

    // patch related config
    public static final String PATCH_DEFAULT_OPERATION_TYPE = "azure.cosmos.sink.write.patch.operationType.default";
    private static final String PATCH_DEFAULT_OPERATION_TYPE_DOC = "Default Cosmos DB patch operation type."
        + " Supported ones include none, add, set, replace, remove, increment."
        + " Choose none for no-op, for others please reference [here](https://docs.microsoft.com/azure/cosmos-db/partial-document-update#supported-operations) for full context.";
    private static final String PATCH_DEFAULT_OPERATION_TYPE_DISPLAY = "Default Cosmos DB patch operation type.";
    private static final String DEFAULT_PATCH_DEFAULT_OPERATION_TYPE = KafkaCosmosPatchOperationType.SET.getName();

    public static final String PATCH_PROPERTY_CONFIGS = "azure.cosmos.sink.write.patch.property.configs";
    private static final String PATCH_PROPERTY_CONFIGS_DOC = "Cosmos DB patch json property configs."
        + " It can contain multiple definitions matching the following patterns separated by comma. property(jsonProperty).op(operationType) or property(jsonProperty).path(patchInCosmosdb).op(operationType) - The difference of the second pattern is that it also allows you to define a different cosmosdb path. "
        + "Note: It does not support nested json property config.";
    private static final String PATCH_PROPERTY_CONFIGS_DISPLAY = "Cosmos DB patch json property configs.";
    private static final String DEFAULT_PATCH_PROPERTY_CONFIGS = StringUtils.EMPTY;

    public static final String PATCH_FILTER = "azure.cosmos.sink.write.patch.filter";
    private static final String PATCH_FILTER_DOC = "Used for [Conditional patch](https://docs.microsoft.com/azure/cosmos-db/partial-document-update-getting-started#java)";
    private static final String PATCH_FILTER_DISPLAY = "Used for [Conditional patch].";
    private static final String DEFAULT_PATCH_FILTER = StringUtils.EMPTY;

    // max retry
    public static final String MAX_RETRY_COUNT_CONF = "azure.cosmos.sink.maxRetryCount";
    private static final String MAX_RETRY_COUNT_DOC =
        "Cosmos DB max retry attempts on write failures for Sink connector. By default, the connector will retry on transient write errors for up to 10 times.";
    private static final String MAX_RETRY_COUNT_DISPLAY = "Cosmos DB max retry attempts on write failures for Sink connector.";
    private static final int DEFAULT_MAX_RETRY_COUNT = 10;

    // database name
    private static final String DATABASE_NAME_CONF = "azure.cosmos.sink.database.name";
    private static final String DATABASE_NAME_CONF_DOC = "Cosmos DB database name.";
    private static final String DATABASE_NAME_CONF_DISPLAY = "Cosmos DB database name.";

    // container topic map
    public static final String CONTAINERS_TOPIC_MAP_CONF = "azure.cosmos.sink.containers.topicMap";
    private static final String CONTAINERS_TOPIC_MAP_DOC =
        "A comma delimited list of Kafka topics mapped to Cosmos containers. For example: topic1#con1,topic2#con2.";
    private static final String CONTAINERS_TOPIC_MAP_DISPLAY = "Topic-Container map";

    // id.strategy
    public static final String ID_STRATEGY_CONF = "azure.cosmos.sink.id.strategy";
    public static final String ID_STRATEGY_DOC =
        "A strategy used to populate the document with an ``id``. Valid strategies are: "
            + "``TemplateStrategy``, ``FullKeyStrategy``, ``KafkaMetadataStrategy``, "
            + "``ProvidedInKeyStrategy``, ``ProvidedInValueStrategy``. Configuration "
            + "properties prefixed with``id.strategy`` are passed through to the strategy. For "
            + "example, when using ``id.strategy=TemplateStrategy`` , "
            + "the property ``id.strategy.template`` is passed through to the template strategy "
            + "and used to specify the template string to be used in constructing the ``id``.";
    public static final String ID_STRATEGY_DISPLAY = "ID Strategy";
    public static final String DEFAULT_ID_STRATEGY = IdStrategyType.PROVIDED_IN_VALUE_STRATEGY.getName();

    // (?i) : The whole matching is case-insensitive
    // property[(](.*?)[)]: json property name match
    // ([.]path[(](.*)[)])*: mapping path match, it is optional
    // [.]op[(](.*)[)]: patch operation mapping
    public static final Pattern PATCH_PROPERTY_CONFIG_PATTERN = Pattern.compile("(?i)property[(](.*?)[)]([.]path[(](.*)[)])*[.]op[(](.*)[)]$");

    private final CosmosSinkWriteConfig writeConfig;
    private final CosmosSinkContainersConfig containersConfig;
    private final IdStrategyType idStrategyType;

    public CosmosSinkConfig(Map<String, ?> parsedConfig) {
        this(getConfigDef(), parsedConfig);
    }

    public CosmosSinkConfig(ConfigDef config, Map<String, ?> parsedConfig) {
        super(config, parsedConfig);
        this.writeConfig = this.parseWriteConfig();
        this.containersConfig = this.parseContainersConfig();
        this.idStrategyType = this.parseIdStrategyType();
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
                BULK_ENABLED,
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
                BULK_MAX_CONCURRENT_PARTITIONS,
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
                BULK_INITIAL_BATCH_SIZE,
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
                WRITE_STRATEGY,
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
                PATCH_DEFAULT_OPERATION_TYPE,
                ConfigDef.Type.STRING,
                DEFAULT_PATCH_DEFAULT_OPERATION_TYPE,
                new CosmosPatchOperationTypeValidator(),
                ConfigDef.Importance.LOW,
                PATCH_DEFAULT_OPERATION_TYPE_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.MEDIUM,
                PATCH_DEFAULT_OPERATION_TYPE_DISPLAY
            )
            .define(
                PATCH_PROPERTY_CONFIGS,
                ConfigDef.Type.STRING,
                DEFAULT_PATCH_PROPERTY_CONFIGS,
                ConfigDef.Importance.LOW,
                PATCH_PROPERTY_CONFIGS_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.LONG,
                PATCH_PROPERTY_CONFIGS_DISPLAY
            )
            .define(
                PATCH_FILTER,
                ConfigDef.Type.STRING,
                DEFAULT_PATCH_FILTER,
                ConfigDef.Importance.LOW,
                PATCH_FILTER_DOC,
                writeConfigGroupName,
                writeConfigGroupOrder++,
                ConfigDef.Width.LONG,
                PATCH_FILTER_DISPLAY
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
                TOLERANCE_ON_ERROR,
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
        boolean bulkEnabled = this.getBoolean(BULK_ENABLED);
        int bulkMaxConcurrentCosmosPartitions = this.getInt(BULK_MAX_CONCURRENT_PARTITIONS);
        int bulkInitialBatchSize = this.getInt(BULK_INITIAL_BATCH_SIZE);
        ItemWriteStrategy writeStrategy = this.parseItemWriteStrategy();
        int maxRetryCount = this.getInt(MAX_RETRY_COUNT_CONF);
        ToleranceOnErrorLevel toleranceOnErrorLevel = this.parseToleranceOnErrorLevel();
        CosmosPatchConfig patchConfig = writeStrategy == ItemWriteStrategy.ITEM_PATCH ? this.parsePatchConfig() : null;

        return new CosmosSinkWriteConfig(
            bulkEnabled,
            bulkMaxConcurrentCosmosPartitions,
            bulkInitialBatchSize,
            writeStrategy,
            maxRetryCount,
            toleranceOnErrorLevel,
            patchConfig);
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
        return ItemWriteStrategy.fromName(this.getString(WRITE_STRATEGY));
    }

    private ToleranceOnErrorLevel parseToleranceOnErrorLevel() {
        return ToleranceOnErrorLevel.fromName(this.getString(TOLERANCE_ON_ERROR));
    }

    private CosmosPatchConfig parsePatchConfig() {
        KafkaCosmosPatchOperationType defaultPatchOperationTypes = parseDefaultPatchOperationType();
        String filter = this.getString(PATCH_FILTER);

        Map<String, CosmosPatchJsonPropertyConfig> jsonPropertyConfigMap = new HashMap<>();
        List<String> patchJsonPropertyConfigs = convertToList(this.getString(PATCH_PROPERTY_CONFIGS));

        for (String patchPropertyConfigString : patchJsonPropertyConfigs) {
            Matcher jsonPropertyConfigMatcher = PATCH_PROPERTY_CONFIG_PATTERN.matcher(patchPropertyConfigString.trim());

            if (jsonPropertyConfigMatcher.matches()) {
                String jsonProperty = jsonPropertyConfigMatcher.group(1);
                String mappingPath = jsonPropertyConfigMatcher.group(3);
                if (StringUtils.isEmpty(mappingPath)) {
                    // in the cases no mapping path defined, using the json property as the mapping path
                    mappingPath = "/" + jsonProperty;
                }

                KafkaCosmosPatchOperationType patchOperationType = KafkaCosmosPatchOperationType.fromName(jsonPropertyConfigMatcher.group(4));

                if (StringUtils.isEmpty(jsonProperty) || patchOperationType == null) {
                    throw new IllegalArgumentException("Patch config invalid " + patchPropertyConfigString);
                }

                jsonPropertyConfigMap.put(jsonProperty, new CosmosPatchJsonPropertyConfig(jsonProperty, patchOperationType, mappingPath));
            } else {
                throw new IllegalArgumentException("Patch config invalid " + patchPropertyConfigString);
            }
        }

        return new CosmosPatchConfig(
            defaultPatchOperationTypes,
            jsonPropertyConfigMap,
            filter);
    }

    private KafkaCosmosPatchOperationType parseDefaultPatchOperationType() {
        String defaultPatchOperationType = this.getString(PATCH_DEFAULT_OPERATION_TYPE);
        return KafkaCosmosPatchOperationType.fromName(defaultPatchOperationType);
    }

    private IdStrategyType parseIdStrategyType() {
        return IdStrategyType.fromName(this.getString(ID_STRATEGY_CONF));
    }

    public CosmosSinkWriteConfig getWriteConfig() {
        return writeConfig;
    }

    public CosmosSinkContainersConfig getContainersConfig() {
        return containersConfig;
    }

    public IdStrategyType getIdStrategy() {
        return idStrategyType;
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

    public static class CosmosPatchOperationTypeValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String patchOperationTypeString = (String) o;
            if (StringUtils.isEmpty(patchOperationTypeString)) {
                throw new ConfigException(name, o, "PatchOperationType can not be empty or null");
            }

            KafkaCosmosPatchOperationType patchOperationType = KafkaCosmosPatchOperationType.fromName(patchOperationTypeString);
            if (patchOperationType == null) {
                throw new ConfigException(name, o, "Invalid PatchOperationType. Allowed values " + KafkaCosmosPatchOperationType.values());
            }
        }

        @Override
        public String toString() {
            return "PatchOperationType. Only allow " + KafkaCosmosPatchOperationType.values();
        }
    }
}
