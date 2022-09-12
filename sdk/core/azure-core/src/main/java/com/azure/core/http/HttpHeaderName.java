// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents well-known HTTP header names.
 */
public final class HttpHeaderName {
    private static final Map<String, HttpHeaderName> KNOWN_HEADER_NAMES;
    private static final Map<String, HttpHeaderName> DYNAMIC_HEADER_NAMES = new ConcurrentHashMap<>();

    static {
        // TODO (alzimmer): Populate with constant values below.
        KNOWN_HEADER_NAMES = new HashMap<>(400);
    }

    private final String http1Name;
    private final String http2Name;

    private HttpHeaderName(String name) {
        this.http1Name = name;
        this.http2Name = name.toLowerCase(Locale.ROOT);
    }

    public String getHttp1Name() {
        return http1Name;
    }

    public String getHttp2Name() {
        return http2Name;
    }

    public static HttpHeaderName fromString(String name) {
        if (name == null) {
            return null;
        }

        HttpHeaderName httpHeaderName = KNOWN_HEADER_NAMES.get(name);
        if (httpHeaderName != null) {
            return httpHeaderName;
        }

        httpHeaderName = DYNAMIC_HEADER_NAMES.get(name);
        if (httpHeaderName != null) {
            return httpHeaderName;
        }

        if (DYNAMIC_HEADER_NAMES.size() > 10000) {
            DYNAMIC_HEADER_NAMES.clear();
        }

        return DYNAMIC_HEADER_NAMES.computeIfAbsent(name, HttpHeaderName::new);
    }

    /**
     * {@code Accept}/{@code accept}
     */
    public static final HttpHeaderName ACCEPT = new HttpHeaderName("Accept");

    /**
     * {@code Accept-Datetime}/{@code accept-datetime}
     */
    public static final HttpHeaderName ACCEPT_DATETIME = new HttpHeaderName("Accept-Datetime");

    public static final HttpHeaderName ALLOW = new HttpHeaderName("Allow");

    public static final HttpHeaderName AUTHORIZATION = new HttpHeaderName("Authorization");

    public static final HttpHeaderName AZURE_ASYNCOPERATION = new HttpHeaderName("Azure-AsyncOperation");

    public static final HttpHeaderName CACHE_CONTROL = new HttpHeaderName("Cache-Control");

    public static final HttpHeaderName CAPABILITIES_ID = new HttpHeaderName("capabilities-id");

    public static final HttpHeaderName CLIENTREQUESTID = new HttpHeaderName("clientRequestId");

    public static final HttpHeaderName CONNECTION = new HttpHeaderName("Connection");

    public static final HttpHeaderName CONSISTENCYLEVEL = new HttpHeaderName("ConsistencyLevel");

    public static final HttpHeaderName CONTENT_DISPOSITION = new HttpHeaderName("Content-Disposition");
    public static final HttpHeaderName CONTENT_ENCODING = new HttpHeaderName("Content-Encoding");
    public static final HttpHeaderName CONTENT_LANGUAGE = new HttpHeaderName("Content-Language");

    /**
     * {@code Content-Length}/{@code content-length}
     */
    public static final HttpHeaderName CONTENT_LENGTH = new HttpHeaderName("Content-Length");

    public static final HttpHeaderName CONTENT_MD5 = new HttpHeaderName("Content-MD5");

    public static final HttpHeaderName CONTENT_RANGE = new HttpHeaderName("Content-Range");

    public static final HttpHeaderName CONTENT_TRANSFER_ENCODING = new HttpHeaderName("Content-Transfer-Encoding");

    /**
     * {@code Content-Type}/{@code content-type}
     */
    public static final HttpHeaderName CONTENT_TYPE = new HttpHeaderName("Content-Type");

    public static final HttpHeaderName DATA_SERVICE_VERSION = new HttpHeaderName("DataServiceVersion");

    public static final HttpHeaderName DATE = new HttpHeaderName("Date");

    public static final HttpHeaderName DOCKER_CONTENT_DIGEST = new HttpHeaderName("Docker-Content-Digest");
    public static final HttpHeaderName DOCKER_UPLOAD_UUID = new HttpHeaderName("Docker-Upload-UUID");

    public static final HttpHeaderName ETag = new HttpHeaderName("ETag");

