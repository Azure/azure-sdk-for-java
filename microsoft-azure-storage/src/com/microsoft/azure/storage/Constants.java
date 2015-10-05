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
package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.file.FileInputStream;
import com.microsoft.azure.storage.file.FileOutputStream;

/**
 * RESERVED FOR INTERNAL USE. Contains storage constants.
 */
public final class Constants {
    /**
     * Defines constants for ServiceProperties requests.
     */
    public static class AnalyticsConstants {

        /**
         * The XML element for the CORS Rule AllowedHeaders
         */
        public static final String ALLOWED_HEADERS_ELEMENT = "AllowedHeaders";

        /**
         * The XML element for the CORS Rule AllowedMethods
         */
        public static final String ALLOWED_METHODS_ELEMENT = "AllowedMethods";

        /**
         * The XML element for the CORS Rule AllowedOrigins
         */
        public static final String ALLOWED_ORIGINS_ELEMENT = "AllowedOrigins";

        /**
         * The XML element for the CORS
         */
        public static final String CORS_ELEMENT = "Cors";

        /**
         * The XML element for the CORS Rules
         */
        public static final String CORS_RULE_ELEMENT = "CorsRule";

        /**
         * The XML element for the RetentionPolicy Days.
         */
        public static final String DAYS_ELEMENT = "Days";

        /**
         * The XML element for the Default Service Version.
         */
        public static final String DEFAULT_SERVICE_VERSION = "DefaultServiceVersion";

        /**
         * The XML element for the Logging Delete type.
         */
        public static final String DELETE_ELEMENT = "Delete";

        /**
         * The XML element for the RetentionPolicy Enabled.
         */
        public static final String ENABLED_ELEMENT = "Enabled";

        /**
         * The XML element for the CORS Rule ExposedHeaders
         */
        public static final String EXPOSED_HEADERS_ELEMENT = "ExposedHeaders";

        /**
         * The XML element for the Hour Metrics
         */
        public static final String HOUR_METRICS_ELEMENT = "HourMetrics";

        /**
         * The XML element for the Metrics IncludeAPIs.
         */
        public static final String INCLUDE_APIS_ELEMENT = "IncludeAPIs";

        /**
         * Constant for the logs container.
         */
        public static final String LOGS_CONTAINER = "$logs";

        /**
         * The XML element for the Logging
         */
        public static final String LOGGING_ELEMENT = "Logging";

        /**
         * The XML element for the CORS Rule MaxAgeInSeconds
         */
        public static final String MAX_AGE_IN_SECONDS_ELEMENT = "MaxAgeInSeconds";

        /**
         * Constant for the blob capacity metrics table.
         */
        public static final String METRICS_CAPACITY_BLOB = "$MetricsCapacityBlob";

        /**
         * Constant for the blob service primary location hourly metrics table.
         */
        public static final String METRICS_HOUR_PRIMARY_TRANSACTIONS_BLOB = "$MetricsHourPrimaryTransactionsBlob";

        /**
         * Constant for the file service primary location hourly metrics table.
         */
        public static final String METRICS_HOUR_PRIMARY_TRANSACTIONS_FILE = "$MetricsHourPrimaryTransactionsFile";

        /**
         * Constant for the table service primary location hourly metrics table.
         */
        public static final String METRICS_HOUR_PRIMARY_TRANSACTIONS_TABLE = "$MetricsHourPrimaryTransactionsTable";

        /**
         * Constant for the queue service primary location hourly metrics table.
         */
        public static final String METRICS_HOUR_PRIMARY_TRANSACTIONS_QUEUE = "$MetricsHourPrimaryTransactionsQueue";

        /**
         * Constant for the blob service primary location minute metrics table.
         */
        public static final String METRICS_MINUTE_PRIMARY_TRANSACTIONS_BLOB = "$MetricsMinutePrimaryTransactionsBlob";

        /**
         * Constant for the file service primary location minute metrics table.
         */
        public static final String METRICS_MINUTE_PRIMARY_TRANSACTIONS_FILE = "$MetricsMinutePrimaryTransactionsFile";

        /**
         * Constant for the table service primary location minute metrics table.
         */
        public static final String METRICS_MINUTE_PRIMARY_TRANSACTIONS_TABLE = "$MetricsMinutePrimaryTransactionsTable";

