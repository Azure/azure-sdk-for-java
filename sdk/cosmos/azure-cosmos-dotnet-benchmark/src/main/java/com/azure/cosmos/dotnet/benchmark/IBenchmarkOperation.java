// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.dotnet.benchmark;

import reactor.core.publisher.Mono;

public interface IBenchmarkOperation {

    Mono<OperationResult> executeOnce();

    Mono<Object> prepare();

}
