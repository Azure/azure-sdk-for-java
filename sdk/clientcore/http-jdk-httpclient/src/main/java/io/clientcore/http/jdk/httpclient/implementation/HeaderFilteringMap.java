// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.jdk.httpclient.implementation;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.util.ClientLogger;

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
 * on the raw HttpHeaders map used in clientcore which can filter out the headers that are not allowed to be set using
 * the known lowercase header names, then return the cased header name and values.
 */
final class HeaderFilteringMap extends AbstractMap<String, List<String>> {
    private final HttpHeaders headers;
    private final Set<String> restrictedHeaders;
    private final ClientLogger logger;

    /**
     * Creates a new HeaderFilteringMap.
     *
     * @param headers The headers.
     * @param restrictedHeaders The header filter.
     * @param logger The logger to log any errors.
     */
    HeaderFilteringMap(HttpHeaders headers, Set<String> restrictedHeaders, ClientLogger logger) {
        this.headers = headers;
        this.restrictedHeaders = restrictedHeaders;
        this.logger = logger;
    }

    @Override
    public Set<Entry<String, List<String>>> entrySet() {
        throw logger.logThrowableAsError(
            new UnsupportedOperationException("The only operation permitted by this Map is forEach."));
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super List<String>> action) {
        headers.forEach(header -> {
            if (restrictedHeaders.contains(header.getName().getCaseInsensitiveName())) {
                logger.atWarning()
                    .addKeyValue("headerName", header.getName())
                    .log("The header is restricted by 'java.net.http.HttpClient' and will be ignored. To allow this "
                        + "header to be set on the request, configure 'jdk.httpclient.allowRestrictedHeaders' with the "
                        + "header added in the comma-separated list.");
            } else {
                action.accept(header.getName().getValue(), header.getValues());
            }
        });
    }
}
