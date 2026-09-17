// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.ai.projects.models.BlobReference;
import com.azure.ai.projects.models.FileUploadOptions;
import com.azure.ai.projects.models.ModelUploadOptions;
import com.azure.ai.projects.models.ModelVersion;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

/** Shared local-file validation and blob upload configuration. */
public final class FileUploadHelper {
    private FileUploadHelper() {
    }

    /**
     * Validates a model upload before requesting remote storage.
     * @param name model name.
     * @param version model version.
     * @param source local source.
     * @param options upload options.
     * @return selected files.
     */
    public static List<Path> getModelFiles(String name, String version, Path source, ModelUploadOptions options) {
        if (name == null || name.trim().isEmpty() || version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("Model name and version must not be empty.");
        }
        if (source == null || source.getFileName() == null || !Files.exists(source)) {
            throw new IllegalArgumentException("A model file or folder is required.");
        }
        if (Files.isDirectory(source)) {
            return getFiles(source, options.getFileUploadOptions());
        }
        try {
            if (!Files.isRegularFile(source) || Files.size(source) == 0) {
                throw new IllegalArgumentException("The model source must be a nonempty regular file.");
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
        return java.util.Collections.singletonList(source);
    }

    /**
     * Creates the model registration payload without SAS query parameters.
     * @param blobUrl uploaded blob or container URL.
     * @param options model metadata.
     * @return the registration payload.
     */
    public static ModelVersion createModelVersion(String blobUrl, ModelUploadOptions options) {
        return new ModelVersion(com.azure.core.util.UrlBuilder.parse(blobUrl).setQuery(null).toString())
            .setWeightType(options.getWeightType())
            .setBaseModel(options.getBaseModel())
            .setDescription(options.getDescription())
            .setTags(options.getTags());
    }

    /**
     * Reads both modeled and datastore-style model pending-upload responses.
     * @param response raw pending-upload response.
     * @return the validated storage reference.
     */
    public static BlobReference getModelBlobReference(BinaryData response) {
        java.util.Map<?, ?> payload = response.toObject(java.util.Map.class);
        Object reference = payload.get("blobReferenceForConsumption");
        if (reference == null) {
            reference = payload.get("blobReference");
        }
        BlobReference result
            = reference == null ? null : BinaryData.fromObject(reference).toObject(BlobReference.class);
        if (result == null
            || CoreUtils.isNullOrEmpty(result.getBlobUrl())
            || result.getCredential() == null
            || CoreUtils.isNullOrEmpty(result.getCredential().getSasUrl())) {
            throw new IllegalArgumentException("The model pending upload response has no blob URI or SAS credential.");
        }
        return result;
    }

    /**
     * Selects regular files recursively, rejecting empty selections before any upload.
     * @param folder the local directory.
     * @param options the optional upload settings.
     * @return the selected files.
     */
    public static List<Path> getFiles(Path folder, FileUploadOptions options) {
        if (folder == null || !Files.isDirectory(folder)) {
            throw new IllegalArgumentException("The provided path is not a folder: " + folder);
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                .filter(path -> options == null
                    || options.getFilePattern() == null
                    || options.getFilePattern().matcher(path.getFileName().toString()).find())
                .collect(Collectors.toList());
            if (files.isEmpty()) {
                throw new IllegalArgumentException("The provided folder contains no matching files.");
            }
            return files;
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to walk the upload folder.", exception);
        }
    }

    /**
     * Builds a blob container client configuration using service-issued SAS credentials.
     * @param reference the service's blob reference.
     * @param options optional configuration callbacks.
     * @return the configured builder.
     */
    public static BlobContainerClientBuilder createContainerBuilder(BlobReference reference,
        FileUploadOptions options) {
        if (reference == null
            || reference.getCredential() == null
            || CoreUtils.isNullOrEmpty(reference.getCredential().getSasUrl())) {
            throw new IllegalArgumentException("The pending upload response has no blob SAS credential.");
        }
        BlobContainerClientBuilder builder = new BlobContainerClientBuilder();
        if (options != null && options.getBlobClientConfiguration() != null) {
            options.getBlobClientConfiguration().accept(builder);
        }
        return builder.endpoint(reference.getCredential().getSasUrl());
    }

    /**
     * Creates fresh upload options for a file.
     * @param file the file to upload.
     * @param options optional configuration callbacks.
     * @return the blob upload options.
     */
    public static BlobParallelUploadOptions createUploadOptions(Path file, FileUploadOptions options) {
        BlobParallelUploadOptions upload = new BlobParallelUploadOptions(
            Flux.using(() -> Files.newInputStream(file), FluxUtil::toFluxByteBuffer, stream -> {
                try {
                    stream.close();
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            }));
        if (options != null && options.getBlobUploadConfiguration() != null) {
            options.getBlobUploadConfiguration().accept(upload);
        }
        return upload;
    }
}
