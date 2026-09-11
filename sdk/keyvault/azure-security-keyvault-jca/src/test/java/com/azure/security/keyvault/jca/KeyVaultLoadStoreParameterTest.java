// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class KeyVaultLoadStoreParameterTest {

    @BeforeEach
    @AfterEach
    public void clearSystemProperties() {
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_TENANT_ID);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_ID);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_SECRET);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_MANAGED_IDENTITY);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_ACCESS_TOKEN);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_DISABLE_CHALLENGE_RESOURCE_VERIFICATION);
        System.clearProperty(KeyVaultJcaPropertyNames.CERT_PATH_WELL_KNOWN);
        System.clearProperty(KeyVaultJcaPropertyNames.CERT_PATH_CUSTOM);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL_IN_MS);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_REFRESH_CERTIFICATES_WHEN_HAVE_UNTRUST_CERTIFICATE);
        System.clearProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD);
        String aliasFilterProperty = KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN;
        System.getProperties()
            .stringPropertyNames()
            .stream()
            .filter(name -> name.equals(aliasFilterProperty) || name.startsWith(aliasFilterProperty + "."))
            .forEach(System::clearProperty);
    }

    @Test
    public void testFromSystemPropertiesCapturesAllProperties() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, "https://test.vault.azure.net");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_TENANT_ID, "tenant-id");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_ID, "client-id");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_CLIENT_SECRET, "client-secret");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_MANAGED_IDENTITY, "managed-identity");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_ACCESS_TOKEN, "access-token");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_DISABLE_CHALLENGE_RESOURCE_VERIFICATION, "true");
        System.setProperty(KeyVaultJcaPropertyNames.CERT_PATH_WELL_KNOWN, "/well-known");
        System.setProperty(KeyVaultJcaPropertyNames.CERT_PATH_CUSTOM, "/custom");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL_IN_MS, "1000");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_REFRESH_CERTIFICATES_WHEN_HAVE_UNTRUST_CERTIFICATE,
            "true");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN, " alias-one ");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATE_ALIAS_FILTER_PATTERN + ".prod",
            "^prod-.*");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD, "true");

        KeyVaultLoadStoreParameter parameter = KeyVaultLoadStoreParameter.fromSystemProperties();

        assertEquals("https://test.vault.azure.net", parameter.getUri());
        assertEquals("tenant-id", parameter.getTenantId());
        assertEquals("client-id", parameter.getClientId());
        assertEquals("client-secret", parameter.getClientSecret());
        assertEquals("managed-identity", parameter.getManagedIdentity());
        assertEquals("access-token", parameter.getAccessToken());
        assertTrue(parameter.isDisableChallengeResourceVerification());
        assertEquals("/well-known", parameter.getCertPathWellKnown());
        assertEquals("/custom", parameter.getCertPathCustom());
        assertEquals(1000L, parameter.getCertificatesRefreshIntervalInMs());
        assertTrue(parameter.isRefreshCertificatesWhenHaveUnTrustCertificate());
        assertEquals(new HashSet<>(Arrays.asList("alias-one", "^prod-.*")),
            parameter.getCertificateAliasFilterPatterns());
        assertTrue(parameter.isAiaDownloadDisabled());
    }

    @Test
    public void testFromSystemPropertiesUsesDefaults() {
        KeyVaultLoadStoreParameter parameter = KeyVaultLoadStoreParameter.fromSystemProperties();

        assertNull(parameter.getUri());
        assertEquals("/etc/certs/well-known/", parameter.getCertPathWellKnown());
        assertEquals("/etc/certs/custom/", parameter.getCertPathCustom());
        assertEquals(0L, parameter.getCertificatesRefreshIntervalInMs());
        assertFalse(parameter.isRefreshCertificatesWhenHaveUnTrustCertificate());
        assertTrue(parameter.getCertificateAliasFilterPatterns().isEmpty());
        assertFalse(parameter.isDisableChallengeResourceVerification());
        assertFalse(parameter.isAiaDownloadDisabled());
    }

    @Test
    public void testRefreshIntervalInMsTakesPrecedence() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "2000");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL_IN_MS, "1000");

        assertEquals(1000L, KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshIntervalInMs());
    }

    @Test
    public void testLegacyRefreshIntervalIsFallback() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "2000");

        assertEquals(2000L, KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshIntervalInMs());
    }

    @Test
    public void testCertificateAliasFilterPatternsAreDefensivelyCopied() {
        Set<String> filterPatterns = new HashSet<>(Arrays.asList("alias-one", "alias-two"));
        KeyVaultLoadStoreParameter parameter
            = new KeyVaultLoadStoreParameter(null).setCertificateAliasFilterPatterns(filterPatterns);

        filterPatterns.clear();
        Set<String> returnedPatterns = parameter.getCertificateAliasFilterPatterns();
        returnedPatterns.clear();

        assertEquals(new HashSet<>(Arrays.asList("alias-one", "alias-two")),
            parameter.getCertificateAliasFilterPatterns());
    }

    @Test
    public void testFromSystemPropertiesCreatesSnapshot() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, "https://first.vault.azure.net");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD, "true");

        KeyVaultLoadStoreParameter parameter = KeyVaultLoadStoreParameter.fromSystemProperties();

        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, "https://second.vault.azure.net");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD, "false");

        assertEquals("https://first.vault.azure.net", parameter.getUri());
        assertTrue(parameter.isAiaDownloadDisabled());
    }
}
