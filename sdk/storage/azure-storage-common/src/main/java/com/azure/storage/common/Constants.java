// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

public final class Constants {
    public static final int KB = 1024;

    /**
     * Constant representing a megabyte (Non-SI version).
     */
    public static final int MB = 1024 * KB;

    /**
     * An empty {@code String} to use for comparison.
     */
    public static final String EMPTY_STRING = "";

    /**
     * The default type for content-type and accept.
     */
    static final String UTF8_CHARSET = "UTF-8";

    /**
     * The query parameter for snapshots.
     */
    public static final String SNAPSHOT_QUERY_PARAMETER = "snapshot";

    static final String HTTPS = "https";
    static final String HTTPS_HTTP = "https,http";

    private Constants() {
    }

    /**
     * Defines constants for use with connection strings.
     */
    public static final class ConnectionStringConstants {
        /**
         * The AccountName key.
         */
        public static final String ACCOUNT_NAME = "accountname";

        /**
         * The AccountKey key.
         */
        public static final String ACCOUNT_KEY = "accountkey";

        /**
         * The DefaultEndpointProtocol key.
         */
        public static final String ENDPOINT_PROTOCOL = "defaultendpointprotocol";

        /**
         * The EndpointSuffix key.
         */
        public static final String ENDPOINT_SUFFIX = "endpointsuffix";

        private ConnectionStringConstants() {
        }
    }

    /**
     * Defines constants for use with HTTP headers.
     */
    public static final class HeaderConstants {

        /**
         * The current storage version header value.
         */
        public static final String TARGET_STORAGE_VERSION = "2018-11-09";

        /**
         * Error code returned from the service.
         */
        public static final String ERROR_CODE = "x-ms-error-code";

        /**
         * Compression type used on the body.
         */
        public static final String CONTENT_ENCODING = "Content-Encoding";

        private HeaderConstants() {
            // Private to prevent construction.
        }
    }

    public static final class UrlConstants {

        /**
         * The SAS service version parameter.
         */
        public static final String SAS_SERVICE_VERSION = "sv";

        /**
         * The SAS services parameter.
         */
        public static final String SAS_SERVICES = "ss";

        /**
         * The SAS resource types parameter.
         */
        public static final String SAS_RESOURCES_TYPES = "srt";

        /**
         * The SAS protocol parameter.
         */
        public static final String SAS_PROTOCOL = "spr";

        /**
         * The SAS start time parameter.
         */
        public static final String SAS_START_TIME = "st";

        /**
         * The SAS expiration time parameter.
         */
        public static final String SAS_EXPIRY_TIME = "se";

        /**
         * The SAS IP range parameter.
         */
        public static final String SAS_IP_RANGE = "sip";

        /**
         * The SAS signed identifier parameter.
         */
        public static final String SAS_SIGNED_IDENTIFIER = "si";

        /**
         * The SAS signed resource parameter.
         */
        public static final String SAS_SIGNED_RESOURCE = "sr";

        /**
         * The SAS signed permissions parameter.
         */
        public static final String SAS_SIGNED_PERMISSIONS = "sp";

        /**
         * The SAS signature parameter.
         */
        public static final String SAS_SIGNATURE = "sig";

        /**
         * The SAS cache control parameter.
         */
        public static final String SAS_CACHE_CONTROL = "rscc";

        /**
         * The SAS content disposition parameter.
         */
        public static final String SAS_CONTENT_DISPOSITION = "rscd";

        /**
         * The SAS content encoding parameter.
         */
        public static final String SAS_CONTENT_ENCODING = "rsce";

        /**
         * The SAS content language parameter.
         */
        public static final String SAS_CONTENT_LANGUAGE = "rscl";

        /**
         * The SAS content type parameter.
         */
        public static final String SAS_CONTENT_TYPE = "rsct";

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

        /**
         * The SAS file constant.
         */
        public static final String SAS_FILE_CONSTANT = "f";

        /**
         * The SAS share constant.
         */
        public static final String SAS_SHARE_CONSTANT = "s";

        /**
         * The SAS queue constant.
         */
        public static final String SAS_QUEUE_CONSTANT = "q";

        private UrlConstants() {
            // Private to prevent construction.
        }
    }

    static final class MessageConstants {
        static final String ARGUMENT_NULL_OR_EMPTY = "The argument must not be null or an empty string. Argument name: %s.";
        static final String PARAMETER_NOT_IN_RANGE = "The value of the parameter '%s' should be between %s and %s.";
        static final String INVALID_DATE_STRING = "Invalid Date String: %s.";
        static final String NO_PATH_SEGMENTS = "URL %s does not contain path segments.";

        private MessageConstants() {
        }
    }
}
