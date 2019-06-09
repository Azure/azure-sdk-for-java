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
package com.azure.data.cosmos.changefeed.internal;

import com.azure.data.cosmos.CosmosClientException;

/**
 * Classifies exceptions based on the status codes.
 */
public class ExceptionClassifier {
    public static final int SubStatusCode_Undefined = -1;

    // 410: partition key range is gone.
    public static final int SubStatusCode_PartitionKeyRangeGone = 1002;

    // 410: partition splitting.
    public static final int SubStatusCode_Splitting = 1007;

    // 404: LSN in session token is higher.
    public static final int SubStatusCode_ReadSessionNotAvailable = 1002;


    public static StatusCodeErrorType classifyClientException(CosmosClientException clientException) {
        Integer subStatusCode = clientException.subStatusCode();

        if (clientException.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND && subStatusCode != SubStatusCode_ReadSessionNotAvailable)
            return StatusCodeErrorType.PARTITION_NOT_FOUND;

        if (clientException.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_GONE && (subStatusCode == SubStatusCode_PartitionKeyRangeGone || subStatusCode == SubStatusCode_Splitting))
            return StatusCodeErrorType.PARTITION_SPLIT;

        if (clientException.statusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_TOO_MANY_REQUESTS || clientException.statusCode() >= ChangeFeedHelper.HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR)
            return StatusCodeErrorType.TRANSIENT_ERROR;

        // Temporary workaround to compare exception message, until server provides better way of handling this case.
        if (clientException.getMessage().contains("Reduce page size and try again."))
            return StatusCodeErrorType.MAX_ITEM_COUNT_TOO_LARGE;

        return StatusCodeErrorType.UNDEFINED;

    }
}
