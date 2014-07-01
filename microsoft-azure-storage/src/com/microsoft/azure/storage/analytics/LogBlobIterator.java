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

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import com.microsoft.azure.storage.LoggingOperations;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobListingDetails;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobDirectory;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.core.SR;

/**
 * RESERVED FOR INTERNAL USE. Provides an overlay on the LazySegmentedIterator class for enumerating Storage Analytics
 * log blobs. This handles the logic for the listLogBlobs() methods found in the <code>CloudAnalyticsClient<code> class.
 */
class LogBlobIterator implements Iterator<ListBlobItem> {
    private static final String HOUR_STRING = "yyyy/MM/dd/HH";

    private static final String DAY_STRING = "yyyy/MM/dd";

    private static final String MONTH_STRING = "yyyy/MM";

    private static final String YEAR_STRING = "yyyy";

    private static final DateFormat HOUR_FORMAT = new SimpleDateFormat(HOUR_STRING);

    private static final DateFormat DAY_FORMAT = new SimpleDateFormat(DAY_STRING);

    private static final DateFormat MONTH_FORMAT = new SimpleDateFormat(MONTH_STRING);

    private static final DateFormat YEAR_FORMAT = new SimpleDateFormat(YEAR_STRING);

    private static final int HOUR_FORMAT_LENGTH = HOUR_STRING.length();

    private static final int DAY_FORMAT_LENGTH = DAY_STRING.length();

    private static final int MONTH_FORMAT_LENGTH = MONTH_STRING.length();

    /**
     * Holds a reference to the parent log CloudBlobDirectory.
     */
    private final CloudBlobDirectory logDirectory;

    /**
     * Holds the start date and time of the log range requested.
     */
    private Calendar startDate = null;

    /**
     * Holds the end date and time of the log range requested.
     */
    private Calendar endDate = null;

    /**
     * Holds the value of which log types are being requested.
     */
    private final EnumSet<LoggingOperations> operations;

    /**
     * Holds the value of which blob details are being requested.
     * 
     * Note that presently we only allow none or metadata to be specified here.
     */
    private final EnumSet<BlobListingDetails> details;

    /**
     * Holds the request options to use when making the listBlob() requests.
     */
    private final BlobRequestOptions options;

    /**
     * Holds an object used to track the execution of the operation.
     */
    private final OperationContext opContext;

    /**
     * Holds the iterator for the current call.
     */
    private Iterator<ListBlobItem> currentIterator;

    /**
     * Represents the current time prefix being passed to listBlobs().
     */
    private String currentPrefixTime = null;

    /**
     * Represents the prefix that marks the end
     */
    private String endPrefix;

    /**
     * Represents whether a valid log has been already retrieved.
     */
    private Boolean isItemPending = false;

    /**
     * Represents whether this iterator has already passed the present moment or its corresponding endTime.
     */
    private Boolean isExpired = false;

    /**
     * The valid retrieved log that isItemPending refers to.
     */
    private ListBlobItem pendingItem;

    public LogBlobIterator(final CloudBlobDirectory logDirectory, final Date startDate, final Date endDate,
            final EnumSet<LoggingOperations> operations, final EnumSet<BlobListingDetails> details,
            final BlobRequestOptions options, final OperationContext opContext) {
        TimeZone gmtTime = TimeZone.getTimeZone("GMT");
        HOUR_FORMAT.setTimeZone(gmtTime);
        DAY_FORMAT.setTimeZone(gmtTime);
        MONTH_FORMAT.setTimeZone(gmtTime);
        YEAR_FORMAT.setTimeZone(gmtTime);

        this.logDirectory = logDirectory;
        this.operations = operations;
        this.details = details;
        this.opContext = opContext;

        if (options == null) {
            this.options = new BlobRequestOptions();
        }
        else {
            this.options = options;
        }

        if (startDate != null) {
            this.startDate = new GregorianCalendar();
            this.startDate.setTime(startDate);
            this.startDate.add(GregorianCalendar.MINUTE, (-this.startDate.get(GregorianCalendar.MINUTE)));
            this.startDate.setTimeZone(gmtTime);
        }
        if (endDate != null) {
            this.endDate = new GregorianCalendar();
            this.endDate.setTime(endDate);
            this.endDate.setTimeZone(gmtTime);
            this.endPrefix = this.logDirectory.getPrefix() + HOUR_FORMAT.format(this.endDate.getTime());
        }
    }

    @Override
    public boolean hasNext() {
        if (this.isItemPending) {
            // Short circuit
            return true;
        }

        try {
            if (this.currentIterator == null) {
                // If this is the first time, get the first iterator before entering loop.
                updateIterator();
            }
            while (!this.isExpired) {
                while (this.currentIterator.hasNext()) {
                    // Go through all of the logs in this iterator and see if any are of the correct type.
                    ListBlobItem current = this.currentIterator.next();
                    if (this.endDate == null || (current.getParent().getPrefix()).compareTo(this.endPrefix) <= 0) {
                        if (isCorrectLogType(current)) {
                            this.pendingItem = current;
                            this.isItemPending = true;
                            return true;
                        }
                    }
                    else {
                        // We have passed endTime
                        this.isExpired = true;
                        return false;
                    }
                }
                updateIterator();
            }
        }
        catch (final StorageException e) {
            final NoSuchElementException ex = new NoSuchElementException(SR.ENUMERATION_ERROR);
            ex.initCause(e);
            throw ex;
        }
        catch (final URISyntaxException e) {
            final NoSuchElementException ex = new NoSuchElementException(SR.ENUMERATION_ERROR);
            ex.initCause(e);
            throw ex;
        }

        return false;
    }