        /**
         * Constant for the queue service primary location minute metrics table.
         */
        public static final String METRICS_MINUTE_PRIMARY_TRANSACTIONS_QUEUE = "$MetricsMinutePrimaryTransactionsQueue";

        /**
         * Constant for the blob service secondary location hourly metrics table.
         */
        public static final String METRICS_HOUR_SECONDARY_TRANSACTIONS_BLOB = "$MetricsHourSecondaryTransactionsBlob";

        /**
         * Constant for the file service secondary location hourly metrics table.
         */
        public static final String METRICS_HOUR_SECONDARY_TRANSACTIONS_FILE = "$MetricsHourSecondaryTransactionsFile";

        /**
         * Constant for the table service secondary location hourly metrics table.
         */
        public static final String METRICS_HOUR_SECONDARY_TRANSACTIONS_TABLE = "$MetricsHourSecondaryTransactionsTable";

        /**
         * Constant for the queue service secondary location hourly metrics table.
         */
        public static final String METRICS_HOUR_SECONDARY_TRANSACTIONS_QUEUE = "$MetricsHourSecondaryTransactionsQueue";

        /**
         * Constant for the blob service secondary location minute metrics table.
         */
        public static final String METRICS_MINUTE_SECONDARY_TRANSACTIONS_BLOB = "$MetricsMinuteSecondaryTransactionsBlob";

        /**
         * Constant for the file service secondary location minute metrics table.
         */
        public static final String METRICS_MINUTE_SECONDARY_TRANSACTIONS_FILE = "$MetricsMinuteSecondaryTransactionsFile";

        /**
         * Constant for the table service secondary location minute metrics table.
         */
        public static final String METRICS_MINUTE_SECONDARY_TRANSACTIONS_TABLE = "$MetricsMinuteSecondaryTransactionsTable";

        /**
         * Constant for the queue service secondary location minute metrics table.
         */
        public static final String METRICS_MINUTE_SECONDARY_TRANSACTIONS_QUEUE = "$MetricsMinuteSecondaryTransactionsQueue";

        /**
         * The XML element for the Minute Metrics
         */
        public static final String MINUTE_METRICS_ELEMENT = "MinuteMetrics";

        /**
         * The XML element for the Logging Read type.
         */
        public static final String READ_ELEMENT = "Read";

        /**
         * The XML element for the RetentionPolicy.
         */
        public static final String RETENTION_POLICY_ELEMENT = "RetentionPolicy";

        /**
         * The XML element for the StorageServiceProperties
         */
        public static final String STORAGE_SERVICE_PROPERTIES_ELEMENT = "StorageServiceProperties";

        /**
         * The XML element for the StorageServiceStats
         */
        public static final String STORAGE_SERVICE_STATS = "StorageServiceStats";

        /**
         * The XML element for the Version
         */
        public static final String VERSION_ELEMENT = "Version";

        /**
         * The XML element for the Logging Write type.
         */
        public static final String WRITE_ELEMENT = "Write";

    }

    /**
     * Defines constants for use with HTTP headers.
     */
    public static class HeaderConstants {
        /**
         * The Accept header.
         */
        public static final String ACCEPT = "Accept";

        /**
         * The Accept header.
         */
        public static final String ACCEPT_CHARSET = "Accept-Charset";

        /**
         * The Authorization header.
         */
        public static final String AUTHORIZATION = "Authorization";

        /**
         * The format string for specifying ranges with only begin offset.
         */
        public static final String BEGIN_RANGE_HEADER_FORMAT = "bytes=%d-";
        
        /**
         * The format string for specifying the blob append offset.
         */
        public static final String BLOB_APPEND_OFFSET = PREFIX_FOR_STORAGE_HEADER + "blob-append-offset";
        
        /**
         * The header that specifies committed block count.
         */
        public static final String BLOB_COMMITTED_BLOCK_COUNT = PREFIX_FOR_STORAGE_HEADER + "blob-committed-block-count";
        
        /**
         * The header that specifies blob sequence number.
         */
        public static final String BLOB_SEQUENCE_NUMBER = PREFIX_FOR_STORAGE_HEADER + "blob-sequence-number";
        
        /**
         * The CacheControl header.
         */
        public static final String CACHE_CONTROL = "Cache-Control";

        /**
         * The header that specifies blob caching control.
         */
        public static final String CACHE_CONTROL_HEADER = PREFIX_FOR_STORAGE_HEADER + "blob-cache-control";

