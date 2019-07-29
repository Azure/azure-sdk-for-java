// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.changefeed.implementation;

import com.azure.data.cosmos.CosmosClientException;

/**
 * Classifies exceptions based on the status codes.
 */
class ExceptionClassifier {
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
