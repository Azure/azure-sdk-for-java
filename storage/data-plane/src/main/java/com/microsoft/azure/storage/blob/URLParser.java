/*
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
package com.microsoft.azure.storage.blob;

import java.net.URL;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class used to conveniently parse URLs into {@link BlobURLParts} to modify the components of the URL.
 */
public final class URLParser {

    /**
     * URLParser parses a URL initializing BlobURLParts' fields including any SAS-related and snapshot query parameters.
     * Any other query parameters remain in the UnparsedParams field. This method overwrites all fields in the
     * BlobURLParts object.
     *
     * @param url
     *         The {@code URL} to be parsed.
     *
     * @return A {@link BlobURLParts} object containing all the components of a BlobURL.
     *
     * @throws UnknownHostException
     *         If the url contains an improperly formatted ipaddress or unknown host address.
     */
    public static BlobURLParts parse(URL url) throws UnknownHostException {

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
                .withScheme(scheme)
                .withHost(host)
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshot(snapshot)
                .withSasQueryParameters(sasQueryParameters)
                .withUnparsedParameters(queryParamsMap);
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
                retVals.put(key, keyValues);
            } else {
                // map contains this key already so append
                final String[] newValues = new String[keyValues.length + 1];
                for (int j = 0; j < keyValues.length; j++) {
                    newValues[j] = keyValues[j];
                }

                newValues[newValues.length - 1] = value;
            }
        }

        return retVals;
    }
}