        /**
         * The header that indicates the client request ID.
         */
        public static final String CLIENT_REQUEST_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "client-request-id";

        /**
         * The ContentDisposition header.
         */
        public static final String CONTENT_DISPOSITION = "Content-Disposition";

        /**
         * The ContentEncoding header.
         */
        public static final String CONTENT_ENCODING = "Content-Encoding";

        /**
         * The ContentLangauge header.
         */
        public static final String CONTENT_LANGUAGE = "Content-Language";

        /**
         * The ContentLength header.
         */
        public static final String CONTENT_LENGTH = "Content-Length";

        /**
         * The ContentMD5 header.
         */
        public static final String CONTENT_MD5 = "Content-MD5";

        /**
         * The ContentRange header.
         */
        public static final String CONTENT_RANGE = "Content-Range";

        /**
         * The ContentType header.
         */
        public static final String CONTENT_TYPE = "Content-Type";

        /**
         * The value of the copy action header that signifies an abort operation.
         */
        public static final String COPY_ACTION_ABORT = "abort";

        /**
         * Header that specifies the copy action.
         */
        public static final String COPY_ACTION_HEADER = PREFIX_FOR_STORAGE_HEADER + "copy-action";

        /**
         * The header that specifies copy completion time.
         */
        public static final String COPY_COMPLETION_TIME = PREFIX_FOR_STORAGE_HEADER + "copy-completion-time";

        /**
         * The header that specifies copy id.
         */
        public static final String COPY_ID = PREFIX_FOR_STORAGE_HEADER + "copy-id";

        /**
         * The header that specifies copy progress.
         */
        public static final String COPY_PROGRESS = PREFIX_FOR_STORAGE_HEADER + "copy-progress";

        /**
         * The header that specifies copy source.
         */
        public static final String COPY_SOURCE = PREFIX_FOR_STORAGE_HEADER + "copy-source";

        /**
         * The header for copy source.
         */
        public static final String COPY_SOURCE_HEADER = PREFIX_FOR_STORAGE_HEADER + "copy-source";

        /**
         * The header that specifies copy status.
         */
        public static final String COPY_STATUS = PREFIX_FOR_STORAGE_HEADER + "copy-status";

        /**
         * The header that specifies copy status description.
         */
        public static final String COPY_STATUS_DESCRIPTION = PREFIX_FOR_STORAGE_HEADER + "copy-status-description";

        /**
         * The header that specifies the date.
         */
        public static final String DATE = PREFIX_FOR_STORAGE_HEADER + "date";

        /**
         * The header to delete snapshots.
         */
        public static final String DELETE_SNAPSHOT_HEADER = PREFIX_FOR_STORAGE_HEADER + "delete-snapshots";

        /**
         * The ETag header.
         */
        public static final String ETAG = "ETag";

        /**
         * An unused HTTP code used internally to indicate a non-http related failure when constructing
         * {@link StorageException} objects
         */
        public static final int HTTP_UNUSED_306 = 306;

        /**
         * The blob append position equal header.
         */
        public static final String IF_APPEND_POSITION_EQUAL_HEADER = PREFIX_FOR_STORAGE_HEADER + "blob-condition-appendpos";
        
        /**
         * The IfMatch header.
         */
        public static final String IF_MATCH = "If-Match";
        
        /**
         * The blob maxsize condition header.
         */
        public static final String IF_MAX_SIZE_LESS_THAN_OR_EQUAL = PREFIX_FOR_STORAGE_HEADER + "blob-condition-maxsize";

        /**
         * The IfModifiedSince header.
         */
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

        /**
         * The IfNoneMatch header.
         */
        public static final String IF_NONE_MATCH = "If-None-Match";

        /**
         * The IfUnmodifiedSince header.
         */
        public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
        
        /**
         * The blob sequence number less than or equal condition header.
         */
        public static final String IF_SEQUENCE_NUMBER_LESS_THAN_OR_EQUAL = PREFIX_FOR_STORAGE_HEADER + "if-sequence-number-le";
        
        /**
         * The blob sequence number less than condition header.
         */
        public static final String IF_SEQUENCE_NUMBER_LESS_THAN = PREFIX_FOR_STORAGE_HEADER + "if-sequence-number-lt";
        
