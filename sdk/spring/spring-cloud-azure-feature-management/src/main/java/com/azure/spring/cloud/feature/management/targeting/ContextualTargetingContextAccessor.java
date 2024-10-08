// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.targeting;

/**
 * Interface for providing a Targeting Context to the `Microsoft.TargetingFilter`.
 */
public interface ContextualTargetingContextAccessor {

    /**
     * Configures the Targeting Context for Feature Targeting evaluation.
     * @param context Targeting Context for selecting feature
     * @param appContext Local context
     */
    void configureTargetingContext(TargetingContext context, Object appContext);

}
