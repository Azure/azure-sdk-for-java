/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. Retrieve appropriate version of CanonicalizationStrategy based on the webrequest for Blob
 * and Queue.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
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
    private static final TableLiteCanonicalizer TABLE_LITE_INSTANCE = new TableLiteCanonicalizer();

    /**
     * Gets the Blob queue Canonicalizer full version 2.
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getBlobQueueFullCanonicalizer(final HttpURLConnection conn) {
        if (validateVersionIsSupported(conn)) {
            return BLOB_QUEUE_FULL_V2_INSTANCE;
        }
        else {
            throw new UnsupportedOperationException("Storage protocol version prior to 2009-09-19 are not supported.");
        }
    }

    /**
     * Gets the Blob queue lite Canonicalizer
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getBlobQueueLiteCanonicalizer(final HttpURLConnection conn) {
        if (validateVersionIsSupported(conn)) {
            return BLOB_QUEUE_LITE_INSTANCE;
        }
        else {
            throw new UnsupportedOperationException(
                    "Versions before 2009-09-19 do not support Shared Key Lite for Blob And Queue.");
        }
    }

    /**
     * Gets the Blob queue lite Canonicalizer
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return the appropriate Canonicalizer for the operation.
     */
    protected static Canonicalizer getTableLiteCanonicalizer(final HttpURLConnection conn) {
        if (validateVersionIsSupported(conn)) {
            return TABLE_LITE_INSTANCE;
        }
        else {
            throw new UnsupportedOperationException(
                    "Versions before 2009-09-19 do not support Shared Key Lite for Blob And Queue.");
        }
    }

    /**
     * Determines if the current request is using a protocol post PDC 2009.
     * 
     * @param conn
     *            the HttpURLConnection for the current operation
     * @return <Code>true</Code> if is greater or equal PDC 09'; otherwise, <Code>false</Code>.
     */
    private static Boolean validateVersionIsSupported(final HttpURLConnection conn) {
        final String versionString = Utility.getStandardHeaderValue(conn,
                Constants.HeaderConstants.STORAGE_VERSION_HEADER);

        if (versionString.length() == 0 || versionString.length() == 0) {
            return true;
        }

        try {
            final Calendar versionThresholdCalendar = Calendar.getInstance(Utility.LOCALE_US);
            versionThresholdCalendar.set(2009, Calendar.SEPTEMBER, 19, 0, 0, 0);
            versionThresholdCalendar.set(Calendar.MILLISECOND, 0);

            final DateFormat versionFormat = new SimpleDateFormat("yyyy-MM-dd");
            final Date versionDate = versionFormat.parse(versionString);
            final Calendar requestVersionCalendar = Calendar.getInstance(Utility.LOCALE_US);
            requestVersionCalendar.setTime(versionDate);
            requestVersionCalendar.set(Calendar.HOUR_OF_DAY, 0);
            requestVersionCalendar.set(Calendar.MINUTE, 0);
            requestVersionCalendar.set(Calendar.SECOND, 0);
            requestVersionCalendar.set(Calendar.MILLISECOND, 1);

            return requestVersionCalendar.compareTo(versionThresholdCalendar) >= 0;

        }
        catch (final ParseException e) {
            return false;
        }
    }

    /**
     * Private Default Ctor
     */
    private CanonicalizerFactory() {
        // No op
    }
}
