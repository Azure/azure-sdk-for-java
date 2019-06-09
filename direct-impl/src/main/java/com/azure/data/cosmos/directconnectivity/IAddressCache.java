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

package com.azure.data.cosmos.directconnectivity;

import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.routing.PartitionKeyRangeIdentity;
import rx.Single;

public interface IAddressCache {

    /**
     * Resolves physical addresses by either PartitionKeyRangeIdentity.
     *
     *
     * @param request Request is needed only by GatewayAddressCache in the only case when request is name based and user has name based auth token.
     *                PartitionkeyRangeIdentity can be used to locate auth token in this case.
     * @param partitionKeyRangeIdentity target partition key range Id
     * @param forceRefreshPartitionAddresses Whether addresses need to be refreshed as previously resolved addresses were determined to be outdated.
     * @return Physical addresses.
     */
    Single<AddressInformation[]> tryGetAddresses(
            RxDocumentServiceRequest request,
            PartitionKeyRangeIdentity partitionKeyRangeIdentity,
            boolean forceRefreshPartitionAddresses);
}