        /**
         * The blob sequence number equal condition header.
         */
        public static final String IF_SEQUENCE_NUMBER_EQUAL = PREFIX_FOR_STORAGE_HEADER + "if-sequence-number-eq";
        
        /**
         * The header that specifies the lease action to perform
         */
        public static final String LEASE_ACTION_HEADER = PREFIX_FOR_STORAGE_HEADER + "lease-action";

        /**
         * The header that specifies the break period of a lease
         */
        public static final String LEASE_BREAK_PERIOD_HEADER = PREFIX_FOR_STORAGE_HEADER + "lease-break-period";

        /**
         * The header that specifies lease duration.
         */
        public static final String LEASE_DURATION = PREFIX_FOR_STORAGE_HEADER + "lease-duration";

        /**
         * The header that specifies lease ID.
         */
        public static final String LEASE_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "lease-id";

        /**
         * The header that specifies lease state.
         */
        public static final String LEASE_STATE = PREFIX_FOR_STORAGE_HEADER + "lease-state";

        /**
         * The header that specifies lease status.
         */
        public static final String LEASE_STATUS = PREFIX_FOR_STORAGE_HEADER + "lease-status";

        /**
         * The header that specifies the remaining lease time
         */
        public static final String LEASE_TIME_HEADER = PREFIX_FOR_STORAGE_HEADER + "lease-time";

        /**
         * The header that specifies the pop receipt.
         */
        public static final String POP_RECEIPT_HEADER = PREFIX_FOR_STORAGE_HEADER + "popreceipt";

        /**
         * The header prefix for metadata.
         */
        public static final String PREFIX_FOR_STORAGE_METADATA = "x-ms-meta-";

        /**
         * The header prefix for properties.
         */
        public static final String PREFIX_FOR_STORAGE_PROPERTIES = "x-ms-prop-";

        /**
         * The header that specifies the proposed lease ID for a leasing operation
         */
        public static final String PROPOSED_LEASE_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "proposed-lease-id";

        /**
         * The Range header.
         */
        public static final String RANGE = "Range";

        /**
         * The header that specifies if the request will populate the ContentMD5 header for range gets.
         */
        public static final String RANGE_GET_CONTENT_MD5 = PREFIX_FOR_STORAGE_HEADER + "range-get-content-md5";

        /**
         * The format string for specifying ranges.
         */
        public static final String RANGE_HEADER_FORMAT = "bytes=%d-%d";

        /**
         * The header that indicates the request ID.
         */
        public static final String REQUEST_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "request-id";

        /**
         * The header field value received that indicates which server was accessed
         */
        public static final String SERVER = "Server";

        /**
         * The header that specifies the snapshot ID.
         */
        public static final String SNAPSHOT_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "snapshot";

        /**
         * The header for the If-Match condition.
         */
        public static final String SOURCE_IF_MATCH_HEADER = PREFIX_FOR_STORAGE_HEADER + "source-if-match";

        /**
         * The header for the If-Modified-Since condition.
         */
        public static final String SOURCE_IF_MODIFIED_SINCE_HEADER = PREFIX_FOR_STORAGE_HEADER
                + "source-if-modified-since";

        /**
         * The header for the If-None-Match condition.
         */
        public static final String SOURCE_IF_NONE_MATCH_HEADER = PREFIX_FOR_STORAGE_HEADER + "source-if-none-match";

        /**
         * The header for the If-Unmodified-Since condition.
         */
        public static final String SOURCE_IF_UNMODIFIED_SINCE_HEADER = PREFIX_FOR_STORAGE_HEADER
                + "source-if-unmodified-since";

        /**
         * The header for the source lease id.
         */
        public static final String SOURCE_LEASE_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "source-lease-id";

        /**
         * The header for data ranges.
         */
        public static final String STORAGE_RANGE_HEADER = PREFIX_FOR_STORAGE_HEADER + "range";

        /**
         * The header for storage version.
         */
        public static final String STORAGE_VERSION_HEADER = PREFIX_FOR_STORAGE_HEADER + "version";

        /**
         * The current storage version header value.
         */
        public static final String TARGET_STORAGE_VERSION = "2015-04-05";

        /**
         * The header that specifies the next visible time for a queue message.
         */
        public static final String TIME_NEXT_VISIBLE_HEADER = PREFIX_FOR_STORAGE_HEADER + "time-next-visible";

        /**
         * The UserAgent header.
         */
        public static final String USER_AGENT = "User-Agent";

