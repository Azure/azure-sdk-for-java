// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.validationstests;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = FeatureManagementProperties.class)
@TestPropertySource(value = { "classpath:/validations-tests/NoFilters.sample.json" }, factory= JsonPropertySourceFactory.class)
public class NoFiltersTest {

    @Autowired
    private FeatureManagementProperties properties;

}
