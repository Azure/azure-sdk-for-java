// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.rest.StreamResponse;
import com.azure.core.implementation.ByteCountingAsynchronousByteChannel;
import com.azure.core.implementation.ByteCountingWritableByteChannel;
import reactor.core.publisher.Mono;

import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.function.BiFunction;

/**
 * Utility type exposing methods for data transfers.
 */
public final class TransferUtil {

    private TransferUtil() {
    }

    /**
     * Transfers the {@link StreamResponse} content to {@link AsynchronousByteChannel}.
     * Resumes the transfer in case of errors.
     *
     * @param channel The destination {@link AsynchronousByteChannel}.
     * @param responseMono The {@link Mono} that emits initial response.
     * @param onDownloadErrorResume A {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     *                              downloading when an error occurs.
     * @param progressReporter The {@link ProgressReporter}.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     * @return A {@link Mono} which completion indicates successful transfer.
     */
    public static Mono<Void> downloadToAsynchronousByteChannel(
        AsynchronousByteChannel channel,
        Mono<StreamResponse> responseMono,
        BiFunction<Exception, Long, Mono<StreamResponse>> onDownloadErrorResume,
        ProgressReporter progressReporter,
        int maxRetries) {

        return downloadToAsynchronousByteChannel(
            new ByteCountingAsynchronousByteChannel(channel, progressReporter), responseMono, onDownloadErrorResume, maxRetries, 0);
    }

    private static Mono<Void> downloadToAsynchronousByteChannel(
        ByteCountingAsynchronousByteChannel channel,
        Mono<StreamResponse> responseMono,
        BiFunction<Exception, Long, Mono<StreamResponse>> onDownloadErrorResume,
        int maxRetries, int retryCount) {

        return responseMono
            .flatMap(response -> response.transferContentToAsync(channel))
            .onErrorResume(Exception.class, exception -> {
                int updatedRetryCount = retryCount + 1;

                if (updatedRetryCount > maxRetries) {
                    return Mono.error(exception);
                }

                return downloadToAsynchronousByteChannel(
                    channel, onDownloadErrorResume.apply(exception, channel.getBytesWritten()),
                    onDownloadErrorResume, maxRetries, updatedRetryCount);
            });
    }

    /**
     * Transfers the {@link StreamResponse} content to {@link WritableByteChannel}.
     * Resumes the transfer in case of errors.
     *
     * @param channel The destination {@link WritableByteChannel}.
     * @param response The initial response.
     * @param onDownloadErrorResume A {@link BiFunction} of {@link Throwable} and {@link Long} which is used to resume
     *                              downloading when an error occurs.
     * @param progressReporter The {@link ProgressReporter}.
     * @param maxRetries The maximum number of times a download can be resumed when an error occurs.
     */
    public static void downloadToWritableByteChannel(
        WritableByteChannel channel,
        StreamResponse response,
        BiFunction<Exception, Long, StreamResponse> onDownloadErrorResume,
        ProgressReporter progressReporter,
        int maxRetries) {

        downloadToWritableByteChannel(
            new ByteCountingWritableByteChannel(channel, progressReporter), response, onDownloadErrorResume, maxRetries, 0);
    }

    private static void downloadToWritableByteChannel(
        ByteCountingWritableByteChannel channel,
        StreamResponse response,
        BiFunction<Exception, Long, StreamResponse> onDownloadErrorResume,
        int maxRetries, int retryCount) {

        try {
            response.transferContentTo(channel);
        } catch (RuntimeException e) {
            int updatedRetryCount = retryCount + 1;

            if (updatedRetryCount > maxRetries) {
                throw e;
            }

            downloadToWritableByteChannel(
                channel, onDownloadErrorResume.apply(e, channel.getBytesWritten()),
                onDownloadErrorResume, maxRetries, updatedRetryCount
            );
        }
    }
}
