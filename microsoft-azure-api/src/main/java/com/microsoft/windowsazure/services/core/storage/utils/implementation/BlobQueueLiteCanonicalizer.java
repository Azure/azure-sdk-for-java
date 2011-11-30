/**
 * Copyright 2011 Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage.utils.implementation;

import java.net.HttpURLConnection;
import java.security.InvalidParameterException;

import com.microsoft.windowsazure.services.core.storage.Constants;
import com.microsoft.windowsazure.services.core.storage.OperationContext;
import com.microsoft.windowsazure.services.core.storage.StorageException;
import com.microsoft.windowsazure.services.core.storage.utils.Utility;

/**
 * RESERVED FOR INTERNAL USE. Provides an implementation of the Canonicalizer class for requests against Blob and Queue
 * Service under the Shared Key authentication scheme.
 */
final class BlobQueueLiteCanonicalizer extends Canonicalizer {

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

        return canonicalizeHttpRequestLite(conn.getURL(), accountName, conn.getRequestMethod(),
                Utility.getStandardHeaderValue(conn, Constants.HeaderConstants.CONTENT_TYPE), contentLength, null,
                conn, opContext);
    }
}
