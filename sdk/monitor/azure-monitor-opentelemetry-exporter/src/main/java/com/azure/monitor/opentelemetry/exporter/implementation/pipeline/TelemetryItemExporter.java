// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.TELEMETRY_ITEM_EXPORTER_ERROR;

public class TelemetryItemExporter {

    // the number 100 was calculated as the max number of concurrent exports that the single worker
    // thread can drive, so anything higher than this should not increase throughput
    private static final int MAX_CONCURRENT_EXPORTS = 100;

    private static final String _OTELRESOURCE_ = "_OTELRESOURCE_";

    private static final ClientLogger logger = new ClientLogger(TelemetryItemExporter.class);

    private static final OperationLogger operationLogger =
        new OperationLogger(
            TelemetryItemExporter.class,
            "Put export into the background (don't wait for it to return)");

    private static final ObjectMapper mapper = createObjectMapper();

    private static final AppInsightsByteBufferPool byteBufferPool = new AppInsightsByteBufferPool();

    private static final OperationLogger encodeBatchOperationLogger =
        new OperationLogger(TelemetryItemExporter.class, "Encoding telemetry batch into json");

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

    private final TelemetryPipeline telemetryPipeline;
    private final TelemetryPipelineListener listener;
    // TODO (trask) should this be all the resources?
    private final Resource environmentResource;

    private final Set<CompletableResultCode> activeExportResults =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    // e.g. construct with diagnostic listener and local storage listener
    public TelemetryItemExporter(
            TelemetryPipeline telemetryPipeline, TelemetryPipelineListener listener, Resource environmentResource) {
        this.telemetryPipeline = telemetryPipeline;
        this.listener = listener;
        this.environmentResource = environmentResource;
    }

    public CompletableResultCode send(List<TelemetryItem> telemetryItems) {
        List<List<TelemetryItem>> result =
            groupTelemetryItemsByConnectionStringAndRoleName(telemetryItems);
        List<CompletableResultCode> resultCodeList = new ArrayList<>();
        for (List<TelemetryItem> batch : result) {
            resultCodeList.add(
                internalSendByConnectionStringAndRoleName(batch, batch.get(0).getConnectionString()));
        }
        return maybeAddToActiveExportResults(resultCodeList);
    }

    // visible for tests
    List<List<TelemetryItem>> groupTelemetryItemsByConnectionStringAndRoleName(
        List<TelemetryItem> telemetryItems) {
        Map<String, List<TelemetryItem>> groupings = new HashMap<>();
        // group TelemetryItem by connection string
        for (TelemetryItem telemetryItem : telemetryItems) {
            groupings
                .computeIfAbsent(telemetryItem.getConnectionString(), k -> new ArrayList<>())
                .add(telemetryItem);
        }

        // and then group TelemetryItem by role name
        List<List<TelemetryItem>> result = new ArrayList<>();
        for (List<TelemetryItem> group : groupings.values()) {
            Map<String, List<TelemetryItem>> roleNameGroupings = new HashMap<>();
            for (TelemetryItem telemetryItem : group) {
                String roleName = "";
                if (telemetryItem.getTags() != null) { // Statsbeat doesn't have tags
                    roleName = telemetryItem.getTags().get(ContextTagKeys.AI_CLOUD_ROLE.toString());
                    roleName = roleName == null ? "" : roleName;
                }
                roleNameGroupings.computeIfAbsent(roleName, k -> new ArrayList<>()).add(telemetryItem);
            }
            result.addAll(roleNameGroupings.values());
        }
        return result;
    }

    private CompletableResultCode maybeAddToActiveExportResults(List<CompletableResultCode> results) {
        if (activeExportResults.size() >= MAX_CONCURRENT_EXPORTS) {
            // this is just a failsafe to limit concurrent exports, it's not ideal because it blocks
            // waiting for the most recent export instead of waiting for the first export to return
            operationLogger.recordFailure(
                "Hit max " + MAX_CONCURRENT_EXPORTS + " active concurrent requests",
                TELEMETRY_ITEM_EXPORTER_ERROR);
            return CompletableResultCode.ofAll(results);
        }

        operationLogger.recordSuccess();

        activeExportResults.addAll(results);
        for (CompletableResultCode result : results) {
            result.whenComplete(() -> activeExportResults.remove(result));
        }

        return CompletableResultCode.ofSuccess();
    }

    public CompletableResultCode flush() {
        return CompletableResultCode.ofAll(activeExportResults);
    }

    public CompletableResultCode shutdown() {
        return listener.shutdown();
    }

    CompletableResultCode internalSendByConnectionStringAndRoleName(
        List<TelemetryItem> telemetryItems, String connectionString) {
        List<ByteBuffer> byteBuffers;

        // Don't send _OTELRESOURCE_ custom metric when OTEL_RESOURCE_ATTRIBUTES env var is empty
        // Don't send _OTELRESOURCE_ custom metric to Statsbeat yet
        // insert _OTELRESOURCE_ at the beginning of each batch
        if (!environmentResource.getAttributes().isEmpty()
            && !"Statsbeat".equals(telemetryItems.get(0).getName())) {
            telemetryItems.add(
                0, createOtelResourceMetric(telemetryItems.get(0).getTags(), connectionString));
        }
        try {
            byteBuffers = encode(telemetryItems);
            encodeBatchOperationLogger.recordSuccess();
        } catch (Throwable t) {
            encodeBatchOperationLogger.recordFailure(t.getMessage(), t);
            return CompletableResultCode.ofFailure();
        }
        return telemetryPipeline.send(byteBuffers, connectionString, listener);
    }

    private TelemetryItem createOtelResourceMetric(
        Map<String, String> existingTags, String connectionString) {
        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create(_OTELRESOURCE_, 0);
        // this is needed in order to stamp iKey onto the telemetry item during serialization
        builder.setConnectionString(connectionString);
        builder.addTag(
            ContextTagKeys.AI_CLOUD_ROLE.toString(),
            existingTags.get(ContextTagKeys.AI_CLOUD_ROLE.toString()));
        builder.addTag(
            ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(),
            existingTags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString()));
        builder.addTag(
            ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(),
            existingTags.get(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString()));

        environmentResource.getAttributes().forEach((k, v) -> builder.addProperty(k.getKey(), v.toString()));

        return builder.build();
    }

    List<ByteBuffer> encode(List<TelemetryItem> telemetryItems) throws IOException {
        if (logger.canLogAtLevel(LogLevel.VERBOSE)) {
            StringWriter debug = new StringWriter();
            try (JsonGenerator jg = mapper.createGenerator(debug)) {
                writeTelemetryItems(jg, telemetryItems);
            }
            logger.verbose("sending telemetry to ingestion service:{}{}", System.lineSeparator(), debug);
        }

        ByteBufferOutputStream out = new ByteBufferOutputStream(byteBufferPool);

        try (JsonGenerator jg = mapper.createGenerator(new GZIPOutputStream(out))) {
            writeTelemetryItems(jg, telemetryItems);
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
    }

    private static void writeTelemetryItems(JsonGenerator jg, List<TelemetryItem> telemetryItems)
        throws IOException {
        jg.setRootValueSeparator(new SerializedString("\n"));
        for (TelemetryItem telemetryItem : telemetryItems) {
            mapper.writeValue(jg, telemetryItem);
        }
    }
}
