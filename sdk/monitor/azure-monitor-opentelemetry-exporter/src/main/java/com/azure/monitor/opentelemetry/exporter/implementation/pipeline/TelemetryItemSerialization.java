// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class TelemetryItemSerialization {

    private static final ObjectMapper mapper = createObjectMapper();
    private static final AppInsightsByteBufferPool byteBufferPool = new AppInsightsByteBufferPool();
    private static final ClientLogger logger = new ClientLogger(TelemetryItemSerialization.class);

    public static List<TelemetryItem> deserialize(byte[] data) {
        return deserializeAlreadyDecoded(decode(data));
    }

    // visible for testing
    // deserialize raw bytes to a list of TelemetryItem without decoding
    public static List<TelemetryItem> deserializeAlreadyDecoded(byte[] data) {
        try {
            MappingIterator<TelemetryItem> iterator = mapper.readerFor(TelemetryItem.class).readValues(data);
            return iterator.readAll();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize byte[] to a list of TelemetryItems", e);
        }
    }

    // decode gzipped request raw bytes back to original request raw bytes
    private static byte[] decode(byte[] rawBytes) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data, 0, data.length)) != -1) {
                baos.write(data, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode byte[]", e);
        }
    }

    public static List<ByteBuffer> serialize(List<TelemetryItem> telemetryItems) {
        try {
            if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
                StringWriter debug = new StringWriter();
                try (JsonGenerator jg = mapper.createGenerator(debug)) {
                    writeTelemetryItems(jg, telemetryItems, mapper);
                }
                logger.verbose("sending telemetry to ingestion service:{}{}", System.lineSeparator(), debug);
            }

            ByteBufferOutputStream out = new ByteBufferOutputStream(byteBufferPool);

            try (JsonGenerator jg = mapper.createGenerator(new GZIPOutputStream(out))) {
                writeTelemetryItems(jg, telemetryItems, mapper);
            } catch (IOException e) {
                byteBufferPool.offer(out.getByteBuffers());
                throw e;
            }

            out.close(); // closing ByteBufferOutputStream is a no-op, but this line makes LGTM happy

            List<ByteBuffer> byteBuffers = out.getByteBuffers();
            for (ByteBuffer byteBuffer : byteBuffers) {
                byteBuffer.flip();
            }
            return byteBuffers;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to encode list of TelemetryItems to byte[]", e);
        }
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // it's important to pass in the "agent class loader" since TelemetryItemPipeline is initialized
        // lazily and can be initialized via an application thread, in which case the thread context
        // class loader is used to look up jsr305 module and its not found
        mapper.registerModules(ObjectMapper.findModules(TelemetryItemExporter.class.getClassLoader()));
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    private static void writeTelemetryItems(JsonGenerator jg, List<TelemetryItem> telemetryItems, ObjectMapper mapper)
        throws IOException {
        jg.setRootValueSeparator(new SerializedString("\n"));
        for (TelemetryItem telemetryItem : telemetryItems) {
            mapper.writeValue(jg, telemetryItem);
        }
    }

    private TelemetryItemSerialization() {
    }
}
