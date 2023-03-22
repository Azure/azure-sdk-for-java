// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import com.azure.cosmos.implementation.query.TriFunction;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

public final class OpenConnectionOperation {

    private final TriFunction<URI, Uri, String, Mono<OpenConnectionResponse>> openConnectionFunc;
    private final DocumentCollection documentCollection;
    private final URI serviceEndpoint;
    private final Uri addressUri;
    private final String openConnectionsConcurrencyMode;

    public OpenConnectionOperation(TriFunction<URI, Uri, String, Mono<OpenConnectionResponse>> openConnectionFunc, DocumentCollection documentCollection, URI serviceEndpoint, Uri addressUri, String openConnectionsConcurrencyMode) {
        this.openConnectionFunc = openConnectionFunc;
        this.documentCollection = documentCollection;
        this.serviceEndpoint = serviceEndpoint;
        this.addressUri = addressUri;
        this.openConnectionsConcurrencyMode = openConnectionsConcurrencyMode;
    }

    public TriFunction<URI, Uri, String, Mono<OpenConnectionResponse>> getOpenConnectionFunc() {
        return openConnectionFunc;
    }

    public DocumentCollection getDocumentCollection() {
        return documentCollection;
    }

    public URI getServiceEndpoint() {
        return serviceEndpoint;
    }

    public Uri getAddressUri() {
        return addressUri;
    }

    public String getOpenConnectionsConcurrencyMode() {
        return openConnectionsConcurrencyMode;
    }
}
