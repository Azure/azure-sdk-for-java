// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.monitor.ingestion.models.LogsUploadOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPOutputStream;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.ingestion.LogsIngestionServiceVersion;

public final class Utils {
    public static final long MAX_REQUEST_PAYLOAD_SIZE = 1024 * 1024; // 1 MB
    public static final String GZIP = "gzip";

    private static final ClientLogger LOGGER = new ClientLogger(Utils.class);

    private Utils() {
    }

    /**
     * Gzips the input byte array.
     * @param bytes The input byte array.
     * @return gzipped byte array.
     */
    public static byte[] gzipRequest(byte[] bytes) {
        // This should be moved to azure-core and should be enabled when the client library requests for gzipping the
        // request body content.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(byteArrayOutputStream)) {
            zip.write(bytes);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static int getConcurrency(LogsUploadOptions options) {
        if (options != null && options.getMaxConcurrency() != null) {
            return options.getMaxConcurrency();
        }

        return 1;
    }

    public static LogsIngestionClientImpl getLogsIngestionClientImpl(HttpPipeline httpPipeline, String endpoint,
        LogsIngestionServiceVersion serviceVersion) {
        return new LogsIngestionClientImpl(httpPipeline, JacksonAdapter.createDefaultSerializerAdapter(), endpoint,
            serviceVersion);
    }
}
