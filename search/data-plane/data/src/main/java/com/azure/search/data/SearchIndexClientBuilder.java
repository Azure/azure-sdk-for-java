package com.azure.search.data;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.data.generated.models.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The Fluent client builder.
 */
public interface SearchIndexClientBuilder {
    // Fluent builder
    SearchIndexClient buildClient();
    SearchIndexClient buildAsyncClient();
}
