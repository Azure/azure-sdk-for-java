// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.ModelsDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import com.azure.iot.modelsrepository.implementation.models.ModelMetadata;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.*;

public final class RepositoryHandler {

    private final URI repositoryUri;
    private final ModelsRepositoryAPIImpl protocolLayer;
    private final ModelFetcher modelFetcher;

    public RepositoryHandler(URI repositoryUri, ModelsRepositoryAPIImpl protocolLayer) {
        this.repositoryUri = repositoryUri;
        this.protocolLayer = protocolLayer;

        if (this.repositoryUri.getScheme() != "file") {
            this.modelFetcher = new RemoteModelFetcher(protocolLayer);
        } else {
            this.modelFetcher = new LocalModelFetcher();
        }
    }

    public Mono<Map<String, String>> processAsync(String dtmi, ModelsDependencyResolution resolutionOptions, Context context) {
        return processAsync(Arrays.asList(dtmi), resolutionOptions, context);
    }

    public Mono<Map<String, String>> processAsync(Iterable<String> dtmis, ModelsDependencyResolution resolutionOptions, Context context) {

        Map<String, String> processedModels = new HashMap<>();
        Queue<String> modelsToProcess = prepareWork(dtmis);
        List<Mono> monos = new ArrayList<>();

        while (modelsToProcess.stream().count() != 0) {
            String targetDtmi = modelsToProcess.poll();

            if (processedModels.containsKey(targetDtmi)) {
                continue;
            }

            try {
                FetchResult result = this.modelFetcher.fetchAsync(targetDtmi, repositoryUri, resolutionOptions, context).block();

                if (result.isFromExpanded()) {
                    Map<String, String> expanded = new ModelsQuery(result.getDefinition()).listToMap();

                    for (Map.Entry<String, String> item : expanded.entrySet()) {
                        if (!processedModels.containsKey(item.getKey())) {
                            processedModels.put(item.getKey(), item.getValue());
                        }
                    }

                    continue;
                }

                ModelMetadata metadata = new ModelsQuery(result.getDefinition()).parseModel();

                if (resolutionOptions == ModelsDependencyResolution.ENABLED || resolutionOptions == ModelsDependencyResolution.TRY_FROM_EXPANDED) {
                    List<String> dependencies = metadata.getDependencies();

                    for (String dependency : dependencies) {
                        modelsToProcess.add(dependency);
                    }
                }

                String parsedDtmi = metadata.getId();
                if (!parsedDtmi.equals(targetDtmi)) {
                    //TODO: azabbasi: throw exception
                }

                processedModels.put(targetDtmi, result.getDefinition());

            } catch (Exception e) {
                // TODO: azabbasi: fix error handling.
            }
        }
        return Mono.just(processedModels);
    }

    private Queue<String> prepareWork(Iterable<String> dtmis) {
        Queue<String> modelsToProcess = new LinkedList<>();
        for (String dtmi : dtmis) {
            if (!DtmiConventions.isValidDtmi(dtmi)) {
                // TODO: azabbasi : error handling
                throw new IllegalArgumentException(dtmi);
            }

            modelsToProcess.add(dtmi);
        }
        return modelsToProcess;
    }
}
