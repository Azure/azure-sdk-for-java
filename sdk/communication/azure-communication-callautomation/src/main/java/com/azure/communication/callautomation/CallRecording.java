// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.DownloadToFileOptions;
import com.azure.communication.callautomation.models.RecordingStateResult;
import com.azure.communication.callautomation.models.StartRecordingOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
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
public final class CallRecording {
    private final CallRecordingAsync callRecordingAsync;

    CallRecording(CallRecordingAsync callRecordingAsync) {
        this.callRecordingAsync = callRecordingAsync;
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecordingStateResult start(StartRecordingOptions options) {
        return callRecordingAsync.start(options).block();
    }

    /**
     * Start recording of the call.
     *
     * @param options A {@link StartRecordingOptions} object containing different options for recording.
     * @param context A {@link Context} representing the request context.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecordingStateResult> startWithResponse(StartRecordingOptions options, Context context) {
        return callRecordingAsync.startWithResponseInternal(options, context).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stop(String recordingId) {
        callRecordingAsync.stop(recordingId).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopWithResponse(String recordingId, Context context) {
        return callRecordingAsync.stopWithResponseInternal(recordingId, context).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void pause(String recordingId) {
        callRecordingAsync.pause(recordingId).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseWithResponse(String recordingId, Context context) {
        return callRecordingAsync.pauseWithResponseInternal(recordingId, context).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resume(String recordingId) {
        callRecordingAsync.resume(recordingId).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeWithResponse(String recordingId, final Context context) {
        return callRecordingAsync.resumeWithResponseInternal(recordingId, context).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RecordingStateResult getState(String recordingId) {
        return callRecordingAsync.getState(recordingId).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RecordingStateResult> getStateWithResponse(String recordingId, Context context) {
        return callRecordingAsync.getStateWithResponseInternal(recordingId, context).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceUrl, OutputStream destinationStream) {
        downloadTo(sourceUrl, destinationStream, null, null);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceUrl,
                           OutputStream destinationStream,
                           HttpRange httpRange,
                           Context context) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        callRecordingAsync.downloadTo(sourceUrl, destinationStream, httpRange, context).block();
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
     * Use {@link #downloadTo(String, OutputStream, HttpRange, Context)} to download larger blobs.</p>
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
        downloadTo(sourceUrl, destinationPath, options, null);
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceUrl - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param options - an optional {@link DownloadToFileOptions} object to modify how the
     *                 download will work.
     * @param context A {@link Context} representing the request context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceUrl,
                                     Path destinationPath,
                                     DownloadToFileOptions options,
                                     final Context context) {
        Objects.requireNonNull(sourceUrl, "'sourceUrl' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        callRecordingAsync.downloadToInternal(sourceUrl, destinationPath, options, context).block();
    }

    /**
     * Delete the content located in the deleteUrl
     *
     * @param deleteUrl - ACS URL where the content is located.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void delete(String deleteUrl) {
        deleteWithResponse(deleteUrl, null);
    }

    /**
     * Delete the content located in the deleteUrl
     *
     * @param deleteUrl - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful delete request..
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteWithResponse(String deleteUrl, Context context) {
        return callRecordingAsync.deleteWithResponseInternal(deleteUrl, context).block();
    }

}
