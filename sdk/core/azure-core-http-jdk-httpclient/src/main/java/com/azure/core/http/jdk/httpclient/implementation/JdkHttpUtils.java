// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;

import java.util.List;
import java.util.Map;

/**
 * Utility class for JDK HttpClient.
 */
public final class JdkHttpUtils {
    /**
     * Converts the given JDK Http headers to azure-core Http header.
     *
     * @param headers the JDK Http headers
     * @return the azure-core Http headers
     */
    @SuppressWarnings("deprecation")
    public static HttpHeaders fromJdkHttpHeaders(java.net.http.HttpHeaders headers) {
        final HttpHeaders httpHeaders = new HttpHeaders((int) (headers.map().size() / 0.75F));

        for (Map.Entry<String, List<String>> kvp : headers.map().entrySet()) {
            if (!CoreUtils.isNullOrEmpty(kvp.getValue())) {
                httpHeaders.set(kvp.getKey(), kvp.getValue());
            }
        }

        return httpHeaders;
    }
}
