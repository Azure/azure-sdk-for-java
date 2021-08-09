// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.Configuration;
import com.azure.storage.common.sas.SasProtocol;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
     * Represents a non-SI gigabyte.
     */
    public static final int GB = 1024 * MB;

    /**
     * Represents a non-SI terabyte.
     */
    public static final long TB = 1024L * GB;

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


    public static final DateTimeFormatter ISO_8601_UTC_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).withZone(ZoneId.of("UTC"));

    public static final String BLOB_ALREADY_EXISTS =
        "Blob already exists. Specify overwrite to true to force update the blob.";

    public static final String FILE_ALREADY_EXISTS =
        "File already exists. Specify overwrite to true to force update the file.";

    /**
     * Buffer width used to copy data to output streams.
     */
    public static final int BUFFER_COPY_LENGTH = 8 * KB;

    /**
     * This constant is used to cap Stream->Flux converter's block size considering that:
     * - Integer.MAX (or near) leads to java.lang.OutOfMemoryError: Requested array size exceeds VM limit
     * - Allocating arrays that are very large can be less successful on busy heap and put extra pressure on GC to
     *   de-fragment.
     * - Going to small on the other hand might be harmful to large upload scenarios. Max block size is 4000MB
     *   so chunking that into blocks that are smaller produces a lot of garbage to just wrap this into ByteBuffers.
     */
    public static final int MAX_INPUT_STREAM_CONVERTER_BUFFER_LENGTH = 64 * MB;

    public static final String STORAGE_SCOPE = "https://storage.azure.com/.default";

    public static final String STORAGE_LOG_STRING_TO_SIGN = "Azure-Storage-Log-String-To-Sign";

    public static final String PROPERTY_AZURE_STORAGE_SAS_SERVICE_VERSION = "AZURE_STORAGE_SAS_SERVICE_VERSION";

    public static final String SAS_SERVICE_VERSION = Configuration.getGlobalConfiguration()
        .get(PROPERTY_AZURE_STORAGE_SAS_SERVICE_VERSION, "2020-10-02");

    private Constants() {
    }

    /**
     * Defines constants for use with connection strings.
     *
     * RESERVED FOR INTERNAL USE.
     */
    public static final class ConnectionStringConstants {
        /**
         * The setting name for the storage account name.
         */
        public static final String ACCOUNT_NAME = "AccountName";

        /**
         * The setting name for the storage account key.
         */
        public static final String ACCOUNT_KEY_NAME = "AccountKey";

        /**
         * The setting name for using the default storage endpoints with the specified protocol.
         */
        public static final String DEFAULT_ENDPOINTS_PROTOCOL_NAME = "DefaultEndpointsProtocol";

        /**
         * The setting name for a custom blob storage endpoint.
         */
        public static final String BLOB_ENDPOINT_NAME = "BlobEndpoint";

        /**
         * The setting name for a custom blob storage secondary endpoint.
         */
        public static final String BLOB_SECONDARY_ENDPOINT_NAME = "BlobSecondaryEndpoint";

        /**
         * The setting name for a custom queue endpoint.
         */
        public static final String QUEUE_ENDPOINT_NAME = "QueueEndpoint";

        /**
         * The setting name for a custom queue secondary endpoint.
         */
        public static final String QUEUE_SECONDARY_ENDPOINT_NAME = "QueueSecondaryEndpoint";

        /**
         * The setting name for a custom file endpoint.
         */
        public static final String FILE_ENDPOINT_NAME = "FileEndpoint";

        /**
         * The setting name for a custom file secondary endpoint.
         */
        public static final String FILE_SECONDARY_ENDPOINT_NAME = "FileSecondaryEndpoint";

        /**
         * The setting name for a custom table storage endpoint.
         */
        public static final String TABLE_ENDPOINT_NAME = "TableEndpoint";

        /**
         * The setting name for a custom table storage secondary endpoint.
         */
        public static final String TABLE_SECONDARY_ENDPOINT_NAME = "TableSecondaryEndpoint";

        /**
         * The setting name for a custom storage endpoint suffix.
         */
        public static final String ENDPOINT_SUFFIX_NAME = "EndpointSuffix";

        /**
         * The setting name for a shared access key.
         */
        public static final String SHARED_ACCESS_SIGNATURE_NAME = "SharedAccessSignature";

        /**
         * The setting name for using the emulator storage.
         */
        public static final String USE_EMULATOR_STORAGE_NAME = "UseDevelopmentStorage";

        /**
         * The setting name for specifying a development storage proxy Uri.
         */
        public static final String EMULATOR_STORAGE_PROXY_URI_NAME = "DevelopmentStorageProxyUri";

        /**
         * The root storage DNS name.
         */
        public static final String DEFAULT_DNS = "core.windows.net";

        /**
         * The format string for the primary endpoint in emulator.
         */
        public static final String EMULATOR_PRIMARY_ENDPOINT_FORMAT = "%s://%s:%s/devstoreaccount1";

        /**
         * The format string for the secondary endpoint in emulator.
         */
        public static final String EMULATOR_SECONDARY_ENDPOINT_FORMAT = "%s://%s:%s/devstoreaccount1-secondary";

        /**
         * The default account key for the development storage.
         */
        public static final String EMULATOR_ACCOUNT_KEY
                = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

        /**
         * The default account name for the development storage.
         */
        public static final String EMULATOR_ACCOUNT_NAME = "devstoreaccount1";

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
         * @deprecated For SAS Service Version use {@link Constants#SAS_SERVICE_VERSION}.
         */
        @Deprecated
        public static final String TARGET_STORAGE_VERSION = "2020-10-02";

        /**
         * Error code returned from the service.
         */
        public static final String ERROR_CODE = "x-ms-error-code";

        /**
         * Compression type used on the body.
         */
        public static final String CONTENT_ENCODING = "Content-Encoding";

        public static final String CONTENT_TYPE = "Content-Type";

        public static final String CLIENT_REQUEST_ID = "x-ms-client-request-id";

        public static final String ENCRYPTION_KEY = "x-ms-encryption-key";

        public static final String ENCRYPTION_KEY_SHA256 = "x-ms-encryption-key-sha256";

        public static final String SERVER_ENCRYPTED = "x-ms-server-encrypted";

        public static final String REQUEST_SERVER_ENCRYPTED = "x-ms-request-server-encrypted";

        public static final String ETAG_WILDCARD = "*";

        public static final String DIRECTORY_METADATA_KEY = "hdi_isfolder";

        public static final String X_MS_META = "x-ms-meta";

        public static final String SMB_PROTOCOL = "SMB";

        public static final String NFS_PROTOCOL = "NFS";

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
         * The versionId parameters.
         */
        public static final String VERSIONID_QUERY_PARAMETER = "versionid";

        /**
         * The deletionId parameters.
         */
        public static final String DELETIONID_QUERY_PARAMETER = "deletionid";

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
         * The SAS preauthorized agent object id parameter for user delegation SAS.
         */
        public static final String SAS_PREAUTHORIZED_AGENT_OBJECT_ID = "saoid";

        /**
         * The SAS agent object id parameter for user delegation SAS.
         */
        public static final String SAS_AGENT_OBJECT_ID = "suoid";

        /**
         * The SAS correlation id parameter for user delegation SAS.
         */
        public static final String SAS_CORRELATION_ID = "scid";

        /**
         * The SAS directory depth parameter.
         */
        public static final String SAS_DIRECTORY_DEPTH = "sdd";

        /**
         * The SAS queue constant.
         */
        public static final String SAS_QUEUE_CONSTANT = "q";

        private UrlConstants() {
            // Private to prevent construction.
        }
    }
}
