// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.implementation.models.FetchMetadataResult;
import com.azure.iot.modelsrepository.implementation.models.FetchModelResult;
import com.fasterxml.jackson.core.JsonProcessingException;
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
    private final ClientLogger logger;

    HttpModelFetcher(ModelsRepositoryAPIImpl protocolLayer) {
        this.logger = new ClientLogger(HttpModelFetcher.class);
        this.protocolLayer = protocolLayer;
    }

    @Override
    public Mono<FetchModelResult> fetchModelAsync(String dtmi, URI repositoryUri, boolean tryFromExpanded, Context context) {
        return Mono.defer(() -> {
            Queue<String> work = new LinkedList<>();
            try {
                if (tryFromExpanded) {
                    work.add(getModelPath(dtmi, repositoryUri, true));
                }
                work.add(getModelPath(dtmi, repositoryUri, false));
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
                        logger.error(String.format(StatusStrings.ERROR_FETCHING_MODEL_CONTENT, tryContentPath));
                        return Mono.error(error);
                    }
                })
                .map(s -> new FetchModelResult().setPath(tryContentPath).setDefinition(s));
        });
    }

    @Override
    public Mono<FetchMetadataResult> fetchMetadataAsync(URI repositoryUri, Context context) {
        return Mono.defer(() -> {
            try {
                String tryContentPath = getMetadataPath(repositoryUri);

                logger.info(StatusStrings.FETCHING_METADATA_CONTENT, tryContentPath);

                return evaluatePath(tryContentPath, context)
                    .onErrorResume(error -> {
                        logger.error(String.format(StatusStrings.ERROR_FETCHING_METADATA_CONTENT + " Error: %s",
                            tryContentPath, error.getMessage()));
                        return null;
                    })
                    .map(s -> {
                        try {
                            if (s == null) {
                                return null;
                            }
                            return new FetchMetadataResult().setPath(tryContentPath).setDefinition(s);
                        } catch (JsonProcessingException e) {
                            logger.error(String.format(StatusStrings.ERROR_FETCHING_METADATA_CONTENT, tryContentPath));
                            return null;
                        }
                    });
            } catch (MalformedURLException | URISyntaxException e) {
                return Mono.error(new AzureException(e));
            }
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

    private String getModelPath(String dtmi, URI repositoryUri, boolean expanded) throws URISyntaxException {
        return DtmiConventions.getModelUri(dtmi, repositoryUri, expanded).getPath();
    }

    private String getMetadataPath(URI repositoryUri) throws URISyntaxException, MalformedURLException {
        return DtmiConventions.getMetadataUri(repositoryUri).getPath();
    }
}
