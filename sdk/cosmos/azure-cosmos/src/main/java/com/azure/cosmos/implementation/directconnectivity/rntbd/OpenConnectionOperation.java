// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.concurrent.Callable;

public final class OpenConnectionOperation {

    private final Callable<Flux<OpenConnectionResponse>> openConnectionCallable;
    private final URI serviceEndpoint;
    private final Uri addressUri;
    private final String openConnectionsConcurrencyMode;


    public OpenConnectionOperation(Callable<Flux<OpenConnectionResponse>> openConnectionCallable, URI serviceEndpoint, Uri addressUri, String openConnectionsConcurrencyMode) {
        this.openConnectionCallable = openConnectionCallable;
        this.serviceEndpoint = serviceEndpoint;
        this.addressUri = addressUri;
        this.openConnectionsConcurrencyMode = openConnectionsConcurrencyMode;
    }

    public Callable<Flux<OpenConnectionResponse>> getOpenConnectionCallable() {
        return openConnectionCallable;
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
