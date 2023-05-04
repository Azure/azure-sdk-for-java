// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

public interface OpenAIServiceBase {

    Mono<Response<BinaryData>> getEmbeddings(
        String endpoint,
        String apiVersion,
        String deploymentId,
        String accept,
        BinaryData embeddingsOptions,
        RequestOptions requestOptions,
        Context context);

    Mono<Response<BinaryData>> getCompletions(
        String endpoint,
        String apiVersion,
        String deploymentId,
        String accept,
        BinaryData completionsOptions,
        RequestOptions requestOptions,
        Context context);

    Mono<Response<BinaryData>> getChatCompletions(
        String endpoint,
        String apiVersion,
        String deploymentId,
        String accept,
        BinaryData chatCompletionsOptions,
        RequestOptions requestOptions,
        Context context);
}
