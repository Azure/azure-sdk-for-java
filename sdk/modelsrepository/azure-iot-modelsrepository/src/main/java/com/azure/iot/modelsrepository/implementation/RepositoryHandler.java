package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.DependencyResolutionOptions;
import reactor.core.publisher.Mono;

import java.util.Map;

public final class RepositoryHandler {

    private final String repositoryUri;
    private final ModelsRepositoryAPIImpl protocolLayer;

    public RepositoryHandler(String repositoryUri, ModelsRepositoryAPIImpl protocolLayer) {
        this.repositoryUri = repositoryUri;
        this.protocolLayer = protocolLayer;
    }

    public Mono<Map<String, String>> ProcessAsync(String dtmi, DependencyResolutionOptions resolutionOptions, Context context) {
        throw new UnsupportedOperationException("TODO: azabbasi");
    }

    public Mono<Map<String, String>> ProcessAsync(Iterable<String> dtmis, DependencyResolutionOptions resolutionOptions, Context context) {
        throw new UnsupportedOperationException("TODO: azabbasi");
    }
}
