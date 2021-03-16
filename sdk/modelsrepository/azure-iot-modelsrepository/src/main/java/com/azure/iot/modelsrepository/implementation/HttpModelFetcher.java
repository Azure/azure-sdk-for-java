// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.ModelsDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The {@link HttpModelFetcher} is an implementation of {@link ModelFetcher} interface
 * for supporting http[s] based model content fetching.
 */
class HttpModelFetcher implements ModelFetcher {
    private final ModelsRepositoryAPIImpl protocolLayer;
    private final ObjectMapper mapper;

    public HttpModelFetcher(ModelsRepositoryAPIImpl protocolLayer) {
        this.protocolLayer = protocolLayer;
        mapper = new JacksonAdapter().serializer();
    }

    @Override
    public Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, ModelsDependencyResolution resolutionOption, Context context) {
        Queue<String> work = new LinkedList<>();
        try {
            if (resolutionOption == ModelsDependencyResolution.TRY_FROM_EXPANDED) {
                work.add(getPath(dtmi, repositoryUri, true).getPath());
            }
            work.add(getPath(dtmi, repositoryUri, false).getPath());

        } catch (Exception e) {
            return Mono.error(e);
        }


        String tryContentPath = work.poll();

        Mono<FetchResult> result = evaluatePath(tryContentPath, context)
            .onErrorResume(error -> {
                if (work.size() != 0) {
                    return evaluatePath(work.poll(), context);
                } else {
                    return Mono.error(error);
                }
            })
            .map(s -> new FetchResult().setPath(tryContentPath).setDefinition(s));

        return result;
    }

    private Mono<String> evaluatePath(String tryContentPath, Context context) {
        return protocolLayer
            .getModelsRepository()
            .getModelFromPathWithResponseAsync(tryContentPath, context)
            .flatMap(response -> {
                String stringResponse = new String(response, StandardCharsets.UTF_8);
                return Mono.just(stringResponse);
            });
    }

    private URI getPath(String dtmi, URI repositoryUri, boolean expanded) throws MalformedURLException, URISyntaxException {
        return DtmiConventions.getModelUri(dtmi, repositoryUri, expanded);
    }
}
