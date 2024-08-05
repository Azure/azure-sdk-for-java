// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.pipeline;

import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.azure.monitor.opentelemetry.exporter.implementation.builders.MetricTelemetryBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.AksResourceAttributes;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.ServiceAttributes;
import io.opentelemetry.semconv.incubating.ServiceIncubatingAttributes;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.TELEMETRY_ITEM_EXPORTER_ERROR;

public class TelemetryItemExporter {

    // the number 100 was calculated as the max number of concurrent exports that the single worker
    // thread can drive, so anything higher than this should not increase throughput
    private static final int MAX_CONCURRENT_EXPORTS = 100;

    private static final String _OTELRESOURCE_ = "_OTELRESOURCE_";

    private static final OperationLogger operationLogger =
        new OperationLogger(
            TelemetryItemExporter.class,
            "Put export into the background (don't wait for it to return)");

    private static final OperationLogger encodeBatchOperationLogger =
        new OperationLogger(TelemetryItemExporter.class, "Encoding telemetry batch into json");

    private final TelemetryPipeline telemetryPipeline;
    private final TelemetryPipelineListener listener;

    private final Set<CompletableResultCode> activeExportResults =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    // e.g. construct with diagnostic listener and local storage listener
    public TelemetryItemExporter(
        TelemetryPipeline telemetryPipeline, TelemetryPipelineListener listener) {
        this.telemetryPipeline = telemetryPipeline;
        this.listener = listener;
    }

    public CompletableResultCode send(List<TelemetryItem> telemetryItems) {
        Map<TelemetryItemBatchKey, List<TelemetryItem>> batches = splitIntoBatches(telemetryItems);
        List<CompletableResultCode> resultCodeList = new ArrayList<>();
        for (Map.Entry<TelemetryItemBatchKey, List<TelemetryItem>> batch : batches.entrySet()) {
            resultCodeList.add(internalSendByBatch(batch.getKey(), batch.getValue()));
        }
        maybeAddToActiveExportResults(resultCodeList);
        return CompletableResultCode.ofAll(resultCodeList);
    }

    // visible for tests
    Map<TelemetryItemBatchKey, List<TelemetryItem>> splitIntoBatches(
        List<TelemetryItem> telemetryItems) {

        Map<TelemetryItemBatchKey, List<TelemetryItem>> groupings = new HashMap<>();
        for (TelemetryItem telemetryItem : telemetryItems) {
            TelemetryItemBatchKey telemetryItemBatchKey = new TelemetryItemBatchKey(
                telemetryItem.getConnectionString(),
                telemetryItem.getResource(),
                telemetryItem.getResourceFromTags()
            );
            groupings
                .computeIfAbsent(telemetryItemBatchKey, k -> new ArrayList<>())
                .add(telemetryItem);
        }
        return groupings;
    }

    private void maybeAddToActiveExportResults(List<CompletableResultCode> results) {
        if (activeExportResults.size() >= MAX_CONCURRENT_EXPORTS) {
            // this is just a failsafe to limit concurrent exports, it's not ideal because it blocks
            // waiting for the most recent export instead of waiting for the first export to return
            operationLogger.recordFailure(
                "Hit max " + MAX_CONCURRENT_EXPORTS + " active concurrent requests",
                TELEMETRY_ITEM_EXPORTER_ERROR);
        }

        operationLogger.recordSuccess();

        activeExportResults.addAll(results);
        for (CompletableResultCode result : results) {
            result.whenComplete(() -> activeExportResults.remove(result));
        }
    }

    public CompletableResultCode flush() {
        return CompletableResultCode.ofAll(activeExportResults);
    }

    public CompletableResultCode shutdown() {
        return listener.shutdown();
    }

    CompletableResultCode internalSendByBatch(TelemetryItemBatchKey telemetryItemBatchKey,
                                              List<TelemetryItem> telemetryItems) {
        List<ByteBuffer> byteBuffers;
        // Don't send _OTELRESOURCE_ custom metric when OTEL_RESOURCE_ATTRIBUTES env var is empty
        // Don't send _OTELRESOURCE_ custom metric to Statsbeat yet
        // Don't Send _OTELRESOURCE_ when the app is running on other env other than AKS
        // insert _OTELRESOURCE_ at the beginning of each batch
        // TODO (heya) add a json config for customers to disable _OTELRESOURCE_ metric to the ingestion service when this feature is GA
        if (!"Statsbeat".equals(telemetryItems.get(0).getName()) && AksResourceAttributes.isAks(telemetryItemBatchKey.resource)) {
            telemetryItems.add(0, createOtelResourceMetric(telemetryItemBatchKey));
        }
        try {
            byteBuffers = serialize(telemetryItems);
            encodeBatchOperationLogger.recordSuccess();
        } catch (Throwable t) {
            encodeBatchOperationLogger.recordFailure(t.getMessage(), t);
            return CompletableResultCode.ofFailure();
        }
        return telemetryPipeline.send(byteBuffers, telemetryItemBatchKey.connectionString, listener);
    }

    // serialize an array of TelemetryItems to an array of byte buffers
    private static List<ByteBuffer> serialize(List<TelemetryItem> telemetryItems) {
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

    // gzip and add new line delimiter from a list of telemetry items to a byte buffer output stream
    private static ByteBufferOutputStream writeTelemetryItemsAsByteBufferOutputStream(List<TelemetryItem> telemetryItems) throws IOException {
        try (ByteBufferOutputStream result = new ByteBufferOutputStream(new AppInsightsByteBufferPool())) {
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


    private TelemetryItem createOtelResourceMetric(TelemetryItemBatchKey telemetryItemBatchKey) {
        MetricTelemetryBuilder builder = MetricTelemetryBuilder.create(_OTELRESOURCE_, 0);
        builder.setConnectionString(telemetryItemBatchKey.connectionString);
        telemetryItemBatchKey.resource.getAttributes().forEach((k, v) -> builder.addProperty(k.getKey(), v.toString()));
        String roleName = telemetryItemBatchKey.resourceFromTags.get(ContextTagKeys.AI_CLOUD_ROLE.toString());
        if (roleName != null) {
            builder.addProperty(ServiceAttributes.SERVICE_NAME.getKey(), roleName);
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE.toString(), roleName);
        }
        String roleInstance = telemetryItemBatchKey.resourceFromTags.get(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString());
        if (roleInstance != null) {
            builder.addProperty(ServiceIncubatingAttributes.SERVICE_INSTANCE_ID.getKey(), roleInstance);
            builder.addTag(ContextTagKeys.AI_CLOUD_ROLE_INSTANCE.toString(), roleInstance);
        }
        String internalSdkVersion = telemetryItemBatchKey.resourceFromTags.get(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString());
        if (internalSdkVersion != null) {
            builder.addTag(ContextTagKeys.AI_INTERNAL_SDK_VERSION.toString(), internalSdkVersion);
        }

        return builder.build();
    }

    private static class TelemetryItemBatchKey {

        private final String connectionString;
        private final Resource resource;
        private final Map<String, String> resourceFromTags;

        private TelemetryItemBatchKey(String connectionString, Resource resource, Map<String, String> resourceFromTags) {
            this.connectionString = connectionString;
            this.resource = resource;
            this.resourceFromTags = resourceFromTags;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            TelemetryItemBatchKey that = (TelemetryItemBatchKey) other;
            return Objects.equals(connectionString, that.connectionString)
                && Objects.equals(resource, that.resource)
                && Objects.equals(resourceFromTags, that.resourceFromTags);
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectionString, resource, resourceFromTags);
        }
    }
}
