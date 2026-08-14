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

    private static final List<String> ALLOWED_REDIRECT_DOMAIN_SUFFIXES = Collections.unmodifiableList(
        Arrays.asList(".livediagnostics.monitor.azure.com", ".monitor.azure.com", ".services.visualstudio.com",
            ".applicationinsights.azure.com", ".applicationinsights.microsoft.com", ".monitor.azure.us",
            ".applicationinsights.azure.us", ".monitor.azure.cn", ".applicationinsights.azure.cn"));

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
        if (!HTTPS.equalsIgnoreCase(redirectUrl.getProtocol())
            || redirectUrl.getUserInfo() != null
            || !isDefaultPort(redirectUrl)) {
            return false;
        }

        String redirectHost = canonicalHost(redirectUrl);
        if (redirectHost.isEmpty()) {
            return false;
        }

        // A redirect back to the current host stays inside the boundary the customer already chose, which keeps
        // custom endpoints and reverse proxies working.
        return redirectHost.equals(canonicalHost(currentUrl)) || hasAllowedSuffix(redirectHost);
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
