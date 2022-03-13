// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.implementation.models.FetchMetadataResult;
import com.azure.iot.modelsrepository.implementation.models.FetchModelResult;
import reactor.core.publisher.Mono;

import java.net.URI;

interface ModelFetcher {
    Mono<FetchModelResult> fetchModelAsync(String dtmi, URI repositoryUri, boolean tryFromExpanded, Context context);
    Mono<FetchMetadataResult> fetchMetadataAsync(URI repositoryUri, Context context);
}
