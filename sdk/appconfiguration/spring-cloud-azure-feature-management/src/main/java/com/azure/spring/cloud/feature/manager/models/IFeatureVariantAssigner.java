// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.models;

import reactor.core.publisher.Mono;

/**
 * Provides a method to assign a variant of a dynamic feature to be used based off of custom conditions.
 */
public interface IFeatureVariantAssigner extends IFeatureVariantAssignerMetadata {
    
    /**
     * Assign a variant of a dynamic feature to be used based off of customized criteria.
     * @param featureDefinition A variant assignment context that contains information needed to assign a variant for a
     *  dynamic feature.
     * @return The variant that should be assigned for a given dynamic feature.
     */
    public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition);

}
