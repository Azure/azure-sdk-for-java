// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Trust boundary checks for server-issued redirects. Following an attacker-controlled redirect would cause the
 * pipeline to attach a freshly signed credential (and the telemetry payload) to a foreign host.
 */
public final class RedirectPolicyHelper {

    private static final String HTTPS = "https";

    private static final List<String> ALLOWED_REDIRECT_DOMAIN_SUFFIXES
        = Collections.unmodifiableList(Arrays.asList(".livediagnostics.monitor.azure.com", ".monitor.azure.com",
            ".services.visualstudio.com", ".applicationinsights.azure.com", ".monitor.azure.us",
            ".applicationinsights.azure.us", ".monitor.azure.cn", ".applicationinsights.azure.cn"));

    /**
     * Returns whether a Live Metrics redirect target is safe to follow.
     *
     * @param configuredUrl the configured Live Metrics endpoint
     * @param redirectUrl the redirect target from the {@code x-ms-qps-service-endpoint-redirect-v2} header
     * @return true if the redirect target is trusted
     */
    public static boolean isTrustedLiveMetricsRedirect(URL configuredUrl, URL redirectUrl) {
        if (!isValidHttpsRedirect(redirectUrl) || !isDefaultPort(redirectUrl)) {
            return false;
        }

        String redirectHost = canonicalHost(redirectUrl);
        if (redirectHost.isEmpty()) {
            return false;
        }

        // A redirect back to the configured host stays inside the boundary the customer already chose, which keeps
        // custom endpoints and reverse proxies working.
        return redirectHost.equals(canonicalHost(configuredUrl)) || hasAllowedSuffix(redirectHost);
    }

    /**
     * Returns whether an ingestion redirect target is safe to follow.
     *
     * @param currentUrl the URL the request is currently targeting
     * @param redirectUrl the redirect target from the {@code Location} header
     * @return true if the redirect target is trusted
     */
    public static boolean isTrustedIngestionRedirect(URL currentUrl, URL redirectUrl) {
        if (!isValidHttpsRedirect(redirectUrl)) {
            return false;
        }

        String currentHost = canonicalHost(currentUrl);
        String redirectHost = canonicalHost(redirectUrl);
        if (currentHost.isEmpty() || redirectHost.isEmpty()) {
            return false;
        }

        if (currentHost.equals(redirectHost)) {
            return HTTPS.equalsIgnoreCase(currentUrl.getProtocol())
                && effectivePort(currentUrl) == effectivePort(redirectUrl);
        }

        if (!isDefaultPort(currentUrl) || !isDefaultPort(redirectUrl)) {
            return false;
        }

        // Cross-host ingestion redirects are stamp reassignments, so both hosts must live under the same suffix.
        for (String suffix : ALLOWED_REDIRECT_DOMAIN_SUFFIXES) {
            if (currentHost.endsWith(suffix) && redirectHost.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidHttpsRedirect(URL redirectUrl) {
        return HTTPS.equalsIgnoreCase(redirectUrl.getProtocol()) && redirectUrl.getUserInfo() == null;
    }

    private static boolean hasAllowedSuffix(String host) {
        for (String suffix : ALLOWED_REDIRECT_DOMAIN_SUFFIXES) {
            if (host.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isDefaultPort(URL url) {
        return url.getPort() == -1 || url.getPort() == url.getDefaultPort();
    }

    private static int effectivePort(URL url) {
        return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
    }

    private static String canonicalHost(URL url) {
        String host = url.getHost();
        if (host == null) {
            return "";
        }
        String canonicalHost = host.toLowerCase(Locale.ROOT);
        while (canonicalHost.endsWith(".")) {
            canonicalHost = canonicalHost.substring(0, canonicalHost.length() - 1);
        }
        return canonicalHost;
    }

    private RedirectPolicyHelper() {
    }
}
