// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class RedirectPolicyHelperTest {

    private static final String LIVE_METRICS_ENDPOINT = "https://westus.livediagnostics.monitor.azure.com/";
    private static final String INGESTION_ENDPOINT = "https://westus-0.in.applicationinsights.azure.com/v2.1/track";

    @Test
    public void liveMetricsAllowsTrustedSuffix() throws MalformedURLException {
        assertThat(isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT,
            "https://eastus.livediagnostics.monitor.azure.com/QuickPulseService.svc/")).isTrue();
    }

    @Test
    public void liveMetricsAllowsConfiguredHost() throws MalformedURLException {
        assertThat(isTrustedLiveMetricsRedirect("https://live.example.com/",
            "https://live.example.com/QuickPulseService.svc/")).isTrue();
    }

    @Test
    public void liveMetricsIsCaseAndTrailingDotInsensitive() throws MalformedURLException {
        assertThat(isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT,
            "https://EastUS.LiveDiagnostics.Monitor.Azure.Com./QuickPulseService.svc/")).isTrue();
    }

    @Test
    public void liveMetricsRejectsUntrustedTargets() throws MalformedURLException {
        assertThat(isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT, "https://attacker.invalid/")).isFalse();
        assertThat(isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT,
            "https://evil.livediagnostics.monitor.azure.com.attacker.invalid/")).isFalse();
        assertThat(isTrustedLiveMetricsRedirect("https://live.example.com/", "https://evil.live.example.com/"))
            .isFalse();
    }

    @Test
    public void liveMetricsRejectsUnsafeUrls() throws MalformedURLException {
        assertThat(
            isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT, "http://eastus.livediagnostics.monitor.azure.com/"))
                .isFalse();
        assertThat(isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT,
            "https://user@eastus.livediagnostics.monitor.azure.com/")).isFalse();
        assertThat(isTrustedLiveMetricsRedirect(LIVE_METRICS_ENDPOINT,
            "https://eastus.livediagnostics.monitor.azure.com:444/")).isFalse();
    }

    @Test
    public void ingestionAllowsSharedSuffix() throws MalformedURLException {
        assertThat(isTrustedIngestionRedirect(INGESTION_ENDPOINT,
            "https://eastus-0.in.applicationinsights.azure.com/v2.1/track")).isTrue();
    }

    @Test
    public void ingestionAllowsSameHost() throws MalformedURLException {
        assertThat(isTrustedIngestionRedirect("https://ingestion.example.com/v2.1/track",
            "https://ingestion.example.com/v2/track")).isTrue();
    }

    @Test
    public void ingestionRejectsSameHostWithDifferentPort() throws MalformedURLException {
        assertThat(isTrustedIngestionRedirect("https://ingestion.example.com/v2.1/track",
            "https://ingestion.example.com:444/v2/track")).isFalse();
    }

    @Test
    public void ingestionRejectsSiblingsOfUntrustedParent() throws MalformedURLException {
        assertThat(isTrustedIngestionRedirect("https://ingestion.example.com/v2.1/track",
            "https://attacker.example.com/v2.1/track")).isFalse();
        assertThat(isTrustedIngestionRedirect("https://foo.azure.com/v2.1/track", "https://bar.azure.com/v2.1/track"))
            .isFalse();
    }

    @Test
    public void ingestionRejectsCrossingBetweenTrustedSuffixes() throws MalformedURLException {
        assertThat(
            isTrustedIngestionRedirect(INGESTION_ENDPOINT, "https://westus.services.visualstudio.com/v2.1/track"))
                .isFalse();
    }

    @Test
    public void ingestionRejectsUnsafeUrls() throws MalformedURLException {
        assertThat(isTrustedIngestionRedirect(INGESTION_ENDPOINT,
            "http://eastus-0.in.applicationinsights.azure.com/v2.1/track")).isFalse();
        assertThat(isTrustedIngestionRedirect(INGESTION_ENDPOINT,
            "https://user@eastus-0.in.applicationinsights.azure.com/v2.1/track")).isFalse();
        assertThat(isTrustedIngestionRedirect(INGESTION_ENDPOINT,
            "https://eastus-0.in.applicationinsights.azure.com:444/v2.1/track")).isFalse();
    }

    private static boolean isTrustedLiveMetricsRedirect(String configuredEndpoint, String redirectLink)
        throws MalformedURLException {
        return RedirectPolicyHelper.isTrustedLiveMetricsRedirect(new URL(configuredEndpoint), new URL(redirectLink));
    }

    private static boolean isTrustedIngestionRedirect(String currentUrl, String redirectLink)
        throws MalformedURLException {
        return RedirectPolicyHelper.isTrustedIngestionRedirect(new URL(currentUrl), new URL(redirectLink));
    }
}
