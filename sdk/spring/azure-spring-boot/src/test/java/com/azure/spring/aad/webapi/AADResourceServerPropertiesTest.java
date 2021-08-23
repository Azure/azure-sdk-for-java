// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapi;

import com.azure.spring.aad.implementation.constants.AADTokenClaim;
import com.azure.spring.autoconfigure.aad.AADAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

import static com.azure.spring.aad.webapi.AADResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AADResourceServerPropertiesTest {

    @Test
    public void testNoPropertiesConfigured() {
        getResourceServerContextRunner()
            .run(context -> {
                AADResourceServerProperties properties = context.getBean(AADResourceServerProperties.class);
                assertEquals(AADTokenClaim.SUB, properties.getPrincipalClaimName());
                assertEquals(DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP, properties.getClaimToAuthorityPrefixMap());
            });
    }

    @Test
    public void testPropertiesConfigured() {
        getResourceServerContextRunner()
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

    private WebApplicationContextRunner getResourceServerContextRunner() {
        return new WebApplicationContextRunner()
            .withUserConfiguration(AADAutoConfiguration.class)
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withPropertyValues(
                "azure.activedirectory.tenant-id=fake-tenant-id",
                "azure.activedirectory.app-id-uri=fake-app-id-uri");
    }
}
