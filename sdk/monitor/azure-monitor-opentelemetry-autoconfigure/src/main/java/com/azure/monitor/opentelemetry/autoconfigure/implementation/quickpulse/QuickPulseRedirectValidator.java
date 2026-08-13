// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

final class QuickPulseRedirectValidator {

    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTPS_PORT = 443;

    private static final List<String> ALLOWED_REDIRECT_DOMAIN_SUFFIXES
        = Collections.unmodifiableList(Arrays.asList(".livediagnostics.monitor.azure.com", ".monitor.azure.com",
            ".services.visualstudio.com", ".applicationinsights.azure.com", ".monitor.azure.us",
            ".applicationinsights.azure.us", ".monitor.azure.cn", ".applicationinsights.azure.cn"));

    private QuickPulseRedirectValidator() {
    }

    static String validateAndGetEndpointPrefix(String configuredEndpoint, String redirectLink)
        throws MalformedURLException {
        URL configuredUrl = new URL(configuredEndpoint);
        URL redirectUrl = new URL(redirectLink);

        if (!HTTPS.equalsIgnoreCase(redirectUrl.getProtocol())
            || redirectUrl.getUserInfo() != null
            || !isDefaultHttpsPort(redirectUrl)) {
            throw new MalformedURLException(
                "Redirect must use https, must not contain user information, and must use the default https port");
        }

        String configuredHost = configuredUrl.getHost();
        String redirectHost = redirectUrl.getHost();
        if (!isSameHost(redirectHost, configuredHost) && !isKnownLiveMetricsHost(redirectHost)) {
            throw new MalformedURLException("Redirect host is outside the configured Live Metrics endpoint boundary");
        }

        return redirectUrl.getProtocol() + "://" + redirectUrl.getAuthority() + "/";
    }

    private static boolean isDefaultHttpsPort(URL url) {
        return url.getPort() == -1 || url.getPort() == DEFAULT_HTTPS_PORT;
    }

    private static boolean isSameHost(String host, String expectedHost) {
        String normalizedHost = normalizeHost(host);
        String normalizedExpectedHost = normalizeHost(expectedHost);
        return normalizedHost.equals(normalizedExpectedHost);
    }

    private static boolean isKnownLiveMetricsHost(String host) {
        String normalizedHost = normalizeHost(host);
        for (String suffix : ALLOWED_REDIRECT_DOMAIN_SUFFIXES) {
            if (normalizedHost.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String normalizeHost(String host) {
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if (normalizedHost.endsWith(".")) {
            return normalizedHost.substring(0, normalizedHost.length() - 1);
        }
        return normalizedHost;
    }
}
