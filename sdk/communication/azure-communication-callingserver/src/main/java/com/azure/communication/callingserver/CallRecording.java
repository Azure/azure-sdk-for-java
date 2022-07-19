// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.GroupCallLocator;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.RecordingChannel;
import com.azure.communication.callingserver.models.RecordingContent;
import com.azure.communication.callingserver.models.RecordingFormat;
import com.azure.communication.callingserver.models.RecordingStatusResponse;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * CallRecording.
 */
public class CallRecording {
    private final CallRecordingAsync callRecordingAsync;

    CallRecording(CallRecordingAsync callRecordingAsync) {
        this.callRecordingAsync = callRecordingAsync;
    }

    /**
     * Start recording of the call.
     *
     * @param callLocator Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecordingStatusResponse startRecording(CallLocator callLocator, URI recordingStateCallbackUri) {
        return callRecordingAsync.startRecording(callLocator, recordingStateCallbackUri).block();
    }

    /**
     * Start recording of the call.     *
     *
     * @param callLocator Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @param content Content Type.
     * @param format Format Type.
     * @param channel Channel Type
     * @param context A {@link Context} representing the request context.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecordingStatusResponse> startRecordingWithResponse(
        CallLocator callLocator,
        URI recordingStateCallbackUri,
        RecordingContent content,
        RecordingFormat format,
        RecordingChannel channel,
        Context context) {
        return callRecordingAsync.startRecordingWithResponse(
            callLocator,
            recordingStateCallbackUri,
            content,
            format,
            channel,
            context).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopRecording(String recordingId) {
        callRecordingAsync.stopRecording(recordingId).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String recordingId, Context context) {
        return callRecordingAsync.stopRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void pauseRecording(String recordingId) {
        callRecordingAsync.pauseRecording(recordingId).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String recordingId, Context context) {
        return callRecordingAsync.pauseRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resumeRecording(String recordingId) {
        callRecordingAsync.resumeRecording(recordingId).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String recordingId, final Context context) {
        return callRecordingAsync.resumeRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecordingStatusResponse getRecordingState(String recordingId) {
        return callRecordingAsync.getRecordingState(recordingId).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecordingStatusResponse> getRecordingStateWithResponse(String recordingId, Context context) {
        return callRecordingAsync.getRecordingStateWithResponse(recordingId, context).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint, OutputStream destinationStream, HttpRange httpRange) {
        downloadToWithResponse(sourceEndpoint, destinationStream, httpRange, null);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceEndpoint,
                                                 OutputStream destinationStream,
                                                 HttpRange httpRange,
                                                 final Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        return callRecordingAsync
            .downloadToWithResponse(sourceEndpoint, destinationStream, httpRange, context)
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
    public void downloadTo(String sourceEndpoint,
                           Path destinationPath,
                           ParallelDownloadOptions parallelDownloadOptions,
                           boolean overwrite) {
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
                                                 final Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        return callRecordingAsync.downloadToWithResponse(sourceEndpoint, destinationPath,
            parallelDownloadOptions, overwrite, context).block();
    }

    /**
     * Delete the content located in the deleteEndpoint
     *
     * @param deleteEndpoint - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRecording(String deleteEndpoint, final Context context) {
        callRecordingAsync.deleteRecordingWithResponse(deleteEndpoint, context).block();
    }

    /**
     * Delete the content located in the deleteEndpoint
     *
     * @param deleteEndpoint - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<HttpResponse> deleteRecordingWithResponse(String deleteEndpoint, final Context context) {
        return callRecordingAsync.deleteRecordingWithResponse(deleteEndpoint, context).block();
    }

}
