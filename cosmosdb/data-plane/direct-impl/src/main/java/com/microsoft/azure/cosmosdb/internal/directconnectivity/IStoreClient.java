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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import com.microsoft.azure.cosmosdb.rx.internal.IRetryPolicy;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;
import rx.Single;
import rx.functions.Func1;

public interface IStoreClient {

    Single<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request,
            IRetryPolicy retryPolicy,
            Func1<RxDocumentServiceRequest, Single<RxDocumentServiceRequest>> prepareRequestAsyncDelegate);

    default Single<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request,
            Func1<RxDocumentServiceRequest, Single<RxDocumentServiceRequest>> prepareRequestAsyncDelegate) {
        return processMessageAsync(request, null, prepareRequestAsyncDelegate);
    }

    default Single<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request,
            IRetryPolicy retryPolicy) {
        return processMessageAsync(request, retryPolicy, null);
    }

    default Single<RxDocumentServiceResponse> processMessageAsync(
            RxDocumentServiceRequest request) {
        return processMessageAsync(request, null, null);
    }
}
