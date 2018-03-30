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
package com.microsoft.azure.cosmosdb.rx.internal;

import rx.Single;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public interface IDocumentClientRetryPolicy extends IRetryPolicy {

    // TODO: this is just a place holder for now. As .Net has this method.
    // I have to spend more time figure out what's the right pattern for this (if method needed)

    /// <summary>
    /// Method that is called before a request is sent to allow the retry policy implementation
    /// to modify the state of the request.
    /// </summary>
    /// <param name="request">The request being sent to the service.</param>
    /// <remarks>
    /// Currently only read operations will invoke this method. There is no scenario for write
    /// operations to modify requests before retrying.
    /// </remarks>

    // TODO: I need to investigate what's the right contract here and/or if/how this is useful
    public void onBeforeSendRequest(RxDocumentServiceRequest request);


    public static class NoRetry implements IDocumentClientRetryPolicy {

        private static NoRetry instance = new NoRetry();

        private NoRetry() {}

        public static NoRetry getInstance() {
            return instance;
        }

        @Override
        public void onBeforeSendRequest(RxDocumentServiceRequest request) {
            // no op
        }

        @Override
        public Single<ShouldRetryResult> shouldRetry(Exception e) {
            return Single.just(ShouldRetryResult.error(e));
        }
    }
}
