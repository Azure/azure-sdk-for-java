// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.DownloadToFileOptions;
import com.azure.communication.callingserver.models.GroupCallLocator;
import com.azure.communication.callingserver.models.RecordingStatusResponse;
import com.azure.communication.callingserver.models.ServerCallLocator;
import com.azure.communication.callingserver.models.StartRecordingOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
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
     * Start recording of the call.
     *
     * @param callLocator Either a {@link GroupCallLocator} or {@link ServerCallLocator} for locating the call.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
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
        StartRecordingOptions options,
        Context context) {
        return callRecordingAsync.startRecordingWithResponse(
            callLocator,
            recordingStateCallbackUri,
            options,
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint, OutputStream destinationStream) {
        downloadToWithResponse(sourceEndpoint, destinationStream, null, null);
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
                                                 Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        return callRecordingAsync
            .downloadToWithResponse(sourceEndpoint, destinationStream, httpRange, context)
            .block();
    }

    /**
     * Downloads the entire content.
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadTo(String, OutputStream)} to download larger blobs.</p>
     *
     * @param sourceEndpoint - ACS URL where the content is located.
     * @return The content of the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData downloadContent(String sourceEndpoint) {
        return callRecordingAsync.downloadContent(sourceEndpoint).block();
    }

    /**
     * Downloads the entire content.
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadToWithResponse(String, OutputStream, HttpRange, Context)} to download larger blobs.</p>
     *
     * @param sourceEndpoint ACS URL where the content is located.
     * @param range An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @return The content of the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> downloadContentWithResponse(String sourceEndpoint, HttpRange range, Context context) {
        return callRecordingAsync.downloadContentWithResponse(sourceEndpoint, range, context).block();
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint,
                           Path destinationPath) {
        DownloadToFileOptions options = new DownloadToFileOptions();
        downloadToWithResponse(sourceEndpoint, destinationPath, options, null);
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param options - an optional {@link DownloadToFileOptions} object to modify how the
     *                 download will work.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceEndpoint,
                                                 Path destinationPath,
                                                 DownloadToFileOptions options,
                                                 final Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        return callRecordingAsync.downloadToWithResponse(sourceEndpoint, destinationPath,
            options, context).block();
    }

    /**
     * Delete the content located in the deleteEndpoint
     *
     * @param deleteEndpoint - ACS URL where the content is located.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRecording(String deleteEndpoint) {
        deleteRecordingWithResponse(deleteEndpoint, null);
    }

    /**
     * Delete the content located in the deleteEndpoint
     *
     * @param deleteEndpoint - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful delete request..
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRecordingWithResponse(String deleteEndpoint, Context context) {
        return callRecordingAsync.deleteRecordingWithResponse(deleteEndpoint, context).block();
    }

}
