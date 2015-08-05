/**
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

import com.microsoft.azure.storage.Constants;

/**
 * Holds the Constants used for the Blob Service.
 */
final class BlobConstants {
    
    /**
     * Specifies the append blob type.
     */
    public static final String APPEND_BLOB = "AppendBlob";
    
    /**
     * XML element for authentication error details.
     */
    public static final String AUTHENTICATION_ERROR_DETAIL = "AuthenticationErrorDetail";

    /**
     * The header that specifies blob content MD5.
     */
    public static final String BLOB_CONTENT_MD5_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-content-md5";

    /**
     * XML element for a blob.
     */
    public static final String BLOB_ELEMENT = "Blob";

    /**
     * XML element for blob prefixes.
     */
    public static final String BLOB_PREFIX_ELEMENT = "BlobPrefix";

    /**
     * The header that specifies public access to blobs.
     */
    public static final String BLOB_PUBLIC_ACCESS_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-public-access";

    /**
     * XML element for a blob type.
     */
    public static final String BLOB_TYPE_ELEMENT = "BlobType";
    /**
     * The header for the blob type.
     */
    public static final String BLOB_TYPE_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-type";

    /**
     * XML element for blobs.
     */
    public static final String BLOBS_ELEMENT = "Blobs";

    /**
     * Specifies the block blob type.
     */
    public static final String BLOCK_BLOB = "BlockBlob";
    
    /**
     * XML element for blocks.
     */
    public static final String BLOCK_ELEMENT = "Block";

    /**
     * XML element for a block list.
     */
    public static final String BLOCK_LIST_ELEMENT = "BlockList";

    /**
     * XML element for committed blocks.
     */
    public static final String COMMITTED_BLOCKS_ELEMENT = "CommittedBlocks";

    /**
     * XML element for committed blocks.
     */
    public static final String COMMITTED_ELEMENT = "Committed";

    /**
     * XML element for a container.
     */
    public static final String CONTAINER_ELEMENT = "Container";

    /**
     * XML element for containers.
     */
    public static final String CONTAINERS_ELEMENT = "Containers";

    /**
     * The header that specifies blob content encoding.
     */
    public static final String CONTENT_DISPOSITION_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER
            + "blob-content-disposition";

    /**
     * The header that specifies blob content encoding.
     */
    public static final String CONTENT_ENCODING_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-content-encoding";

    /**
     * The header that specifies blob content language.
     */
    public static final String CONTENT_LANGUAGE_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-content-language";

    /**
     * The header that specifies blob content length.
     */
    public static final String CONTENT_LENGTH_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-content-length";

    /**
     * The header that specifies blob content type.
     */
    public static final String CONTENT_TYPE_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-content-type";

    /**
     * The number of default concurrent requests for parallel operation.
     */
    public static final int DEFAULT_CONCURRENT_REQUEST_COUNT = 1;

    /**
     * The default timeout of a copy operation.
     */
    public static final int DEFAULT_COPY_TIMEOUT_IN_SECONDS = 3600;

    /**
     * The default delimiter used to create a virtual directory structure of blobs.
     */
    public static final String DEFAULT_DELIMITER = "/";

    /**
     * The default polling interval of a copy operation.
     */
    public static final int DEFAULT_POLLING_INTERVAL_IN_SECONDS = 30;

    /**
     * The default maximum size, in bytes, of a blob before it must be separated into blocks.
     */
    public static final int DEFAULT_SINGLE_BLOB_PUT_THRESHOLD_IN_BYTES = 32 * Constants.MB;

    /**
     * Specifies snapshots are to be included.
     */
    public static final String INCLUDE_SNAPSHOTS_VALUE = "include";

    /**
     * XML element for the latest.
     */
    public static final String LATEST_ELEMENT = "Latest";

    /**
     * The maximum size, in bytes, of a blob before it must be separated into blocks
     */
    // Note if this is updated then Constants.MAX_MARK_LENGTH needs to be as well.
    public static final int MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES = 64 * Constants.MB;

    /**
     * Specifies the page blob type.
     */
    public static final String PAGE_BLOB = "PageBlob";

    /**
     * XML element for page list elements.
     */
    public static final String PAGE_LIST_ELEMENT = "PageList";

    /**
     * XML element for a page range.
     */
    public static final String PAGE_RANGE_ELEMENT = "PageRange";

    /**
     * The header that specifies page write mode.
     */
    public static final String PAGE_WRITE = Constants.PREFIX_FOR_STORAGE_HEADER + "page-write";

    /**
     * The header for specifying the sequence number.
     */
    public static final String SEQUENCE_NUMBER = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-sequence-number";

    /**
     * The header for the blob content length.
     */
    public static final String SIZE = Constants.PREFIX_FOR_STORAGE_HEADER + "blob-content-length";

    /**
     * XML element for the block length.
     */
    public static final String SIZE_ELEMENT = "Size";

    /**
     * The Snapshot value.
     */
    public static final String SNAPSHOT = "snapshot";

    /**
     * XML element for a snapshot.
     */
    public static final String SNAPSHOT_ELEMENT = "Snapshot";

    /**
     * The header for snapshots.
     */
    public static final String SNAPSHOT_HEADER = Constants.PREFIX_FOR_STORAGE_HEADER + "snapshot";

    /**
     * Specifies only snapshots are to be included.
     */
    public static final String SNAPSHOTS_ONLY_VALUE = "only";

    /**
     * XML element for page range start elements.
     */
    public static final String START_ELEMENT = "Start";

    /**
     * XML element for uncommitted blocks.
     */
    public static final String UNCOMMITTED_BLOCKS_ELEMENT = "UncommittedBlocks";

    /**
     * XML element for uncommitted blocks.
     */
    public static final String UNCOMMITTED_ELEMENT = "Uncommitted";

    /**
     * Query component value for lease.
     */
    public static final String LEASE = "lease";

    /**
     * Private Default Constructor.
     */
    private BlobConstants() {
        // No op
    }
}
