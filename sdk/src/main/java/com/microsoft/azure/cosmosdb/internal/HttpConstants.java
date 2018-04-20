/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal;

/**
 * Used internally. HTTP constants in the Azure Cosmos DB database service Java SDK.
 */
public class HttpConstants {
    public static class HttpMethods {
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String HEAD = "HEAD";
        public static final String OPTIONS = "OPTIONS";
        public static final String PATCH = "PATCH";
    }

    public static class QueryStrings {
        public static final String URL = "$resolveFor";
        public static final String FILTER = "$filter";
        public static final String PARTITION_KEY_RANGE_IDS = "$partitionKeyRangeIds";
    }

    public static class HttpHeaders {
        public static final String AUTHORIZATION = "authorization";
        public static final String E_TAG = "etag";
        public static final String METHOD_OVERRIDE = "X-HTTP-Method";
        public static final String SLUG = "Slug";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String LAST_MODIFIED = "Last-Modified";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String CHARACTER_SET = "CharacterSet";
        public static final String USER_AGENT = "User-Agent";
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        public static final String IF_MATCH = "If-Match";
        public static final String IF_NONE_MATCH = "If-None-Match";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String ACCEPT_ENCODING = "Accept-Encoding";
        public static final String KEEP_ALIVE = "Keep-Alive";
        public static final String CONNECTION = "Connection";
        public static final String CACHE_CONTROL = "Cache-Control";
        public static final String TRANSFER_ENCODING = "Transfer-Encoding";
        public static final String CONTENT_LANGUAGE = "Content-Language";
        public static final String CONTENT_LOCATION = "Content-Location";
        public static final String CONTENT_MD5 = "Content-Md5";
        public static final String CONTENT_RANGE = "Content-Range";
        public static final String ACCEPT = "Accept";
        public static final String ACCEPT_CHARSET = "Accept-Charset";
        public static final String ACCEPT_LANGUAGE = "Accept-Language";
        public static final String IF_RANGE = "If-Range";
        public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
        public static final String MAX_FORWARDS = "Max-Forwards";
        public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
        public static final String ACCEPT_RANGES = "Accept-Ranges";
        public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
        public static final String RETRY_AFTER = "Retry-After";
        public static final String SET_COOKIE = "Set-Cookie";
        public static final String WWW_AUTHENTICATE = "Www-Authenticate";
        public static final String ORIGIN = "Origin";
        public static final String HOST = "Host";
        public static final String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
        public static final String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";
        public static final String KEY_VALUE_ENCODING_FORMAT = "application/x-www-form-urlencoded";
        public static final String WRAP_ASSERTION_FORMAT = "wrap_assertion_format";
        public static final String WRAP_ASSERTION = "wrap_assertion";
        public static final String WRAP_SCOPE = "wrap_scope";
        public static final String SIMPLE_TOKEN = "SWT";
        public static final String HTTP_DATE = "date";
        public static final String PREFER = "Prefer";
        public static final String LOCATION = "Location";
        public static final String REFERER = "referer";

        // Query
        public static final String QUERY = "x-ms-documentdb-query";
        public static final String IS_QUERY = "x-ms-documentdb-isquery";
        public static final String ENABLE_CROSS_PARTITION_QUERY = "x-ms-documentdb-query-enablecrosspartition";
        public static final String PARALLELIZE_CROSS_PARTITION_QUERY = "x-ms-documentdb-query-parallelizecrosspartitionquery";

        // Our custom DocDB headers
        public static final String CONTINUATION = "x-ms-continuation";
        public static final String PAGE_SIZE = "x-ms-max-item-count";

