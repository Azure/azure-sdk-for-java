// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.implementation.util.ImplUtils;

import java.net.URL;
import java.util.Map;

/**
 * A class used to conveniently parse URLs into {@link BlobURLParts} to modify the components of the URL.
 */
final class URLParser {

    /**
     * URLParser parses a URL initializing BlobURLParts' fields including any SAS-related and snapshot query parameters.
     * Any other query parameters remain in the UnparsedParams field. This method overwrites all fields in the
     * BlobURLParts object.
     *
     * @param url The {@code URL} to be parsed.
     * @return A {@link BlobURLParts} object containing all the components of a BlobURL.
     */
    public static BlobURLParts parse(URL url) {

        final String scheme = url.getProtocol();
        final String host = url.getHost();

        String containerName = null;
        String blobName = null;

        // find the container & blob names (if any)
        String path = url.getPath();
        if (!ImplUtils.isNullOrEmpty(path)) {
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
        Map<String, String> queryParamsMap = com.azure.storage.common.Utility.parseQueryString(url.getQuery());

        String snapshot = queryParamsMap.remove("snapshot");

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
}
