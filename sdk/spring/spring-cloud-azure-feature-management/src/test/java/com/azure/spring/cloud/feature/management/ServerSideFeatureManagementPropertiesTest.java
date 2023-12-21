/*
 * // Copyright (c) Microsoft Corporation. All rights reserved.
 * // Licensed under the MIT License.
 */

package com.azure.spring.cloud.feature.management;

import com.azure.spring.cloud.feature.management.implementation.ServerSideFeatureManagementProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = ServerSideFeatureManagementProperties.class)
@TestPropertySource("classpath:server-side-schema-test.yaml")
@SpringBootTest(classes = { SpringBootTest.class })
public class ServerSideFeatureManagementPropertiesTest {
    @Autowired
    private ServerSideFeatureManagementProperties serverSideProperties;

    @Test
    void givenUserDefinedPOJO_whenBindingPropertiesFile_thenAllFieldsAreSet() {
        assertTrue(serverSideProperties.getOnOff().get("Gamma"));
        serverSideProperties.getFeatureManagement().get("Beta");
    }
}
