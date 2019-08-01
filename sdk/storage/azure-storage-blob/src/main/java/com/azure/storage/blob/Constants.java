// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

/**
 * RESERVED FOR INTERNAL USE. Contains storage constants.
 */
final class Constants {

    /**
     * The master Microsoft Azure Storage header prefix.
     */
    static final String PREFIX_FOR_STORAGE_HEADER = "x-ms-";
    /**
     * Constant representing a kilobyte (Non-SI version).
     */
    static final int KB = 1024;
    /**
     * Constant representing a megabyte (Non-SI version).
     */
    static final int MB = 1024 * KB;
    /**
     * An empty {@code String} to use for comparison.
     */
    static final String EMPTY_STRING = "";
    /**
     * Specifies HTTP.
     */
    static final String HTTP = "http";
    /**
     * Specifies HTTPS.
     */
    static final String HTTPS = "https";
    /**
     * Specifies both HTTPS and HTTP.
     */
    static final String HTTPS_HTTP = "https,http";
    /**
     * The default type for content-type and accept.
     */
    static final String UTF8_CHARSET = "UTF-8";
    /**
     * The query parameter for snapshots.
     */
    static final String SNAPSHOT_QUERY_PARAMETER = "snapshot";
    /**
     * The word redacted.
     */
    static final String REDACTED = "REDACTED";
    /**
     * The default amount of parallelism for TransferManager operations.
     */
    // We chose this to match Go, which followed AWS' default.
    static final int TRANSFER_MANAGER_DEFAULT_PARALLELISM = 5;

    /**
     * The size of a page, in bytes, in a page blob.
     */
    public static final int PAGE_SIZE = 512;


    /**
     * Private Default Ctor
     */
    private Constants() {
        // Private to prevent construction.
    }

    /**
     * Defines constants for use with HTTP headers.
     */
    static final class HeaderConstants {
        /**
         * The Authorization header.
         */
        static final String AUTHORIZATION = "Authorization";

        /**
         * The header that indicates the client request ID.
         */
        static final String CLIENT_REQUEST_ID_HEADER = PREFIX_FOR_STORAGE_HEADER + "client-request-id";

        /**
         * The ContentEncoding header.
         */
        static final String CONTENT_ENCODING = "Content-Encoding";

        /**
         * The ContentLangauge header.
         */
        static final String CONTENT_LANGUAGE = "Content-Language";

        /**
         * The ContentLength header.
         */
        static final String CONTENT_LENGTH = "Content-Length";

        /**
         * The ContentMD5 header.
         */
        static final String CONTENT_MD5 = "Content-MD5";

        /**
         * The ContentType header.
         */
        static final String CONTENT_TYPE = "Content-Type";

        /**
         * The header that specifies the date.
         */
        static final String DATE = PREFIX_FOR_STORAGE_HEADER + "date";

        /**
         * The header that specifies the error code on unsuccessful responses.
         */
        static final String ERROR_CODE = PREFIX_FOR_STORAGE_HEADER + "error-code";

        /**
         * The IfMatch header.
         */
        static final String IF_MATCH = "If-Match";

        /**
         * The IfModifiedSince header.
         */
        static final String IF_MODIFIED_SINCE = "If-Modified-Since";

        /**
         * The IfNoneMatch header.
         */
        static final String IF_NONE_MATCH = "If-None-Match";

        /**
         * The IfUnmodifiedSince header.
         */
        static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

        /**
         * The Range header.
         */
        static final String RANGE = "Range";

        /**
         * The copy source header.
         */
        static final String COPY_SOURCE = "x-ms-copy-source";

        /**
         * The version header.
         */
        static final String VERSION = "x-ms-version";

        /**
         * The current storage version header value.
         */
        static final String TARGET_STORAGE_VERSION = "2018-11-09";

        /**
         * The UserAgent header.
         */
        static final String USER_AGENT = "User-Agent";

        /**
         * Specifies the value to use for UserAgent header.
         */
        static final String USER_AGENT_PREFIX = "Azure-Storage";

        /**
         * Specifies the value to use for UserAgent header.
         */
        static final String USER_AGENT_VERSION = "11.0.1";

        private HeaderConstants() {
            // Private to prevent construction.
        }
    }

    static final class UrlConstants {

        /**
         * The SAS service version parameter.
         */
        static final String SAS_SERVICE_VERSION = "sv";

        /**
         * The SAS services parameter.
         */
        static final String SAS_SERVICES = "ss";

        /**
         * The SAS resource types parameter.
         */
        static final String SAS_RESOURCES_TYPES = "srt";

        /**
         * The SAS protocol parameter.
         */
        static final String SAS_PROTOCOL = "spr";

        /**
         * The SAS start time parameter.
         */
        static final String SAS_START_TIME = "st";

        /**
         * The SAS expiration time parameter.
         */
        static final String SAS_EXPIRY_TIME = "se";

        /**
         * The SAS IP range parameter.
         */
        static final String SAS_IP_RANGE = "sip";

        /**
         * The SAS signed identifier parameter.
         */
        static final String SAS_SIGNED_IDENTIFIER = "si";

        /**
         * The SAS signed resource parameter.
         */
        static final String SAS_SIGNED_RESOURCE = "sr";

        /**
         * The SAS signed permissions parameter.
         */
        static final String SAS_SIGNED_PERMISSIONS = "sp";

        /**
         * The SAS signature parameter.
         */
        static final String SAS_SIGNATURE = "sig";

        /**
         * The SAS cache control parameter.
         */
        static final String SAS_CACHE_CONTROL = "rscc";

        /**
         * The SAS content disposition parameter.
         */
        static final String SAS_CONTENT_DISPOSITION = "rscd";

        /**
         * The SAS content encoding parameter.
         */
        static final String SAS_CONTENT_ENCODING = "rsce";

        /**
         * The SAS content language parameter.
         */
        static final String SAS_CONTENT_LANGUAGE = "rscl";

        /**
         * The SAS content type parameter.
         */
        static final String SAS_CONTENT_TYPE = "rsct";

        /**
         * The SAS signed object id parameter for user delegation SAS.
         */
        public static final String SAS_SIGNED_OBJECT_ID = "skoid";

        /**
         * The SAS signed tenant id parameter for user delegation SAS.
         */
        public static final String SAS_SIGNED_TENANT_ID = "sktid";

        /**
         * The SAS signed key-start parameter for user delegation SAS.
         */
        public static final String SAS_SIGNED_KEY_START = "skt";

        /**
         * The SAS signed key-expiry parameter for user delegation SAS.
         */
        public static final String SAS_SIGNED_KEY_EXPIRY = "ske";

        /**
         * The SAS signed service parameter for user delegation SAS.
         */
        public static final String SAS_SIGNED_KEY_SERVICE = "sks";

        /**
         * The SAS signed version parameter for user delegation SAS.
         */
        public static final String SAS_SIGNED_KEY_VERSION = "skv";

        /**
         * The SAS blob constant.
         */
        public static final String SAS_BLOB_CONSTANT = "b";

        /**
         * The SAS blob snapshot constant.
         */
        public static final String SAS_BLOB_SNAPSHOT_CONSTANT = "bs";

        /**
         * The SAS blob snapshot constant.
         */
        public static final String SAS_CONTAINER_CONSTANT = "c";

        private UrlConstants() {
            // Private to prevent construction.
        }
    }
}
