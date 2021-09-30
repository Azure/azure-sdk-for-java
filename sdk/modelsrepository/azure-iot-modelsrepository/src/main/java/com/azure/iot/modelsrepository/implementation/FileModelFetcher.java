// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation;

import com.azure.core.exception.AzureException;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.iot.modelsrepository.DtmiConventions;
import com.azure.iot.modelsrepository.implementation.models.FetchMetadataResult;
import com.azure.iot.modelsrepository.implementation.models.FetchModelResult;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    FileModelFetcher() {
        this.logger = new ClientLogger(FileModelFetcher.class);
    }

    @Override
    public Mono<FetchModelResult> fetchModelAsync(String dtmi, URI repositoryUri, boolean tryFromExpanded, Context context) {
        return Mono.defer(() -> {
            Queue<String> work = new LinkedList<>();

            try {
                if (tryFromExpanded) {
                    work.add(getModelPath(dtmi, repositoryUri, true));
                }
                work.add(getModelPath(dtmi, repositoryUri, false));
            } catch (MalformedURLException | URISyntaxException e) {
                return Mono.error(new AzureException(e));
            }

            String fnfError = "";
            while (work.size() != 0) {
                String tryContentPath = work.poll();

                Path path = Paths.get(new File(tryContentPath).getPath());

                logger.info(StatusStrings.FETCHING_MODEL_CONTENT, path);

                if (Files.exists(path)) {
                    try {
                        return Mono.just(
                            new FetchModelResult()
                                .setDefinition(new String(Files.readAllBytes(path), StandardCharsets.UTF_8))
                                .setPath(tryContentPath));
                    } catch (IOException e) {
                        return Mono.error(new AzureException(e));
                    }
                }

                logger.error(String.format(StatusStrings.ERROR_FETCHING_MODEL_CONTENT, path.toString()));

                fnfError = String.format(StatusStrings.ERROR_FETCHING_MODEL_CONTENT, tryContentPath);
            }

            return Mono.error(new AzureException(fnfError));
        });
    }

    @Override
    public Mono<FetchMetadataResult> fetchMetadataAsync(URI repositoryUri, Context context) {
        return Mono.defer(() -> {

            try {
                String tryContentPath = getMetadataPath(repositoryUri);
                Path path = Paths.get(new File(tryContentPath).getPath());

                logger.info(StatusStrings.FETCHING_METADATA_CONTENT, path);

                if (Files.exists(path)) {
                    try {
                        return Mono.just(
                            new FetchMetadataResult()
                                .setDefinition(new String(Files.readAllBytes(path), StandardCharsets.UTF_8))
                                .setPath(tryContentPath));
                    } catch (IOException e) {
                        logger.error(String.format(StatusStrings.ERROR_FETCHING_METADATA_CONTENT + " Error: %s.",
                            path.toString(), e.getMessage()));
                        return null;
                    }
                }

                logger.error(String.format(StatusStrings.ERROR_FETCHING_METADATA_CONTENT, path.toString()));
                return null;
            } catch (MalformedURLException | URISyntaxException e) {
                return Mono.error(new AzureException(e));
            }
        });
    }

    private String getModelPath(String dtmi, URI repositoryUri, boolean expanded) throws URISyntaxException, MalformedURLException {
        return DtmiConventions.getModelUri(dtmi, repositoryUri, expanded)
            .getPath();
    }

    private String getMetadataPath(URI repositoryUri) throws URISyntaxException, MalformedURLException  {
        return DtmiConventions.getMetadataUri(repositoryUri)
            .getPath();
    }
}
