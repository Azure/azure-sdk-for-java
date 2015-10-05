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

/**
 * RESERVED FOR INTERNAL USE. Holds the constants used for the Table Service.
 */
final class TableConstants {
    public static class ErrorConstants {
        /**
         * XML element for error codes.
         */
        public static final String ERROR_CODE = "code";

        /**
         * XML element for stack traces.
         */
        public static final String ERROR_EXCEPTION_STACK_TRACE = "stacktrace";

        /**
         * XML element for error type
         */
        public static final String ERROR_EXCEPTION_TYPE = "type";

        /**
         * XML element for error messages.
         */
        public static final String ERROR_MESSAGE = "message";

        /**
         * XML root element for errors.
         */
        public static final String ERROR_ROOT_ELEMENT = "error";

        /**
         * XML element for inner error details
         */
        public static final String INNER_ERROR = "innererror";
    }

    /**
     * The constants used in HTML header fields for Table service requests.
     */
    public static class HeaderConstants {
        /**
         * The Content-ID header value to send for batch operations.
         */
        public static final String CONTENT_ID = "Content-ID";

        /**
         * The DataServiceVersion header field label.
         */
        public static final String DATA_SERVICE_VERSION = "DataServiceVersion";

        /**
         * The DataServiceVersion header value to send.
         */
        public static final String DATA_SERVICE_VERSION_VALUE = "3.0";

        /**
         * The ETag header field label.
         */
        public static final String ETAG = "ETag";

        /**
         * The Accept header value to send for JsonLight.
         */
        public static final String JSON_ACCEPT_TYPE = "application/json;odata=minimalmetadata";

        /**
         * The Content-Type header value to send for JSON.
         */
        public static final String JSON_CONTENT_TYPE = "application/json";

        /**
         * The Accept header value to send for JsonFullMetadata.
         */
        public static final String JSON_FULL_METADATA_ACCEPT_TYPE = "application/json;odata=fullmetadata";

        /**
         * The Accept header value to send for JsonNoMetadata.
         */
        public static final String JSON_NO_METADATA_ACCEPT_TYPE = "application/json;odata=nometadata";

        /**
         * The MaxDataServiceVersion header field label.
         */
        public static final String MAX_DATA_SERVICE_VERSION = "MaxDataServiceVersion";

        /**
         * The MaxDataServiceVersion header value to send.
         */
        public static final String MAX_DATA_SERVICE_VERSION_VALUE = "3.0";

        /**
         * The Content-Type header value to send for batch operations.
         */
        public static final String MULTIPART_MIXED_FORMAT = "multipart/mixed; boundary=%s";

        /**
         * The Prefer header field label for inserts
         */
        public static final String PREFER = "Prefer";

        /**
         * The Prefer header field value to send for content return on inserts
         */
        public static final String RETURN_CONTENT = "return-content";

        /**
         * The Prefer header field value to send for no content return on inserts
         */
        public static final String RETURN_NO_CONTENT = "return-no-content";

        /**
         * The x-http-method header to send for batch operations.
         */
        public static final String X_HTTP_METHOD = "X-HTTP-Method";

    }

    /**
     * The prefix used in all ETags.
     */
    protected static final String ETAG_PREFIX = "\"datetime'";

    /**
     * The query filter clause name.
     */
    public static final String FILTER = "$filter";

    /**
     * The name of the partition key property.
     */
    public static final String PARTITION_KEY = "PartitionKey";

    /**
     * The name of the row key property.
     */
    public static final String ROW_KEY = "RowKey";

    /**
     * The query select clause name.
     */
    public static final String SELECT = "$select";

    /**
     * The name of the property that stores the table name.
     */
    public static final String TABLE_NAME = "TableName";

    /**
     * Stores the header suffix for the next marker.
     */
    public static final String TABLE_SERVICE_NEXT_MARKER = "NextMarker";

    /**
     * Stores the header suffix for the next partition key.
     */
    public static final String TABLE_SERVICE_NEXT_PARTITION_KEY = "NextPartitionKey";

    /**
     * Stores the header suffix for the next row key.
     */
    public static final String TABLE_SERVICE_NEXT_ROW_KEY = "NextRowKey";

    /**
     * Stores the table suffix for the next table name.
     */
    public static final String TABLE_SERVICE_NEXT_TABLE_NAME = "NextTableName";

    /**
     * Stores the header prefix for continuation information.
     */
    public static final String TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION = "x-ms-continuation-";

    /**
     * The name of the special table used to store tables.
     */
    public static final String TABLES_SERVICE_TABLES_NAME = "Tables";

    /**
     * The name of the Timestamp property.
     */
    public static final String TIMESTAMP = "Timestamp";

    /**
     * The query top clause name.
     */
    public static final String TOP = "$top";

    /**
     * Private Default Constructor.
     */
    private TableConstants() {
        // No op
    }
}
