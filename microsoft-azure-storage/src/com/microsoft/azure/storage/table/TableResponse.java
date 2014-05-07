/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.storage.table;

import java.net.HttpURLConnection;

import com.microsoft.azure.storage.ResultContinuation;
import com.microsoft.azure.storage.ResultContinuationType;

/**
 * Reserved for internal use. A class used to help parse responses from the Table service.
 */
class TableResponse {
    /**
     * Reserved for internal use. A static factory method that constructs a {@link ResultContinuation} instance from the
     * continuation token information in a table operation response, if any.
     * 
     * @param queryRequest
     *            The <code>java.net.HttpURLConnection<code> request response to parse for continuation token 
     *            information.
     * 
     * @return
     *         A {@link ResultContinuation} instance from continuation token information in the response, or
     *         <code>null</code> if none is found.
     */
    protected static ResultContinuation getTableContinuationFromResponse(final HttpURLConnection queryRequest) {
        final ResultContinuation retVal = new ResultContinuation();
        retVal.setContinuationType(ResultContinuationType.TABLE);

        boolean foundToken = false;

        String tString = queryRequest.getHeaderField(TableConstants.TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION
                .concat(TableConstants.TABLE_SERVICE_NEXT_PARTITION_KEY));
        if (tString != null) {
            retVal.setNextPartitionKey(tString);
            foundToken = true;
        }

        tString = queryRequest.getHeaderField(TableConstants.TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION
                .concat(TableConstants.TABLE_SERVICE_NEXT_ROW_KEY));
        if (tString != null) {
            retVal.setNextRowKey(tString);
            foundToken = true;
        }

        tString = queryRequest.getHeaderField(TableConstants.TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION
                .concat(TableConstants.TABLE_SERVICE_NEXT_MARKER));
        if (tString != null) {
            retVal.setNextMarker(tString);
            foundToken = true;
        }

        tString = queryRequest.getHeaderField(TableConstants.TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION
                .concat(TableConstants.TABLE_SERVICE_NEXT_TABLE_NAME));
        if (tString != null) {
            retVal.setNextTableName(tString);
            foundToken = true;
        }

        return foundToken ? retVal : null;
    }
}
