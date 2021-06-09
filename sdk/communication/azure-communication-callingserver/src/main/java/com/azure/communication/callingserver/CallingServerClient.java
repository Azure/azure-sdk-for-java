// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Sync Client that supports server calling operations.
 */
@ServiceClient(builder = CallingServerClientBuilder.class)
public final class CallingServerClient {
    private final CallingServerAsyncClient callingServerAsyncClient;

    CallingServerClient(CallingServerAsyncClient callingServerAsyncClient) {
        this.callingServerAsyncClient = callingServerAsyncClient;
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @return response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection createCallConnection(CommunicationIdentifier source,
                                         CommunicationIdentifier[] targets,
                                         CreateCallOptions createCallOptions) {
        return callingServerAsyncClient
            .createCallConnectionInternal(source, targets, createCallOptions).block();
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> createCallConnectionWithResponse(CommunicationIdentifier source,
                                                                     CommunicationIdentifier[] targets,
                                                                     CreateCallOptions createCallOptions,
                                                                     Context context) {
        return callingServerAsyncClient
            .createCallConnectionWithResponseInternal(source, targets, createCallOptions, context).block();
    }

    /**
     * Join a call
     *
     * @param serverCallId The server call id.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @return CallConnection for a successful Join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection join(String serverCallId, CommunicationIdentifier source, JoinCallOptions joinCallOptions) {
        return callingServerAsyncClient.joinInternal(serverCallId, source, joinCallOptions).block();
    }

    /**
     * Join a call
     *
     * @param serverCallId The server call id.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @param context A {@link Context} representing the request context.
     * @return response for a successful Join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> joinWithResponse(String serverCallId, CommunicationIdentifier source, JoinCallOptions joinCallOptions, Context context) {
        return callingServerAsyncClient.joinWithResponseInternal(serverCallId, source, joinCallOptions, context).block();
    }

    /**
     * Get CallConnection object
     *
     * @param callConnectionId The call connection id.
     * @return CallConnection
     */
    public CallConnection getCallConnection(String callConnectionId) {
        return callingServerAsyncClient.getCallConnectionInternal(callConnectionId);
    }

    /**
     * Get ServerCall object
     *
     * @param serverCallId The server call id.
     * @return ServerCall
     */
    public ServerCall initializeServerCall(String serverCallId) {
        return callingServerAsyncClient.initializeServerCallInternal(serverCallId);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it into the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint, OutputStream destinationStream,
                           ParallelDownloadOptions parallelDownloadOptions) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        downloadToWithResponse(sourceEndpoint, destinationStream, parallelDownloadOptions, null);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceEndpoint, OutputStream destinationStream,
                                                 ParallelDownloadOptions parallelDownloadOptions, Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        return callingServerAsyncClient.downloadToWithResponse(sourceEndpoint, destinationStream, parallelDownloadOptions, context)
            .block();
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint, Path destinationPath,
                           ParallelDownloadOptions parallelDownloadOptions, boolean overwrite) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, null);
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceEndpoint,
                                                 Path destinationPath,
                                                 ParallelDownloadOptions parallelDownloadOptions,
                                                 boolean overwrite,
                                                 Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        return callingServerAsyncClient.downloadToWithResponse(sourceEndpoint, destinationPath,
            parallelDownloadOptions, overwrite, context).block();
    }
}


