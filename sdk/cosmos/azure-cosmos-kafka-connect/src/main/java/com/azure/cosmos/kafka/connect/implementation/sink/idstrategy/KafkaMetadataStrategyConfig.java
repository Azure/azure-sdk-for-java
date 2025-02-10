// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idstrategy;

import org.apache.kafka.common.config.ConfigDef;

import java.util.Map;

public class KafkaMetadataStrategyConfig extends AbstractIdStrategyConfig {
    public static final String DELIMITER_CONFIG = "delimiter";
    public static final String DELIMITER_CONFIG_DEFAULT = "-";
    public static final String DELIMITER_CONFIG_DOC = "The delimiter between metadata components";
    public static final String DELIMITER_CONFIG_DISPLAY = "Kafka Metadata";

    private String delimiter;

    public KafkaMetadataStrategyConfig(Map<String, ?> props) {
        this(getConfig(), props);
    }

    public KafkaMetadataStrategyConfig(ConfigDef definition, Map<?, ?> originals) {
        super(definition, originals);

        this.delimiter = getString(DELIMITER_CONFIG);
    }

    public static ConfigDef getConfig() {
        ConfigDef result = new ConfigDef();

        final String groupName = "Kafka Metadata Parameters";
        int groupOrder = 0;

        result.define(
            DELIMITER_CONFIG,
            ConfigDef.Type.STRING,
            DELIMITER_CONFIG_DEFAULT,
            ConfigDef.Importance.MEDIUM,
            DELIMITER_CONFIG_DOC,
            groupName,
            groupOrder++,
            ConfigDef.Width.MEDIUM,
            DELIMITER_CONFIG_DISPLAY
        );

        return result;
    }

    public String delimiter() {
        return delimiter;
    }
}

