// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.common.implementation.Constants;

/**
 * Constants used in Blob Client classes.
 * <p>
 * RESERVED FOR INTERNAL USE.
 */
public final class BlobConstants {
    /**
     * Special container name for the root container in the Storage account.
     */
    public static final String ROOT_CONTAINER_NAME = "$root";
    /**
     * Special container name for the static website container in the Storage account.
     */
    public static final String STATIC_WEBSITE_CONTAINER_NAME = "$web";
    /**
     * Special container name for the logs container in the Storage account.
     */
    public static final String LOG_CONTAINER_NAME = "$logs";
    /**
     * The block size to use if none is specified in parallel operations.
     */
    public static final int BLOB_DEFAULT_UPLOAD_BLOCK_SIZE = 4 * Constants.MB;
    /**
     * The number of buffers to use if none is specified on the buffered upload method.
     */
    public static final int BLOB_DEFAULT_NUMBER_OF_BUFFERS = 8;
    /**
     * If a blob  is known to be greater than 100MB, using a larger block size will trigger some server-side
     * optimizations. If the block size is not set and the size of the blob is known to be greater than 100MB, this
     * value will be used.
     */
    public static final int BLOB_DEFAULT_HTBB_UPLOAD_BLOCK_SIZE = 8 * Constants.MB;
    /**
     * The default block size used in BlobBaseClient and BlobBaseAsyncClient's uploadFromFile.
     */
    public static final int DEFAULT_FILE_READ_CHUNK_SIZE = 1024 * 64;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     */
    public static final int MAX_APPEND_BLOCK_BYTES_VERSIONS_2021_12_02_AND_BELOW = 4 * Constants.MB;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to appendBlock.
     * For versions 2022-11-02 and above.
     */
    public static final int MAX_APPEND_BLOCK_BYTES_VERSIONS_2022_11_02_AND_ABOVE = 100 * Constants.MB;
    /**
     * Indicates the maximum number of blocks allowed in an append/block blob.
     */
    public static final int MAX_BLOCKS = 50000;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to upload.
     */
    public static final long MAX_UPLOAD_BLOB_BYTES_LONG = 5000L * Constants.MB;
    /**
     * Indicates the maximum number of bytes that can be sent in a call to stageBlock.
     */
    public static final long MAX_STAGE_BLOCK_BYTES_LONG = 4000L * Constants.MB;
    /**
     * Indicates the number of bytes in a page.
     */
    public static final int PAGE_BYTES = 512;
    /**
     * Indicates the maximum number of bytes that may be sent in a call to putPage.
     */
    public static final int MAX_PUT_PAGES_BYTES = 4 * Constants.MB;
}
