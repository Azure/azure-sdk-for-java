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
 * RESERVED FOR INTERNAL USE. 
 * Retrieve the appropriate version of the canonicalizer based on the service type.
 */
final class CanonicalizerFactory {
    /**
     * The Canonicalizer instance for Blob & Queue
     */
    private static final BlobQueueFileCanonicalizer BLOB_QUEUE_FILE_V2_INSTANCE = new BlobQueueFileCanonicalizer();

    /**
     * The Canonicalizer instance for Table
     */
    private static final TableCanonicalizer TABLE_INSTANCE = new TableCanonicalizer();

    /**
     * Gets the blob, queue or file Canonicalizer version 2.
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getBlobQueueFileCanonicalizer(final HttpURLConnection conn) {
        return BLOB_QUEUE_FILE_V2_INSTANCE;
    }

    /**
     * Gets the table Canonicalizer.
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getTableCanonicalizer(final HttpURLConnection conn) {
        return TABLE_INSTANCE;

    }

    /**
     * A private default constructor. All methods of this class are static so no instances of it should ever be created.
     */
    private CanonicalizerFactory() {
        // No op
    }
}
