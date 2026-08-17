// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class RedirectPolicyHelperTest {

    private static final String DEFAULT_LIVE_METRICS_ENDPOINT = "https://rt.services.visualstudio.com/";
    private static final String DEFAULT_INGESTION_ENDPOINT = "https://dc.services.visualstudio.com/v2.1/track";

    @Test
    public void allowsLiveMetricsStampRedirect() throws MalformedURLException {
        assertThat(isTrustedRedirect(DEFAULT_LIVE_METRICS_ENDPOINT,
            "https://westus.livediagnostics.monitor.azure.com/QuickPulseService.svc/")).isTrue();
    }

    @Test
    public void allowsIngestionStampRedirect() throws MalformedURLException {
        assertThat(isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT,
            "https://westus-0.in.applicationinsights.azure.com/v2.1/track")).isTrue();
    }

    @Test
    public void allowsGlobalApplicationInsightsHosts() throws MalformedURLException {
        assertThat(
            isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT, "https://dc.applicationinsights.microsoft.com/v2.1/track"))
                .isTrue();
        assertThat(isTrustedRedirect(DEFAULT_LIVE_METRICS_ENDPOINT,
            "https://rt.applicationinsights.microsoft.com/QuickPulseService.svc/")).isTrue();
    }

    @Test
    public void allowsSovereignCloudHosts() throws MalformedURLException {
        assertThat(isTrustedRedirect("https://dc.applicationinsights.azure.us/v2.1/track",
            "https://usgovvirginia.livediagnostics.monitor.azure.us/QuickPulseService.svc/")).isTrue();
        assertThat(isTrustedRedirect("https://dc.applicationinsights.us/v2.1/track",
            "https://usgovvirginia-0.in.applicationinsights.us/v2.1/track")).isTrue();
        assertThat(isTrustedRedirect("https://dc.applicationinsights.azure.cn/v2.1/track",
            "https://chinanorth2.in.applicationinsights.azure.cn/v2.1/track")).isTrue();
    }

    @Test
    public void allowsSameOrigin() throws MalformedURLException {
        assertThat(
            isTrustedRedirect("https://ingestion.example.com/v2.1/track", "https://ingestion.example.com/v2/track"))
                .isTrue();
        assertThat(
            isTrustedRedirect("https://collector.internal:8443/v2.1/track", "https://collector.internal:8443/v2/track"))
                .isTrue();
        assertThat(isTrustedRedirect("http://localhost:4318/v2.1/track", "http://localhost:4318/v2/track")).isTrue();
    }

    @Test
    public void isCaseAndTrailingDotInsensitive() throws MalformedURLException {
        assertThat(isTrustedRedirect(DEFAULT_LIVE_METRICS_ENDPOINT,
            "https://WestUS.LiveDiagnostics.Monitor.Azure.Com./QuickPulseService.svc/")).isTrue();
    }

    @Test
    public void rejectsUntrustedTargets() throws MalformedURLException {
        assertThat(isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT, "https://attacker.invalid/v2.1/track")).isFalse();
        assertThat(isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT,
            "https://evil.applicationinsights.azure.com.attacker.invalid/v2.1/track")).isFalse();
        assertThat(isTrustedRedirect("https://ingestion.example.com/v2.1/track",
            "https://evil.ingestion.example.com/v2.1/track")).isFalse();
        assertThat(isTrustedRedirect("https://foo.azure.com/v2.1/track", "https://bar.azure.com/v2.1/track")).isFalse();
    }

    @Test
    public void rejectsUnsafeUrls() throws MalformedURLException {
        assertThat(isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT,
            "http://westus-0.in.applicationinsights.azure.com/v2.1/track")).isFalse();
        assertThat(isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT,
            "https://user@westus-0.in.applicationinsights.azure.com/v2.1/track")).isFalse();
        assertThat(isTrustedRedirect(DEFAULT_INGESTION_ENDPOINT,
            "https://westus-0.in.applicationinsights.azure.com:444/v2.1/track")).isFalse();
        assertThat(
            isTrustedRedirect("https://ingestion.example.com/v2.1/track", "https://ingestion.example.com:444/v2/track"))
                .isFalse();
        assertThat(
            isTrustedRedirect("https://collector.internal:8443/v2.1/track", "http://collector.internal:8443/v2/track"))
                .isFalse();
    }

    private static boolean isTrustedRedirect(String currentUrl, String redirectLink) throws MalformedURLException {
        return RedirectPolicyHelper.isTrustedRedirect(new URL(currentUrl), new URL(redirectLink));
    }
}
