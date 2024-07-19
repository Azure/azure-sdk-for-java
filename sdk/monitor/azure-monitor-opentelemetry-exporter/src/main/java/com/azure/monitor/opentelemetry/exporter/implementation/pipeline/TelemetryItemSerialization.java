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
    public static TelemetryItem deserialize(byte[] rawBytes) {
        try {
            JsonReader reader = JsonProviders.createReader(rawBytes);
            return TelemetryItem.fromJson(reader);
        } catch (Throwable th) {
            throw new IllegalStateException("failed to deserialize ", th);
        }
    }

    private static String serialize(TelemetryItem telemetryItem) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            telemetryItem.toJson(writer);
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String printJson(List<TelemetryItem> telemetryItems) throws IOException {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for (TelemetryItem telemetryItem : telemetryItems) {
            sb.append(serialize(telemetryItem));
            if (index < telemetryItems.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private static ByteBufferOutputStream writeTelemetryItemsAsByteBufferOutputStream(List<TelemetryItem> telemetryItems) throws IOException {
        try (ByteBufferOutputStream result = new ByteBufferOutputStream(byteBufferPool)) {
            JsonWriter jsonWriter = null;
            int countNewLinesAdded = 0;
            for (int i = 0; i < telemetryItems.size(); i++) {
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
                jsonWriter = JsonProviders.createWriter(gzipOutputStream);
                telemetryItems.get(i).toJson(jsonWriter);
                jsonWriter.flush();
                gzipOutputStream.finish(); // need to call this otherwise, gzip content would be corrupted.

                if (i < telemetryItems.size() - 1) {
                    result.write('\n');
                    countNewLinesAdded++;
                }
            }
            System.out.println("Number of new lines added: " + countNewLinesAdded);
            if (jsonWriter != null) {
                jsonWriter.close();
            }
            return result;
        }
    }

    // visible for testing
    // decode gzipped TelemetryItems raw bytes back to original TelemetryItems raw bytes
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

    public static int countNewLines(byte[] inputBytes) {
        return new String(inputBytes).split("\n", -1).length - 1;
//        int count = 0;
//        for (byte inputByte : inputBytes) {
//            if (inputByte == '\n') {
//                count++;
//            }
//        }
//        return count;
    }

    // convert list of byte buffers to byte array
    public static byte[] convertByteBufferListToByteArray(List<ByteBuffer> byteBuffers) {
        int totalSize = byteBuffers.stream().mapToInt(ByteBuffer::remaining).sum();
        ByteBuffer resultBuffer = ByteBuffer.allocate(totalSize);

        for (ByteBuffer buffer : byteBuffers) {
            byte[] byteArray = new byte[buffer.remaining()];
            buffer.get(byteArray);
            resultBuffer.put(byteArray);
        }

        return resultBuffer.array();
    }

    // TODO - add unit test
    public static List<ByteBuffer> addNewLineAsLineDelimiter(List<ByteBuffer> byteBuffers) {
        List<ByteBuffer> result = new ArrayList<>();
        for (int i = 0; i < byteBuffers.size(); i++) {
            ByteBuffer byteBuffer = byteBuffers.get(i);
            ByteBuffer newByteBuffer;
            if (i < byteBuffers.size() - 1) {
                ByteBuffer newLine = ByteBuffer.wrap(new byte[]{'\n'});
                newByteBuffer = ByteBuffer.allocate(byteBuffer.remaining() + newLine.remaining());
                newByteBuffer.put(byteBuffer);
                newByteBuffer.put(newLine);
                newByteBuffer.flip();
            } else {
                newByteBuffer = ByteBuffer.allocate(byteBuffer.remaining());
                newByteBuffer.put(byteBuffer);
                newByteBuffer.flip();
            }
            result.add(newByteBuffer);
        }
        return result;
    }


    private TelemetryItemSerialization() {
    }
}
