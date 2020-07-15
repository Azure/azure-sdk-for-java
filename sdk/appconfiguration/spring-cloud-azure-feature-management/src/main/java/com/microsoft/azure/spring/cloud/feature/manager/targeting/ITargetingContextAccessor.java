/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.targeting;

import reactor.core.publisher.Mono;

public interface ITargetingContextAccessor {
    
    Mono<TargetingContext> getContextAsync();

}
