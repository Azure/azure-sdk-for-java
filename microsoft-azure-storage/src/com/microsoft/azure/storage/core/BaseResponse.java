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
package com.microsoft.azure.storage.core;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.microsoft.azure.storage.Constants;

/**
 * RESERVED FOR INTERNAL USE. The base response class for the protocol layer
 */
public class BaseResponse {
    /**
     * Gets the ContentMD5
     * 
     * @param request
     *            The response from server.
     * @return The ContentMD5.
     */
    public static String getContentMD5(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.CONTENT_MD5);
    }

    /**
     * Gets the Date
     * 
     * @param request
     *            The response from server.
     * @return The Date.
     */
    public static String getDate(final HttpURLConnection request) {
        final String retString = request.getHeaderField("Date");
        return retString == null ? request.getHeaderField(Constants.HeaderConstants.DATE) : retString;
    }

    /**
     * Gets the Etag
     * 
     * @param request
     *            The response from server.
     * @return The Etag.
     */
    public static String getEtag(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.ETAG);
    }

    /**
     * Gets the metadata from the request The response from server.
     * 
     * @return the metadata from the request
     */
    public static HashMap<String, String> getMetadata(final HttpURLConnection request) {
        return getValuesByHeaderPrefix(request, Constants.HeaderConstants.PREFIX_FOR_STORAGE_METADATA);
    }

    /**
     * Gets the request id.
     * 
     * @param request
     *            The response from server.
     * @return The request ID.
     */
    public static String getRequestId(final HttpURLConnection request) {
        return request.getHeaderField(Constants.HeaderConstants.REQUEST_ID_HEADER);
    }

    /**
     * Returns all the header/value pairs with the given prefix.
     * 
     * @param request
     *            the request object containing headers to parse.
     * @param prefix
     *            the prefix for headers to be returned.
     * @return all the header/value pairs with the given prefix.
     */
    private static HashMap<String, String> getValuesByHeaderPrefix(final HttpURLConnection request, final String prefix) {
        final HashMap<String, String> retVals = new HashMap<String, String>();
        final Map<String, List<String>> headerMap = request.getHeaderFields();
        final int prefixLength = prefix.length();

        for (final Entry<String, List<String>> entry : headerMap.entrySet()) {
            if (entry.getKey() != null && entry.getKey().startsWith(prefix)) {
                final List<String> currHeaderValues = entry.getValue();
                retVals.put(entry.getKey().substring(prefixLength), currHeaderValues.get(0));
            }
        }

        return retVals;
    }

    /**
     * Private Default Ctor
     */
    protected BaseResponse() {
        // No op
    }
}
