// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapi;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AadResourceServerPropertiesTests {

    @Test
    void testNoPropertiesConfigured() {
        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true"
            )
            .run(context -> {
                AadResourceServerProperties properties = context.getBean(AadResourceServerProperties.class);
                assertEquals(AadJwtClaimNames.SUB, properties.getPrincipalClaimName());
                assertEquals(DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP, properties.getClaimToAuthorityPrefixMap());
            });
    }

    @Test
    void testPropertiesConfigured() {
        resourceServerContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.enabled=true",
                "spring.cloud.azure.active-directory.resource-server.principal-claim-name=fake-claim-name",
                "spring.cloud.azure.active-directory.resource-server.claim-to-authority-prefix-map.fake-key-1=fake-value-1",
                "spring.cloud.azure.active-directory.resource-server.claim-to-authority-prefix-map.fake-key-2=fake-value-2")
            .run(context -> {
                AadResourceServerProperties properties = context.getBean(AadResourceServerProperties.class);
                assertEquals(properties.getPrincipalClaimName(), "fake-claim-name");
                assertEquals(2, properties.getClaimToAuthorityPrefixMap().size());
                assertEquals("fake-value-1", properties.getClaimToAuthorityPrefixMap().get("fake-key-1"));
                assertEquals("fake-value-2", properties.getClaimToAuthorityPrefixMap().get("fake-key-2"));
            });
    }
}
