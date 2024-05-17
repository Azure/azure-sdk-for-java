// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.kafka.connect.implementation.KafkaCosmosConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Common Configuration for Cosmos DB Kafka source connector.
 */
public class CosmosSourceConfig extends KafkaCosmosConfig {

    // configuration only targets to source connector

    // database name
    private static final String DATABASE_NAME_CONF = "azure.cosmos.source.database.name";
    private static final String DATABASE_NAME_CONF_DOC = "Cosmos DB database name.";
    private static final String DATABASE_NAME_CONF_DISPLAY = "Cosmos DB database name.";

    // Source containers config
    private static final String CONTAINERS_INCLUDE_ALL_CONFIG = "azure.cosmos.source.containers.includeAll";
    private static final String CONTAINERS_INCLUDE_ALL_CONFIG_DOC = "Flag to indicate whether reading from all containers.";
    private static final String CONTAINERS_INCLUDE_ALL_CONFIG_DISPLAY = "Include all containers.";
    private static final boolean DEFAULT_CONTAINERS_INCLUDE_ALL = false;

    private static final String CONTAINERS_INCLUDED_LIST_CONFIG = "azure.cosmos.source.containers.includedList";
    private static final String CONTAINERS_INCLUDED_LIST_CONFIG_DOC =
        "Containers included. This config will be ignored if kafka.connect.cosmos.source.containers.includeAll is true.";
    private static final String CONTAINERS_INCLUDED_LIST_CONFIG_DISPLAY = "Containers included.";

    private static final String CONTAINERS_TOPIC_MAP_CONFIG = "azure.cosmos.source.containers.topicMap";
    private static final String CONTAINERS_TOPIC_MAP_CONFIG_DOC =
        "A comma delimited list of Kafka topics mapped to Cosmos containers. For example: topic1#con1,topic2#con2. "
            + "By default, container name is used as the name of the kafka topic to publish data to, "
            + "can use this property to override the default config ";
    private static final String CONTAINERS_TOPIC_MAP_CONFIG_DISPLAY = "Cosmos container topic map.";

    // changeFeed config
    private static final String CHANGE_FEED_START_FROM_CONFIG = "azure.cosmos.source.changeFeed.startFrom";
    private static final String CHANGE_FEED_START_FROM_CONFIG_DOC = "ChangeFeed Start from settings (Now, Beginning "
        + "or a certain point in time (UTC) for example 2020-02-10T14:15:03) - the default value is 'Beginning'. ";
    private static final String CHANGE_FEED_START_FROM_CONFIG_DISPLAY = "Change feed start from.";
    private static final String DEFAULT_CHANGE_FEED_START_FROM = CosmosChangeFeedStartFromMode.BEGINNING.getName();

    private static final String CHANGE_FEED_MODE_CONFIG = "azure.cosmos.source.changeFeed.mode";
    private static final String CHANGE_FEED_MODE_CONFIG_DOC = "ChangeFeed mode (LatestVersion or AllVersionsAndDeletes)";
    private static final String CHANGE_FEED_MODE_CONFIG_DISPLAY = "ChangeFeed mode (LatestVersion or AllVersionsAndDeletes)";
    private static final String DEFAULT_CHANGE_FEED_MODE = CosmosChangeFeedMode.LATEST_VERSION.getName();

    private static final String CHANGE_FEED_MAX_ITEM_COUNT_CONFIG = "azure.cosmos.source.changeFeed.maxItemCountHint";
    private static final String CHANGE_FEED_MAX_ITEM_COUNT_CONFIG_DOC =
        "The maximum number of documents returned in a single change feed request."
            + " But the number of items received might be higher than the specified value if multiple items are changed by the same transaction."
            + " The default is 1000.";
    private static final String CHANGE_FEED_MAX_ITEM_COUNT_CONFIG_DISPLAY = "The maximum number hint of documents returned in a single request. ";
    private static final int DEFAULT_CHANGE_FEED_MAX_ITEM_COUNT = 1000;

    // Metadata config
    private static final String METADATA_POLL_DELAY_MS_CONFIG = "azure.cosmos.source.metadata.poll.delay.ms";
    private static final String METADATA_POLL_DELAY_MS_CONFIG_DOC =
        "Indicates how often to check the metadata changes (including container split/merge, adding/removing/recreated containers). "
            + "When changes are detected, it will reconfigure the tasks. Default is 5 minutes.";
    private static final String METADATA_POLL_DELAY_MS_CONFIG_DISPLAY = "Metadata polling delay in ms.";
    private static final int DEFAULT_METADATA_POLL_DELAY_MS = 5 * 60 * 1000; // default is every 5 minutes

