// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.PollResult;
import com.azure.core.util.polling.PollingContext;

public interface PollingStrategy {
    boolean canPoll(Response<?> activationResponse);
    String getPollingUrl(PollingContext<PollResult> ctx);
    String getFinalResultUrl(PollingContext<PollResult> ctx);
    PollResult parseInitialResponse(Response<?> response, PollingContext<PollResult> ctx);
    PollResult parsePollingResponse(HttpResponse response, String responseBody, PollingContext<PollResult> ctx);
}
