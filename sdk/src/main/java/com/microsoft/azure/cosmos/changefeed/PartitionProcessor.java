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
package com.microsoft.azure.cosmos.changefeed;

import com.microsoft.azure.cosmos.ChangeFeedObserver;
import com.microsoft.azure.cosmos.ChangeFeedObserverContext;
import reactor.core.publisher.Mono;

/**
 * Provides an API to run continious processing on a single partition of some resource.
 * <p>
 * Created by {@link PartitionProcessorFactory}.create() after some lease is acquired by the current host.
 *  Processing can perform the following tasks in a loop:
 *    1. Read some data from the resource partition.
 *    2. Handle possible problems with the read.
 *    3. Pass the obtained data to an observer by calling {@link ChangeFeedObserver}.processChangesAsync{} with the context {@link ChangeFeedObserverContext}.
 */
public interface PartitionProcessor {
    /**
     * Perform partition processing.
     *
     * @param cancellationToken the cancellation token.
     * @return a representation of the deferred computation of this call.
     */
    Mono<Void> run(CancellationToken cancellationToken);

    /**
     * @return the inner exception if any, otherwise null.
     */
    RuntimeException getResultException();
}
