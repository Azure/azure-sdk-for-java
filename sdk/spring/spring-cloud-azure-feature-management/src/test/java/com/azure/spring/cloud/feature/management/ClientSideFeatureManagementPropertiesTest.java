// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management;

import com.azure.spring.cloud.feature.management.implementation.FeatureManagementProperties;
import com.azure.spring.cloud.feature.management.implementation.models.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = FeatureManagementProperties.class)
@SpringBootTest(classes = { SpringBootTest.class })
@ActiveProfiles("client")
public class ClientSideFeatureManagementPropertiesTest {
    @Autowired
    private FeatureManagementProperties clientSideProperties;

    @Test
    void onOffMapTest() {
        assertTrue(clientSideProperties.getOnOff().get("gamma"));
    }

    @Test
    void featureManagementTest() {
        final Feature alphaFeatureItem = clientSideProperties.getFeatureManagement().get("alpha");
        assertEquals(alphaFeatureItem.getKey(), "alpha");
        assertEquals(alphaFeatureItem.getEnabledFor().size(), 1);
        assertEquals(alphaFeatureItem.getEnabledFor().get(0).getName(), "randomFilter");

        final Feature betaFeatureItem = clientSideProperties.getFeatureManagement().get("beta");
        assertEquals(betaFeatureItem.getKey(), "beta");
        assertEquals(betaFeatureItem.getEnabledFor().size(), 1);
        assertEquals(betaFeatureItem.getEnabledFor().get(0).getName(), "timeWindowFilter");
    }
}
