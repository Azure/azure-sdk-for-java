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
/**
 * 
 */
package com.microsoft.azure.storage.core;

import java.net.HttpURLConnection;

/**
 * RESERVED FOR INTERNAL USE. Retrieve appropriate version of CanonicalizationStrategy based on the webrequest for Blob
 * and Queue.
 */
final class CanonicalizerFactory {
    /**
     * The Canonicalizer instance for Blob & Queue
     */
    private static final BlobQueueFullCanonicalizer BLOB_QUEUE_FULL_V2_INSTANCE = new BlobQueueFullCanonicalizer();

    /**
     * The Canonicalizer instance for Blob & Queue Shared Key Lite
     */
    private static final BlobQueueLiteCanonicalizer BLOB_QUEUE_LITE_INSTANCE = new BlobQueueLiteCanonicalizer();

    /**
     * The Canonicalizer instance for Table
     */
    private static final TableFullCanonicalizer TABLE_FULL_INSTANCE = new TableFullCanonicalizer();

    /**
     * The Canonicalizer instance for Table Lite
     */
    private static final TableLiteCanonicalizer TABLE_LITE_INSTANCE = new TableLiteCanonicalizer();

    /**
     * Gets the Blob queue Canonicalizer full version 2.
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getBlobQueueFullCanonicalizer(final HttpURLConnection conn) {
        return BLOB_QUEUE_FULL_V2_INSTANCE;
    }

    /**
     * Gets the Blob queue lite Canonicalizer
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getBlobQueueLiteCanonicalizer(final HttpURLConnection conn) {
        return BLOB_QUEUE_LITE_INSTANCE;
    }

    /**
     * Gets the table full Canonicalizer.
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getTableFullCanonicalizer(final HttpURLConnection conn) {
        return TABLE_FULL_INSTANCE;

    }

    /**
     * Gets the table lite Canonicalizer
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getTableLiteCanonicalizer(final HttpURLConnection conn) {
        return TABLE_LITE_INSTANCE;
    }

    /**
     * Private Default Ctor
     */
    private CanonicalizerFactory() {
        // No op
    }
}
