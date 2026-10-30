// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomTokenProxyConfigurationTest {
    private static final String PROXY_URL = "AZURE_KUBERNETES_TOKEN_PROXY";
    private static final String CA_FILE = "AZURE_KUBERNETES_CA_FILE";
    private static final String CA_DATA = "AZURE_KUBERNETES_CA_DATA";
    private static final String SNI_NAME = "AZURE_KUBERNETES_SNI_NAME";

    @Test
    public void noConfigurationUsesDirectAuthentication() {
        assertNull(CustomTokenProxyConfiguration
            .parseAndValidate(TestUtils.createTestConfiguration(new TestConfigurationSource())));
    }

    @ParameterizedTest
    @ValueSource(strings = { CA_FILE, CA_DATA, SNI_NAME })
    public void partialConfigurationFails(String setting) {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put(setting, "configured-value"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CustomTokenProxyConfiguration.parseAndValidate(configuration));
        assertTrue(exception.getMessage().contains(PROXY_URL));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "http://proxy.example.com",
            "not-a-url",
            "https:/proxy/path",
            "https:///proxy/path",
            "https://proxy.example.com:65536/path",
            "https://proxy.example.com:invalid/path",
            "https://user:password@proxy.example.com/path",
            "https://proxy.example.com/path?query=value",
            "https://proxy.example.com/path#fragment" })
    public void invalidProxyUrlFails(String url) {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put(PROXY_URL, url));

        assertThrows(IllegalArgumentException.class,
            () -> CustomTokenProxyConfiguration.parseAndValidate(configuration));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://proxy.example.com",
            "HTTPS://proxy.example.com",
            "https://proxy.example.com:8443/api%2Ftoken",
            "https://[::1]:8443/proxy" })
    public void validProxyUrlPreservesEndpoint(String url) {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(PROXY_URL, url).put(SNI_NAME, "proxy.ests.aks"));

        ProxyConfig proxyConfig = CustomTokenProxyConfiguration.parseAndValidate(configuration);

        assertEquals("proxy.ests.aks", proxyConfig.getSniName());
        assertEquals(url.substring(url.indexOf("://") + 3),
            proxyConfig.getTokenProxyUrl().toString().substring("https://".length()));
    }

    @Test
    public void conflictingCaSourcesFail() {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(PROXY_URL, "https://proxy.example.com")
                .put(CA_FILE, "ca.pem")
                .put(CA_DATA, "certificate"));

        assertThrows(IllegalArgumentException.class,
            () -> CustomTokenProxyConfiguration.parseAndValidate(configuration));
    }

    @Test
    public void missingCaFileFails(@TempDir Path directory) {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put(PROXY_URL, "https://proxy.example.com")
                .put(CA_FILE, directory.resolve("missing.pem").toString()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CustomTokenProxyConfiguration.parseAndValidate(configuration));
        assertTrue(exception.getMessage().contains(CA_FILE));
    }

    @Test
    public void existingCaFileIsPreserved(@TempDir Path directory) throws IOException {
        Path caFile = directory.resolve("ca.pem");
        Files.write(caFile, "certificate".getBytes(StandardCharsets.UTF_8));
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(PROXY_URL, "https://proxy.example.com").put(CA_FILE, caFile.toString()));

        assertEquals(caFile.toString(), CustomTokenProxyConfiguration.parseAndValidate(configuration).getCaFile());
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "\t", "\r\n" })
    public void whitespaceCaDataFails(String caData) {
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put(PROXY_URL, "https://proxy.example.com").put(CA_DATA, caData));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CustomTokenProxyConfiguration.parseAndValidate(configuration));
        assertTrue(exception.getMessage().contains(CA_DATA));
    }
}
