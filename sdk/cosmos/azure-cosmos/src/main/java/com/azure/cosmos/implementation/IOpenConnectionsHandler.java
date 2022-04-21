// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Uri;
import reactor.core.publisher.Flux;

import java.util.List;

public interface IOpenConnectionsHandler {
    Flux<OpenConnectionResponse> openConnections(List<Uri> addresses);
}