    private static final String METADATA_STORAGE_TYPE = "azure.cosmos.source.metadata.storage.type";
    private static final String METADATA_STORAGE_TYPE_DOC = "The storage type of the metadata. Two types are supported: Cosmos, Kafka.";
    private static final String METADATA_STORAGE_TYPE_DISPLAY = "The storage source of the metadata.";
    private static final String DEFAULT_METADATA_STORAGE_TYPE = CosmosMetadataStorageType.KAFKA.getName();

    private static final String METADATA_STORAGE_NAME =  "azure.cosmos.source.metadata.storage.name";
    private static final String METADATA_STORAGE_NAME_DOC = "The resource name of the metadata storage."
        + " If metadata storage type is Kafka topic, then this config refers to kafka topic name, the metadata topic will be created if it does not already exist, else it will use the pre-created topic."
        + " If metadata storage type is `Cosmos`, then this config refers to container name, for `MasterKey` auth, this container will be created with `AutoScale` with 4000 RU if not already exists, for `ServicePrincipal` auth, it requires the container to be created ahead of time .";
    private static final String METADATA_STORAGE_NAME_DISPLAY = "The metadata storage name.";
    private static final String DEFAULT_METADATA_STORAGE_NAME = "_cosmos.metadata.topic";

    // messageKey
    private static final String MESSAGE_KEY_ENABLED_CONF = "azure.cosmos.source.messageKey.enabled";
    private static final String MESSAGE_KEY_ENABLED_CONF_DOC = "Whether to set the kafka record message key.";
    private static final String MESSAGE_KEY_ENABLED_CONF_DISPLAY = "Kafka record message key enabled.";
    private static final boolean DEFAULT_MESSAGE_KEY_ENABLED = true;

    private static final String MESSAGE_KEY_FIELD_CONFIG = "azure.cosmos.source.messageKey.field";
    private static final String MESSAGE_KEY_FIELD_CONFIG_DOC = "The field to use as the message key.";
    private static final String MESSAGE_KEY_FIELD_CONFIG_DISPLAY = "Kafka message key field.";
    private static final String DEFAULT_MESSAGE_KEY_FIELD = "id";

    private final CosmosSourceContainersConfig containersConfig;
    private final CosmosMetadataConfig metadataConfig;
    private final CosmosSourceChangeFeedConfig changeFeedConfig;
    private final CosmosSourceMessageKeyConfig messageKeyConfig;

    public CosmosSourceConfig(Map<String, ?> parsedConfigs) {
        this(getConfigDef(), parsedConfigs);
    }

    public CosmosSourceConfig(ConfigDef configDef, Map<String, ?> parsedConfigs) {
        super(configDef, parsedConfigs);
        this.containersConfig = this.parseContainersConfig();
        this.metadataConfig = this.parseMetadataConfig();
        this.changeFeedConfig = this.parseChangeFeedConfig();
        this.messageKeyConfig = this.parseMessageKeyConfig();
    }

    public static ConfigDef getConfigDef() {
        ConfigDef configDef = KafkaCosmosConfig.getConfigDef();

        defineContainersConfig(configDef);
        defineMetadataConfig(configDef);
        defineChangeFeedConfig(configDef);
        defineMessageKeyConfig(configDef);

        return configDef;
    }

