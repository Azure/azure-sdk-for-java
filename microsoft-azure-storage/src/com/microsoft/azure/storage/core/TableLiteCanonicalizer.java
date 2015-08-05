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
import java.security.InvalidParameterException;

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. Provides an implementation of the Canonicalizer class for requests against Table Service
 * under the Shared Key Lite authentication scheme.
 */
class TableLiteCanonicalizer extends Canonicalizer {

    /**
     * The expected length for the canonicalized string when SharedKeyLite is used to sign table requests.
     */
    private static final int ExpectedTableLiteCanonicalizedStringLength = 150;

    /**
     * Constructs a canonicalized string for signing a request.
     * 
     * @param conn
     *            the HttpURLConnection to canonicalize
     * @param accountName
     *            the account name associated with the request
     * @param contentLength
     *            the length of the content written to the outputstream in bytes, -1 if unknown
     * @return a canonicalized string.
     * @throws StorageException
     */
    @Override
    protected String canonicalize(final HttpURLConnection conn, final String accountName, final Long contentLength)
            throws StorageException {
        if (contentLength < -1) {
            throw new InvalidParameterException(SR.INVALID_CONTENT_LENGTH);
        }

        final String dateString = Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.DATE);
        if (Utility.isNullOrEmpty(dateString)) {
            throw new IllegalArgumentException(SR.MISSING_MANDATORY_DATE_HEADER);
        }
        final StringBuilder canonicalizedString = new StringBuilder(ExpectedTableLiteCanonicalizedStringLength);
        canonicalizedString.append(dateString);
        appendCanonicalizedElement(canonicalizedString, getCanonicalizedResourceLite(conn.getURL(), accountName));

        return canonicalizedString.toString();
    }
}
