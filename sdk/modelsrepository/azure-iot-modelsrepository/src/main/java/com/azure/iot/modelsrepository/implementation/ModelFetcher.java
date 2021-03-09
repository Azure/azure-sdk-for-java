package com.azure.iot.modelsrepository.implementation;


import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.DependencyResolutionOptions;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import reactor.core.publisher.Mono;

import java.net.URI;

interface ModelFetcher {
    Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, DependencyResolutionOptions resolutionOption, Context context) throws Exception;
}
