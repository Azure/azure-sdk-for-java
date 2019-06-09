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
package com.azure.data.cosmos.changefeed;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Used to estimate the pending work remaining to be read in the Change Feed. Calculates the sum of pending work
 *   based on the difference between the latest status of the feed and the status of each existing lease.
 */
public interface RemainingWorkEstimator {
    /**
     * Calculates an estimate of the pending work remaining to be read in the Change Feed in amount of documents in the whole collection.
     *
     * @return an estimation of pending work in amount of documents.
     */
    Mono<Long> getEstimatedRemainingWork();

    /**
     * Calculates an estimate of the pending work remaining to be read in the Change Feed in amount of documents per partition.
     *
     * @return an estimation of pending work in amount of documents per partitions.
     */
    Flux<RemainingPartitionWork> getEstimatedRemainingWorkPerPartition();
}
