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

import com.microsoft.azure.cosmosdb.DocumentClientException;
import com.microsoft.azure.cosmosdb.internal.HttpConstants;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public class Exceptions {

    public static boolean isStatusCode(DocumentClientException e, int status) {
        return status == e.getStatusCode();
    }

    public static boolean isSubStatusCode(DocumentClientException e, int subStatus) {
        return subStatus == e.getSubStatusCode();
    }

    public static boolean isPartitionSplit(DocumentClientException e) {
        return isStatusCode(e, HttpConstants.StatusCodes.GONE)
                && isSubStatusCode(e, HttpConstants.SubStatusCodes.PARTITION_KEY_RANGE_GONE);
    }

    public static boolean isNameCacheStale(DocumentClientException e) {
        return isStatusCode(e, HttpConstants.StatusCodes.GONE)
                && isSubStatusCode(e, HttpConstants.SubStatusCodes.NAME_CACHE_IS_STALE);
    }
}
