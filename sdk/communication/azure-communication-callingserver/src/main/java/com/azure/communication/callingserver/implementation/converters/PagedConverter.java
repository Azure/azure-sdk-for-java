// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation.converters;

import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.paging.PageRetriever;
import reactor.core.publisher.Flux;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A converter for inside type of {@link PagedFlux}
 */
public final class PagedConverter {
    private static <T, S> Function<PagedResponse<T>, PagedResponse<S>> mapPagedResponse(Function<T, S> mapper) {
        return pagedResponse -> new PagedResponseBase<Void, S>(pagedResponse.getRequest(),
            pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue().stream().map(mapper).collect(Collectors.toList()),
            pagedResponse.getContinuationToken(),
            null);
    }

    /**
     * Applies map transform to elements of PagedFlux.
     *
     * @param pagedFlux the input of PagedFlux.
     * @param mapper the map transform of element T to element S.
     * @param <T> input type of PagedFlux.
     * @param <S> return type of PagedFlux.
     * @return the PagedFlux with elements in PagedResponse transformed.
     */
    public static <T, S> PagedFlux<S> mapPage(PagedFlux<T> pagedFlux, Function<T, S> mapper) {
        Supplier<PageRetriever<String, PagedResponse<S>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T>> flux = (continuationToken == null)
                ? pagedFlux.byPage().take(1)
                : pagedFlux.byPage(continuationToken).take(1);
            return flux
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(mapPagedResponse(mapper));
        };
        return PagedFlux.create(provider);
    }
}
