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

import java.util.Iterator;

import com.microsoft.azure.storage.blob.ListBlobItem;

/**
 * RESERVED FOR INTERNAL USE. Provides an overlay on the LogBlobIterable class for enumerating Storage Analytics
 * log records. This handles the logic for the listLogRecords() methods found in the
 * <code>CloudAnalyticsClient<code> class.
 */
class LogRecordIterable implements Iterable<LogRecord> {

    /**
     * Holds the iterator from which we get log blobs.
     */
    private final Iterator<ListBlobItem> logBlobIterator;

    public LogRecordIterable(Iterator<ListBlobItem> logBlobIterator) {
        this.logBlobIterator = logBlobIterator;
    }

    @Override
    public Iterator<LogRecord> iterator() {
        return new LogRecordIterator(this.logBlobIterator);
    }
}
