// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

/**
 * Interface for providing a Targeting Context to the `Microsoft.TargetingFilter`.
 */
public interface ITargetingContextAccessor {

    /**
     * Returns the Targeting Context for Feature Targeting evaluation.
     * @return Mono{@literal <}TargetContext{@literal >}
     */
    void getContextAsync(ITargetingContext context);

}
