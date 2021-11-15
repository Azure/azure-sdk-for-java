// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.entities.featurevariants;

import reactor.core.publisher.Mono;

public interface IFeatureVariantAssigner extends IFeatureVariantAssignerMetadata {
    
    public Mono<FeatureVariant> assignVariantAsync(FeatureDefinition featureDefinition);

}
