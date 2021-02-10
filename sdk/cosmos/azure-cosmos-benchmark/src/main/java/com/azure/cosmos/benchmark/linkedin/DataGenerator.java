// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.benchmark.linkedin.data.InvitationDataGenerator;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * This class facilitates generating data in batches.
 */
public class DataGenerator implements Iterator<Map<Key, ObjectNode>> {

    public static final int BATCH_SIZE = 200000;

    private final InvitationDataGenerator _dataGenerator;
    private final int _totalRecordCount;
    private int _totalDataGenerated;
    private final Set<Key> _generatedKeys;

    /**
     * @param recordCount Number of records we want to generate generate for this test
     *                    Actual data generation happens in pre-determined batch size
     */
    public DataGenerator(int recordCount) {
        _dataGenerator = new InvitationDataGenerator(recordCount);
        _totalRecordCount = recordCount;
        _totalDataGenerated = 0;
        _generatedKeys = new HashSet<>();
    }

    @Override
    public boolean hasNext() {
        return _totalDataGenerated < _totalRecordCount;
    }

    @Override
    public Map<Key, ObjectNode> next() {
        final int recordsToGenerate = Math.min(BATCH_SIZE, _totalRecordCount - _totalDataGenerated);

        // Filter Keys in case there are duplicates
        final Map<Key, ObjectNode> newDocuments = _dataGenerator.generate(recordsToGenerate)
            .entrySet()
            .stream()
            .filter(keyObjectNodeEntry -> !_generatedKeys.contains(keyObjectNodeEntry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        _generatedKeys.addAll(newDocuments.keySet());
        _totalDataGenerated += newDocuments.size();
        return newDocuments;
    }

    /**
     * @return Set of Keys representing each document's id and partitioningKey
     */
    public Set<Key> getGeneratedKeys() {
        return Collections.unmodifiableSet(_generatedKeys);
    }
}
