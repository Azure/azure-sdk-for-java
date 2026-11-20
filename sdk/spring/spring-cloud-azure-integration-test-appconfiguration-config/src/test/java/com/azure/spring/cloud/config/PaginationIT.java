// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = AppConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "spring.config.import=azureAppConfiguration",
    "spring.cloud.azure.appconfiguration.stores[0].endpoint=${STORE_NAME}",
    "spring.cloud.azure.appconfiguration.stores[0].selects[0].key-filter=" + PaginationTestData.KEY_PREFIX,
    "spring.cloud.azure.appconfiguration.stores[0].trim-key-prefix[0]=/application/",
    "spring.cloud.azure.appconfiguration.stores[0].replica-discovery-enabled=false"
})
class PaginationIT {

    @Autowired
    private PaginationProperties properties;

    @Autowired
    private PaginationPagePolicy pagePolicy;

    @Test
    void loads101KeysInTwoPages() {
        assertEquals(List.of(new PaginationPagePolicy.Page(100, true), new PaginationPagePolicy.Page(1, false)),
            pagePolicy.getPages());
        assertEquals(PaginationTestData.KEY_COUNT, properties.entries().size());
        assertEquals(PaginationTestData.expectedEntries(), properties.entries());
    }
}