        /**
         * Specifies the value to use for UserAgent header.
         */
        public static final String USER_AGENT_PREFIX = "Azure-Storage";

        /**
         * Specifies the value to use for UserAgent header.
         */
        public static final String USER_AGENT_VERSION = "4.0.0";

        /**
         * The default type for content-type and accept
         */
        public static final String XML_TYPE = "application/xml";
    }

    /**
     * Defines constants for use with query strings.
     */
    public static class QueryConstants {
        /**
         * The query component for the api version.
         */
        public static final String API_VERSION = "api-version";

        /**
         * Query component for SAS cache control.
         */
        public static final String CACHE_CONTROL = "rscc";

        /**
         * Query component for SAS content type.
         */
        public static final String CONTENT_TYPE = "rsct";

        /**
         * Query component for SAS content encoding.
         */
        public static final String CONTENT_ENCODING = "rsce";

        /**
         * Query component for SAS content language.
         */
        public static final String CONTENT_LANGUAGE = "rscl";

        /**
         * Query component for SAS content disposition.
         */
        public static final String CONTENT_DISPOSITION = "rscd";

        /**
         * Query component for the operation (component) to access.
         */
        public static final String COMPONENT = "comp";

        /**
         * Query component for copy.
         */
        public static final String COPY = "copy";

        /**
         * Query component for the copy ID.
         */
        public static final String COPY_ID = "copyid";

        /**
         * The query component for the SAS end partition key.
         */
        public static final String END_PARTITION_KEY = "epk";

        /**
         * The query component for the SAS end row key.
         */
        public static final String END_ROW_KEY = "erk";

        /**
         * Query component value for list.
         */
        public static final String LIST = "list";

        /**
         * Query component value for properties.
         */
        public static final String PROPERTIES = "properties";

        /**
         * Query component for resource type.
         */
        public static final String RESOURCETYPE = "restype";

        /**
         * The query component for the SAS table name.
         */
        public static final String SAS_TABLE_NAME = "tn";

        /**
         * The query component for the SAS signature.
         */
        public static final String SIGNATURE = "sig";

        /**
         * The query component for the signed SAS expiry time.
         */
        public static final String SIGNED_EXPIRY = "se";

        /**
         * The query component for the signed SAS identifier.
         */
        public static final String SIGNED_IDENTIFIER = "si";

        /**
         * The query component for the signed SAS IP address.
         */
        public static final String SIGNED_IP = "sip";

        /**
         * The query component for the signing SAS key.
         */
        public static final String SIGNED_KEY = "sk";

        /**
         * The query component for the signed SAS permissions.
         */
        public static final String SIGNED_PERMISSIONS = "sp";

        /**
         * The query component for the signed SAS Internet protocols.
         */
        public static final String SIGNED_PROTOCOLS = "spr";

        /**
         * The query component for the signed SAS resource.
         */
        public static final String SIGNED_RESOURCE = "sr";

        /**
         * The query component for the signed SAS resource type.
         */
        public static final String SIGNED_RESOURCE_TYPE = "srt";

        /**
         * The query component for the signed SAS service.
         */
        public static final String SIGNED_SERVICE = "ss";

        /**
         * The query component for the signed SAS start time.
         */
        public static final String SIGNED_START = "st";

        /**
         * The query component for the signed SAS version.
         */
        public static final String SIGNED_VERSION = "sv";

        /**
         * The query component for snapshot time.
         */
        public static final String SNAPSHOT = "snapshot";

        /**
         * The query component for the SAS start partition key.
         */
        public static final String START_PARTITION_KEY = "spk";

        /**
         * The query component for the SAS start row key.
         */
        public static final String START_ROW_KEY = "srk";

        /**
         * The query component for stats.
         */
        public static final String STATS = "stats";

        /**
         * The query component for delimiter.
         */
        public static final String DELIMITER = "delimiter";

        /**
         * The query component for include.
         */
        public static final String INCLUDE = "include";

        /**
         * The query component for marker.
         */
        public static final String MARKER = "marker";

        /**
         * The query component for max results.
         */
        public static final String MAX_RESULTS = "maxresults";

        /**
         * The query component for metadata.
         */
        public static final String METADATA = "metadata";

        /**
         * The query component for prefix.
         */
        public static final String PREFIX = "prefix";

        /**
         * The query component for acl.
         */
        public static final String ACL = "acl";
    }

