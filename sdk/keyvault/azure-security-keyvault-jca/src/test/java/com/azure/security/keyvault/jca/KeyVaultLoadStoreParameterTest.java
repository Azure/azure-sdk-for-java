// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
public class KeyVaultLoadStoreParameterTest {
    private static final String REMOVED_REFRESH_INTERVAL_PROPERTY
        = "azure.keyvault.jca.certificates-refresh-interval-in-ms";

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
        System.clearProperty(REMOVED_REFRESH_INTERVAL_PROPERTY);
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
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "PT1S");
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
        assertEquals(Duration.ofSeconds(1), parameter.getCertificatesRefreshInterval());
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
        assertEquals(Duration.ZERO, parameter.getCertificatesRefreshInterval());
        assertFalse(parameter.isRefreshCertificatesWhenHaveUnTrustCertificate());
        assertTrue(parameter.getCertificateAliasFilterPatterns().isEmpty());
        assertFalse(parameter.isDisableChallengeResourceVerification());
        assertFalse(parameter.isAiaDownloadDisabled());
    }

    @ParameterizedTest
    @ValueSource(strings = { "PT30S", "30000", "+30000" })
    public void testRefreshIntervalAcceptsIso8601DurationAndMilliseconds(String refreshInterval) {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, refreshInterval);

        assertEquals(Duration.ofSeconds(30),
            KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshInterval());
    }

    @Test
    public void testLegacyRefreshIntervalMillisecondsAreSupported() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "2000");

        assertEquals(Duration.ofMillis(2000),
            KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshInterval());
    }

    @Test
    public void testRefreshIntervalAcceptsIso8601Duration() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "PT30S");

        assertEquals(Duration.ofSeconds(30),
            KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshInterval());
    }

    @Test
    public void testRefreshIntervalAcceptsFractionalSeconds() {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "PT0.5S");

        assertEquals(Duration.ofMillis(500),
            KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshInterval());
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "PT0S", "-1", "-PT1S" })
    public void testRefreshIntervalAllowsDisabledRefresh(String refreshInterval) {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, refreshInterval);

        Duration interval = KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshInterval();
        assertTrue(interval.isZero() || interval.isNegative());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "not-a-duration", "PT", "30S" })
    public void testInvalidRefreshIntervalIsRejected(String refreshInterval) {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, refreshInterval);

        assertThrows(DateTimeParseException.class, KeyVaultLoadStoreParameter::fromSystemProperties);
    }

    @Test
    public void testProgrammaticRefreshInterval() {
        KeyVaultLoadStoreParameter parameter = new KeyVaultLoadStoreParameter(null);

        assertSame(parameter, parameter.setCertificatesRefreshInterval(Duration.ofSeconds(30)));
        assertEquals(Duration.ofSeconds(30), parameter.getCertificatesRefreshInterval());
        assertThrows(NullPointerException.class, () -> parameter.setCertificatesRefreshInterval(null));
    }

    @Test
    public void testRemovedRefreshIntervalPropertyWarnsAndIsIgnored() {
        assertRemovedRefreshIntervalPropertyWarnsAndIsIgnored(Duration.ZERO);
    }

    @ParameterizedTest
    @ValueSource(strings = { "PT30S", "30000" })
    public void testRemovedRefreshIntervalPropertyDoesNotOverrideSupportedProperty(String refreshInterval) {
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, refreshInterval);

        assertRemovedRefreshIntervalPropertyWarnsAndIsIgnored(Duration.ofSeconds(30));
    }

    private void assertRemovedRefreshIntervalPropertyWarnsAndIsIgnored(Duration expectedInterval) {
        System.setProperty(REMOVED_REFRESH_INTERVAL_PROPERTY, "removed-property-value");
        Logger logger = Logger.getLogger(KeyVaultKeyStore.class.getName());
        Level originalLevel = logger.getLevel();
        List<LogRecord> records = new ArrayList<>();
        Handler collector = new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(collector);
        logger.setLevel(Level.ALL);
        try {
            assertEquals(expectedInterval,
                KeyVaultLoadStoreParameter.fromSystemProperties().getCertificatesRefreshInterval());
            assertEquals(1, records.size());
            LogRecord warning = records.get(0);
            assertEquals(Level.WARNING, warning.getLevel());
            String message = MessageFormat.format(warning.getMessage(), warning.getParameters());
            assertTrue(message.contains(REMOVED_REFRESH_INTERVAL_PROPERTY));
            assertTrue(message.contains("removed and is ignored"));
            assertTrue(message.contains(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL));
            assertTrue(message.contains("ISO-8601"));
            assertTrue(message.contains("PT30S"));
            assertFalse(message.contains("removed-property-value"));
        } finally {
            logger.removeHandler(collector);
            logger.setLevel(originalLevel);
        }
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
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "PT30S");

        KeyVaultLoadStoreParameter parameter = KeyVaultLoadStoreParameter.fromSystemProperties();

        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_URI, "https://second.vault.azure.net");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_DISABLE_AIA_DOWNLOAD, "false");
        System.setProperty(KeyVaultJcaPropertyNames.KEYVAULT_JCA_CERTIFICATES_REFRESH_INTERVAL, "PT1S");

        assertEquals("https://first.vault.azure.net", parameter.getUri());
        assertTrue(parameter.isAiaDownloadDisabled());
        assertEquals(Duration.ofSeconds(30), parameter.getCertificatesRefreshInterval());
    }
}