    private static void defineContainersConfig(ConfigDef result) {
        final String containersGroupName = "Containers";
        int containersGroupOrder = 0;

        result
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
                CONTAINERS_INCLUDE_ALL_CONFIG,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_CONTAINERS_INCLUDE_ALL,
                ConfigDef.Importance.HIGH,
                CONTAINERS_INCLUDE_ALL_CONFIG_DOC,
                containersGroupName,
                containersGroupOrder++,
                ConfigDef.Width.MEDIUM,
                CONTAINERS_INCLUDE_ALL_CONFIG_DISPLAY
            )
            .define(
                CONTAINERS_INCLUDED_LIST_CONFIG,
                ConfigDef.Type.STRING,
                Strings.Emtpy,
                ConfigDef.Importance.MEDIUM,
                CONTAINERS_INCLUDED_LIST_CONFIG_DOC,
                containersGroupName,
                containersGroupOrder++,
                ConfigDef.Width.LONG,
                CONTAINERS_INCLUDED_LIST_CONFIG_DISPLAY
            )
            .define(
                CONTAINERS_TOPIC_MAP_CONFIG,
                ConfigDef.Type.STRING,
                Strings.Emtpy,
                new ContainersTopicMapValidator(),
                ConfigDef.Importance.MEDIUM,
                CONTAINERS_TOPIC_MAP_CONFIG_DOC,
                containersGroupName,
                containersGroupOrder++,
                ConfigDef.Width.LONG,
                CONTAINERS_TOPIC_MAP_CONFIG_DISPLAY
            );
    }

    private static void defineMetadataConfig(ConfigDef result) {
        final String metadataGroupName = "Metadata";
        int metadataGroupOrder = 0;

        result
            .define(
                METADATA_POLL_DELAY_MS_CONFIG,
                ConfigDef.Type.INT,
                DEFAULT_METADATA_POLL_DELAY_MS,
                new PositiveValueValidator(),
                ConfigDef.Importance.MEDIUM,
                METADATA_POLL_DELAY_MS_CONFIG_DOC,
                metadataGroupName,
                metadataGroupOrder++,
                ConfigDef.Width.MEDIUM,
                METADATA_POLL_DELAY_MS_CONFIG_DISPLAY
            )
            .define(
                METADATA_STORAGE_TYPE,
                ConfigDef.Type.STRING,
                DEFAULT_METADATA_STORAGE_TYPE,
                new CosmosMetadataStorageTypeValidator(),
                ConfigDef.Importance.MEDIUM,
                METADATA_STORAGE_TYPE_DOC,
                metadataGroupName,
                metadataGroupOrder++,
                ConfigDef.Width.MEDIUM,
                METADATA_STORAGE_TYPE_DISPLAY
            )
            .define(
                METADATA_STORAGE_NAME,
                ConfigDef.Type.STRING,
                DEFAULT_METADATA_STORAGE_NAME,
                ConfigDef.Importance.MEDIUM,
                METADATA_STORAGE_NAME_DOC,
                metadataGroupName,
                metadataGroupOrder++,
                ConfigDef.Width.LONG,
                METADATA_STORAGE_NAME_DISPLAY
            );
    }

    private static void defineChangeFeedConfig(ConfigDef result) {
        final String changeFeedGroupName = "ChangeFeed";
        int changeFeedGroupOrder = 0;

        result
            .define(
                CHANGE_FEED_MODE_CONFIG,
                ConfigDef.Type.STRING,
                DEFAULT_CHANGE_FEED_MODE,
                new ChangeFeedModeValidator(),
                ConfigDef.Importance.HIGH,
                CHANGE_FEED_MODE_CONFIG_DOC,
                changeFeedGroupName,
                changeFeedGroupOrder++,
                ConfigDef.Width.MEDIUM,
                CHANGE_FEED_MODE_CONFIG_DISPLAY
            )
            .define(
                CHANGE_FEED_START_FROM_CONFIG,
                ConfigDef.Type.STRING,
                DEFAULT_CHANGE_FEED_START_FROM,
                new ChangeFeedStartFromValidator(),
                ConfigDef.Importance.HIGH,
                CHANGE_FEED_START_FROM_CONFIG_DOC,
                changeFeedGroupName,
                changeFeedGroupOrder++,
                ConfigDef.Width.MEDIUM,
                CHANGE_FEED_START_FROM_CONFIG_DISPLAY
            )
            .define(
                CHANGE_FEED_MAX_ITEM_COUNT_CONFIG,
                ConfigDef.Type.INT,
                DEFAULT_CHANGE_FEED_MAX_ITEM_COUNT,
                new PositiveValueValidator(),
                ConfigDef.Importance.MEDIUM,
                CHANGE_FEED_MAX_ITEM_COUNT_CONFIG_DOC,
                changeFeedGroupName,
                changeFeedGroupOrder++,
                ConfigDef.Width.MEDIUM,
                CHANGE_FEED_MAX_ITEM_COUNT_CONFIG_DISPLAY
            );
    }

    private static void defineMessageKeyConfig(ConfigDef result) {
        final String messageGroupName = "Message Key";
        int messageGroupOrder = 0;

        result
            .define(
                MESSAGE_KEY_ENABLED_CONF,
                ConfigDef.Type.BOOLEAN,
                DEFAULT_MESSAGE_KEY_ENABLED,
                ConfigDef.Importance.MEDIUM,
                MESSAGE_KEY_ENABLED_CONF_DOC,
                messageGroupName,
                messageGroupOrder++,
                ConfigDef.Width.SHORT,
                MESSAGE_KEY_ENABLED_CONF_DISPLAY
            )
            .define(
                MESSAGE_KEY_FIELD_CONFIG,
                ConfigDef.Type.STRING,
                DEFAULT_MESSAGE_KEY_FIELD,
                ConfigDef.Importance.HIGH,
                MESSAGE_KEY_FIELD_CONFIG_DOC,
                messageGroupName,
                messageGroupOrder++,
                ConfigDef.Width.MEDIUM,
                MESSAGE_KEY_FIELD_CONFIG_DISPLAY
            );
    }

    private CosmosSourceContainersConfig parseContainersConfig() {
        String databaseName = this.getString(DATABASE_NAME_CONF);
        boolean includeAllContainers = this.getBoolean(CONTAINERS_INCLUDE_ALL_CONFIG);
        List<String> containersIncludedList = this.getContainersIncludedList();
        List<String> containersTopicMap = this.getContainersTopicMap();

        return new CosmosSourceContainersConfig(
            databaseName,
            includeAllContainers,
            containersIncludedList,
            containersTopicMap
        );
    }

    private List<String> getContainersIncludedList() {
        return convertToList(this.getString(CONTAINERS_INCLUDED_LIST_CONFIG));
    }

    private List<String> getContainersTopicMap() {
        return convertToList(this.getString(CONTAINERS_TOPIC_MAP_CONFIG));
    }

    private CosmosMetadataConfig parseMetadataConfig() {
        int pollDelayInMs = this.getInt(METADATA_POLL_DELAY_MS_CONFIG);
        CosmosMetadataStorageType storageType = this.parseMetadataStorageSource();
        String storageName = this.getString(METADATA_STORAGE_NAME);

        return new CosmosMetadataConfig(pollDelayInMs, storageType, storageName);
    }

    private CosmosMetadataStorageType parseMetadataStorageSource() {
        String source = this.getString(METADATA_STORAGE_TYPE);
        return CosmosMetadataStorageType.fromName(source);
    }
    private CosmosSourceChangeFeedConfig parseChangeFeedConfig() {
        CosmosChangeFeedMode changeFeedModes = this.parseChangeFeedMode();
        CosmosChangeFeedStartFromMode changeFeedStartFromMode = this.parseChangeFeedStartFromMode();
        Instant changeFeedStartFrom = this.parseChangeFeedStartFrom(changeFeedStartFromMode);
        Integer changeFeedMaxItemCountHint = this.getInt(CHANGE_FEED_MAX_ITEM_COUNT_CONFIG);

        return new CosmosSourceChangeFeedConfig(
            changeFeedModes,
            changeFeedStartFromMode,
            changeFeedStartFrom,
            changeFeedMaxItemCountHint);
    }

    private CosmosSourceMessageKeyConfig parseMessageKeyConfig() {
        boolean messageKeyEnabled = this.getBoolean(MESSAGE_KEY_ENABLED_CONF);
        String messageKeyField = this.getString(MESSAGE_KEY_FIELD_CONFIG);

        return new CosmosSourceMessageKeyConfig(messageKeyEnabled, messageKeyField);
    }
    private CosmosChangeFeedStartFromMode parseChangeFeedStartFromMode() {
        String changeFeedStartFrom = this.getString(CHANGE_FEED_START_FROM_CONFIG);
        if (changeFeedStartFrom.equalsIgnoreCase(CosmosChangeFeedStartFromMode.BEGINNING.getName())) {
            return CosmosChangeFeedStartFromMode.BEGINNING;
        }

        if (changeFeedStartFrom.equalsIgnoreCase(CosmosChangeFeedStartFromMode.NOW.getName())) {
            return CosmosChangeFeedStartFromMode.NOW;
        }

        return CosmosChangeFeedStartFromMode.POINT_IN_TIME;
    }

    private Instant parseChangeFeedStartFrom(CosmosChangeFeedStartFromMode startFromMode) {
        if (startFromMode == CosmosChangeFeedStartFromMode.POINT_IN_TIME) {
            String changeFeedStartFrom = this.getString(CHANGE_FEED_START_FROM_CONFIG);
            return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(changeFeedStartFrom.trim()));
        }

        return null;
    }

    private CosmosChangeFeedMode parseChangeFeedMode() {
        String changeFeedMode = this.getString(CHANGE_FEED_MODE_CONFIG);
        return CosmosChangeFeedMode.fromName(changeFeedMode);
    }

    public CosmosSourceContainersConfig getContainersConfig() {
        return containersConfig;
    }

    public CosmosMetadataConfig getMetadataConfig() {
        return metadataConfig;
    }

    public CosmosSourceChangeFeedConfig getChangeFeedConfig() {
        return changeFeedConfig;
    }

    public CosmosSourceMessageKeyConfig getMessageKeyConfig() {
        return messageKeyConfig;
    }

    public static class ContainersTopicMapValidator implements ConfigDef.Validator {
        private static final String INVALID_TOPIC_MAP_FORMAT =
            "Invalid entry for topic-container map. The topic-container map should be a comma-delimited "
                + "list of Kafka topic to Cosmos containers. Each mapping should be a pair of Kafka "
                + "topic and Cosmos container separated by '#'. For example: topic1#con1,topic2#con2.";

        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String configValue = (String) o;
            if (StringUtils.isEmpty(configValue)) {
                return;
            }

            List<String> containerTopicMapList = convertToList(configValue);

            // validate each item should be in topic#container format
            boolean invalidFormatExists =
                containerTopicMapList
                    .stream()
                    .anyMatch(containerTopicMap ->
                        containerTopicMap
                            .split(CosmosSourceContainersConfig.CONTAINER_TOPIC_MAP_SEPARATOR)
                            .length != 2);

            if (invalidFormatExists) {
                throw new ConfigException(name, o, INVALID_TOPIC_MAP_FORMAT);
            }
        }

        @Override
        public String toString() {
            return "Containers topic map";
        }
    }

    public static class ChangeFeedModeValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String changeFeedModeString = (String) o;
            if (StringUtils.isEmpty(changeFeedModeString)) {
                throw new ConfigException(name, o, "ChangeFeedMode can not be empty or null");
            }

            CosmosChangeFeedMode changeFeedMode = CosmosChangeFeedMode.fromName(changeFeedModeString);
            if (changeFeedMode == null) {
                throw new ConfigException(name, o, "Invalid ChangeFeedMode, only allow LatestVersion or AllVersionsAndDeletes");
            }
        }

        @Override
        public String toString() {
            return "ChangeFeedMode. Only allow " + CosmosChangeFeedMode.values();
        }
    }

    public static class ChangeFeedStartFromValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String changeFeedStartFromString = (String) o;
            if (StringUtils.isEmpty(changeFeedStartFromString)) {
                throw new ConfigException(name, o, "ChangeFeedStartFrom can not be empty or null");
            }

            CosmosChangeFeedStartFromMode changeFeedStartFromModes =
                CosmosChangeFeedStartFromMode.fromName(changeFeedStartFromString);
            if (changeFeedStartFromModes == null) {
                try {
                    Instant.parse(changeFeedStartFromString);
                } catch (DateTimeParseException dateTimeParseException) {
                    throw new ConfigException(
                        name,
                        o,
                        "Invalid changeFeedStartFrom."
                            + " only allow Now, Beginning or a certain point in time (UTC) for example 2020-02-10T14:15:03 ");
                }
            }
        }

        @Override
        public String toString() {
            return "ChangeFeedStartFrom. Only allow Now, Beginning or a certain point in time (UTC) for example 2020-02-10T14:15:03";
        }
    }

    public static class PositiveValueValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            int value = Integer.parseInt(o.toString());

            if (value <= 0) {
                throw new ConfigException(name, o, "Invalid value, need to be >= 0");
            }
        }

        @Override
        public String toString() {
            return "Value need to be >= 0";
        }
    }

    public static class CosmosMetadataStorageTypeValidator implements ConfigDef.Validator {
        @Override
        @SuppressWarnings("unchecked")
        public void ensureValid(String name, Object o) {
            String storageTypeString = (String) o;
            if (StringUtils.isEmpty(storageTypeString)) {
                throw new ConfigException(name, o, "Cosmos metadata storage type can not be empty or null");
            }

            CosmosMetadataStorageType storageType = CosmosMetadataStorageType.fromName(storageTypeString);
            if (storageType == null) {
                throw new ConfigException(name, o, "Invalid CosmosMetadataStorageType, only allow Cosmos or Kafka");
            }
        }

        @Override
        public String toString() {
            return "CosmosMetadataStorageType. Only allow " + CosmosMetadataStorageType.values();
        }
    }
}
