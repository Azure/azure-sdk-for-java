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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.microsoft.azure.storage.StorageException;

/**
 * RESERVED FOR INTERNAL USE. A helper class to help modify the query string of a URI
 */
public final class UriQueryBuilder {
    /**
     * Stores the one to one key/ value collection of query parameters.
     */
    private final HashMap<String, ArrayList<String>> parameters = new HashMap<String, ArrayList<String>>();

    /**
     * Adds a value to the URI with escaping.
     * 
     * @param name
     *            the query key name.
     * @param value
     *            the query value.
     * @throws StorageException
     */
    public void add(final String name, final String value) throws StorageException {
        if (Utility.isNullOrEmpty(name)) {
            throw new IllegalArgumentException(SR.QUERY_PARAMETER_NULL_OR_EMPTY);
        }

        this.insertKeyValue(name, value);
    }

    /**
     * Add query parameter to an existing Uri. This takes care of any existing query parameters in the Uri.
     * 
     * @param uri
     *            the original uri.
     * @return the appended uri
     * @throws URISyntaxException
     *             if the resulting uri is invalid.
     * @throws StorageException
     */
    public URI addToURI(final URI uri) throws URISyntaxException, StorageException {
        final String origRawQuery = uri.getRawQuery();
        final String rawFragment = uri.getRawFragment();
        final String uriString = uri.resolve(uri).toASCIIString();

        final HashMap<String, String[]> origQueryMap = PathUtility.parseQueryString(origRawQuery);

        // Try/Insert original queries to map

        for (final Entry<String, String[]> entry : origQueryMap.entrySet()) {
            for (final String val : entry.getValue()) {
                this.insertKeyValue(entry.getKey(), val);
            }
        }

        final StringBuilder retBuilder = new StringBuilder();

        // has a fragment
        if (Utility.isNullOrEmpty(origRawQuery) && !Utility.isNullOrEmpty(rawFragment)) {
            final int bangDex = uriString.indexOf('#');
            retBuilder.append(uriString.substring(0, bangDex));
        }
        else if (!Utility.isNullOrEmpty(origRawQuery)) {
            // has a query
            final int queryDex = uriString.indexOf('?');
            retBuilder.append(uriString.substring(0, queryDex));
        }
        else {
            // no fragment or query
            retBuilder.append(uriString);
            if (uri.getRawPath().length() <= 0) {
                retBuilder.append("/");
            }
        }

        final String finalQuery = this.toString();

        if (finalQuery.length() > 0) {
            retBuilder.append("?");
            retBuilder.append(finalQuery);
        }

        if (!Utility.isNullOrEmpty(rawFragment)) {
            retBuilder.append("#");
            retBuilder.append(rawFragment);
        }

        return new URI(retBuilder.toString());
    }

    /**
     * Inserts a key / value in the Hashmap, assumes the value is already utf-8 encoded.
     * 
     * @param key
     *            the key to insert
     * @param value
     *            the value to insert
     * @throws StorageException
     */
    private void insertKeyValue(String key, String value) throws StorageException {
        if (value != null) {
            value = Utility.safeEncode(value);
        }
        if (!key.startsWith("$")) {
            key = Utility.safeEncode(key);
        }

        ArrayList<String> list = this.parameters.get(key);
        if (list == null) {
            list = new ArrayList<String>();
            list.add(value);
            this.parameters.put(key, list);
        }
        else {
            if (!list.contains(value)) {
                list.add(value);
            }
        }
    }

    /**
     * Returns a string that represents this instance. This will construct the full URI.
     * 
     * @return a string that represents this instance.
     */
    @Override
    public String toString() {
        final StringBuilder outString = new StringBuilder();
        Boolean isFirstPair = true;

        for (final String key : this.parameters.keySet()) {
            if (this.parameters.get(key) != null) {
                for (final String val : this.parameters.get(key)) {
                    if (isFirstPair) {
                        isFirstPair = false;
                    }
                    else {
                        outString.append("&");
                    }

                    outString.append(String.format("%s=%s", key, val));
                }
            }
        }

        return outString.toString();
    }
}
