// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class TelemetryItemSerialization {

    private static final AppInsightsByteBufferPool byteBufferPool = new AppInsightsByteBufferPool();

    // serialize an array of TelemetryItems to an array of byte buffers
    public static List<ByteBuffer> serialize(List<TelemetryItem> telemetryItems) {
        try {
            ByteBufferOutputStream out = writeTelemetryItemsAsByteBufferOutputStream(telemetryItems);
            out.close(); // closing ByteBufferOutputStream is a no-op, but this line makes LGTM happy
            List<ByteBuffer> byteBuffers = out.getByteBuffers();
            for (ByteBuffer byteBuffer : byteBuffers) {
                byteBuffer.flip();
            }
            return out.getByteBuffers();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize list of TelemetryItems to List<ByteBuffer>", e);
        }
    }

    // visible for testing
    // deserialize single TelemetryItem raw bytes to TelemetryItem
    public static TelemetryItem deserialize(byte[] rawBytes) {
        try {
            JsonReader reader = JsonProviders.createReader(rawBytes);
            return TelemetryItem.fromJson(reader);
        } catch (Throwable th) {
            throw new IllegalStateException("failed to deserialize ", th);
        }
    }

    // encode and adding newline delimiter are required before persisting to the offline disk for handling 206 status code
    public static List<ByteBuffer> encode(List<ByteBuffer> byteBuffers) {
        try (ByteBufferOutputStream result = new ByteBufferOutputStream(byteBufferPool)) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
            for (int i = 0; i < byteBuffers.size(); i++) {
                ByteBuffer byteBuffer = byteBuffers.get(i);
                byte[] arr = new byte[byteBuffer.remaining()];
                byteBuffer.get(arr);
                gzipOutputStream.write(arr);
                if (i < byteBuffers.size() - 1) {
                    gzipOutputStream.write('\n');
                }
            }
            gzipOutputStream.close();
            List<ByteBuffer> resultByteBuffers = result.getByteBuffers();
            for (ByteBuffer byteBuffer : resultByteBuffers) {
                byteBuffer.flip();
            }
            return result.getByteBuffers();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to encode list of ByteBuffers before persisting to the offline disk", e);
        }
    }

    // visible for testing
    // decode gzipped TelemetryItems raw bytes back to original un-gzipped TelemetryItems raw bytes
    public static byte[] decode(byte[] rawBytes) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data)) != -1) {
                baos.write(data, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode byte[]", e);
        }
    }

    // split the byte array by newline character
    public static List<byte[]> splitBytesByNewline(byte[] inputBytes) {
        List<byte[]> lines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < inputBytes.length; i++) {
            if (inputBytes[i] == '\n') {
                byte[] line = new byte[i - start];
                System.arraycopy(inputBytes, start, line, 0, i - start);
                lines.add(line);
                start = i + 1;
            }
        }
        // Add the last line (if any)
        if (start < inputBytes.length) {
            byte[] lastLine = new byte[inputBytes.length - start];
            System.arraycopy(inputBytes, start, lastLine, 0, inputBytes.length - start);
            lines.add(lastLine);
        }
        return lines;
    }

    // convert list of byte buffers to byte array
    public static byte[] convertByteBufferListToByteArray(List<ByteBuffer> byteBuffers) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (ByteBuffer buffer : byteBuffers) {
            byte[] arr = new byte[buffer.remaining()];
            buffer.get(arr);
            try {
                baos.write(arr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return baos.toByteArray();
    }

    // gzip and add new line delimiter from a list of telemetry items to a byte buffer output stream
    private static ByteBufferOutputStream writeTelemetryItemsAsByteBufferOutputStream(List<TelemetryItem> telemetryItems) throws IOException {
        try (ByteBufferOutputStream result = new ByteBufferOutputStream(byteBufferPool)) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
            for (int i = 0; i < telemetryItems.size(); i++) {
                JsonWriter jsonWriter = JsonProviders.createWriter(gzipOutputStream);
                telemetryItems.get(i).toJson(jsonWriter);
                jsonWriter.flush();

                if (i < telemetryItems.size() - 1) {
                    gzipOutputStream.write('\n');
                }
            }
            gzipOutputStream.close();
            return result;
        }
    }

    private TelemetryItemSerialization() {
    }
}
