/**
 * 
 */
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import java.net.HttpURLConnection;
import java.security.InvalidParameterException;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. Provides an implementation of the Canonicalizer class for requests against Table Service
 * under the Shared Key Lite authentication scheme.
 */
class TableLiteCanonicalizer extends Canonicalizer {
    /**
     * Constructs a canonicalized string for signing a request.
     * 
     * @param conn
     *            the HttpURLConnection to canonicalize
     * @param accountName
     *            the account name associated with the request
     * @param contentLength
     *            the length of the content written to the outputstream in bytes, -1 if unknown
     * @param opContext
     *            the OperationContext for the given request
     * @return a canonicalized string.
     * @throws StorageException
     */
    @Override
    protected String canonicalize(final HttpURLConnection conn, final String accountName, final Long contentLength,
            final OperationContext opContext) throws StorageException {
        if (contentLength < -1) {
            throw new InvalidParameterException("ContentLength must be set to -1 or positive Long value");
        }

        final String dateString = Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.DATE);
        if (Utility.isNullOrEmpty(dateString)) {
            throw new IllegalArgumentException(
                    "Canonicalization did not find a non empty x-ms-date header in the request. Please use a request with a valid x-ms-date header in RFC 123 format.");
        }

        final StringBuilder canonicalizedString = new StringBuilder(dateString);
        appendCanonicalizedElement(canonicalizedString, getCanonicalizedResourceLite(conn.getURL(), accountName));

        return canonicalizedString.toString();
    }
}
