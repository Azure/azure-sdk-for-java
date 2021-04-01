// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import reactor.core.publisher.Mono;

public interface SupportsUpdatingPrivateEndpointConnection {

    void approvePrivateEndpointConnection(String privateEndpointConnectionName);

    Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName);
}
