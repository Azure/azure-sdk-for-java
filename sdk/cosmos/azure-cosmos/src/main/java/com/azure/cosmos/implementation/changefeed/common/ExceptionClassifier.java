// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;

/**
 * Classifies exceptions based on the status codes.
 */
public class ExceptionClassifier {
    public static final int SubStatusCode_Undefined = -1;

    // 410: partition key range is gone.
    public static final int SubStatusCode_PartitionKeyRangeGone = 1002;

    // 410: split and merge use the same exception status code and substatus code.
    public static final int SubStatusCode_Splitting_Or_Merging = 1007;

    // 404: LSN in session token is higher.
    public static final int SubStatusCode_ReadSessionNotAvailable = 1002;


    public static StatusCodeErrorType classifyClientException(CosmosException clientException) {
        int subStatusCode = clientException.getSubStatusCode();

        if (clientException.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_NOT_FOUND && subStatusCode != SubStatusCode_ReadSessionNotAvailable) {
            return StatusCodeErrorType.PARTITION_NOT_FOUND;
        }

        if (clientException.getStatusCode() == HttpConstants.StatusCodes.INTERNAL_SERVER_ERROR) {
            if (subStatusCode == HttpConstants.SubStatusCodes.JACKSON_STREAMS_CONSTRAINED) {
                return StatusCodeErrorType.JACKSON_STREAMS_CONSTRAINED;
            } else if (subStatusCode == HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE) {
                return StatusCodeErrorType.JSON_PARSING_ERROR;
            }
        }

        if (clientException.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_GONE
                && (subStatusCode == SubStatusCode_PartitionKeyRangeGone || subStatusCode == SubStatusCode_Splitting_Or_Merging)) {
            return StatusCodeErrorType.PARTITION_SPLIT_OR_MERGE;
        }

        if (clientException.getStatusCode() == ChangeFeedHelper.HTTP_STATUS_CODE_TOO_MANY_REQUESTS || clientException.getStatusCode() >= ChangeFeedHelper.HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR) {
            return StatusCodeErrorType.TRANSIENT_ERROR;
        }

        // Temporary workaround to compare exception message, until server provides better way of handling this case.
        if (clientException.getMessage().contains("Reduce page size and try again.")) {
            return StatusCodeErrorType.MAX_ITEM_COUNT_TOO_LARGE;
        }

        return StatusCodeErrorType.UNDEFINED;
    }
}
