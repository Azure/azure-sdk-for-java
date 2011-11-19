package com.microsoft.windowsazure.services.core.storage;

/**
 * Defines constants for use with blob operations, HTTP headers, and query strings.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class Constants {
    /**
     * Defines constants for use Analytics requests.
     */
    public static class AnalyticsConstants {
        /**
         * The XML element for the Analytics RetentionPolicy Days.
         */
        public static final String DAYS_ELEMENT = "Days";

        /**
         * The XML element for the Default Service Version.
         */
        public static final String DEFAULT_SERVICE_VERSION = "DefaultServiceVersion";

        /**
         * The XML element for the Analytics Logging Delete type.
         */
        public static final String DELETE_ELEMENT = "Delete";

        /**
         * The XML element for the Analytics RetentionPolicy Enabled.
         */
        public static final String ENABLED_ELEMENT = "Enabled";

        /**
         * The XML element for the Analytics Metrics IncludeAPIs.
         */
        public static final String INCLUDE_APIS_ELEMENT = "IncludeAPIs";

        /**
         * The XML element for the Analytics Logging
         */
        public static final String LOGGING_ELEMENT = "Logging";

        /**
         * The XML element for the Analytics Metrics
         */
        public static final String METRICS_ELEMENT = "Metrics";

        /**
         * The XML element for the Analytics Logging Read type.
         */
        public static final String READ_ELEMENT = "Read";

        /**
         * The XML element for the Analytics RetentionPolicy.
         */
        public static final String RETENTION_POLICY_ELEMENT = "RetentionPolicy";

        /**
         * The XML element for the StorageServiceProperties
         */
        public static final String STORAGE_SERVICE_PROPERTIES_ELEMENT = "StorageServiceProperties";

        /**
         * The XML element for the Analytics Version
         */
        public static final String VERSION_ELEMENT = "Version";

        /**
         * The XML element for the Analytics Logging Write type.
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
         * The CacheControl header.
         */
        public static final String CACHE_CONTROL = "Cache-Control";

        /**
         * The header that specifies blob caching control.
         */
        public static final String CACHE_CONTROL_HEADER = PREFIX_FOR_STORAGE_HEADER + "blob-cache-control";

        /**
         * The Comp value.
         */
        public static final String COMP = "comp";

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
        public static final String CONTENT_RANGE = "Cache-Range";

        /**
         * The ContentType header.
         */
        public static final String CONTENT_TYPE = "Content-Type";

        /**
         * The header for copy source.
         */
        public static final String COPY_SOURCE_HEADER = PREFIX_FOR_STORAGE_HEADER + "copy-source";

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
         * Buffer width used to copy data to output streams.
         */
        public static final int HTTP_UNUSED_306 = 306;

        /**
         * The IfMatch header.
         */
        public static final String IF_MATCH = "If-Match";

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
         * The header that specifies lease ID.
         */
        public static final String LEASE_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "lease-id";

        /**
         * The header that specifies lease status.
         */
        public static final String LEASE_STATUS = PREFIX_FOR_STORAGE_HEADER + "lease-status";

        /**
         * The header prefix for metadata.
         */
        public static final String PREFIX_FOR_STORAGE_METADATA = "x-ms-meta-";

        /**
         * The header prefix for properties.
         */
        public static final String PREFIX_FOR_STORAGE_PROPERTIES = "x-ms-prop-";

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
        public static final String TARGET_STORAGE_VERSION = "2011-08-18";

        /**
         * The UserAgent header.
         */
        public static final String USER_AGENT = "User-Agent";

        /**
         * Specifies the value to use for UserAgent header.
         */
        public static final String USER_AGENT_PREFIX = "WA-Storage";

        /**
         * Specifies the value to use for UserAgent header.
         */
        public static final String USER_AGENT_VERSION = "Client v0.1";
    }

    /**
     * The master Windows Azure Storage header prefix.
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
     * Buffer width used to copy data to output streams.
     */
    public static final int BUFFER_COPY_LENGTH = 8 * KB;

    /**
     * Default client side timeout, in milliseconds, for all service clients.
     */
    public static final int DEFAULT_TIMEOUT_IN_MS = 90 * 1000;

    /**
     * XML element for delimiters.
     */
    public static final String DELIMITER_ELEMENT = "Delimiter";

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
     * Constant for False.
     */
    public static final String FALSE = "false";

    /**
     * Specifies HTTP.
     */
    public static final String HTTP = "http";

    /**
     * Specifies HTTPS.
     */
    public static final String HTTPS = "https";

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
     * XML element for maximum results.
     */
    public static final String MAX_RESULTS_ELEMENT = "MaxResults";

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
     * XML element for a prefix.
     */
    public static final String PREFIX_ELEMENT = "Prefix";

    /**
     * Constant for True.
     */
    public static final String TRUE = "true";

    /**
     * Constant signaling the resource is unlocked.
     */
    public static final String UNLOCKED_VALUE = "Unlocked";

    /**
     * XML element for the URL.
     */
    public static final String URL_ELEMENT = "Url";

    /**
     * Private Default Ctor
     */
    private Constants() {
        // No op
    }
}
