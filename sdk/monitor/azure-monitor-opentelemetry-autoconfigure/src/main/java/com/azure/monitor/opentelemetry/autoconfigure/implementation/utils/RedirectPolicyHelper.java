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

    // Grouped by cloud, and within each group telemetry ingestion first then live metrics, following
    // https://learn.microsoft.com/azure/azure-monitor/fundamentals/azure-monitor-network-access#application-insights-ingestion
    // Government endpoints are documented separately, in
    // https://learn.microsoft.com/azure/azure-government/compare-azure-government-global-azure#guidance-for-developers
    // @formatter:off
    private static final List<String> ALLOWED_REDIRECT_DOMAIN_SUFFIXES = Collections.unmodifiableList(Arrays.asList(
        // global, shared by every cloud
        ".services.visualstudio.com",
        ".applicationinsights.microsoft.com",
        // public
        ".applicationinsights.azure.com",
        ".monitor.azure.com",
        // government
        ".applicationinsights.us",
        ".applicationinsights.azure.us",
        ".monitor.azure.us",
        // china
        ".applicationinsights.azure.cn",
        ".monitor.azure.cn"));
    // @formatter:on

    /**
     * Returns whether a redirect target is safe to follow.
     * <p>
     * Stamp reassignment moves between suffixes (for example {@code rt.services.visualstudio.com} to
     * {@code &lt;region&gt;.livediagnostics.monitor.azure.com}), so the target host is checked on its own rather than
     * being required to share a suffix with the current host.
     *
     * @param currentUrl the endpoint the request is currently targeting
     * @param redirectUrl the redirect target
     * @return true if the redirect target is trusted
     */
    public static boolean isTrustedRedirect(URL currentUrl, URL redirectUrl) {
        if (redirectUrl.getUserInfo() != null) {
            return false;
        }

        String redirectHost = canonicalHost(redirectUrl);
        if (redirectHost.isEmpty()) {
            return false;
        }

        // A same-origin redirect crosses no trust boundary, which keeps custom endpoints and reverse proxies working.
        if (redirectHost.equals(canonicalHost(currentUrl))
            && redirectUrl.getProtocol().equalsIgnoreCase(currentUrl.getProtocol())
            && effectivePort(redirectUrl) == effectivePort(currentUrl)) {
            return true;
        }

        return HTTPS.equalsIgnoreCase(redirectUrl.getProtocol())
            && isDefaultPort(redirectUrl)
            && hasAllowedSuffix(redirectHost);
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
