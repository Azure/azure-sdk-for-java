// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.ModelDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import reactor.core.publisher.Mono;

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
    private final ClientLogger logger;

    HttpModelFetcher(ModelsRepositoryAPIImpl protocolLayer) {
        this.logger = new ClientLogger(HttpModelFetcher.class);
        this.protocolLayer = protocolLayer;
    }

    @Override
    public Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, ModelDependencyResolution resolutionOption, Context context) {
        return Mono.defer(() -> {
            Queue<String> work = new LinkedList<>();
            try {
                if (resolutionOption == ModelDependencyResolution.TRY_FROM_EXPANDED) {
                    work.add(getPath(dtmi, repositoryUri, true));
                }
                work.add(getPath(dtmi, repositoryUri, false));
            } catch (Exception e) {
                return Mono.error(new AzureException(e));
            }

            String tryContentPath = work.poll();

            logger.info(StatusStrings.FETCHING_MODEL_CONTENT, tryContentPath);

            return evaluatePath(tryContentPath, context)
                .onErrorResume(error -> {
                    if (work.size() != 0) {
                        return evaluatePath(work.poll(), context);
                    } else {
                        return Mono.error(error);
                    }
                })
                .map(s -> new FetchResult().setPath(tryContentPath).setDefinition(s));
        });
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

    private String getPath(String dtmi, URI repositoryUri, boolean expanded) throws URISyntaxException {
        return DtmiConventions.getModelUri(dtmi, repositoryUri, expanded).getPath();
    }
}
