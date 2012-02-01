/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import com.microsoft.windowsazure.services.core.storage.RequestOptions;
import com.microsoft.windowsazure.services.core.storage.ResultContinuation;

/**
 * RESERVED FOR INTERNAL USE. A base class which encapsulate the execution of a given segmented storage operation.
 * 
 * @param <C>
 *            The service client type
 * @param <P>
 *            The type of the parent object, i.e. CloudBlobContainer for downloadAttributes etc.
 * @param <R>
 *            The type of the expected result
 */
public abstract class SegmentedStorageOperation<C, P, R> extends StorageOperation<C, P, R> {

    /**
     * Holds the ResultContinuation between executions.
     */
    private ResultContinuation token;

    /**
     * Initializes a new instance of the SegmentedStorageOperation class.
     * 
     * @param options
     *            the RequestOptions to use
     */
    public SegmentedStorageOperation(final RequestOptions options) {
        super(options);
    }

    /**
     * Initializes a new instance of the SegmentedStorageOperation class.
     * 
     * @param options
     *            the RequestOptions to use
     * @param token
     *            the ResultContinuation to use
     */
    public SegmentedStorageOperation(final RequestOptions options, final ResultContinuation token) {
        super(options);
        this.setToken(token);
    }

    /**
     * @return the token.
     */
    protected final ResultContinuation getToken() {
        return this.token;
    }

    /**
     * @param token
     *            the token to set.
     */
    protected final void setToken(final ResultContinuation token) {
        this.token = token;
    }
}
