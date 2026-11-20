// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.models.PartitionKeyDefinition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper for constructing SQL partition key selector fragments from partition key definitions.
 */
final class PartitionKeyQueryHelper {

    private PartitionKeyQueryHelper() {
    }

    static List<String> createPkSelectors(PartitionKeyDefinition partitionKeyDefinition) {
        return partitionKeyDefinition.getPaths()
            .stream()
            .map(PathParser::getPathParts)
            .map(pathParts -> pathParts.stream()
                .map(pathPart -> "[\"" + pathPart.replace("\"", "\\\"") + "\"]")
                .collect(Collectors.joining()))
            .collect(Collectors.toList());
    }
}