    public static final HttpHeaderName IF_MATCH = new HttpHeaderName("If-Match");
    public static final HttpHeaderName IF_MODIFIED_SINCE = new HttpHeaderName("If-Modified-Since");
    public static final HttpHeaderName IF_NONE_MATCH = new HttpHeaderName("If-None-Match");
    public static final HttpHeaderName IF_UNMODIFIED_SINCE = new HttpHeaderName("If-Unmodified-Since");
    public static final HttpHeaderName LAST_MODIFIED = new HttpHeaderName("Last-Modified");
    public static final HttpHeaderName LINK = new HttpHeaderName("Link");
    public static final HttpHeaderName LOCATION = new HttpHeaderName("Location");
    public static final HttpHeaderName MAX_ITEMS_PER_PAGE = new HttpHeaderName("max-items-per-page");
    public static final HttpHeaderName MS_CV = new HttpHeaderName("MS-CV");
    public static final HttpHeaderName OPERATION_ID = new HttpHeaderName("operation-id");
    public static final HttpHeaderName OPERATION_LOCATION = new HttpHeaderName("Operation-Location");
    public static final HttpHeaderName QUERY_CHARGE = new HttpHeaderName("query-charge");
    public static final HttpHeaderName PREFER = new HttpHeaderName("Prefer");
    public static final HttpHeaderName PROXY_AUTHENTICATE = new HttpHeaderName("Proxy-Authenticate");
    public static final HttpHeaderName PROXY_AUTHORIZATION = new HttpHeaderName("Proxy-Authorization");
    public static final HttpHeaderName PURCHASE_ID = new HttpHeaderName("purchase-id");
    public static final HttpHeaderName RANGE = new HttpHeaderName("Range");

    public static final HttpHeaderName REFERER = new HttpHeaderName("Referer");

    public static final HttpHeaderName RELEASE_ID = new HttpHeaderName("release-id");

    public static final HttpHeaderName REPEATABILITY_FIRST_SENT = new HttpHeaderName("repeatability-first-sent");
    public static final HttpHeaderName REPEATABILITY_RESULT = new HttpHeaderName("Repeatability-Result");
    public static final HttpHeaderName REPEATABILITY_REQUEST_ID = new HttpHeaderName("repeatability-request-id");

    public static final HttpHeaderName RETRY_AFTER = new HttpHeaderName("Retry-After");

    public static final HttpHeaderName SCHEMA_GROUP_NAME = new HttpHeaderName("Schema-Group-Name");
    public static final HttpHeaderName SCHEMA_ID = new HttpHeaderName("Schema-Id");
    public static final HttpHeaderName SCHEMA_ID_LOCATION = new HttpHeaderName("Schema-Id-Location");
    public static final HttpHeaderName SCHEMA_NAME = new HttpHeaderName("Schema-Name");
    public static final HttpHeaderName SCHEMA_VERSION = new HttpHeaderName("Schema-Version");

    public static final HttpHeaderName SEARCH_ID = new HttpHeaderName("search-id");

    public static final HttpHeaderName TELEMETRY_SOURCE_TIME = new HttpHeaderName("Telemetry-Source-Time");
    public static final HttpHeaderName TRACEPARENT = new HttpHeaderName("traceparent");
    public static final HttpHeaderName TRACESTATE = new HttpHeaderName("tracestate");

    public static final HttpHeaderName USER_AGENT = new HttpHeaderName("User-Agent");


