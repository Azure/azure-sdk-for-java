package com.azure.cosmos.dotnet.benchmark;

import reactor.core.publisher.Mono;

public interface IBenchmarkOperation {

    Mono<Object> prepare();

    Mono<OperationResult> executeOnce();

}
