// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.implementation.PersonalizerClientV1Preview3Impl;
import com.azure.ai.personalizer.implementation.models.MultiSlotRankRequest;
import com.azure.ai.personalizer.implementation.models.MultiSlotRankResponse;
import com.azure.ai.personalizer.implementation.models.MultiSlotRewardRequest;
import com.azure.ai.personalizer.implementation.models.RankRequest;
import com.azure.ai.personalizer.implementation.models.RankResponse;
import com.azure.ai.personalizer.implementation.models.RewardRequest;
import com.azure.ai.personalizer.implementation.util.Transforms;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

public final class PersonalizerAsyncClient {

    private final ClientLogger logger = new ClientLogger(PersonalizerAsyncClient.class);
    private PersonalizerClientV1Preview3Impl service;

    public PersonalizerAsyncClient(PersonalizerClientV1Preview3Impl service) {
        this.service = service;
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RankResponse> rank(RankRequest rankRequest) {
        return rankWithResponse(rankRequest).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RankResponse>> rankWithResponse(RankRequest rankRequest) {
        try {
            return withContext(context -> rankWithResponse(rankRequest, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<RankResponse>> rankWithResponse(RankRequest rankRequest, Context context) {
        if (rankRequest == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'rankRequest' is required and cannot be null"));
        }
        return service.rankWithResponseAsync(rankRequest, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }


    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> reward(String eventId, RewardRequest rewardRequest) {
        return rewardWithResponse(eventId, rewardRequest).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rewardWithResponse(String eventId, RewardRequest rewardRequest) {
        try {
            return withContext(context -> rewardWithResponse(eventId, rewardRequest, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> rewardWithResponse(String eventId, RewardRequest rewardRequest, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        return service.getEvents().rewardWithResponseAsync(eventId, rewardRequest, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> activate(String eventId) {
        return activateWithResponse(eventId).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> activateWithResponse(String eventId) {
        try {
            return withContext(context -> activateWithResponse(eventId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> activateWithResponse(String eventId, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        return service.getEvents().activateWithResponseAsync(eventId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }




    public Mono<Response<MultiSlotRankResponse>> rankMultiSlot(MultiSlotRankRequest rankRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getMultiSlots().rankWithResponseAsync(rankRequest, context);
    }


    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<MultiSlotRankResponse> rankMultiSlot(MultiSlotRankRequest rankRequest) {
        return rankMultiSlotWithResponse(rankRequest).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<MultiSlotRankResponse>> rankMultiSlotWithResponse(MultiSlotRankRequest rankRequest) {
        try {
            return withContext(context -> rankMultiSlotWithResponse(rankRequest, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<MultiSlotRankResponse>> rankMultiSlotWithResponse(MultiSlotRankRequest rankRequest, Context context) {
        if (rankRequest == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'rankRequest' is required and cannot be null"));
        }
        return service.getMultiSlots().rankWithResponseAsync(rankRequest, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    public Mono<Response<Void>> rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest, Context context) {
        context = context == null ? Context.NONE : context;
        return service.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardRequest, context);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rewardMultiSlot(String eventId, MultiSlotRewardRequest rewardRequest) {
        return rewardMultiSlotWithResponse(eventId, rewardRequest).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rewardMultiSlotWithResponse(String eventId, MultiSlotRewardRequest rewardRequest) {
        try {
            return withContext(context -> rewardMultiSlotWithResponse(eventId, rewardRequest, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> rewardMultiSlotWithResponse(String eventId, MultiSlotRewardRequest rewardRequest, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        if (rewardRequest == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'rewardRequest' is required and cannot be null"));
        }
        return service.getMultiSlotEvents().rewardWithResponseAsync(eventId, rewardRequest, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> activateMultiSlot(String eventId) {
        return activateWithResponse(eventId).flatMap(FluxUtil::toMono);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> activateMultiSlotWithResponse(String eventId) {
        try {
            return withContext(context -> activateWithResponse(eventId, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> activateMultiSlotWithResponse(String eventId, Context context) {
        if (CoreUtils.isNullOrEmpty(eventId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'eventId' is required and cannot be null or empty"));
        }
        return service.getMultiSlotEvents().activateWithResponseAsync(eventId, context)
            .onErrorMap(Transforms::mapToHttpResponseExceptionIfExists)
            .map(response -> new SimpleResponse<>(response, null));
    }
}
