/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.table.client;

/**
 * Holds the constants used for the Table Service.
 */
public final class TableConstants {
    /**
     * The constants used in HTML header fields for Table service requests.
     */
    public static class HeaderConstants {
        /**
         * The ETag header field label.
         */
        public static final String ETAG = "ETag";

        /**
         * The Accept header value to send.
         */
        public static final String ACCEPT_TYPE = "application/atom+xml,application/xml";

        /**
         * The Content-Type header value to send for single operations.
         */
        public static final String ATOMPUB_TYPE = "application/atom+xml";

        /**
         * The Content-Type header value to send for batch operations.
         */
        public static final String MULTIPART_MIXED_FORMAT = "multipart/mixed; boundary=%s";

        /**
         * The DataServiceVersion header field label.
         */
        public static final String DATA_SERVICE_VERSION = "DataServiceVersion";

        /**
         * The DataServiceVersion header value to send.
         */
        public static final String DATA_SERVICE_VERSION_VALUE = "1.0;NetFx";

        /**
         * The MaxDataServiceVersion header field label.
         */
        public static final String MAX_DATA_SERVICE_VERSION = "MaxDataServiceVersion";

        /**
         * The MaxDataServiceVersion header value to send.
         */
        public static final String MAX_DATA_SERVICE_VERSION_VALUE = "2.0;NetFx";
    }

    /**
     * Default client side timeout, in milliseconds, for table clients.
     */
    public static final int TABLE_DEFAULT_TIMEOUT_IN_MS = 60 * 1000;

    /**
     * Stores the header prefix for continuation information.
     */
    public static final String TABLE_SERVICE_PREFIX_FOR_TABLE_CONTINUATION = "x-ms-continuation-";

    /**
     * Stores the header suffix for the next partition key.
     */
    public static final String TABLE_SERVICE_NEXT_PARTITION_KEY = "NextPartitionKey";

    /**
     * Stores the header suffix for the next row key.
     */
    public static final String TABLE_SERVICE_NEXT_ROW_KEY = "NextRowKey";

    /**
     * Stores the header suffix for the next marker.
     */
    public static final String TABLE_SERVICE_NEXT_MARKER = "NextMarker";

    /**
     * Stores the table suffix for the next table name.
     */
    public static final String TABLE_SERVICE_NEXT_TABLE_NAME = "NextTableName";

    /**
     * The name of the partition key property.
     */
    public static final String PARTITION_KEY = "PartitionKey";

    /**
     * The name of the row key property.
     */
    public static final String ROW_KEY = "RowKey";

    /**
     * The name of the Timestamp property.
     */
    public static final String TIMESTAMP = "Timestamp";

    /**
     * The name of the special table used to store tables.
     */
    public static final String TABLES_SERVICE_TABLES_NAME = "Tables";

    /**
     * The name of the property that stores the table name.
     */
    public static final String TABLE_NAME = "TableName";

    /**
     * The query filter clause name.
     */
    public static final String FILTER = "$filter";

    /**
     * The query top clause name.
     */
    public static final String TOP = "$top";

    /**
     * The query select clause name.
     */
    public static final String SELECT = "$select";

    /**
     * Private Default Constructor.
     */
    private TableConstants() {
        // No op
    }
}
