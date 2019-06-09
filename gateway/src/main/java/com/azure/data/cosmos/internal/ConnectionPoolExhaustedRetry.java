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

package com.azure.data.cosmos.internal;

import io.reactivex.netty.client.PoolExhaustedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.time.Duration;

// rxnetty in servicing a new request throws PoolExhaustedException
// if all connections are in used and max connection pool size is configured.
class ConnectionPoolExhaustedRetry implements IDocumentClientRetryPolicy {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolExhaustedRetry.class);
    static final Duration RETRY_WAIT_TIME = Duration.ofMillis(10);
    static final int MAX_RETRY_COUNT = 10;

    private int retryCount = 0;

    @Override
    public Single<ShouldRetryResult> shouldRetry(Exception e) {
        boolean isConnectionPoolExhaustedException = isConnectionPoolExhaustedException(e);
        assert isConnectionPoolExhaustedException;
        if (!isConnectionPoolExhaustedException) {
            logger.error("Fatal error invalid retry path for {}", e.getMessage(), e);
            return Single.just(ShouldRetryResult.error(e));
        }

        if (++retryCount <= MAX_RETRY_COUNT) {
            logger.warn("PoolExhaustedException failure indicates" +
                                 " the load on the SDK is higher than what current connection pool size can support" +
                                 " either increase the connection pool size for the configured connection mode," +
                                 " or distribute the load on more machines. retry count {}", retryCount);
            return Single.just(ShouldRetryResult.retryAfter(RETRY_WAIT_TIME));
        } else {
            logger.error("PoolExhaustedException failure indicates" +
                                 " the load on the SDK is higher than what current connection pool size can support" +
                                 " either increase the connection pool size for the configured connection mode," +
                                 " or distribute the load on more machines. ALL retries exhausted!");
            return Single.just(ShouldRetryResult.error(e));
        }
    }

    @Override
    public void onBeforeSendRequest(RxDocumentServiceRequest request) {
        // no op
    }

    static boolean isConnectionPoolExhaustedException(Exception ex) {
        while (ex != null) {
            if (ex instanceof PoolExhaustedException) {
                return true;
            }

            Throwable t = ex.getCause();
            if (!(t instanceof Exception)) {
                break;
            }

            ex = (Exception) t;
        }

        return false;
    }
}
