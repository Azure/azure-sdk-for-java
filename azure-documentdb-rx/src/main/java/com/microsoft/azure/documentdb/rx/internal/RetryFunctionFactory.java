/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx.internal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.documentdb.DocumentClientException;

import rx.Observable;
import rx.functions.Func1;

class RetryFunctionFactory {

    private final static Logger LOGGER = LoggerFactory.getLogger(RetryFunctionFactory.class);
    
    // this is just a safe guard, to ensure even if the retry policy doesn't give up we avoid infinite retries.
    private final static int MAX_RETRIES_LIMIT = 200;
    
    public static Func1<Observable<? extends Throwable>, Observable<Long>> from(RxRetryHandler retryPolicy) {
        return new Func1<Observable<? extends Throwable>, Observable<Long>>() {

            @Override
            public Observable<Long> call(final Observable<? extends Throwable> failures) {

                return failures
                        .zipWith(Observable.range(1, MAX_RETRIES_LIMIT),
                                (err, attempt) ->
                        attempt < MAX_RETRIES_LIMIT ?
                                handleRetryAttempt(err, attempt, retryPolicy) :
                                    Observable.<Long>error(extractDocumentClientCause(err, attempt)) )
                        .flatMap(x -> x);
            }
        };
    }
        
    private static Throwable extractDocumentClientCause(Throwable t, int attemptNumber) {
        if (t instanceof DocumentClientException) {
            return t;
        } else if (t instanceof RuntimeException && t.getCause() instanceof DocumentClientException) {
            return t.getCause();
        } else {
            LOGGER.warn("unknown failure, cannot retry [{}], attempt number [{}]", t.getMessage(), attemptNumber, t);
            return t;
        }
    }

    private static Observable<Long> handleRetryAttempt(Throwable t, int attemptNumber, RxRetryHandler retryPolicy) {
        Throwable cause = extractDocumentClientCause(t, attemptNumber);
        
        if (LOGGER.isDebugEnabled()) {
            if (cause instanceof DocumentClientException) {
                DocumentClientException ex = (DocumentClientException) cause;
                LOGGER.debug("Handling Failure Attempt [{}], StatusCode [{}], SubStatusCode,"
                        + " Error: [{}] ", attemptNumber, ex.getStatusCode(), ex.getSubStatusCode(), ex.getError(), ex);
            } else {
                LOGGER.debug("Handling Failure Attempt [{}], req [{}]", attemptNumber, cause);
            }
        }
        
        try {
            return retryPolicy.handleRetryAttempt(cause, attemptNumber);
        } catch (Exception e) {
            return Observable.error(e);
        }
    }
}
