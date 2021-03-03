// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.benchmark.linkedin.data.DataGenerator;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * This class facilitates generating data in batches.
 */
public class DataGenerationIterator implements Iterator<Map<Key, ObjectNode>> {

    public static final int BATCH_SIZE = 200000;

    private final DataGenerator _dataGenerator;
    private final int _totalRecordCount;
    private int _totalDataGenerated;

    /**
     * @param dataGenerator The underlying DataGenerator capable of generating a batch of records
     * @param recordCount Number of records we want to generate generate for this test.
     *                    Actual data generation happens in pre-determined batch size
     */
    public DataGenerationIterator(final DataGenerator dataGenerator, int recordCount) {
        _dataGenerator = Preconditions.checkNotNull(dataGenerator,
            "The underlying DataGenerator for this iterator can not be null");
        _totalRecordCount = recordCount;
        _totalDataGenerated = 0;
    }

    @Override
    public boolean hasNext() {
        return _totalDataGenerated < _totalRecordCount;
    }

    @Override
    public Map<Key, ObjectNode> next() {
        final int recordsToGenerate = Math.min(BATCH_SIZE, _totalRecordCount - _totalDataGenerated);

        // Filter Keys in case there are duplicates
        final Map<Key, ObjectNode> newDocuments = _dataGenerator.generate(recordsToGenerate);
        _totalDataGenerated += newDocuments.size();
        return newDocuments;
    }
}
