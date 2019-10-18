// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.storage.common.sas.SasProtocol;

/**
 * Defines general constants.
 *
 * RESERVED FOR INTERNAL USE.
 */
public final class Constants {
    /**
     * Represents a non-SI kilobyte.
     */
    public static final int KB = 1024;

    /**
     * Represents a non-SI megabyte.
     */
    public static final int MB = 1024 * KB;

    /**
     * Represents the value for {@link SasProtocol#HTTPS_ONLY}.
     */
    public static final String HTTPS = "https";

    /**
     * Represents the value for {@link SasProtocol#HTTPS_HTTP}.
     */
    public static final String HTTPS_HTTP = "https,http";

    /**
     * Exception message when the underlying stream has already been closed.
     */
    public static final String STREAM_CLOSED = "Stream is already closed.";

    /**
     * Exception message when the value could not be parsed into an enum.
     */
    public static final String ENUM_COULD_NOT_BE_PARSED_INVALID_VALUE =
        "%s could not be parsed from '%s' due to invalid value %s.";

    private Constants() {
    }

    /**
     * Defines constants for use with connection strings.
     *
     * RESERVED FOR INTERNAL USE.
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
         * The DefaultEndpointsProtocol key.
         */
        public static final String ENDPOINT_PROTOCOL = "defaultendpointsprotocol";

        /**
         * The EndpointSuffix key.
         */
        public static final String ENDPOINT_SUFFIX = "endpointsuffix";

        private ConnectionStringConstants() {
        }
    }

    /**
     * Defines constants for use with HTTP headers.
     *
     * RESERVED FOR INTERNAL USE.
     */
    public static final class HeaderConstants {

        /**
         * The current storage version header value.
         */
        public static final String TARGET_STORAGE_VERSION = "2019-02-02";

        /**
         * Error code returned from the service.
         */
        public static final String ERROR_CODE = "x-ms-error-code";

        /**
         * Compression type used on the body.
         */
        public static final String CONTENT_ENCODING = "Content-Encoding";

        public static final String CLIENT_REQUEST_ID = "x-ms-client-request-id";

        public static final String ENCRYPTION_KEY = "x-ms-encryption-key";

        public static final String ENCRYPTION_KEY_SHA256 = "x-ms-encryption-key-sha256";

        public static final String SERVER_ENCRYPTED = "x-ms-server-encrypted";

        public static final String REQUEST_SERVER_ENCRYPTED = "x-ms-request-server-encrypted";

        private HeaderConstants() {
            // Private to prevent construction.
        }
    }

    /**
     * Defines constants for use with URLs.
     *
     * RESERVED FOR INTERNAL USE.
     */
    public static final class UrlConstants {

        /**
         * The snapshot parameters.
         */
        public static final String SNAPSHOT_QUERY_PARAMETER = "snapshot";

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
         * The SAS queue constant.
         */
        public static final String SAS_QUEUE_CONSTANT = "q";

        private UrlConstants() {
            // Private to prevent construction.
        }
    }
}