        // Request sender generated. Simply echoed by backend.
        public static final String ACTIVITY_ID = "x-ms-activity-id";
        public static final String PRE_TRIGGER_INCLUDE = "x-ms-documentdb-pre-trigger-include";
        public static final String PRE_TRIGGER_EXCLUDE = "x-ms-documentdb-pre-trigger-exclude";
        public static final String POST_TRIGGER_INCLUDE = "x-ms-documentdb-post-trigger-include";
        public static final String POST_TRIGGER_EXCLUDE = "x-ms-documentdb-post-trigger-exclude";
        public static final String INDEXING_DIRECTIVE = "x-ms-indexing-directive";
        public static final String SESSION_TOKEN = "x-ms-session-token";
        public static final String CONSISTENCY_LEVEL = "x-ms-consistency-level";
        public static final String X_DATE = "x-ms-date";
        public static final String COLLECTION_PARTITION_INFO = "x-ms-collection-partition-info";
        public static final String COLLECTION_SERVICE_INFO = "x-ms-collection-service-info";
        public static final String RETRY_AFTER_IN_MILLISECONDS = "x-ms-retry-after-ms";
        public static final String IS_FEED_UNFILTERED = "x-ms-is-feed-unfiltered";
        public static final String RESOURCE_TOKEN_EXPIRY = "x-ms-documentdb-expiry-seconds";
        public static final String ENABLE_SCAN_IN_QUERY = "x-ms-documentdb-query-enable-scan";
        public static final String EMIT_VERBOSE_TRACES_IN_QUERY = "x-ms-documentdb-query-emit-traces";

        //Request validation
        public static final String REQUEST_VALIDATION_FAILURE = "x-ms-request-validation-failure";

        // Quota Info
        public static final String MAX_RESOURCE_QUOTA = "x-ms-resource-quota";
        public static final String CURRENT_RESOURCE_QUOTA_USAGE = "x-ms-resource-usage";
        public static final String MAX_MEDIA_STORAGE_USAGE_IN_MB = "x-ms-max-media-storage-usage-mb";

        // Usage Info
        public static final String REQUEST_CHARGE = "x-ms-request-charge";
        public static final String CURRENT_MEDIA_STORAGE_USAGE_IN_MB = "x-ms-media-storage-usage-mb";
        public static final String DATABASE_ACCOUNT_CONSUMED_DOCUMENT_STORAGE_IN_MB = "x-ms-databaseaccount-consumed-mb";
        public static final String DATABASE_ACCOUNT_RESERVED_DOCUMENT_STORAGE_IN_MB = "x-ms-databaseaccount-reserved-mb";
        public static final String DATABASE_ACCOUNT_PROVISIONED_DOCUMENT_STORAGE_IN_MB = "x-ms-databaseaccount-provisioned-mb";

        // Address related headers.
        public static final String FORCE_REFRESH = "x-ms-force-refresh";
        public static final String ITEM_COUNT = "x-ms-item-count";
        public static final String NEW_RESOURCE_ID = "x-ms-new-resource-id";
        public static final String USE_MASTER_COLLECTION_RESOLVER = "x-ms-use-master-collection-resolver";

        // Admin Headers
        public static final String FULL_UPGRADE = "x-ms-force-full-upgrade";
        public static final String ONLY_UPGRADE_SYSTEM_APPLICATIONS = "x-ms-only-upgrade-system-applications";
        public static final String ONLY_UPGRADE_NON_SYSTEM_APPLICATIONS = "x-ms-only-upgrade-non-system-applications";
        public static final String UPGRADE_FABRIC_RING_CODE_AND_CONFIG = "x-ms-upgrade-fabric-code-config";
        public static final String IGNORE_IN_PROGRESS_UPGRADE = "x-ms-ignore-inprogress-upgrade";
        public static final String UPGRADE_VERIFICATION_KIND = "x-ms-upgrade-verification-kind";
        public static final String IS_CANARY = "x-ms-iscanary";
        public static final String FORCE_DELETE = "x-ms-force-delete";

        // Version headers and values
        public static final String VERSION = "x-ms-version";
        public static final String SCHEMA_VERSION = "x-ms-schemaversion";
        public static final String SERVER_VERSION = "x-ms-serviceversion";
        public static final String GATEWAY_VERSION = "x-ms-gatewayversion";

        // RDFE Resource Provider headers
        public static final String OCP_RESOURCE_PROVIDER_REGISTERED_URI = "ocp-resourceprovider-registered-uri";

