// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation;

/***
 * Constants to be used by CallingServer.
 */
public final class Constants {
    public static final int KB = 1024;
    public static final int MB = KB * 1024;

    /***
     * Content downloader constants
     */
    public static class ContentDownloader {
        public static final int DEFAULT_CONCURRENT_TRANSFERS_COUNT = 5;
        public static final int DEFAULT_BUFFER_SIZE = 4 * MB;
        public static final int DEFAULT_INITIAL_DOWNLOAD_RANGE_SIZE = 256 * MB;
        public static final int MAX_RETRIES = 4;
    }

    /***
     * HTTP Header Names
     */
    public static class HeaderNames {
        public static final String RANGE = "Range";
        public static final String CONTENT_RANGE = "Content-Range";
    }
}
