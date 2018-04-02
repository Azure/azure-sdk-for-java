/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

/**
 * Specifies the options associated with change feed methods (enumeration operations) in the Azure Cosmos DB database service.
 */
public final class ChangeFeedOptions extends FeedOptionsBase {
    private String partitionKeyRangeId;
    private boolean startFromBeginning;

    public ChangeFeedOptions() {}

    public ChangeFeedOptions(ChangeFeedOptions options) {
        super(options);
        this.partitionKeyRangeId = options.partitionKeyRangeId;
        this.startFromBeginning = options.startFromBeginning;
    }

    /**
     * Get the partition key range id for the current request
     * <p>
     * ChangeFeed requests can be executed against specific partition key ranges.
     * This is used to process the change feed in parallel across multiple consumers.
     * </p>
     *
     * @return a string indicating the partition key range ID
     * @see PartitionKeyRange
     * @see AsyncDocumentClient#readPartitionKeyRanges(String, FeedOptions)
     */
    public String getPartitionKeyRangeId() {
        return partitionKeyRangeId;
    }

    /**
     * Set the partition key range id for the current request
     * <p>
     * ChangeFeed requests can be executed against specific partition key ranges.
     * This is used to process the change feed in parallel across multiple consumers.
     * </p>
     *
     * @param partitionKeyRangeId a string indicating the partition key range ID
     * @see PartitionKeyRange
     * @see AsyncDocumentClient#readPartitionKeyRanges(String, FeedOptions)
     */
    public void setPartitionKeyRangeId(String partitionKeyRangeId) {
        this.partitionKeyRangeId = partitionKeyRangeId;
    }

    /**
     * Get whether change feed should start from beginning (true) or from current (false).
     * By default it's start from current (false).
     *
     * @return a boolean value indicating change feed should start from beginning or not
     */
    public boolean isStartFromBeginning() {
        return startFromBeginning;
    }

    /**
     * Set whether change feed should start from beginning (true) or from current (false).
     * By default it's start from current (false).
     *
     * @param startFromBeginning a boolean value indicating change feed should start from beginning or not
     */
    public void setStartFromBeginning(boolean startFromBeginning) {
        this.startFromBeginning = startFromBeginning;
    }
}
