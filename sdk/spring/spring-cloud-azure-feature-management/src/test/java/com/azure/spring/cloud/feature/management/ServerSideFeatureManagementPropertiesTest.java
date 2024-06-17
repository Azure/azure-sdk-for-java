// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = FeatureManagementProperties.class)
@SpringBootTest(classes = { SpringBootTest.class })
@ActiveProfiles("server")
public class ServerSideFeatureManagementPropertiesTest {
    @Autowired
    private FeatureManagementProperties serverSideProperties;

    @Test
    void onOffMapTest() {
        assertTrue(serverSideProperties.getOnOff().get("Gamma"));
    }

    @Test
    void featureManagementTest() {
        final Feature alphaFeatureItem = serverSideProperties.getFeatureManagement().get("Alpha");
        assertEquals(alphaFeatureItem.getKey(), "Alpha");
        assertEquals(alphaFeatureItem.getEnabledFor().size(), 1);
        assertEquals(alphaFeatureItem.getEnabledFor().get(0).getName(), "Microsoft.Random");

        final Feature betaFeatureItem = serverSideProperties.getFeatureManagement().get("Beta");
        assertEquals(betaFeatureItem.getKey(), "Beta");
        assertEquals(betaFeatureItem.getEnabledFor().size(), 1);
        assertEquals(betaFeatureItem.getEnabledFor().get(0).getName(), "Microsoft.TimeWindowFilter");

        final Boolean deltaFeatureItem = serverSideProperties.getOnOff().get("Delta");
        assertTrue(deltaFeatureItem);
    }

}
