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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.storage.core.SR;

/**
 * Reserved for internal use. A class that represents a given MIME Part.
 */
final class MimePart {
    int httpStatusCode = -1;
    String httpStatusMessage;
    HashMap<String, String> headers = new HashMap<String, String>();
    String payload;
    TableOperationType op;
    URI requestIdentity;

    String toRequestString() {
        StringBuilder builder = new StringBuilder();

        // append mime part header
        appendHeader(builder);

        builder.append(String.format("%s %s HTTP/1.1\r\n", getHttpVerbForOperation(this.op),
                this.requestIdentity.toString()));

        for (Map.Entry<String, String> header : this.headers.entrySet()) {
            builder.append(String.format("%s: %s\r\n", header.getKey(), header.getValue()));
        }

        builder.append("\r\n");

        if (this.payload != null) {
            builder.append(this.payload);
        }

        return builder.toString();
    }

    static void appendHeader(StringBuilder builder) {
        builder.append("Content-Type: application/http\r\n");
        builder.append("Content-Transfer-Encoding: binary\r\n\r\n");
    }

    /**
     * Reserved for internal use. Returns the HTTP verb for a table operation.
     * 
     * @param operationType
     *            The {@link TableOperation} instance to get the HTTP verb for.
     * @return
     *         A <code>String</code> containing the HTTP verb to use with the operation.
     */
    static String getHttpVerbForOperation(final TableOperationType operationType) {
        if (operationType == TableOperationType.INSERT) {
            return "POST";
        }
        else if (operationType == TableOperationType.DELETE) {
            return "DELETE";
        }
        else if (operationType == TableOperationType.MERGE || operationType == TableOperationType.INSERT_OR_MERGE) {
            return "MERGE";
        }
        else if (operationType == TableOperationType.REPLACE || operationType == TableOperationType.INSERT_OR_REPLACE) {
            return "PUT";
        }
        else if (operationType == TableOperationType.RETRIEVE) {
            return "GET";
        }
        else {
            throw new IllegalArgumentException(SR.UNKNOWN_TABLE_OPERATION);
        }
    }
}
