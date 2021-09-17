// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.data.cosmos;

import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CosmosDataAutoConfigurationTest {

    static final String TEST_URI_HTTPS = "https://test.https.documents.azure.com:443/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CosmosDataAutoConfiguration.class));

    @Test
    void configureWithoutCosmosTemplate() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(CosmosTemplate.class))
            .run((context) -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void configureWithCosmosDisabled() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

    @Test
    void configureWithoutUri() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.cosmos.enabled=true")
            .run(context -> assertThat(context).doesNotHaveBean(CosmosConfig.class));
    }

}
