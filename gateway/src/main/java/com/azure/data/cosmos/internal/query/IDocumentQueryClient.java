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
package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.ConsistencyLevel;
import com.azure.data.cosmos.internal.IRetryPolicyFactory;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.RxDocumentServiceResponse;
import com.azure.data.cosmos.internal.caches.IPartitionKeyRangeCache;
import com.azure.data.cosmos.internal.caches.RxCollectionCache;
import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IDocumentQueryClient {

    /**
     * TODO: this should be async returning observable
     * @return
     */
    RxCollectionCache getCollectionCache();

    /**
     * TODO: this should be async returning observable
     * @return
     */
    IPartitionKeyRangeCache getPartitionKeyRangeCache();

    /**
     * @return
     */
    IRetryPolicyFactory getResetSessionTokenRetryPolicy();

    /**
     * TODO: this should be async returning observable
     * @return 
     */
    ConsistencyLevel getDefaultConsistencyLevelAsync();

    /**
     * TODO: this should be async returning observable
     * @return 
     */
    ConsistencyLevel getDesiredConsistencyLevelAsync();

    Single<RxDocumentServiceResponse> executeQueryAsync(RxDocumentServiceRequest request);

    QueryCompatibilityMode getQueryCompatibilityMode();

    /// <summary>
    /// A client query compatibility mode when making query request.
    /// Can be used to force a specific query request format.
    /// </summary>
    enum QueryCompatibilityMode {
        /// <summary>
        /// DEFAULT (latest) query format.
        /// </summary>
        Default,

        /// <summary>
        /// Query (application/query+json).
        /// DEFAULT.
        /// </summary>
        Query,

        /// <summary>
        /// SqlQuery (application/sql).
        /// </summary>
        SqlQuery
    }

    Single<RxDocumentServiceResponse> readFeedAsync(RxDocumentServiceRequest request);
}