    // A different pattern that could be taken is to only have RFC defined HTTP headers defined as constants
    // and provide a well-known factory method that adds headers into the well-known Map that won't be cleared if it
    // hits a certain size.
    public static final HttpHeaderName X_MS_ACCESS_TIER = new HttpHeaderName("x-ms-access-tier");
    public static final HttpHeaderName X_MS_ACL = new HttpHeaderName("x-ms-acl");
    public static final HttpHeaderName X_MS_BLOB_APPEND_OFFSET = new HttpHeaderName("x-ms-blob-append-offset");
    public static final HttpHeaderName X_MS_BLOB_CACHE_CONTROL = new HttpHeaderName("x-ms-blob-cache-control");
    public static final HttpHeaderName X_MS_BLOB_COMMITTED_BLOCK_COUNT
        = new HttpHeaderName("x-ms-blob-committed-block-count");
    public static final HttpHeaderName X_MS_BLOB_CONDITION_APPENDPOS
        = new HttpHeaderName("x-ms-blob-condition-appendpos");
    public static final HttpHeaderName X_MS_BLOB_CONDITION_MAXSIZE = new HttpHeaderName("x-ms-blob-condition-maxsize");
    public static final HttpHeaderName X_MS_BLOB_CONTENT_DISPOSITION
        = new HttpHeaderName("x-ms-blob-content-disposition");
    public static final HttpHeaderName X_MS_BLOB_CONTENT_ENCODING = new HttpHeaderName("x-ms-blob-content-encoding");
    public static final HttpHeaderName X_MS_BLOB_CONTENT_LANGUAGE = new HttpHeaderName("x-ms-blob-content-language");
    public static final HttpHeaderName X_MS_BLOB_CONTENT_MD5 = new HttpHeaderName("x-ms-blob-content-md5");
    public static final HttpHeaderName X_MS_BLOB_CONTENT_TYPE = new HttpHeaderName("x-ms-blob-content-type");
    public static final HttpHeaderName X_MS_BLOB_PUBLIC_ACCESS = new HttpHeaderName("x-ms-blob-public-access");
    public static final HttpHeaderName X_MS_BLOB_SEQUENCE_NUMBER = new HttpHeaderName("x-ms-blob-sequence-number");
    public static final HttpHeaderName X_MS_BLOB_TYPE = new HttpHeaderName("x-ms-blob-type");
    public static final HttpHeaderName X_MS_CACHE_CONTROL = new HttpHeaderName("x-ms-cache-control");
    public static final HttpHeaderName X_MS_CLIENT_ID = new HttpHeaderName("x-ms-client-id");
    public static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = new HttpHeaderName("x-ms-client-request-id");
    public static final HttpHeaderName X_MS_CLIENT_TENANT_ID = new HttpHeaderName("x-ms-client-tenant-id");
    public static final HttpHeaderName X_MS_CONTENT_CRC64 = new HttpHeaderName("x-ms-content-crc64");
    public static final HttpHeaderName X_MS_CONTENT_DISPOSITION = new HttpHeaderName("x-ms-content-disposition");
    public static final HttpHeaderName X_MS_CONTENT_ENCODING = new HttpHeaderName("x-ms-content-encoding");
    public static final HttpHeaderName X_MS_CONTENT_LANGUAGE = new HttpHeaderName("x-ms-content-language");
    public static final HttpHeaderName X_MS_CONTENT_LENGTH = new HttpHeaderName("x-ms-content-length");
    public static final HttpHeaderName X_MS_CONTENT_MD5 = new HttpHeaderName("x-ms-content-md5");
    public static final HttpHeaderName X_MS_CONTENT_TYPE = new HttpHeaderName("x-ms-content-type");
    public static final HttpHeaderName X_MS_CONTINUATION = new HttpHeaderName("x-ms-continuation");
    public static final HttpHeaderName X_MS_COPY_ACTION = new HttpHeaderName("x-ms-copy-action");
    public static final HttpHeaderName X_MS_COPY_SOURCE = new HttpHeaderName("x-ms-copy-source");
    public static final HttpHeaderName X_MS_COPY_SOURCE_AUTHORIZATION
        = new HttpHeaderName("x-ms-copy-source-authorization");
    public static final HttpHeaderName X_MS_COPY_SOURCE_BLOB_PROPERTIES
        = new HttpHeaderName("x-ms-copy-source-blob-properties");
    public static final HttpHeaderName X_MS_COPY_SOURCE_TAG_OPTION = new HttpHeaderName("x-ms-copy-source-tag-option");
    public static final HttpHeaderName X_MS_DEFAULT_ENCRYPTION_SCOPE
        = new HttpHeaderName("x-ms-default-encryption-scope");
    public static final HttpHeaderName X_MS_DELETE_SNAPSHOTS = new HttpHeaderName("x-ms-delete-snapshots");
    public static final HttpHeaderName X_MS_DELETED_CONTAINER_NAME = new HttpHeaderName("x-ms-deleted-container-name");
    public static final HttpHeaderName X_MS_DELETED_CONTAINER_VERSION
        = new HttpHeaderName("x-ms-deleted-container-version");
    public static final HttpHeaderName X_MS_DELETED_SHARE_NAME = new HttpHeaderName("x-ms-deleted-share-name");
    public static final HttpHeaderName X_MS_DELETED_SHARE_VERSION = new HttpHeaderName("x-ms-deleted-share-version");
    public static final HttpHeaderName X_MS_DENY_ENCRYPTION_SCOPE_OVERRIDE
        = new HttpHeaderName("x-ms-deny-encryption-scope-override");
    public static final HttpHeaderName X_MS_DESTINATION_LEASE_ID = new HttpHeaderName("x-ms-destination-lease-id");
    public static final HttpHeaderName X_MS_ENABLED_PROTOCOLS = new HttpHeaderName("x-ms-enabled-protocols");
    public static final HttpHeaderName X_MS_ENCRYPTION_ALGORITHM = new HttpHeaderName("x-ms-encryption-algorithm");
    public static final HttpHeaderName X_MS_ENCRYPTION_KEY = new HttpHeaderName("x-ms-encryption-key");
    public static final HttpHeaderName X_MS_ENCRYPTION_KEY_SHA256 = new HttpHeaderName("x-ms-encryption-key-sha256");
    public static final HttpHeaderName X_MS_ENCRYPTION_SCOPE = new HttpHeaderName("x-ms-encryption-scope");
    public static final HttpHeaderName X_MS_EXPIRY_OPTION = new HttpHeaderName("x-ms-expiry-option");
    public static final HttpHeaderName X_MS_EXPIRY_TIME = new HttpHeaderName("x-ms-expiry-time");
    public static final HttpHeaderName X_MS_FILE_ATTRIBUTES = new HttpHeaderName("x-ms-file-attributes");
    public static final HttpHeaderName X_MS_FILE_CHANGE_TIME = new HttpHeaderName("x-ms-file-change-time");
    public static final HttpHeaderName X_MS_FILE_COPY_SET_ARCHIVE = new HttpHeaderName("x-ms-file-copy-set-archive");
    public static final HttpHeaderName X_MS_FILE_CREATION_TIME = new HttpHeaderName("x-ms-file-creation-time");
    public static final HttpHeaderName X_MS_FILE_EXTENDED_INFO = new HttpHeaderName("x-ms-file-extended-info");
    public static final HttpHeaderName X_MS_FILE_LAST_WRITE_TIME = new HttpHeaderName("x-ms-file-last-write-time");
    public static final HttpHeaderName X_MS_FILE_PERMISSION = new HttpHeaderName("x-ms-file-permission");
    public static final HttpHeaderName X_MS_FILE_PERMISSION_COPY_MODE
        = new HttpHeaderName("x-ms-file-permission-copy-mode");
    public static final HttpHeaderName X_MS_FILE_PERMISSION_KEY = new HttpHeaderName("x-ms-file-permission-key");
    public static final HttpHeaderName X_MS_FILE_RENAME_IGNORE_READONLY
        = new HttpHeaderName("x-ms-file-rename-ignore-readonly");
    public static final HttpHeaderName X_MS_FILE_RENAME_REPLACE_IF_EXISTS
        = new HttpHeaderName("x-ms-file-rename-replace-if-exists");
    public static final HttpHeaderName X_MS_FILE_RENAME_SOURCE = new HttpHeaderName("x-ms-file-rename-source");
    public static final HttpHeaderName X_MS_GROUP = new HttpHeaderName("x-ms-group");
    public static final HttpHeaderName X_MS_HANDLE_ID = new HttpHeaderName("x-ms-handle-id");
    public static final HttpHeaderName X_MS_IF_SEQUENCE_NUMBER_EQ = new HttpHeaderName("x-ms-if-sequence-number-eq");
    public static final HttpHeaderName X_MS_IF_SEQUENCE_NUMBER_LE = new HttpHeaderName("x-ms-if-sequence-number-le");
    public static final HttpHeaderName X_MS_IF_SEQUENCE_NUMBER_LT = new HttpHeaderName("x-ms-if-sequence-number-lt");
    public static final HttpHeaderName X_MS_IF_TAGS = new HttpHeaderName("x-ms-if-tags");
    public static final HttpHeaderName X_MS_LEASE_ACTION = new HttpHeaderName("x-ms-lease-action");
    public static final HttpHeaderName X_MS_LEASE_BREAK_PERIOD = new HttpHeaderName("x-ms-lease-break-period");
    public static final HttpHeaderName X_MS_LEASE_DURATION = new HttpHeaderName("x-ms-lease-duration");
    public static final HttpHeaderName X_MS_LEASE_ID = new HttpHeaderName("x-ms-lease-id");
    public static final HttpHeaderName X_MS_LEGAL_HOLD = new HttpHeaderName("x-ms-legal-hold");
    public static final HttpHeaderName X_MS_IMMUTABILITY_POLICY_MODE
        = new HttpHeaderName("x-ms-immutability-policy-mode");
    public static final HttpHeaderName X_MS_IMMUTABILITY_POLICY_UNTIL_DATE
        = new HttpHeaderName("x-ms-immutability-policy-until-date");
    public static final HttpHeaderName X_MRC_CV = new HttpHeaderName("X-MRC-CV");
    public static final HttpHeaderName X_MS_OWNER = new HttpHeaderName("x-ms-owner");
    public static final HttpHeaderName X_MS_PAGE_WRITE = new HttpHeaderName("x-ms-page-write");
    public static final HttpHeaderName X_MS_PERMISSIONS = new HttpHeaderName("x-ms-permissions");
    public static final HttpHeaderName X_MS_PREVIOUS_SNAPSHOT_URL = new HttpHeaderName("x-ms-previous-snapshot-url");
    public static final HttpHeaderName X_MS_PROPERTIES = new HttpHeaderName("x-ms-properties");
    public static final HttpHeaderName X_MS_PROPOSED_LEASE_ID = new HttpHeaderName("x-ms-proposed-lease-id");
    public static final HttpHeaderName X_MS_RANGE = new HttpHeaderName("x-ms-range");
    public static final HttpHeaderName X_MS_RANGE_GET_CONTENT_CRC64
        = new HttpHeaderName("x-ms-range-get-content-crc64");
    public static final HttpHeaderName X_MS_RANGE_GET_CONTENT_MD5 = new HttpHeaderName("x-ms-range-get-content-md5");
    public static final HttpHeaderName X_MS_RECURSIVE = new HttpHeaderName("x-ms-recursive");
    public static final HttpHeaderName X_MS_REHYDRATION_PRIORITY = new HttpHeaderName("x-ms-rehydration-priority");
    public static final HttpHeaderName X_MS_RENAME_SOURCE = new HttpHeaderName("x-ms-rename-source");
    public static final HttpHeaderName X_MS_REQUEST_ID = new HttpHeaderName("x-ms-request-id");
    public static final HttpHeaderName X_MS_REQUEST_SERVER_ENCRYPTED
        = new HttpHeaderName("x-ms-request-server-encrypted");
    public static final HttpHeaderName X_MS_REQUIRES_SYNC = new HttpHeaderName("x-ms-requires-sync");
    public static final HttpHeaderName X_MS_ROOT_SQUASH = new HttpHeaderName("x-ms-root-squash");
    public static final HttpHeaderName X_MS_SEAL_BLOB = new HttpHeaderName("x-ms-seal-blob");
    public static final HttpHeaderName X_MS_SEQUENCE_NUMBER_ACTION = new HttpHeaderName("x-ms-sequence-number-action");
    public static final HttpHeaderName X_MS_SHARE_QUOTA = new HttpHeaderName("x-ms-share-quota");
    public static final HttpHeaderName X_MS_SNAPSHOT = new HttpHeaderName("x-ms-snapshot");
    public static final HttpHeaderName X_MS_SOURCE_CONTAINER_NAME = new HttpHeaderName("x-ms-source-container-name");
    public static final HttpHeaderName X_MS_SOURCE_CONTENT_CRC64 = new HttpHeaderName("x-ms-source-content-crc64");
    public static final HttpHeaderName X_MS_SOURCE_CONTENT_MD5 = new HttpHeaderName("x-ms-source-content-md5");
    public static final HttpHeaderName X_MS_SOURCE_IF_MATCH = new HttpHeaderName("x-ms-source-if-match");
    public static final HttpHeaderName X_MS_SOURCE_IF_MATCH_CRC64 = new HttpHeaderName("x-ms-source-if-match-crc64");
    public static final HttpHeaderName X_MS_SOURCE_IF_MODIFIED_SINCE
        = new HttpHeaderName("x-ms-source-if-modified-since");
    public static final HttpHeaderName X_MS_SOURCE_IF_NONE_MATCH = new HttpHeaderName("x-ms-source-if-none-match");
    public static final HttpHeaderName X_MS_SOURCE_IF_NONE_MATCH_CRC64
        = new HttpHeaderName("x-ms-source-if-none-match-crc64");
    public static final HttpHeaderName X_MS_SOURCE_IF_TAGS = new HttpHeaderName("x-ms-source-if-tags");
    public static final HttpHeaderName X_MS_SOURCE_IF_UNMODIFIED_SINCE
        = new HttpHeaderName("x-ms-source-if-unmodified-since");
    public static final HttpHeaderName X_MS_SOURCE_LEASE_ID = new HttpHeaderName("x-ms-source-lease-id");
    public static final HttpHeaderName X_MS_SOURCE_RANGE = new HttpHeaderName("x-ms-source-range");
    public static final HttpHeaderName X_MS_STATUS_LOCATION = new HttpHeaderName("x-ms-status-location");
    public static final HttpHeaderName X_MS_TAGS = new HttpHeaderName("x-ms-tags");
    public static final HttpHeaderName X_MS_UMASK = new HttpHeaderName("x-ms-umask");
    public static final HttpHeaderName X_MS_UNDELETE_SOURCE = new HttpHeaderName("x-ms-undelete-source");
    public static final HttpHeaderName X_MS_VERSION = new HttpHeaderName("x-ms-version");
}
