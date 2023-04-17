// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.DownloadToFileOptions;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;

import java.io.OutputStream;
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
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecordingStateResult startRecording(StartRecordingOptions options) {
        return callRecordingAsync.startRecording(options).block();
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @param context A {@link Context} representing the request context.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecordingStateResult> startRecordingWithResponse(StartRecordingOptions options, Context context) {
        return callRecordingAsync.startRecordingWithResponseInternal(options, context).block();
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
        return callRecordingAsync.stopRecordingWithResponseInternal(recordingId, context).block();
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
        return callRecordingAsync.pauseRecordingWithResponseInternal(recordingId, context).block();
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
        return callRecordingAsync.resumeRecordingWithResponseInternal(recordingId, context).block();
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
    public RecordingStateResult getRecordingState(String recordingId) {
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
    public Response<RecordingStateResult> getRecordingStateWithResponse(String recordingId, Context context) {
        return callRecordingAsync.getRecordingStateWithResponseInternal(recordingId, context).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceUrl, OutputStream destinationStream) {
        downloadToWithResponse(sourceUrl, destinationStream, null, null);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceUrl,
                                                 OutputStream destinationStream,
                                                 HttpRange httpRange,
                                                 Context context) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        return callRecordingAsync
            .downloadToWithResponse(sourceUrl, destinationStream, httpRange, context)
            .block();
    }

    /**
     * Downloads the entire content.
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadTo(String, OutputStream)} to download larger blobs.</p>
     *
     * @param sourceUrl - ACS URL where the content is located.
     * @return The content of the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public BinaryData downloadContent(String sourceUrl) {
        return callRecordingAsync.downloadContent(sourceUrl).block();
    }

    /**
     * Downloads the entire content.
     * <p>This method supports downloads up to 2GB of data.
     * Use {@link #downloadToWithResponse(String, OutputStream, HttpRange, Context)} to download larger blobs.</p>
     *
     * @param sourceUrl ACS URL where the content is located.
     * @param range An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @return The content of the blob.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<BinaryData> downloadContentWithResponse(String sourceUrl, HttpRange range, Context context) {
        return callRecordingAsync.downloadContentWithResponseInternal(sourceUrl, range, context).block();
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationPath - File location.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceUrl,
                           Path destinationPath) {
        DownloadToFileOptions options = new DownloadToFileOptions();
        downloadToWithResponse(sourceUrl, destinationPath, options, null);
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param options - an optional {@link DownloadToFileOptions} object to modify how the
     *                 download will work.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceUrl,
                                                 Path destinationPath,
                                                 DownloadToFileOptions options,
                                                 final Context context) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        return callRecordingAsync.downloadToWithResponseInternal(sourceUrl, destinationPath,
            options, context).block();
    }

    /**
     * Delete the content located in the deleteUrl
     *
     * @param deleteUrl - ACS URL where the content is located.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRecording(String deleteUrl) {
        deleteRecordingWithResponse(deleteUrl, null);
    }

    /**
     * Delete the content located in the deleteUrl
     *
     * @param deleteUrl - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful delete request..
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRecordingWithResponse(String deleteUrl, Context context) {
        return callRecordingAsync.deleteRecordingWithResponseInternal(deleteUrl, context).block();
    }

}
