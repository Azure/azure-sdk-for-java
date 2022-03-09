// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.ModelDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchModelResult;
import com.azure.iot.modelsrepository.implementation.models.ModelMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The {@link RepositoryHandler} is responsible for processing fetched models
 * and generates processed results which are sent back to the client.
 */
public final class RepositoryHandler {

    private final URI repositoryUri;
    private final ModelFetcher modelFetcher;
    private final ClientLogger logger;

    public RepositoryHandler(URI repositoryUri, ModelsRepositoryAPIImpl protocolLayer) {
        this.repositoryUri = repositoryUri;
        this.logger = new ClientLogger(RepositoryHandler.class);

        if (this.repositoryUri.getScheme() != null
            && this.repositoryUri.getScheme()
            .toLowerCase(Locale.getDefault())
            .startsWith(ModelsRepositoryConstants.HTTP)) {
            this.modelFetcher = new HttpModelFetcher(protocolLayer);
        } else {
            this.modelFetcher = new FileModelFetcher();
        }
    }

    public Mono<Map<String, String>> processAsync(String dtmi, ModelDependencyResolution resolutionOptions, Context context) {
        return processAsync(Collections.singletonList(dtmi), resolutionOptions, context);
    }

    public Mono<Map<String, String>> processAsync(Iterable<String> dtmis, ModelDependencyResolution resolutionOptions, Context context) {
        List<String> modelsToProcess = prepareWork(dtmis);

        return isExpandedAvailable(resolutionOptions, context)
            .flatMapMany(tryExpanded -> processAsync(tryExpanded, modelsToProcess, resolutionOptions, context))
            .collectList()
            .flatMap(results -> {
                Map<String, String> processedModels = new HashMap<>();

                try {
                    for (FetchModelResult result : results) {
                        ModelsQuery modelsQuery = new ModelsQuery(result.getDefinition());
                        if (result.isFromExpanded()) {
                            Map<String, String> expanded = modelsQuery.listToMap();
                            for (Map.Entry<String, String> item : expanded.entrySet()) {
                                processedModels.putIfAbsent(item.getKey(), item.getValue());
                            }
                        } else {
                            ModelMetadata metadata = modelsQuery.parseModel();
                            processedModels.put(metadata.getId(), result.getDefinition());
                        }
                    }
                } catch (JsonProcessingException ex) {
                    return Mono.error(ex);
                }

                return Mono.just(processedModels);
            });
    }

    private Flux<FetchModelResult> processAsync(boolean tryExpanded, List<String> dtmis, ModelDependencyResolution resolution, Context context) {
        return Flux.concat(dtmis.stream().map(dtmi -> processDtmi(tryExpanded, dtmi, resolution, context))
            .collect(Collectors.toList()));
    }

    private Flux<FetchModelResult> processDtmi(boolean tryExpanded, String dtmi, ModelDependencyResolution resolution, Context context) {
        return modelFetcher.fetchModelAsync(dtmi, repositoryUri, tryExpanded, context)
            .flatMapMany(response -> {
                // If the model was pre-computed, already expanded, processing of it is done.
                if (response.isFromExpanded()) {
                    return Flux.just(response);
                }

                // If resolving dependencies isn't supported processing completes once the model response is returned.
                if (resolution != ModelDependencyResolution.ENABLED) {
                    return Flux.just(response);
                }

                try {
                    ModelMetadata metadata = new ModelsQuery(response.getDefinition()).parseModel();
                    List<String> dependencies = metadata.getDependencies();

                    if (!CoreUtils.isNullOrEmpty(dependencies)) {
                        logger.log(LogLevel.INFORMATIONAL, () ->
                            String.format(StatusStrings.DISCOVERED_DEPENDENCIES, String.join("\", \"", dependencies)));
                        return processAsync(tryExpanded, dependencies, resolution, context).concatWith(Flux.just(response));
                    }
                    return Flux.just(response);
                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    private List<String> prepareWork(Iterable<String> dtmis) {
        List<String> modelsToProcess = new ArrayList<>();
        for (String dtmi : dtmis) {
            if (!DtmiConventions.isValidDtmi(dtmi)) {
                logger.log(LogLevel.ERROR, () -> String.format(StatusStrings.INVALID_DTMI_FORMAT_S, dtmi));
            }

            modelsToProcess.add(dtmi);
        }

        return modelsToProcess;
    }

    private Mono<Boolean> isExpandedAvailable(ModelDependencyResolution resolutionOption, Context context) {
        // If ModelDependencyResolution.Enabled is requested the client will first attempt to fetch
        // metadata.json content from the target repository. The metadata object includes supported features
        // of the repository.
        // If the metadata indicates expanded models are available. The client will try to fetch pre-computed model
        // dependencies using .expanded.json.
        // If the model expanded form does not exist fall back to computing model dependencies just-in-time.
        if (resolutionOption == ModelDependencyResolution.ENABLED) {
            return modelFetcher.fetchMetadataAsync(repositoryUri, context)
                .map(result -> result.getDefinition() != null
                    && result.getDefinition().getFeatures() != null
                    && result.getDefinition().getFeatures().isExpanded())
                .onErrorReturn(false);
        }

        return Mono.just(false);
    }
}