    @Override
    public ListBlobItem next() {
        if (this.isItemPending) {
            this.isItemPending = false;
            return this.pendingItem;
        }

        if (this.hasNext()) {
            return this.next();
        }
        else {
            throw new NoSuchElementException(SR.ITERATOR_EMPTY);
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Validates that the log given is of the correct log type.
     * 
     * @param current
     *            the current log
     * @return whether or not the log is of the correct type.
     */
    private boolean isCorrectLogType(ListBlobItem current) {
        HashMap<String, String> metadata = ((CloudBlob) current).getMetadata();
        String logType = metadata.get("LogType");

        if (logType == null) {
            return true;
        }

        if (this.operations.contains(LoggingOperations.READ) && logType.contains("read")) {
            return true;
        }

        if (this.operations.contains(LoggingOperations.WRITE) && logType.contains("write")) {
            return true;
        }

        if (this.operations.contains(LoggingOperations.DELETE) && logType.contains("delete")) {
            return true;
        }

        return false;
    }

    /**
     * Makes the next listBlob call if necessary and updates the currentIterator.
     * 
     * @throws StorageException
     * @throws URISyntaxException
     */
    private void updateIterator() throws StorageException, URISyntaxException {
        if (this.currentPrefixTime != null && this.currentPrefixTime.isEmpty()) {
            // If we've already called listBlobs() with an empty prefix, don't do so again.
            this.isExpired = true;
            return;
        }

        GregorianCalendar now = new GregorianCalendar();
        now.add(GregorianCalendar.HOUR_OF_DAY, 1);
        now.setTimeZone(TimeZone.getTimeZone("GMT"));
        updatePrefix();
        if ((this.startDate == null || this.startDate.compareTo(now) <= 0)
                && (this.endDate == null || ((this.logDirectory.getPrefix() + this.currentPrefixTime)
                        .compareTo(this.endPrefix) <= 0))) {
            // Only make the next call if the prefix is still possible
            this.currentIterator = this.logDirectory.listBlobs(this.currentPrefixTime, true, this.details,
                    this.options, this.opContext).iterator();
        }
        else {
            // We are in the future.
            this.isExpired = true;
        }
    }

    /**
     * Updates the currentPrefixTime so that we can make a new call to listBlobs() with the next prefix.
     */
    private void updatePrefix() {
        if (this.startDate == null) {
            // startDate not specified
            this.currentPrefixTime = "";
        }
        else if (this.currentPrefixTime == null) {
            // First prefix 
            this.currentPrefixTime = HOUR_FORMAT.format(this.startDate.getTime());
        }
        else if (this.currentPrefixTime.length() == HOUR_FORMAT_LENGTH) {
            // Increment the hour
            this.startDate.add(GregorianCalendar.HOUR_OF_DAY, 1);
            if (this.startDate.get(GregorianCalendar.HOUR_OF_DAY) != 0) {
                // If we are still within the same day, use the hour format
                this.currentPrefixTime = HOUR_FORMAT.format(this.startDate.getTime());
            }
            else {
                // If we've reached a day boundary, get the entire next day's logs
                this.currentPrefixTime = DAY_FORMAT.format(this.startDate.getTime());
            }
        }
        else if (this.currentPrefixTime.length() == DAY_FORMAT_LENGTH) {
            // Increment the day
            this.startDate.add(GregorianCalendar.DAY_OF_MONTH, 1);
            if (this.startDate.get(GregorianCalendar.DAY_OF_MONTH) != 1) {
                // If we are still within the same month, use the day format
                this.currentPrefixTime = DAY_FORMAT.format(this.startDate.getTime());
            }
            else {
                // If we've reached a month boundary, get the entire next month's logs
                this.currentPrefixTime = MONTH_FORMAT.format(this.startDate.getTime());
            }
        }
        else if (this.currentPrefixTime.length() == MONTH_FORMAT_LENGTH) {
            // Increment the month
            this.startDate.add(GregorianCalendar.MONTH, 1);
            if (this.startDate.get(GregorianCalendar.MONTH) != 13) { // Undecember
                // If we are still within the same year, use the month format
                this.currentPrefixTime = MONTH_FORMAT.format(this.startDate.getTime());
            }
            else {
                // If we've reached a year boundary, get the entire next year's logs
                this.currentPrefixTime = YEAR_FORMAT.format(this.startDate.getTime());
            }
        }
        else {
            // Continue to increment year and get the next year's worth of logs.
            this.startDate.add(GregorianCalendar.YEAR, 1);
            this.currentPrefixTime = YEAR_FORMAT.format(this.startDate.getTime());
        }
    }
}
