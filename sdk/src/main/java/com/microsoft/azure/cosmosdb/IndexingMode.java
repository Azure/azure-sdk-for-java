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

/**
 * Specifies the supported indexing modes in the Azure Cosmos DB database service.
 */
public enum IndexingMode {
    /**
     * Index is updated synchronously with a create or update operation.
     * <p>
     * With consistent indexing, query behavior is the same as the default consistency level for the collection. The
     * index is always kept up to date with the data.
     */
    Consistent,

    /**
     * Index is updated asynchronously with respect to a create or update operation.
     * <p>
     * With lazy indexing, queries are eventually consistent. The index is updated when the collection is idle.
     */
    Lazy,

    /**
     * No index is provided.
     * <p>
     * Setting IndexingMode to "None" drops the index. Use this if you don't want to maintain the index for a document
     * collection, to save the storage cost or improve the write throughput. Your queries will degenerate to scans of
     * the entire collection.
     */
    None
}
