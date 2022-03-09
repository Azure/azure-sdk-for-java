// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.benchmark.linkedin.data.DataGenerator;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import java.util.Iterator;
import java.util.Map;


/**
 * This class facilitates generating data in batches.
 */
public class DataGenerationIterator implements Iterator<Map<Key, ObjectNode>> {

    private final DataGenerator _dataGenerator;
    private final int _totalRecordCount;
    private int _totalDataGenerated;
    private final int _batchLoadBatchSize;

    /**
     * @param dataGenerator The underlying DataGenerator capable of generating a batch of records
     * @param recordCount Number of records we want to generate generate for this test.
     * @param batchLoadBatchSize The number of documents to generate, and load, in each BulkExecutor iteration
     */
    public DataGenerationIterator(final DataGenerator dataGenerator, int recordCount, int batchLoadBatchSize) {
        Preconditions.checkArgument(recordCount > 0,
            "The number of documents to generate must be greater than 0");
        Preconditions.checkArgument(batchLoadBatchSize > 0,
            "The  number of documents to generate and load on each BulkExecutor load iteration must be greater than 0");
        _dataGenerator = Preconditions.checkNotNull(dataGenerator,
            "The underlying DataGenerator for this iterator can not be null");
        _totalRecordCount = recordCount;
        _batchLoadBatchSize = batchLoadBatchSize;
        _totalDataGenerated = 0;
    }

    @Override
    public boolean hasNext() {
        return _totalDataGenerated < _totalRecordCount;
    }

    @Override
    public Map<Key, ObjectNode> next() {
        final int recordsToGenerate = Math.min(_batchLoadBatchSize, _totalRecordCount - _totalDataGenerated);

        // Filter Keys in case there are duplicates
        final Map<Key, ObjectNode> newDocuments = _dataGenerator.generate(recordsToGenerate);
        _totalDataGenerated += newDocuments.size();
        return newDocuments;
    }
}
