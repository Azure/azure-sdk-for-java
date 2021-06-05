// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.verticals.agrifood.farming;

import com.azure.core.util.polling.PollResult;
import com.azure.core.util.polling.PollingContext;
import com.azure.core.util.polling.strategy.LocationPollingStrategy;

public class FarmBeatsPollingStrategy extends LocationPollingStrategy {
    private String apiVersion;

    public FarmBeatsPollingStrategy(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public String getPollingUrl(PollingContext<PollResult> ctx) {
        return normalizeUrl(super.getPollingUrl(ctx));
    }

    @Override
    public String getFinalResultUrl(PollingContext<PollResult> ctx) {
        return normalizeUrl(super.getFinalResultUrl(ctx));
    }

    private String normalizeUrl(String url) {
        url = url.replace("http://", "https://");
        if (!url.contains("api-version=")) {
            String apiVersionQuery = "api-version=" + apiVersion;
            if (!url.contains("?")) {
                url += "?" + apiVersionQuery;
            } else {
                url += "&" + apiVersionQuery;
            }
        }
        return url;
    }
}
