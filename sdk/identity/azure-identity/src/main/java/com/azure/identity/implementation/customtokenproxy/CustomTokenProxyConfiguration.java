// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

public final class CustomTokenProxyConfiguration {

    private static final ClientLogger LOGGER = new ClientLogger(CustomTokenProxyConfiguration.class);

    private static final String AZURE_KUBERNETES_TOKEN_PROXY = "AZURE_KUBERNETES_TOKEN_PROXY";
    private static final String AZURE_KUBERNETES_CA_FILE = "AZURE_KUBERNETES_CA_FILE";
    private static final String AZURE_KUBERNETES_CA_DATA = "AZURE_KUBERNETES_CA_DATA";
    private static final String AZURE_KUBERNETES_SNI_NAME = "AZURE_KUBERNETES_SNI_NAME";

    private CustomTokenProxyConfiguration() {
    }

    public static ProxyConfig parseAndValidate(Configuration configuration) {
        String tokenProxyUrl = configuration.get(AZURE_KUBERNETES_TOKEN_PROXY);
        String caFile = configuration.get(AZURE_KUBERNETES_CA_FILE);
        String caData = configuration.get(AZURE_KUBERNETES_CA_DATA);
        String sniName = configuration.get(AZURE_KUBERNETES_SNI_NAME);

        if (CoreUtils.isNullOrEmpty(tokenProxyUrl)) {
            if (!CoreUtils.isNullOrEmpty(sniName)
                || !CoreUtils.isNullOrEmpty(caFile)
                || !CoreUtils.isNullOrEmpty(caData)) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "AZURE_KUBERNETES_TOKEN_PROXY is not set but other custom endpoint-related environment variables are present"));
            }
            return null;
        }

        if (!CoreUtils.isNullOrEmpty(caFile) && !CoreUtils.isNullOrEmpty(caData)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Only one of AZURE_KUBERNETES_CA_FILE or AZURE_KUBERNETES_CA_DATA can be set."));
        }

        URL proxyUrl = validateProxyUrl(tokenProxyUrl);

        if (!CoreUtils.isNullOrEmpty(caFile) && !Files.isRegularFile(Paths.get(caFile))) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("AZURE_KUBERNETES_CA_FILE must identify an existing certificate file."));
        }

        byte[] caCertBytes = null;
        if (!CoreUtils.isNullOrEmpty(caData)) {
            if (caData.trim().isEmpty()) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("AZURE_KUBERNETES_CA_DATA must not contain only whitespace."));
            }
            caCertBytes = caData.getBytes(StandardCharsets.UTF_8);
        }

        return new ProxyConfig(proxyUrl, sniName, caFile, caCertBytes);
    }

    private static URL validateProxyUrl(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Proxy endpoint cannot be null or empty"));
        }

        try {
            URI tokenProxy = new URI(endpoint);

            if (!"https".equalsIgnoreCase(tokenProxy.getScheme())) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Custom token endpoint must use https scheme, got: " + tokenProxy.getScheme()));
            }

            if (tokenProxy.getRawUserInfo() != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Custom token endpoint URL must not contain user info."));
            }

            if (tokenProxy.getRawQuery() != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Custom token endpoint URL must not contain a query."));
            }

            if (tokenProxy.getRawFragment() != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Custom token endpoint URL must not contain a fragment."));
            }

            if (CoreUtils.isNullOrEmpty(tokenProxy.getHost())
                || tokenProxy.getPort() < -1
                || tokenProxy.getPort() > 65535) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "AZURE_KUBERNETES_TOKEN_PROXY must be an absolute HTTPS URL with a valid host and port."));
            }

            return tokenProxy.toURL();

        } catch (URISyntaxException | MalformedURLException e) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("AZURE_KUBERNETES_TOKEN_PROXY must be a valid absolute HTTPS URL.", e));
        }
    }

}