    /**
     * The master Microsoft Azure Storage header prefix.
     */
    public static final String PREFIX_FOR_STORAGE_HEADER = "x-ms-";

    /**
     * Constant representing a kilobyte (Non-SI version).
     */
    public static final int KB = 1024;

    /**
     * Constant representing a megabyte (Non-SI version).
     */
    public static final int MB = 1024 * KB;

    /**
     * Constant representing a gigabyte (Non-SI version).
     */
    public static final int GB = 1024 * MB;

    /**
     * XML element for an access policy.
     */
    public static final String ACCESS_POLICY = "AccessPolicy";
    
    /**
     * Buffer width used to copy data to output streams.
     */
    public static final int BUFFER_COPY_LENGTH = 8 * KB;

    /**
     * XML element for the copy completion time.
     */
    public static final String COPY_COMPLETION_TIME_ELEMENT = "CopyCompletionTime";

    /**
     * XML element for the copy id.
     */
    public static final String COPY_ID_ELEMENT = "CopyId";

    /**
     * XML element for the copy progress.
     */
    public static final String COPY_PROGRESS_ELEMENT = "CopyProgress";

    /**
     * XML element for the copy source .
     */
    public static final String COPY_SOURCE_ELEMENT = "CopySource";

    /**
     * XML element for the copy status description.
     */
    public static final String COPY_STATUS_DESCRIPTION_ELEMENT = "CopyStatusDescription";

    /**
     * XML element for the copy status.
     */
    public static final String COPY_STATUS_ELEMENT = "CopyStatus";

    /**
     * Default read timeout. 5 min * 60 seconds * 1000 ms
     */
    public static final int DEFAULT_READ_TIMEOUT = 5 * 60 * 1000;
    
    /**
     * XML element for delimiters.
     */
    public static final String DELIMITER_ELEMENT = "Delimiter";

    /**
     * Http GET method.
     */
    public static final String HTTP_GET = "GET";

    /**
     * Http PUT method.
     */
    public static final String HTTP_PUT = "PUT";

    /**
     * Http DELETE method.
     */
    public static final String HTTP_DELETE = "DELETE";

    /**
     * Http HEAD method.
     */
    public static final String HTTP_HEAD = "HEAD";

    /**
     * Http POST method.
     */
    public static final String HTTP_POST = "POST";

    /**
     * An empty <code>String</code> to use for comparison.
     */
    public static final String EMPTY_STRING = "";

    /**
     * XML element for page range end elements.
     */
    public static final String END_ELEMENT = "End";

    /**
     * XML element for error codes.
     */
    public static final String ERROR_CODE = "Code";

    /**
     * XML element for exception details.
     */
    public static final String ERROR_EXCEPTION = "ExceptionDetails";

    /**
     * XML element for exception messages.
     */
    public static final String ERROR_EXCEPTION_MESSAGE = "ExceptionMessage";

    /**
     * XML element for stack traces.
     */
    public static final String ERROR_EXCEPTION_STACK_TRACE = "StackTrace";

    /**
     * XML element for error messages.
     */
    public static final String ERROR_MESSAGE = "Message";

    /**
     * XML root element for errors.
     */
    public static final String ERROR_ROOT_ELEMENT = "Error";

    /**
     * XML element for the ETag.
     */
    public static final String ETAG_ELEMENT = "Etag";

    /**
     * XML element for the end time of an access policy.
     */
    public static final String EXPIRY = "Expiry";

    /**
     * Constant for False.
     */
    public static final String FALSE = "false";

    /**
     * Constant for bootstrap geo-replication status.
     */
    public static final String GEO_BOOTSTRAP_VALUE = "bootstrap";

    /**
     * Constant for live geo-replication status.
     */
    public static final String GEO_LIVE_VALUE = "live";

    /**
     * Constant for unavailable geo-replication status.
     */
    public static final String GEO_UNAVAILABLE_VALUE = "unavailable";

    /**
     * Specifies HTTP.
     */
    public static final String HTTP = "http";

    /**
     * Specifies HTTPS.
     */
    public static final String HTTPS = "https";

    /**
     * Specifies both HTTPS and HTTP.
     */
    public static final String HTTPS_HTTP = "https,http";

    /**
     * XML attribute for IDs.
     */
    public static final String ID = "Id";

