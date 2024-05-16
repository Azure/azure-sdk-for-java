// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.implementation.token;

import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.extensions.implementation.enums.AuthProperty;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccessTokenResolverOptionsTest {

    @Test
    void testDefaultConstructor() {
        AccessTokenResolverOptions accessTokenResolverOptions = new AccessTokenResolverOptions();
        assertNull(accessTokenResolverOptions.getClaims());
        assertNull(accessTokenResolverOptions.getScopes());
        assertNull(accessTokenResolverOptions.getTenantId());
    }

    @Test
    void testConstructorWithProperties() {
        Properties properties = new Properties();
        properties.setProperty(AuthProperty.TENANT_ID.getPropertyKey(), "fake-tenant-id");
        properties.setProperty(AuthProperty.CLAIMS.getPropertyKey(), "fake-claims");
        properties.setProperty(AuthProperty.SCOPES.getPropertyKey(), "fake-scopes");

        AccessTokenResolverOptions accessTokenResolverOptions = new AccessTokenResolverOptions(properties);
        assertEquals("fake-claims", accessTokenResolverOptions.getClaims());
        assertArrayEquals(new String[]{"fake-scopes"}, accessTokenResolverOptions.getScopes());
        assertEquals("fake-tenant-id", accessTokenResolverOptions.getTenantId());
    }

    @ParameterizedTest
    @MethodSource("provideAuthorityHostScopeMap")
    void testDifferentAuthorties(String authorityHost, String scope) {
        Properties properties = new Properties();
        AuthProperty.AUTHORITY_HOST.setProperty(properties, authorityHost);
        AccessTokenResolverOptions accessTokenResolverOptions = new AccessTokenResolverOptions(properties);
        assertArrayEquals(new String[]{scope}, accessTokenResolverOptions.getScopes());
    }

    private static Stream<Arguments> provideAuthorityHostScopeMap() {
        return Stream.of(
                Arguments.of(null,  "https://ossrdbms-aad.database.windows.net/.default"),
                Arguments.of("",  "https://ossrdbms-aad.database.windows.net/.default"),
                Arguments.of(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD,  "https://ossrdbms-aad.database.windows.net/.default"),
                Arguments.of(AzureAuthorityHosts.AZURE_CHINA, "https://ossrdbms-aad.database.chinacloudapi.cn/.default"),
                Arguments.of(AzureAuthorityHosts.AZURE_GERMANY, "https://ossrdbms-aad.database.cloudapi.de/.default"),
                Arguments.of(AzureAuthorityHosts.AZURE_GOVERNMENT, "https://ossrdbms-aad.database.usgovcloudapi.net/.default")
        );
    }
}
