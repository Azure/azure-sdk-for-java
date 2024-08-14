// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.IdentityUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IdentityUtilTests {

    @Test
    public void testMultiTenantAuthenticationEnabled() {
        String currentTenant = "tenant";
        String newTenant = "tenant-new";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(Arrays.asList(newTenant));

        Assertions.assertEquals(newTenant, IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

    @Test
    public void testMultiTenantAuthenticationDisabled() {
        String currentTenant = "tenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId("newTenant");
        IdentityClientOptions options = new IdentityClientOptions();
        options.disableMultiTenantAuthentication();

        Assertions.assertThrows(ClientAuthenticationException.class,
            () -> IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

    @Test
    public void testParseJsonIntoMap() throws IOException {
        String json = "{\n"
            + "    \"string\": \"string_value\",\n"
            + "    \"boolean\": true,\n"
            + "    \"number\": 1,\n"
            + "    \"array\": [\"an\",\"array\"],\n"
            + "    \"object\": {\n"
            + "        \"a\": \"nested\",\n"
            + "        \"b\": \"object\"\n"
            + "    }\n"
            + "}";
        Map<String,String> map = IdentityUtil.parseJsonIntoMap(json);
        assertTrue(map.containsKey("string"));
        assertTrue(map.containsKey("boolean"));
        assertTrue(map.containsKey("number"));
        assertTrue(map.containsKey("array"));
        assertTrue(map.containsKey("object"));
        assertEquals("string_value", map.get("string"));
        assertEquals("true", map.get("boolean"));
        assertEquals("1", map.get("number"));
        assertEquals("[\"an\",\"array\"]", map.get("array"));
        assertEquals("{\"a\":\"nested\",\"b\":\"object\"}", map.get("object"));
    }

    @Test
    public void testGetAccessToken() throws IOException {
        String json = "{\n"
            + "  \"token_type\": \"fake_token_type\",\n"
            + "  \"scope\": \"fake_scope\",\n"
            + "  \"expires_in\": 4986,\n"
            + "  \"ext_expires_in\": 4986,\n"
            + "  \"access_token\": \"fake_access_token\",\n"
            + "  \"refresh_token\": \"fake_refresh_token\",\n"
            + "  \"foci\": \"1\",\n"
            + "  \"id_token\": \"fake_id_token\",\n"
            + "  \"client_info\": \"fake_client_info\",\n"
            + "}";
        String result = IdentityUtil.getAccessToken(json);
        assertEquals("fake_access_token", result);
    }

    @Test
    public void testAdditionallyAllowedTenants() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAdditionallyAllowedTenants(Arrays.asList(IdentityUtil.ALL_TENANTS));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assertions.assertEquals(newTenant, resolvedTenant);
    }

    @Test
    public void testAdditionallyAllowedTenantsCaseInsensitive() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAdditionallyAllowedTenants(Arrays.asList("newtenant"));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assertions.assertEquals(newTenant, resolvedTenant);
    }

    @Test
    public void testAlienTenantWithAdditionallyAllowedTenants() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAdditionallyAllowedTenants(Arrays.asList("tenant"));

        Assertions.assertThrows(ClientAuthenticationException.class,
            () -> IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

    @Test
    public void testAlienTenantWithAdditionallyAllowedNotConfigured() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();

        Assertions.assertThrows(ClientAuthenticationException.class,
            () -> IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

    @Test
    public void testTenantWithAdditionalTenantsFromEnv() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        String allowedTenants = "newTenant;oldTenant";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, allowedTenants));

        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);

        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assertions.assertEquals(newTenant, resolvedTenant);

    }

    @Test
    public void testTenantWithWildCardAdditionalTenantsFromEnv() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        String allowedTenants = "*;randomTenant";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, allowedTenants));

        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);

        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assertions.assertEquals(newTenant, resolvedTenant);

    }

    @Test
    public void testAlienTenantWithAdditionalTenantsFromEnv() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        String allowedTenants = "randomTenant";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, allowedTenants));
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);

        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));

        Assertions.assertThrows(ClientAuthenticationException.class,
            () -> IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

}