    /**
     * XML element for an invalid metadata name.
     */
    public static final String INVALID_METADATA_NAME = "x-ms-invalid-name";

    /**
     * XML element for the last modified date.
     */
    public static final String LAST_MODIFIED_ELEMENT = "Last-Modified";

    /**
     * Lease break period max in seconds.
     */
    public static final int LEASE_BREAK_PERIOD_MAX = 60;

    /**
     * Lease break period min in seconds.
     */
    public static final int LEASE_BREAK_PERIOD_MIN = 0;

    /**
     * XML element for the lease duration.
     */
    public static final String LEASE_DURATION_ELEMENT = "LeaseDuration";

    /**
     * Lease duration max in seconds.
     */
    public static final int LEASE_DURATION_MAX = 60;

    /**
     * Lease duration min in seconds.
     */
    public static final int LEASE_DURATION_MIN = 15;
    
    /**
     * XML element for the lease state.
     */
    public static final String LEASE_STATE_ELEMENT = "LeaseState";

    /**
     * XML element for the lease status.
     */
    public static final String LEASE_STATUS_ELEMENT = "LeaseStatus";

    /**
     * Constant signaling the resource is locked.
     */
    public static final String LOCKED_VALUE = "Locked";

    /**
     * XML element for a marker.
     */
    public static final String MARKER_ELEMENT = "Marker";

    /**
     * The maximum size of a single block.
     */
    public static int MAX_BLOCK_SIZE = 4 * MB;

    /**
     * The default write size, in bytes, used by {@link BlobOutputStream} or {@link FileOutputStream}.
     */
    public static final int DEFAULT_STREAM_WRITE_IN_BYTES = Constants.MAX_BLOCK_SIZE;

    /**
     * The default minimum read size, in bytes, for a {@link BlobInputStream} or {@link FileInputStream}.
     */
    public static final int DEFAULT_MINIMUM_READ_SIZE_IN_BYTES = Constants.MAX_BLOCK_SIZE;

    /**
     * The maximum size, in bytes, of a given stream mark operation.
     */
    // Note if BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES is updated then this needs to be as well.
    public static final int MAX_MARK_LENGTH = 64 * MB;

    /**
     * XML element for maximum results.
     */
    public static final String MAX_RESULTS_ELEMENT = "MaxResults";

    /**
     * Maximum number of shared access policy identifiers supported by server.
     */
    public static final int MAX_SHARED_ACCESS_POLICY_IDENTIFIERS = 5;

    /**
     * Number of default concurrent requests for parallel operation.
     */
    public static final int MAXIMUM_SEGMENTED_RESULTS = 5000;

    /**
     * XML element for the metadata.
     */
    public static final String METADATA_ELEMENT = "Metadata";

    /**
     * XML element for names.
     */
    public static final String NAME_ELEMENT = "Name";

    /**
     * XML element for the next marker.
     */
    public static final String NEXT_MARKER_ELEMENT = "NextMarker";

    /**
     * The size of a page, in bytes, in a page blob.
     */
    public static final int PAGE_SIZE = 512;

    /**
     * XML element for the permission of an access policy.
     */
    public static final String PERMISSION = "Permission";

    /**
     * XML element for a prefix.
     */
    public static final String PREFIX_ELEMENT = "Prefix";

    /**
     * XML element for properties.
     */
    public static final String PROPERTIES = "Properties";

    /**
     * XML element for a signed identifier.
     */
    public static final String SIGNED_IDENTIFIER_ELEMENT = "SignedIdentifier";

    /**
     * XML element for signed identifiers.
     */
    public static final String SIGNED_IDENTIFIERS_ELEMENT = "SignedIdentifiers";

    /**
     * XML element for the start time of an access policy.
     */
    public static final String START = "Start";

    /**
     * Constant for True.
     */
    public static final String TRUE = "true";

    /**
     * Constant signaling the resource is unlocked.
     */
    public static final String UNLOCKED_VALUE = "Unlocked";

    /**
     * Constant signaling the resource lease duration, state or status is unspecified.
     */
    public static final String UNSPECIFIED_VALUE = "Unspecified";

    /**
     * XML element for the URL.
     */
    public static final String URL_ELEMENT = "Url";

    /**
     * The default type for content-type and accept
     */
    public static final String UTF8_CHARSET = "UTF-8";

    /**
     * Private Default Ctor
     */
    private Constants() {
        // No op
    }
}
