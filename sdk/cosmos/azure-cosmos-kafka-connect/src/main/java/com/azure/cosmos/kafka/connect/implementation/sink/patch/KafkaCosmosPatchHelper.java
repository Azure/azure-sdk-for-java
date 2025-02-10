// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.patch;

import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ShortNode;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class KafkaCosmosPatchHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCosmosPatchHelper.class);
    private static final String TIMESTAMP_ATTRIBUTE_NAME = "_ts";
    private static final String ID_ATTRIBUTE_NAME = "id";
    private static final String ETAG_ATTRIBUTE_NAME = "_etag";
    private static final String SELF_ATTRIBUTE_NAME = "_self";
    private static final String RESOURCE_ID_ATTRIBUTE_NAME = "_rid";
    private static final String ATTACHMENT_ATTRIBUTE_NAME = "_attachments";
    private static final List<String> SYSTEM_PROPERTIES = Arrays.asList(
        TIMESTAMP_ATTRIBUTE_NAME,
        ETAG_ATTRIBUTE_NAME,
        SELF_ATTRIBUTE_NAME,
        RESOURCE_ID_ATTRIBUTE_NAME,
        ATTACHMENT_ATTRIBUTE_NAME);

    @SuppressWarnings("unchecked")
    public static CosmosPatchOperations createCosmosPatchOperations(
        String itemId,
        PartitionKeyDefinition partitionKeyDefinition,
        SinkRecord sinkRecord,
        CosmosPatchConfig patchConfig) {

        checkNotNull(itemId, "Argument 'itemId' should not be null");
        checkNotNull(partitionKeyDefinition, "Argument 'partitionKeyDefinition' should not be null");
        checkNotNull(sinkRecord, "Argument 'sinkRecord' should not be null");
        checkNotNull(patchConfig, "Argument 'patchConfig' should not be null");

        if (!(sinkRecord.value() instanceof Map)) {
            LOGGER.warn("SinkRecord value is not map - skip adding cosmos patch operations");
            return null;
        }

        Map<String, Object> sinkRecordValue = (Map<String, Object>) sinkRecord.value();
        CosmosPatchOperations cosmosPatchOperations = CosmosPatchOperations.create();

        Map<String, CosmosPatchJsonPropertyConfig> aggregatedPropertyConfigMap = new HashMap<>();

        // process all property configs from sink record value
        for (Map.Entry<String, Object> property : sinkRecordValue.entrySet()) {
            // check any custom config for the property
            CosmosPatchJsonPropertyConfig propertyPatchConfig = patchConfig.getJsonPropertyConfigMap().get(property.getKey());
            if (propertyPatchConfig == null) {
                // construct a default config - using default patch operation, mapping path will be the same as the property name
                propertyPatchConfig = new CosmosPatchJsonPropertyConfig(
                    property.getKey(),
                    patchConfig.getDefaultPatchOperationType(),
                    "/" + property.getKey()
                );
            }

            if (isAllowedProperty(propertyPatchConfig.getMappingPath(), partitionKeyDefinition)) {
                aggregatedPropertyConfigMap.put(property.getKey(), propertyPatchConfig);
            } else {
                // if mappingPath is one of the cosmos system properties or id/partitionKey, then it can not be patched
                LOGGER.debug("Can not patch property " + property.getKey());
            }
        }

        // process all property configs from patch configs
        for (Map.Entry<String, CosmosPatchJsonPropertyConfig> config : patchConfig.getJsonPropertyConfigMap().entrySet()) {
            aggregatedPropertyConfigMap.putIfAbsent(config.getKey(), config.getValue());
        }

        // adding cosmos patch operation based on the aggregated property config
        for (CosmosPatchJsonPropertyConfig jsonPropertyConfig : aggregatedPropertyConfigMap.values()) {
            switch (jsonPropertyConfig.getPatchOperationType()) {
                case REMOVE:
                    cosmosPatchOperations.remove(jsonPropertyConfig.getMappingPath());
                    break;
                case NONE: // no-op
                    break;
                default:
                    addPatchOperation(cosmosPatchOperations, jsonPropertyConfig, sinkRecordValue);
            }
        }

        if (
            ImplementationBridgeHelpers
                .CosmosPatchOperationsHelper
                .getCosmosPatchOperationsAccessor()
                .getPatchOperations(cosmosPatchOperations).size() == 0) {
            // If we reach here, it means there are no valid operations being included in the patch operation.
            // It could be caused by few reasons:
            // 1. The patch operation type for all columns are None which result in no-op
            // 2. There is no properties which are allowed for partial updates included (id, partitionKey path, system properties)
            // 3. Due to serialization settings, it could filter out null/empty/default properties
            //
            // As of today, we start with more restrict rules: throw exception if there is no operations being included in the patch operation
            // But in the future, if it is a common scenario that we will reach here,
            // we can consider to relax the rules by adding another config to allow this behavior in patch configs
            throw new IllegalStateException("There is no operations included in the patch operation for itemId: " + itemId);
        }

        return cosmosPatchOperations;
    }

    private static boolean isAllowedProperty(String path, PartitionKeyDefinition partitionKeyDefinition) {
        checkArgument(StringUtils.isNotEmpty(path), "Argument 'path' can not be null");
        checkNotNull(partitionKeyDefinition, "Argument 'partitionKeyDefinition' can not be null");
        // There are some properties are immutable, these kind properties include:
        // 1. System properties : _ts, _rid, _etag
        // 2. id, and partitionKeyPath
        String effectivePath = path.startsWith("/") ? path.substring(1) : path;
        if (SYSTEM_PROPERTIES.contains(effectivePath)
            || ID_ATTRIBUTE_NAME.equals(effectivePath)
            || StringUtils.join(partitionKeyDefinition.getPaths(), "").contains(effectivePath)) {
            return false;
        }

        return true;
    }

    private static void addPatchOperation(
        CosmosPatchOperations cosmosPatchOperations,
        CosmosPatchJsonPropertyConfig jsonPropertyConfig,
        Map<String, Object> sinkRecordValue) {
        switch (jsonPropertyConfig.getPatchOperationType()) {
            case ADD:
                addIfJsonPropertyExists(
                    jsonPropertyConfig.getProperty(),
                    sinkRecordValue,
                    (pathValue) -> cosmosPatchOperations.add(jsonPropertyConfig.getMappingPath(), pathValue));
                break;
            case SET:
                addIfJsonPropertyExists(
                    jsonPropertyConfig.getProperty(),
                    sinkRecordValue,
                    (pathValue) -> cosmosPatchOperations.set(jsonPropertyConfig.getMappingPath(), pathValue));
                break;
            case REPLACE:
                addIfJsonPropertyExists(
                    jsonPropertyConfig.getProperty(),
                    sinkRecordValue,
                    (pathValue) -> cosmosPatchOperations.replace(jsonPropertyConfig.getMappingPath(), pathValue));
                break;
            case INCREMENT:
                addIfJsonPropertyExists(
                    jsonPropertyConfig.getProperty(),
                    sinkRecordValue,
                    (pathValue) -> addIncrementPatchOperation(cosmosPatchOperations, jsonPropertyConfig.getMappingPath(), pathValue));
                break;
            default:
                throw new IllegalArgumentException("Patch operation type " + jsonPropertyConfig.getPatchOperationType() + " is not supported");
        }
    }

    private static void addIfJsonPropertyExists(String property, Map<String, Object> sinkRecordValue, Consumer<Object> consumer) {
        if (sinkRecordValue.containsKey(property)) {
            consumer.accept(sinkRecordValue.get(property));
        } else {
            LOGGER.debug("The operation will not be added due to sink record value does not contain property at root level" + property);
        }
    }

    private static void addIncrementPatchOperation(
        CosmosPatchOperations cosmosPatchOperations,
        String mappingPath,
        Object pathValue) {

        JsonNode jsonNode = Utils.getSimpleObjectMapper().valueToTree(pathValue);
        if (jsonNode.isNumber()) {
            if (jsonNode instanceof ShortNode || jsonNode instanceof IntNode || jsonNode instanceof LongNode) {
                cosmosPatchOperations.increment(mappingPath, jsonNode.longValue());
            } else if (jsonNode instanceof FloatNode || jsonNode instanceof DoubleNode) {
                cosmosPatchOperations.increment(mappingPath, jsonNode.doubleValue());
            } else if (pathValue instanceof BigIntegerNode) {
                if (((BigIntegerNode) pathValue).canConvertToLong()) {
                    cosmosPatchOperations.increment(mappingPath, ((BigIntegerNode) pathValue).longValue());
                }

                throw new IllegalArgumentException("BigInteger " + jsonNode.bigIntegerValue() + " is too large, can not be converted to long");
            } else if (pathValue instanceof DecimalNode) {
                if (((DecimalNode) pathValue).canConvertToLong()) {
                    cosmosPatchOperations.increment(mappingPath, ((DecimalNode) pathValue).longValue());
                }
                throw new IllegalArgumentException("Decimal " + jsonNode.decimalValue() + " is too large, can not be converted to long");
            } else {
                throw new IllegalArgumentException("Increment operation is not supported for type " + pathValue.getClass());
            }
        }
    }
}
