// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.throughputControl.controller.request;

import com.azure.cosmos.implementation.throughputControl.controller.IThroughputController;
import reactor.core.publisher.Mono;

public interface IThroughputRequestController extends IThroughputController {
    Mono<Void> renewThroughputUsageCycle(double throughput);
}
