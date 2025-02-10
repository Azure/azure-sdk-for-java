// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.util.logging.ClientLogger;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 * This class is a {@link Map} implementation that filters out headers that are not allowed to be set on an HTTP
 * request.
 * <p>
 * Based on logic used in {@link java.net.http.HttpHeaders#of(Map, BiPredicate)} it's known that the headers will be
 * accessed using {@link Map#forEach(BiConsumer)}. Give that, this class can be optimized to use the forEach method
 * on the raw HttpHeaders map used in azure-core which can filter out the headers that are not allowed to be set using
 * the known lowercase header names, then return the cased header name and values.
 */
final class HeaderFilteringMap extends AbstractMap<String, List<String>> {
    private final Map<String, HttpHeader> rawHeaders;
    private final Set<String> restrictedHeaders;
    private final ClientLogger logger;

    /**
     * Creates a new HeaderFilteringMap.
     *
     * @param rawHeaders The raw headers map.
     * @param restrictedHeaders The header filter.
     * @param logger The logger to log any errors.
     */
    HeaderFilteringMap(Map<String, HttpHeader> rawHeaders, Set<String> restrictedHeaders, ClientLogger logger) {
        this.rawHeaders = rawHeaders;
        this.restrictedHeaders = restrictedHeaders;
        this.logger = logger;
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        throw logger.logExceptionAsError(
            new UnsupportedOperationException("The only operation permitted by this Map is forEach."));
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super List<String>> action) {
        rawHeaders.forEach((headerName, header) -> {
            if (restrictedHeaders.contains(headerName)) {
                logger.atWarning()
                    .addKeyValue("headerName", headerName)
                    .log("The header is restricted by 'java.net.http.HttpClient' and will be ignored. To allow this "
                        + "header to be set on the request, configure 'jdk.httpclient.allowRestrictedHeaders' with the "
                        + "header added in the comma-separated list.");
            } else {
                action.accept(header.getName(), header.getValuesList());
            }
        });
    }
}
