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
package com.microsoft.azure.storage.analytics;

import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;

import com.microsoft.azure.storage.LoggingOperations;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;

/**
 * RESERVED FOR INTERNAL USE. Provides an overlay on the LazySegmentedIterable class for enumerating Storage Analytics
 * log blobs. This handles the logic for the listLogBlobs() methods found in the <code>CloudAnalyticsClient<code> class.
 */
class LogBlobIterable implements Iterable<ListBlobItem> {

    /**
     * Holds a reference to the parent object, i.e. the log CloudBlobDirectory.
     */
    private final CloudBlobDirectory logDirectory;

    /**
     * Holds an object used to track the execution of the operation
     */
    private final OperationContext opContext;

    /**
     * Holds an object representing the start of the log timeframe desired.
     */
    private final Date startTime;

    /**
     * Holds an object representing the end of the log timeframe desired.
     */
    private final Date endTime;

    /**
     * Holds an enumeration set of the listing details desired (metadata or none).
     */
    private final EnumSet<BlobListingDetails> details;

    /**
     * Holds an object containing other options for the request.
     */
    private final BlobRequestOptions options;

    /**
     * Holds an enumeration set of the log types desired.
     */
    private final EnumSet<LoggingOperations> operations;

    protected LogBlobIterable(final CloudBlobDirectory logDirectory, final Date startTime, final Date endTime,
            final EnumSet<LoggingOperations> operations, final EnumSet<BlobListingDetails> details,
            final BlobRequestOptions options, final OperationContext opContext) {
        this.logDirectory = logDirectory;
        this.startTime = startTime;
        this.endTime = endTime;
        this.operations = operations;
        this.details = details;
        this.options = options;
        this.opContext = opContext;
    }

    @Override
    public Iterator<ListBlobItem> iterator() {
        return new LogBlobIterator(this.logDirectory, this.startTime, this.endTime, this.operations, this.details,
                this.options, this.opContext);
    }
}
