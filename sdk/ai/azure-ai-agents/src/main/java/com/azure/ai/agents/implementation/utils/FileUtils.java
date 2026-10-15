// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.agents.implementation.utils;

import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Internal utilities for file operations.
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Writes to file.
     *
     * @param content response content to write to file.
     * @param destinationFile destination file path.
     * @param overwrite whether an existing file at {@code destinationFile} may be overwritten. When {@code false} the
     * returned {@link Mono} fails with a {@link FileAlreadyExistsException} if the file already exists.
     * @return a {@link Mono} that completes when the file has been written.
     */
    public static Mono<Void> writeToFileAsync(BinaryData content, String destinationFile, boolean overwrite) {
        // file handling is done asynchronously. Errors either opening or closing the file are bubbled up to the
        // subscriber
        return Mono.usingWhen(
            Mono.fromCallable(() -> AsynchronousFileChannel.open(Paths.get(destinationFile), openOptions(overwrite))),
            channel -> FluxUtil.writeFile(content.toFluxByteBuffer(), channel), channel -> Mono.defer(() -> {
                try {
                    channel.close();
                    return Mono.empty();
                } catch (IOException e) {
                    return Mono.error(e);
                }
            }));
    }

    /**
     * Writes to file and returns a simplified response.
     *
     * @param response request response and its content.
     * @param destinationFile destination file path
     * @param overwrite whether an existing file at {@code destinationFile} may be overwritten. When {@code false} the
     * returned {@link Mono} fails with a {@link FileAlreadyExistsException} if the file already exists.
     * @return a {@link Mono} that completes when the file has been written and the request {@link Response}
     */
    public static Mono<Response<Void>> writeToFileWithResponseAsync(Response<BinaryData> response,
        String destinationFile, boolean overwrite) {
        return writeToFileAsync(response.getValue(), destinationFile, overwrite).thenReturn(
            new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null));
    }

    /**
     * Writes to a file.
     *
     * @param content contents to store in the file.
     * @param destinationFile destination file.
     * @param overwrite whether an existing file at {@code destinationFile} may be overwritten. When {@code false} a
     * {@link FileAlreadyExistsException} is thrown if the file already exists.
     * @throws IOException file operation failures.
     */
    public static void writeToFile(BinaryData content, String destinationFile, boolean overwrite) throws IOException {
        try (OutputStream stream = Files.newOutputStream(Paths.get(destinationFile), openOptions(overwrite))) {
            content.writeTo(stream);
        }
    }

    /**
     * Writes to a file using a response
     *
     * @param response HTTP response containing the file contents.
     * @param destinationFile destination file.
     * @param overwrite whether an existing file at {@code destinationFile} may be overwritten. When {@code false} a
     * {@link FileAlreadyExistsException} is thrown if the file already exists.
     * @throws IOException file operation failures.
     */
    public static void writeToFile(Response<BinaryData> response, String destinationFile, boolean overwrite)
        throws IOException {
        writeToFile(response.getValue(), destinationFile, overwrite);
    }

    /**
     * Builds the {@link OpenOption} set used when opening the destination file for writing.
     *
     * @param overwrite whether an existing file may be overwritten.
     * @return open options that truncate an existing file when {@code overwrite} is {@code true}, or fail with a
     * {@link FileAlreadyExistsException} when {@code false} and the file already exists.
     */
    private static OpenOption[] openOptions(boolean overwrite) {
        return overwrite
            ? new OpenOption[] {
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE }
            : new OpenOption[] { StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE };
    }

    /**
     * Computes the lowercase hex-encoded SHA-256 digest of the given binary content.
     *
     * <p>The content is fully read in order to compute the digest.</p>
     *
     * @param content the binary content to hash.
     * @return the lowercase hex-encoded SHA-256 digest of {@code content}.
     */
    public static String computeSha256(BinaryData content) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(content.toBytes());
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(Character.forDigit((value >> 4) & 0xF, 16));
                builder.append(Character.forDigit(value & 0xF, 16));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available.", e);
        }
    }
}
