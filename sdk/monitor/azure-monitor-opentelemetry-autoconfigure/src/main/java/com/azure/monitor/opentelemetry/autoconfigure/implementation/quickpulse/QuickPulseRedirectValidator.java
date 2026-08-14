// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.RedirectPolicyHelper;

import java.net.MalformedURLException;
import java.net.URL;

final class QuickPulseRedirectValidator {

    private QuickPulseRedirectValidator() {
    }

    static String validateAndGetEndpointPrefix(String configuredEndpoint, String redirectLink)
        throws MalformedURLException {
        URL configuredUrl = new URL(configuredEndpoint);
        URL redirectUrl = new URL(redirectLink);

        if (!RedirectPolicyHelper.isTrustedLiveMetricsRedirect(configuredUrl, redirectUrl)) {
            throw new MalformedURLException("Redirect host is outside the configured Live Metrics endpoint boundary");
        }

        return redirectUrl.getProtocol() + "://" + redirectUrl.getAuthority() + "/";
    }
}
