// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling.strategy;

import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.polling.PollResult;
import com.azure.core.util.polling.PollingContext;
import reactor.core.publisher.Mono;

import java.lang.reflect.Type;

public interface PollingStrategy {
    boolean canPoll(Response<?> activationResponse);
    Mono<PollResult> onActivationResponse(Response<?> response, PollingContext<PollResult> ctx);
    String getPollingUrl(PollingContext<PollResult> ctx);
    Mono<PollResult> onPollingResponse(HttpResponse response, PollingContext<PollResult> ctx);
    String getFinalGetUrl(PollingContext<PollResult> ctx);
    <U> Mono<U> getFinalResult(HttpResponse response, PollingContext<PollResult> ctx, Type resultType);
}
