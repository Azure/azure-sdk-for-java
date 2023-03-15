// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Uri;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

public interface IOpenConnectionsHandler {
    Mono<List<OpenConnectionResponse>> openConnection(URI serviceEndpoint, Uri addressUri);
    Mono<List<OpenConnectionResponse>> openConnection(URI serviceEndpoint, Uri addressUri, String semaphoreSettingsMode);
    Flux<List<OpenConnectionResponse>> openConnections(URI serviceEndpoint, List<Uri> addresses);
    Flux<List<OpenConnectionResponse>> openConnections(URI serviceEndpoint, List<Uri> addresses, String semaphoreSettingsMode);
}
