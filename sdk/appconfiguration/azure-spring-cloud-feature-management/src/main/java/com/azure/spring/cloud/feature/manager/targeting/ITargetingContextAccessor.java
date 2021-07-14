// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

import reactor.core.publisher.Mono;

public interface ITargetingContextAccessor {

    Mono<TargetingContext> getContextAsync();

}