        // For Document service management operations only. This is in
        // essence a 'handle' to (long running) operations.
        public static final String REQUEST_ID = "x-ms-request-id";

        // Object returning this determines what constitutes state and what
        // last state change means. For replica, it is the last role change.
        public static final String LAST_STATE_CHANGE_UTC = "x-ms-last-state-change-utc";

        // CSM specific headers
        // Client-request-id: Optional caller-specified request ID, in the form
        // of a GUID
        public static final String CLIENT_REQUEST_ID = "x-ms-client-request-id";

        // Offer header
        public static final String OFFER_TYPE = "x-ms-offer-type";
        public static final String OFFER_THROUGHPUT = "x-ms-offer-throughput";
        public static final String OFFER_IS_RU_PER_MINUTE_THROUGHPUT_ENABLED = "x-ms-offer-is-ru-per-minute-throughput-enabled";

        // Upsert header
        public static final String IS_UPSERT = "x-ms-documentdb-is-upsert";

        // Index progress headers
        public static final String INDEX_TRANSFORMATION_PROGRESS = "x-ms-documentdb-collection-index-transformation-progress";
        public static final String LAZY_INDEXING_PROGRESS = "x-ms-documentdb-collection-lazy-indexing-progress";

        //Owner name
        public static final String OWNER_FULL_NAME = "x-ms-alt-content-path";

        // Owner ID used for name based request in session token.
        public static final String OWNER_ID = "x-ms-content-path";

        // Partition headers
        public static final String PARTITION_KEY = "x-ms-documentdb-partitionkey";
        public static final String PARTITION_KEY_RANGE_ID = "x-ms-documentdb-partitionkeyrangeid";

        // Error response sub status code
        public static final String SUB_STATUS = "x-ms-substatus";

        public static final String LSN = "lsn";

        // Custom DocDB JavaScript logging headers
        public static final String SCRIPT_ENABLE_LOGGING = "x-ms-documentdb-script-enable-logging";
        public static final String SCRIPT_LOG_RESULTS = "x-ms-documentdb-script-log-results";

        // Collection quota
        public static final String POPULATE_QUOTA_INFO = "x-ms-documentdb-populatequotainfo";

        // ChangeFeed
        public static final String A_IM = "A-IM";
    }
    
    public static class A_IMHeaderValues {
        public static final String INCREMENTAL_FEED = "Incremental Feed";
    }

    public static class Versions {
        public static final String CURRENT_VERSION = "2017-11-15";
        
        // TODO: FIXME we can use maven plugin for generating a version file
        // @see https://stackoverflow.com/questions/2469922/generate-a-version-java-file-in-maven
        public static final String SDK_VERSION = "1.0.1";
        public static final String SDK_NAME = "cosmosdb-java-sdk";
    }

    public static class StatusCodes {
        public static final int NOT_MODIFIED = 304;
        // Client error
        public static final int MINIMUM_STATUSCODE_AS_ERROR_GATEWAY = 400;
        public static final int BADREQUEST = 400;
        public static final int FORBIDDEN = 403;
        public static final int NOTFOUND = 404;
        public static final int GONE = 410;
        public static final int TOO_MANY_REQUESTS = 429;
        public static final int RETRY_WITH = 449;
        public static final int TIMEOUT = 408;

    }

    public static class SubStatusCodes {
        // 400: Bad Request substatus
        public static final int PARTITION_KEY_MISMATCH = 1001;
        public static final int CROSS_PARTITION_QUERY_NOT_SERVABLE = 1004;

        // 410: StatusCodeType_Gone: substatus
        public static final int NAME_CACHE_IS_STALE = 1000;
        public static final int PARTITION_KEY_RANGE_GONE = 1002;
        public static final int SPLITTING = 1007;

        // 403: Forbidden substatus
        public static final int FORBIDDEN_WRITEFORBIDDEN = 3;
        public static final int DATABASE_ACCOUNT_NOTFOUND = 1008;

        // 404: LSN in session token is higher
        public static final int READ_SESSION_NOT_AVAILABLE = 1002;
    }
}
