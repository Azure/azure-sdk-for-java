// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.ModelDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import reactor.core.publisher.Mono;

import java.net.URI;

interface ModelFetcher {
    Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, ModelDependencyResolution resolutionOption, Context context);
}
