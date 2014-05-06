/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.core;

import java.util.Iterator;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ResultSegment;
import com.microsoft.azure.storage.RetryPolicyFactory;

/**
 * RESERVED FOR INTERNAL USE. Provides a lazy iterator which will retrieve the next segment of a result as the iterator
 * is consumed
 * 
 * @param <CLIENT_TYPE>
 *            The service client type
 * @param <PARENT_TYPE>
 *            The type of the parent object, i.e. CloudBlobClient for ListContainers etc.
 * @param <ENTITY_TYPE>
 *            The type of the objects the resulting iterable objects
 */
public final class LazySegmentedIterable<CLIENT_TYPE, PARENT_TYPE, ENTITY_TYPE> implements Iterable<ENTITY_TYPE> {
    /**
     * Holds the service client associated with the operations.
     */
    private final CLIENT_TYPE client;

    /**
     * Holds a reference to the parent object, i.e. CloudBlobContainer for list blobs.
     */
    private final PARENT_TYPE parentObject;

    /**
     * Holds the reference to the RetryPolicyFactory object.
     */
    private final RetryPolicyFactory policyFactory;

    /**
     * Holds the StorageRequest which is used to retrieve the next segment of results.
     */
    private final StorageRequest<CLIENT_TYPE, PARENT_TYPE, ResultSegment<ENTITY_TYPE>> segmentGenerator;

    /**
     * Holds an object used to track the execution of the operation
     */
    private final OperationContext opContext;

    public LazySegmentedIterable(
            final StorageRequest<CLIENT_TYPE, PARENT_TYPE, ResultSegment<ENTITY_TYPE>> segmentGenerator,
            final CLIENT_TYPE client, final PARENT_TYPE parent, final RetryPolicyFactory policyFactory,
            final OperationContext opContext) {
        this.segmentGenerator = segmentGenerator;
        this.parentObject = parent;
        this.opContext = opContext;
        this.policyFactory = policyFactory;
        this.client = client;
    }

    @Override
    public Iterator<ENTITY_TYPE> iterator() {
        return new LazySegmentedIterator<CLIENT_TYPE, PARENT_TYPE, ENTITY_TYPE>(this.segmentGenerator, this.client,
                this.parentObject, this.policyFactory, this.opContext);
    }
}
