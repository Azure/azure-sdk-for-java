package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.DependencyResolutionOptions;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public final class RepositoryHandler {

    private final String repositoryUri;
    private final ModelsRepositoryAPIImpl protocolLayer;

    public RepositoryHandler(String repositoryUri, ModelsRepositoryAPIImpl protocolLayer) {
        this.repositoryUri = repositoryUri;
        this.protocolLayer = protocolLayer;
    }

    public Mono<Map<String, String>> ProcessAsync(String dtmi, DependencyResolutionOptions resolutionOptions, Context context) throws Exception {
        RemoteModelFetcher fetcher = new RemoteModelFetcher(this.protocolLayer);
        return fetcher.FetchAsync(dtmi, new URI(repositoryUri), resolutionOptions, context).map(s -> new HashMap<String, String>() {{
            put(s.getPath(), s.getDefinition());
        }});
    }

    public Mono<Map<String, String>> ProcessAsync(Iterable<String> dtmis, DependencyResolutionOptions resolutionOptions, Context context) {
        throw new UnsupportedOperationException("TODO: azabbasi");
    }
}
