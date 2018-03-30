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
 * These are the indexing types available for indexing a path in the Azure Cosmos DB database service.
 * For additional details, refer to
 * http://azure.microsoft.com/documentation/articles/documentdb-indexing-policies/#ConfigPolicy.
 */
public enum IndexKind {
    // The index entries are hashed to serve point look up queries.
    // Can be used to serve queries like: SELECT * FROM docs d WHERE d.prop = 5
    Hash,

    // The index entries are ordered. Range indexes are optimized for inequality predicate queries with efficient range
    // scans.
    // Can be used to serve queries like: SELECT * FROM docs d WHERE d.prop > 5
    Range,

    // The index entries are indexed to serve spatial queries like below:
    // SELECT * FROM Root r WHERE ST_DISTANCE({"type":"Point","coordinates":[71.0589,42.3601]}, r.location) $LE 10000
    Spatial
}
