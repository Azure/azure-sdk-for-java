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


import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.BadRequestException;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceRequest;
import com.microsoft.azure.cosmosdb.rx.internal.RxDocumentServiceResponse;
import com.microsoft.azure.cosmosdb.rx.internal.RxStoreModel;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import org.apache.commons.lang3.EnumUtils;
import rx.Observable;
import rx.Single;

public class ServerStoreModel implements RxStoreModel {
    private final StoreClient storeClient;

    public ServerStoreModel(StoreClient storeClient) {
        this.storeClient = storeClient;
    }

    public Observable<RxDocumentServiceResponse> processMessage(RxDocumentServiceRequest request) {
        String requestConsistencyLevelHeaderValue = request.getHeaders().get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL);

        request.requestContext.originalRequestConsistencyLevel = null;

        if (!Strings.isNullOrEmpty(requestConsistencyLevelHeaderValue)) {
            ConsistencyLevel requestConsistencyLevel;

            if ((requestConsistencyLevel = EnumUtils.getEnum(ConsistencyLevel.class, requestConsistencyLevelHeaderValue)) == null) {
                return Observable.error(new BadRequestException(
                    String.format(
                        RMResources.InvalidHeaderValue,
                        requestConsistencyLevelHeaderValue,
                        HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)));
            }

            request.requestContext.originalRequestConsistencyLevel = requestConsistencyLevel;
        }

        if (ReplicatedResourceClient.isMasterResource(request.getResourceType())) {
            request.getHeaders().put(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL, ConsistencyLevel.Strong.toString());
        }

        Single<RxDocumentServiceResponse> response = this.storeClient.processMessageAsync(request);
        return response.toObservable();
    }
}
