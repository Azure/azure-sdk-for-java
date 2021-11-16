// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapi;

import com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.cloud.autoconfigure.aad.implementation.properties.AADResourceServerProperties;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils.resourceServerContextRunner;
import static com.azure.spring.cloud.autoconfigure.aad.implementation.properties.AADResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AADResourceServerPropertiesTest {

    @Test
    void testNoPropertiesConfigured() {
        resourceServerContextRunner()
            .run(context -> {
                AADResourceServerProperties properties = context.getBean(AADResourceServerProperties.class);
                assertEquals(AADTokenClaim.SUB, properties.getPrincipalClaimName());
                assertEquals(DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP, properties.getClaimToAuthorityPrefixMap());
            });
    }

    @Test
    void testPropertiesConfigured() {
        resourceServerContextRunner()
            .withPropertyValues(
                "azure.activedirectory.resource-server.principal-claim-name=fake-claim-name",
                "azure.activedirectory.resource-server.claim-to-authority-prefix-map.fake-key-1=fake-value-1",
                "azure.activedirectory.resource-server.claim-to-authority-prefix-map.fake-key-2=fake-value-2")
            .run(context -> {
                AADResourceServerProperties properties = context.getBean(AADResourceServerProperties.class);
                assertEquals(properties.getPrincipalClaimName(), "fake-claim-name");
                assertEquals(2, properties.getClaimToAuthorityPrefixMap().size());
                assertEquals("fake-value-1", properties.getClaimToAuthorityPrefixMap().get("fake-key-1"));
                assertEquals("fake-value-2", properties.getClaimToAuthorityPrefixMap().get("fake-key-2"));
            });
    }
}
