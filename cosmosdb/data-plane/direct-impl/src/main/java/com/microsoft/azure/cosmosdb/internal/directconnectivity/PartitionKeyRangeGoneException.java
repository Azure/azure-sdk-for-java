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

import com.microsoft.azure.cosmosdb.BridgeInternal;
import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.Error;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;
import com.microsoft.azure.cosmosdb.rx.internal.RMResources;
import io.reactivex.netty.protocol.http.client.HttpResponseHeaders;
import org.apache.commons.collections4.CollectionUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This exception is thrown when DocumentServiceRequest contains x-ms-documentdb-partitionkeyrangeid
 * header and such range id doesn't exist.
 * <p>
 * No retries should be made in this case, as either split or merge might have happened and query/readfeed
 * must take appropriate actions.
 */
public class PartitionKeyRangeGoneException extends DocumentClientException {

    public PartitionKeyRangeGoneException() {
        this(RMResources.Gone);
    }

    public PartitionKeyRangeGoneException(Error error, long lsn, String partitionKeyRangeId, Map<String, String> responseHeaders) {
        super(HttpConstants.StatusCodes.GONE, error, responseHeaders);
        BridgeInternal.setLSN(this, lsn);
        BridgeInternal.setPartitionKeyRangeId(this, partitionKeyRangeId);
        this.setSubstatus();
    }

    public PartitionKeyRangeGoneException(String message) {
        this(message, (Exception) null, null, null);
    }

    public PartitionKeyRangeGoneException(String message, Exception innerException) {
        this(message, innerException, (HttpResponseHeaders) null, null);
    }

    public PartitionKeyRangeGoneException(Exception innerException) {
        this(RMResources.Gone, innerException, (HttpResponseHeaders) null, null);
    }


    public PartitionKeyRangeGoneException(String message, HttpResponseHeaders headers, String requestUri) {
        super(message, null, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUri);
        this.setSubstatus();
    }

    public PartitionKeyRangeGoneException(String message, Exception innerException, HttpResponseHeaders headers, String requestUri) {
        super(message, innerException, HttpUtils.asMap(headers), HttpConstants.StatusCodes.GONE, requestUri);
        this.setSubstatus();
    }

    private void setSubstatus() {
        this.getResponseHeaders().put(WFConstants.BackendHeaders.SUB_STATUS, Integer.toString(HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE));
    }
}
