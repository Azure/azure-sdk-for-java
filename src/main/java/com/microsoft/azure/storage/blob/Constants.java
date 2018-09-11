/*
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
package com.microsoft.azure.storage.blob;

/**
 * RESERVED FOR INTERNAL USE. Contains storage constants.
 */
final class Constants {

    /**
     * Defines constants for use with HTTP headers.
     */
    static final class HeaderConstants {
        /**
         * The Authorization header.
         */
        static final String AUTHORIZATION = "Authorization";

        /**
         * The format string for specifying ranges with only begin offset.
         */
        static final String BEGIN_RANGE_HEADER_FORMAT = "bytes=%d-";

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
         * The format string for specifying ranges.
         */
        static final String RANGE_HEADER_FORMAT = "bytes=%d-%d";

        /**
         * The current storage version header value.
         */
        static final String TARGET_STORAGE_VERSION = "2018-03-28";

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
        static final String USER_AGENT_VERSION = "10.1.0";

        private HeaderConstants() {
            // Private to prevent construction.
        }
    }

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
     * The default amount of parallelism for TransferManager operations.
     */
    // We chose this to match Go, which followed AWS' default.
    static final int TRANSFER_MANAGER_DEFAULT_PARALLELISM = 5;

    /**
     * Private Default Ctor
     */
    private Constants() {
        // Private to prevent construction.
    }
}