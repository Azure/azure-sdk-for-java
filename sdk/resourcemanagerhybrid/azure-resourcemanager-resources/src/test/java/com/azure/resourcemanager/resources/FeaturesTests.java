// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources;

import com.azure.resourcemanager.resources.models.Feature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class FeaturesTests extends ResourceManagementTest {

    @Test
    public void canListAndRegisterFeature() {
        List<Feature> features = resourceClient.features().list().stream().collect(Collectors.toList());
        Assertions.assertNotNull(features);

        features.stream()
            .filter(f -> "NotRegistered".equals(f.state()))
            .findFirst()
            .ifPresent(feature -> resourceClient.features()
                .register(feature.resourceProviderName(), feature.featureName()));
    }
}
