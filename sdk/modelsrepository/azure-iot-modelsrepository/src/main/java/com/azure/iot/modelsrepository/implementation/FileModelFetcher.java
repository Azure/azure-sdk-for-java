// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.ModelsDependencyResolution;
import com.azure.iot.modelsrepository.implementation.models.FetchResult;
import reactor.core.publisher.Mono;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The {@link FileModelFetcher} is an implementation of {@link ModelFetcher} interface
 * for supporting local filesystem based model content fetching.
 */
class FileModelFetcher implements ModelFetcher {

    private final ClientLogger logger;

    /**
     * Creates an instance of {@link FileModelFetcher}
     */
    public FileModelFetcher(ClientLogger logger) {
        this.logger = logger;
    }

    @Override
    public Mono<FetchResult> fetchAsync(String dtmi, URI repositoryUri, ModelsDependencyResolution resolutionOption, Context context) {
        Queue<String> work = new LinkedList<>();

        try {
            if (resolutionOption == ModelsDependencyResolution.TRY_FROM_EXPANDED) {
                work.add(getPath(dtmi, repositoryUri, true));
            }

            work.add(getPath(dtmi, repositoryUri, false));
        } catch (Exception e) {
            return Mono.error(e);
        }

        String fnfError = "";
        while (work.size() != 0) {
            String tryContentPath = work.poll();
            Path path = Path.of(tryContentPath);

            logger.info(String.format(LoggerStandardStrings.FetchingModelContent, path.toString()));

            if (Files.exists(path)) {
                try {
                    return Mono.just(
                            new FetchResult()
                                    .setDefinition(new String(Files.readAllBytes(path)))
                                    .setPath(tryContentPath));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            }

            logger.error(String.format(LoggerStandardStrings.ErrorFetchingModelContent, path.toString()));

            fnfError = String.format(LoggerStandardStrings.ErrorFetchingModelContent, tryContentPath);
        }

        return Mono.error(new FileNotFoundException(fnfError));
    }

    private String getPath(String dtmi, URI repositoryUri, boolean expanded) throws URISyntaxException {
        return DtmiConventions.getModelUri(
                dtmi,
                repositoryUri,
                expanded)
                .getPath();
    }
}
