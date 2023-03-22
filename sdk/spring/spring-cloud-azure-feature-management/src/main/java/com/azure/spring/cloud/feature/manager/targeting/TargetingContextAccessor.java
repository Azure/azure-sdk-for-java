// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

/**
 * Interface for providing a Targeting Context to the `Microsoft.TargetingFilter`.
 */
public interface TargetingContextAccessor {

    /**
     * Configures the Targeting Context for Feature Targeting evaluation.
     * @param context Targeting Context for selecting feature
     */
    void configureTargetingContext(TargetingContext context);

}
