// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import java.net.URL;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class used to conveniently parse URLs into {@link BlobURLParts} to modify the components of the URL.
 */
final class URLParser {

    /**
     * URLParser parses a URL initializing BlobURLParts' fields including any SAS-related and snapshot query parameters.
     * Any other query parameters remain in the UnparsedParams field. This method overwrites all fields in the
     * BlobURLParts object.
     *
     * @param url
     *         The {@code URL} to be parsed.
     *
     * @return A {@link BlobURLParts} object containing all the components of a BlobURL.
     */
    public static BlobURLParts parse(URL url) {

        final String scheme = url.getProtocol();
        final String host = url.getHost();

        String containerName = null;
        String blobName = null;

        // find the container & blob names (if any)
        String path = url.getPath();
        if (!Utility.isNullOrEmpty(path)) {
            // if the path starts with a slash remove it
            if (path.charAt(0) == '/') {
                path = path.substring(1);
            }

            int containerEndIndex = path.indexOf('/');
            if (containerEndIndex == -1) {
                // path contains only a container name and no blob name
                containerName = path;
            } else {
                // path contains the container name up until the slash and blob name is everything after the slash
                containerName = path.substring(0, containerEndIndex);
                blobName = path.substring(containerEndIndex + 1);
            }
        }
        Map<String, String[]> queryParamsMap = parseQueryString(url.getQuery());

        String snapshot = null;
        String[] snapshotArray = queryParamsMap.get("snapshot");
        if (snapshotArray != null) {
            snapshot = snapshotArray[0];
            queryParamsMap.remove("snapshot");
        }

        SASQueryParameters sasQueryParameters = new SASQueryParameters(queryParamsMap, true);

        return new BlobURLParts()
                .scheme(scheme)
                .host(host)
                .containerName(containerName)
                .blobName(blobName)
                .snapshot(snapshot)
                .sasQueryParameters(sasQueryParameters)
                .unparsedParameters(queryParamsMap);
    }

    /**
     * Parses a query string into a one to many hashmap.
     *
     * @param queryParams
     *         The string of query params to parse.
     *
     * @return A {@code HashMap<String, String[]>} of the key values.
     */
    private static TreeMap<String, String[]> parseQueryString(String queryParams) {

        final TreeMap<String, String[]> retVals = new TreeMap<String, String[]>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        if (Utility.isNullOrEmpty(queryParams)) {
            return retVals;
        }

        // split name value pairs by splitting on the 'c&' character
        final String[] valuePairs = queryParams.split("&");

        // for each field value pair parse into appropriate map entries
        for (int m = 0; m < valuePairs.length; m++) {
            // Getting key and value for a single query parameter
            final int equalDex = valuePairs[m].indexOf("=");
            String key = Utility.safeURLDecode(valuePairs[m].substring(0, equalDex)).toLowerCase(Locale.ROOT);
            String value = Utility.safeURLDecode(valuePairs[m].substring(equalDex + 1));

            // add to map
            String[] keyValues = retVals.get(key);

            // check if map already contains key
            if (keyValues == null) {
                // map does not contain this key
                keyValues = new String[]{value};
            } else {
                // map contains this key already so append
                final String[] newValues = new String[keyValues.length + 1];
                for (int j = 0; j < keyValues.length; j++) {
                    newValues[j] = keyValues[j];
                }

                newValues[newValues.length - 1] = value;
                keyValues = newValues;
            }
            retVals.put(key, keyValues);
        }

        return retVals;
    }
}
