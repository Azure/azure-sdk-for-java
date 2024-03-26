// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaCosmosUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaCosmosUtils.class);
    private static final int JAVA_VERSION = getJavaVersion();
    private static ObjectMapper simpleObjectMapper = createAndInitializeObjectMapper(false);

    private static int getJavaVersion() {
        int version = -1;
        try {
            String completeJavaVersion = System.getProperty("java.version");
            String[] versionElements = completeJavaVersion.split("\\.");
            int versionFirstPart = Integer.parseInt(versionElements[0]);
            // Java 8 or lower format is 1.6.0, 1.7.0, 1.7.0, 1.8.0
            // Java 9 or higher format is 9.0, 10.0, 11.0
            if (versionFirstPart == 1) {
                version = Integer.parseInt(versionElements[1]);
            } else {
                version = versionFirstPart;
            }
            return version;
        } catch (Exception ex) {
            // Consumed the exception we got during parsing
            // For unknown version we wil mark it as -1
            LOGGER.warn("Error while fetching java version", ex);
            return version;
        }
    }

    private static ObjectMapper createAndInitializeObjectMapper(boolean allowDuplicateProperties) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_TRAILING_COMMA, true);
        if (!allowDuplicateProperties) {
            objectMapper.configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true);
        }
        objectMapper.configure(DeserializationFeature.ACCEPT_FLOAT_AS_INT, false);


        // We will not register after burner for java 16+, due to its breaking changes
        // https://github.com/Azure/azure-sdk-for-java/issues/23005
        if (JAVA_VERSION != -1 && JAVA_VERSION < 16) {
            objectMapper.registerModule(new AfterburnerModule());
        }

        objectMapper.registerModule(new JavaTimeModule());

        return objectMapper;
    }

    public static ObjectMapper getSimpleObjectMapper() {
        return KafkaCosmosUtils.simpleObjectMapper;
    }
}
