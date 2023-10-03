// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Collections;
import java.util.List;

/**
 * Utility class for partition key extraction
 */
public class PartitionKeyHelper {
    public static PartitionKeyInternal extractPartitionKeyValueFromDocument(
        JsonSerializable document,
        PartitionKeyDefinition partitionKeyDefinition) {

        PartitionKey partitionKey = extractPartitionKeyFromDocument(document, partitionKeyDefinition);
        return partitionKey == null ? null : ModelBridgeInternal.getPartitionKeyInternal(partitionKey);
    }

    public static PartitionKey extractPartitionKeyFromDocument(
        JsonSerializable document,
        PartitionKeyDefinition partitionKeyDefinition) {
        if (partitionKeyDefinition != null) {
            switch (partitionKeyDefinition.getKind()) {
                case HASH:
                    String path = partitionKeyDefinition.getPaths().iterator().next();
                    List<String> parts = PathParser.getPathParts(path);
                    if (parts.size() >= 1) {
                        Object value = ModelBridgeInternal.getObjectByPathFromJsonSerializable(document, parts);
                        if (value == null || value.getClass() == ObjectNode.class) {
                            value = ModelBridgeInternal.getNonePartitionKey(partitionKeyDefinition);
                        }

                        if (value instanceof PartitionKeyInternal) {
                            return ImplementationBridgeHelpers
                                .PartitionKeyHelper
                                .getPartitionKeyAccessor()
                                .toPartitionKey((PartitionKeyInternal) value);
                        } else {
                            return ImplementationBridgeHelpers
                                .PartitionKeyHelper
                                .getPartitionKeyAccessor()
                                .toPartitionKey(value, PartitionKeyInternal.fromObjectArray(Collections.singletonList(value), false));
                        }
                    }
                    break;
                case MULTI_HASH:
                    Object[] partitionKeyValues = new Object[partitionKeyDefinition.getPaths().size()];
                    for(int pathIter = 0 ; pathIter < partitionKeyDefinition.getPaths().size(); pathIter++){
                        String partitionPath = partitionKeyDefinition.getPaths().get(pathIter);
                        List<String> partitionPathParts = PathParser.getPathParts(partitionPath);
                        partitionKeyValues[pathIter] = ModelBridgeInternal.getObjectByPathFromJsonSerializable(document, partitionPathParts);
                    }

                    return ImplementationBridgeHelpers
                        .PartitionKeyHelper
                        .getPartitionKeyAccessor()
                        .toPartitionKey(
                            getPartitionKeyObjectKey(partitionKeyValues),
                            PartitionKeyInternal.fromObjectArray(partitionKeyValues, false));

                default:
                    throw new IllegalArgumentException("Unrecognized Partition kind: " + partitionKeyDefinition.getKind());
            }
        }

        return null;
    }

    public static String getPartitionKeyObjectKey(Object[] values) {
        StringBuilder backendValues = new StringBuilder();
        if (values.length == 1) {
            backendValues.append((String) values[0]);
        } else {
            for (int i = 0; i < values.length; i++) {
                backendValues.append((String) values[i]);
                if (i < values.length-1) {
                    backendValues.append("=");
                }
            }
        }

        return backendValues.toString();
    }
}
