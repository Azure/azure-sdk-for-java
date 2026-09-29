// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation;

import com.azure.core.annotation.ExpectedResponses;
import com.azure.core.annotation.Get;
import com.azure.core.annotation.Head;
import com.azure.core.annotation.HeaderParam;
import com.azure.core.annotation.Host;
import com.azure.core.annotation.HostParam;
import com.azure.core.annotation.PathParam;
import com.azure.core.annotation.QueryParam;
import com.azure.core.annotation.ServiceInterface;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.Context;
import com.azure.storage.file.share.implementation.models.DirectoriesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.FilesGetPropertiesHeaders;
import com.azure.storage.file.share.implementation.models.ShareStorageExceptionInternal;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.ShareTokenIntent;
import reactor.core.publisher.Mono;

/**
 * REST operations that accept a file ID as a query parameter but are not represented by the generated operation
 * signatures.
 */
public final class FileIdOperations {
    private static final ClientLogger LOGGER = new ClientLogger(FileIdOperations.class);

    private final OperationsService service;
    private final AzureFileStorageImpl client;

    /**
     * Creates a file-ID operations client.
     *
     * @param client The generated service client.
     */
    public FileIdOperations(AzureFileStorageImpl client) {
        this.client = client;
        this.service
            = RestProxy.create(OperationsService.class, client.getHttpPipeline(), client.getSerializerAdapter());
    }

