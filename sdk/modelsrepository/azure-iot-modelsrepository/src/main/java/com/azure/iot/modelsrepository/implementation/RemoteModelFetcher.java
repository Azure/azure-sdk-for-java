package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.iot.modelsrepository.DependencyResolutionOptions;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

class RemoteModelFetcher implements ModelFetcher {
    private final ModelsRepositoryAPIImpl protocolLayer;
    private final ObjectMapper mapper;

    public RemoteModelFetcher(ModelsRepositoryAPIImpl protocolLayer) {
        this.protocolLayer = protocolLayer;
        mapper = new JacksonAdapter().serializer();
    }

    @Override
    public Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, DependencyResolutionOptions resolutionOption, Context context) throws Exception {
        Queue<String> work = new LinkedList<>();

        if (resolutionOption == DependencyResolutionOptions.TRY_FROM_EXPANDED) {
            work.add(getPath(dtmi, repositoryUri, true));
        }

        work.add(getPath(dtmi, repositoryUri, false));

        String tryContentPath = work.poll();

        // TODO: azabbasi: error handling
        Mono<FetchResult> result = evaluatePath(tryContentPath, context)
            .onErrorResume(s -> {
                if (work.stream().count() != 0) {
                    return evaluatePath(work.poll(), context);
                } else {
                    return Mono.error(s);
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

    private String getPath(String dtmi, URI repositoryUri, boolean expanded) {
        return DtmiConventions.dtmiToQualifiedPath(dtmi, repositoryUri.getPath(), expanded);
    }
}
