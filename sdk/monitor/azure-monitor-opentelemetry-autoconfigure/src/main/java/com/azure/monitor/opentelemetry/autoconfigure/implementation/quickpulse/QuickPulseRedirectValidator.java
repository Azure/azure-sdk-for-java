// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

final class QuickPulseRedirectValidator {

    private static final String HTTPS = "https";

    private QuickPulseRedirectValidator() {
    }

    static String validateAndGetEndpointPrefix(String configuredEndpoint, String redirectLink)
        throws MalformedURLException {
        URL configuredUrl = new URL(configuredEndpoint);
        URL redirectUrl = new URL(redirectLink);

        if (!HTTPS.equalsIgnoreCase(redirectUrl.getProtocol()) || redirectUrl.getUserInfo() != null) {
            throw new MalformedURLException("Redirect must use https and must not contain user information");
        }

        String configuredHost = configuredUrl.getHost();
        String redirectHost = redirectUrl.getHost();
        if (!isSameOrSubdomain(redirectHost, configuredHost) && !isKnownLiveMetricsHost(redirectHost)) {
            throw new MalformedURLException("Redirect host is outside the configured Live Metrics endpoint boundary");
        }

        return redirectUrl.getProtocol() + "://" + redirectUrl.getAuthority() + "/";
    }

    private static boolean isSameOrSubdomain(String host, String expectedDomain) {
        String normalizedHost = normalizeHost(host);
        String normalizedDomain = normalizeHost(expectedDomain);
        return normalizedHost.equals(normalizedDomain) || normalizedHost.endsWith("." + normalizedDomain);
    }

    private static boolean isKnownLiveMetricsHost(String host) {
        String normalizedHost = normalizeHost(host);
        return normalizedHost.endsWith(".services.visualstudio.com")
            || normalizedHost.endsWith(".livediagnostics.monitor.azure.com")
            || normalizedHost.endsWith(".applicationinsights.azure.com")
            || normalizedHost.endsWith(".applicationinsights.azure.cn")
            || normalizedHost.endsWith(".applicationinsights.us")
            || normalizedHost.endsWith(".applicationinsights.azure.us");
    }

    private static String normalizeHost(String host) {
        return host.toLowerCase(Locale.ROOT);
    }
}
