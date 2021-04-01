// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.ModelDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import com.azure.iot.modelsrepository.implementation.models.ModelMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;


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

    public Mono<Map<String, String>> processAsync(Iterable<String> dtmis, ModelDependencyResolution
        resolutionOptions, Context context) {

        Map<String, String> processedModels = new HashMap<>();
        Queue<String> modelsToProcess = prepareWork(dtmis);

        return processAsync(modelsToProcess, resolutionOptions, context, processedModels)
            .last()
            .map(IntermediateFetchResult::getMap);
    }

    private Flux<IntermediateFetchResult> processAsync(
        Queue<String> remainingWork,
        ModelDependencyResolution resolutionOption,
        Context context,
        Map<String, String> currentResults) {

        if (remainingWork.isEmpty()) {
            return Flux.empty();
        }

        String targetDtmi = remainingWork.poll();

        logger.info(String.format(StatusStrings.PROCESSING_DTMIS, targetDtmi));

        return modelFetcher.fetchAsync(targetDtmi, repositoryUri, resolutionOption, context)
            .map(result -> new IntermediateFetchResult(result, currentResults))
            .expand(customType -> {
                Map<String, String> results = customType.getMap();
                FetchResult response = customType.getFetchResult();

                if (response.isFromExpanded()) {
                    try {
                        Map<String, String> expanded = new ModelsQuery(response.getDefinition()).listToMap();
                        for (Map.Entry<String, String> item : expanded.entrySet()) {
                            if (!results.containsKey(item.getKey())) {
                                results.put(item.getKey(), item.getValue());
                            }
                        }

                        return processAsync(remainingWork, resolutionOption, context, results);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                }

                try {
                    ModelMetadata metadata = new ModelsQuery(response.getDefinition()).parseModel();

                    if (resolutionOption == ModelDependencyResolution.ENABLED || resolutionOption == ModelDependencyResolution.TRY_FROM_EXPANDED) {
                        List<String> dependencies = metadata.getDependencies();

                        if (dependencies.size() > 0) {
                            logger.info(StatusStrings.DISCOVERED_DEPENDENCIES, String.join("\", \"", dependencies));
                        }

                        remainingWork.addAll(dependencies);
                    }

                    String parsedDtmi = metadata.getId();
                    if (!parsedDtmi.equals(targetDtmi)) {
                        logger.error(String.format(StatusStrings.INCORRECT_DTMI_CASING, targetDtmi, parsedDtmi));
                        String errorMessage = String.format(StatusStrings.GENERIC_GET_MODELS_ERROR, targetDtmi) + String.format(StatusStrings.INCORRECT_DTMI_CASING, targetDtmi, parsedDtmi);

                        return Mono.error(new AzureException(errorMessage));
                    }

                    results.put(targetDtmi, response.getDefinition());
                    return processAsync(remainingWork, resolutionOption, context, results);

                } catch (Exception e) {
                    return Mono.error(e);
                }
            });
    }

    private Queue<String> prepareWork(Iterable<String> dtmis) {
        Queue<String> modelsToProcess = new LinkedList<>();
        for (String dtmi : dtmis) {
            if (!DtmiConventions.isValidDtmi(dtmi)) {
                logger.logExceptionAsError(new IllegalArgumentException(String.format(StatusStrings.INVALID_DTMI_FORMAT_S, dtmi)));
            }

            modelsToProcess.add(dtmi);
        }

        return modelsToProcess;
    }
}
