// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin.data;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;


public interface DataGenerator {
    /**
     * Generates the desired batch of records for a specific entity
     *
     * @param recordCount Number of records we want to create in this invocation
     * @return Map containing desired count of record key to value entries
     */
    Map<Key, ObjectNode> generate(int recordCount);
}
