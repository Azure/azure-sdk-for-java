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

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.core.SR;

/**
 * RESERVED FOR INTERNAL USE. Provides an overlay on the LogBlobIterator class for enumerating Storage Analytics
 * log records. This handles the logic for the listLogRecords() methods found in the
 * <code>CloudAnalyticsClient<code> class.
 */
class LogRecordIterator implements Iterator<LogRecord> {

    /**
     * Holds the iterator from which we get log blobs.
     */
    private final Iterator<ListBlobItem> logBlobIterator;

    /**
     * Holds the StreamReader for the current blob.
     */
    private LogRecordStreamReader reader;

    /**
     * Holds the current LogRecord.
     */
    private LogRecord pendingLogRecord;

    /**
     * Flag to indicate whether there's a current record pending.
     */
    private boolean isLogRecordPending = false;

    /**
     * Constructs a new iterator using an underlying LogBlobIterator.
     * 
     * @param logBlobIterator
     * @throws StorageException
     */
    public LogRecordIterator(final Iterator<ListBlobItem> logBlobIterator) {
        this.logBlobIterator = logBlobIterator;
    }

    @Override
    public boolean hasNext() {
        if (this.isLogRecordPending) {
            // Already have a log record cached.
            return true;
        }

        try {
            if (this.reader == null || this.reader.isEndOfFile()) {
                // This reader is not usable 
                if (this.logBlobIterator.hasNext()) {
                    // Valid $logs blobs still pending
                    if (this.reader != null) {
                        // If this isn't our first time, clean up the previous reader.
                        this.reader.close();
                    }
                    CloudBlockBlob nextBlob = (CloudBlockBlob) this.logBlobIterator.next();
                    this.reader = new LogRecordStreamReader(nextBlob.openInputStream());
                    return this.hasNext();
                }
                else {
                    // We are out of $logs blobs altogether.
                    return false;
                }
            }
            else {
                // We still have log records in this $logs blob's reader.
                this.pendingLogRecord = new LogRecord(this.reader);
                this.isLogRecordPending = true;
                return true;
            }
        }
        catch (IOException e) {
            final NoSuchElementException ex = new NoSuchElementException(SR.ENUMERATION_ERROR);
            ex.initCause(e);
            throw ex;
        }
        catch (StorageException e) {
            final NoSuchElementException ex = new NoSuchElementException(SR.ENUMERATION_ERROR);
            ex.initCause(e);
            throw ex;
        }
        catch (ParseException e) {
            final NoSuchElementException ex = new NoSuchElementException(SR.ENUMERATION_ERROR);
            ex.initCause(e);
            throw ex;
        }
        catch (URISyntaxException e) {
            final NoSuchElementException ex = new NoSuchElementException(SR.ENUMERATION_ERROR);
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public LogRecord next() {
        if (this.isLogRecordPending) {
            // Log record already cached.
            this.isLogRecordPending = false;
            return this.pendingLogRecord;
        }

        if (this.hasNext()) {
            // Cache the next log record and try again.
            return this.next();
        }
        else {
            throw new NoSuchElementException(SR.ITERATOR_EMPTY);
        }
    }

    @Override
    public void remove() {
        // Remove not supported.
        throw new UnsupportedOperationException();
    }
}
