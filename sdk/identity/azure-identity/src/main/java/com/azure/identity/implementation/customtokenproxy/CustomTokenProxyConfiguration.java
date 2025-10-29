// Copyright (c) Microsoft Corporation. All rights reserved.  
// Licensed under the MIT License.

package com.azure.identity.implementation.customtokenproxy;

import com.azure.core.util.logging.ClientLogger;

import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URISyntaxException;

import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;

public class CustomTokenProxyConfiguration {

    private static final ClientLogger LOGGER = new ClientLogger(CustomTokenProxyConfiguration.class);

    public static final String AZURE_KUBERNETES_TOKEN_PROXY = "AZURE_KUBERNETES_TOKEN_PROXY";
    public static final String AZURE_KUBERNETES_CA_FILE = "AZURE_KUBERNETES_CA_FILE";
    public static final String AZURE_KUBERNETES_CA_DATA = "AZURE_KUBERNETES_CA_DATA";
    public static final String AZURE_KUBERNETES_SNI_NAME = "AZURE_KUBERNETES_SNI_NAME";

    private CustomTokenProxyConfiguration() {
    }

    public static boolean isConfigured(Configuration configuration) {
        String tokenProxyUrl = configuration.get(AZURE_KUBERNETES_TOKEN_PROXY);
        return !CoreUtils.isNullOrEmpty(tokenProxyUrl);
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

        byte[] caCertBytes = null;
        if (!CoreUtils.isNullOrEmpty(caData)) {
            try {
                caCertBytes = caData.getBytes(StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Failed to decode CA certificate data from AZURE_KUBERNETES_CA_DATA", e));
            }
        }

        ProxyConfig config = new ProxyConfig(proxyUrl, sniName, caFile, caCertBytes);
        return config;
    }

    private static URL validateProxyUrl(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Proxy endpoint cannot be null or empty"));
        }

        try {
            URI tokenProxy = new URI(endpoint);

            if (!"https".equals(tokenProxy.getScheme())) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                    "Custom token endpoint must use https scheme, got: " + tokenProxy.getScheme()));
            }

            if (tokenProxy.getRawUserInfo() != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Custom token endpoint URL must not contain user info: " + endpoint));
            }

            if (tokenProxy.getRawQuery() != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Custom token endpoint URL must not contain a query: " + endpoint));
            }

            if (tokenProxy.getRawFragment() != null) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Custom token endpoint URL must not contain a fragment: " + endpoint));
            }

            if (tokenProxy.getRawPath() == null || tokenProxy.getRawPath().isEmpty()) {
                tokenProxy = new URI(tokenProxy.getScheme(), null, tokenProxy.getHost(), tokenProxy.getPort(), "/",
                    null, null);
            }

            return tokenProxy.toURL();

        } catch (URISyntaxException | IllegalArgumentException e) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("Failed to normalize proxy URL path", e));
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error while validating proxy URL: " + endpoint, e);
        }
    }

}
