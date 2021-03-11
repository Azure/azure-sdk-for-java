// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.ModelsDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The {@link LocalModelFetcher} is an implementation of {@link ModelFetcher}
 * for supporting local filesystem based model content fetching.
 */
class LocalModelFetcher implements ModelFetcher {

    /**
     * Creates an instance of {@link LocalModelFetcher}
     */
    public LocalModelFetcher() {
    }

    @Override
    public Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, ModelsDependencyResolution resolutionOption, Context context) throws IOException {
        Queue<String> work = new LinkedList<>();

        if (resolutionOption == ModelsDependencyResolution.TRY_FROM_EXPANDED) {
            work.add(GetPath(dtmi, repositoryUri, true));
        }

        String fnfError = "";
        while (work.stream().count() != 0) {
            String tryContentPath = work.poll();
            Path path = Path.of(tryContentPath);
            if (Files.exists(path)) {
                return Mono.just(
                    new FetchResult()
                        .setDefinition(new String(Files.readAllBytes(path)))
                        .setPath(tryContentPath));
            }
            // TODO: azabbasi: Error messages
            fnfError = "FileNotFound";
        }

        throw new FileNotFoundException(fnfError);
    }

    private String GetPath(String dtmi, URI repositoryUri, boolean expanded) {
        return DtmiConventions.dtmiToQualifiedPath(
            dtmi,
            repositoryUri.getPath(),
            expanded);
    }
}