    /**
     * Validates a file ID accepted by a client factory or builder.
     *
     * @param fileId The ID to validate.
     * @throws IllegalArgumentException If the ID is null or blank.
     */
    public static void validateFileId(String fileId) {
        if (fileId == null) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'fileId' cannot be null."));
        }
        if (fileId.trim().isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'fileId' cannot be empty."));
        }
    }

    /**
     * Ensures an operation is used with a file-ID-addressed client.
     *
     * @param fileId The client's file ID.
     * @param operationName The operation name to report on failure.
     */
    public static void ensureFileIdAddressed(String fileId, String operationName) {
        if (fileId == null || fileId.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(operationName + " requires a file-ID-addressed client."));
        }
    }

    /**
     * Ensures a path-based operation is not used with a file-ID-addressed client.
     *
     * @param fileId The client's file ID.
     * @param operationName The operation name to report on failure.
     */
    public static void ensurePathAddressed(String fileId, String operationName) {
        if (fileId != null && !fileId.isEmpty()) {
            throw LOGGER.logExceptionAsError(
                new IllegalStateException(operationName + " is not supported for a file-ID-addressed client."));
        }
    }

    public Mono<ResponseBase<FilesGetPropertiesHeaders, Void>> getFilePropertiesAsync(String shareName, String fileId,
        String snapshot, Integer timeout, String leaseId, Context context) {
        return service
            .getFileProperties(client.getUrl(), shareName, fileId, snapshot, timeout, client.isAllowTrailingDot(),
                client.getVersion(), leaseId, client.getFileRequestIntent(), null, "application/xml", context)
            .onErrorMap(ShareStorageExceptionInternal.class, ModelHelper::mapToShareStorageException);
    }

    public ResponseBase<FilesGetPropertiesHeaders, Void> getFileProperties(String shareName, String fileId,
        String snapshot, Integer timeout, String leaseId, Context context) {
        try {
            return service.getFilePropertiesSync(client.getUrl(), shareName, fileId, snapshot, timeout,
                client.isAllowTrailingDot(), client.getVersion(), leaseId, client.getFileRequestIntent(), null,
                "application/xml", context);
        } catch (ShareStorageExceptionInternal exception) {
            throw LOGGER.logExceptionAsError(ModelHelper.mapToShareStorageException(exception));
        }
    }

    public Mono<ResponseBase<DirectoriesGetPropertiesHeaders, Void>> getDirectoryPropertiesAsync(String shareName,
        String fileId, String snapshot, Integer timeout, Context context) {
        return service
            .getDirectoryProperties(client.getUrl(), shareName, "directory", fileId, snapshot, timeout,
                client.isAllowTrailingDot(), client.getVersion(), client.getFileRequestIntent(), null,
                "application/xml", context)
            .onErrorMap(ShareStorageExceptionInternal.class, ModelHelper::mapToShareStorageException);
    }

    public ResponseBase<DirectoriesGetPropertiesHeaders, Void> getDirectoryProperties(String shareName, String fileId,
        String snapshot, Integer timeout, Context context) {
        try {
            return service.getDirectoryPropertiesSync(client.getUrl(), shareName, "directory", fileId, snapshot,
                timeout, client.isAllowTrailingDot(), client.getVersion(), client.getFileRequestIntent(), null,
                "application/xml", context);
        } catch (ShareStorageExceptionInternal exception) {
            throw LOGGER.logExceptionAsError(ModelHelper.mapToShareStorageException(exception));
        }
    }

    @Host("{url}")
    @ServiceInterface(name = "AzureFileStorageByFileId")
    public interface OperationsService {
        @Head("/{shareName}")
        @ExpectedResponses({ 200 })
        Mono<ResponseBase<FilesGetPropertiesHeaders, Void>> getFileProperties(@HostParam("url") String url,
            @PathParam("shareName") String shareName, @QueryParam("fileid") String fileId,
            @QueryParam("sharesnapshot") String snapshot, @QueryParam("timeout") Integer timeout,
            @HeaderParam("x-ms-allow-trailing-dot") Boolean allowTrailingDot,
            @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-lease-id") String leaseId,
            @HeaderParam("x-ms-file-request-intent") ShareTokenIntent fileRequestIntent,
            @HeaderParam("x-ms-client-request-id") String requestId, @HeaderParam("Accept") String accept,
            Context context);

        @Head("/{shareName}")
        @ExpectedResponses({ 200 })
        ResponseBase<FilesGetPropertiesHeaders, Void> getFilePropertiesSync(@HostParam("url") String url,
            @PathParam("shareName") String shareName, @QueryParam("fileid") String fileId,
            @QueryParam("sharesnapshot") String snapshot, @QueryParam("timeout") Integer timeout,
            @HeaderParam("x-ms-allow-trailing-dot") Boolean allowTrailingDot,
            @HeaderParam("x-ms-version") String version, @HeaderParam("x-ms-lease-id") String leaseId,
            @HeaderParam("x-ms-file-request-intent") ShareTokenIntent fileRequestIntent,
            @HeaderParam("x-ms-client-request-id") String requestId, @HeaderParam("Accept") String accept,
            Context context);

        @Get("/{shareName}")
        @ExpectedResponses({ 200 })
        Mono<ResponseBase<DirectoriesGetPropertiesHeaders, Void>> getDirectoryProperties(@HostParam("url") String url,
            @PathParam("shareName") String shareName, @QueryParam("restype") String resourceType,
            @QueryParam("fileid") String fileId, @QueryParam("sharesnapshot") String snapshot,
            @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-allow-trailing-dot") Boolean allowTrailingDot,
            @HeaderParam("x-ms-version") String version,
            @HeaderParam("x-ms-file-request-intent") ShareTokenIntent fileRequestIntent,
            @HeaderParam("x-ms-client-request-id") String requestId, @HeaderParam("Accept") String accept,
            Context context);

        @Get("/{shareName}")
        @ExpectedResponses({ 200 })
        ResponseBase<DirectoriesGetPropertiesHeaders, Void> getDirectoryPropertiesSync(@HostParam("url") String url,
            @PathParam("shareName") String shareName, @QueryParam("restype") String resourceType,
            @QueryParam("fileid") String fileId, @QueryParam("sharesnapshot") String snapshot,
            @QueryParam("timeout") Integer timeout, @HeaderParam("x-ms-allow-trailing-dot") Boolean allowTrailingDot,
            @HeaderParam("x-ms-version") String version,
            @HeaderParam("x-ms-file-request-intent") ShareTokenIntent fileRequestIntent,
            @HeaderParam("x-ms-client-request-id") String requestId, @HeaderParam("Accept") String accept,
            Context context);
    }
}
