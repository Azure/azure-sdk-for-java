// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.DiagnosticsInstantSerializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

@JsonSerialize(using = PerPartitionAutomaticFailoverInfoHolder.PerPartitionFailoverInfoHolderSerializer.class)
public class PerPartitionAutomaticFailoverInfoHolder implements Serializable {

    public static final PerPartitionAutomaticFailoverInfoHolder EMPTY
        = new PerPartitionAutomaticFailoverInfoHolder(PerPartitionAutomaticFailoverDiagnostics.EMPTY);

    private final AtomicReference<PerPartitionAutomaticFailoverDiagnostics> diagnosticsSnapshot;

    public PerPartitionAutomaticFailoverInfoHolder() {
        this(PerPartitionAutomaticFailoverDiagnostics.EMPTY);
    }

    private PerPartitionAutomaticFailoverInfoHolder(PerPartitionAutomaticFailoverDiagnostics diagnosticsSnapshot) {
        this.diagnosticsSnapshot = new AtomicReference<>(diagnosticsSnapshot);
    }

    PerPartitionAutomaticFailoverDiagnostics getDiagnosticsSnapshot() {
        return this.diagnosticsSnapshot.get();
    }

    public void setPartitionLevelFailoverInfo(PartitionLevelAutomaticFailoverInfo partitionLevelAutomaticFailoverInfo) {
        if (this == EMPTY) {
            return;
        }

        this.diagnosticsSnapshot.set(partitionLevelAutomaticFailoverInfo == null
            ? PerPartitionAutomaticFailoverDiagnostics.EMPTY
            : partitionLevelAutomaticFailoverInfo.snapshot());
    }

    public PerPartitionAutomaticFailoverInfoHolder snapshot() {
        PerPartitionAutomaticFailoverDiagnostics snapshot = this.diagnosticsSnapshot.get();
        return snapshot == PerPartitionAutomaticFailoverDiagnostics.EMPTY
            ? EMPTY
            : new PerPartitionAutomaticFailoverInfoHolder(snapshot);
    }

    public static class PerPartitionFailoverInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionAutomaticFailoverInfoHolder> {

        @Override
        public void serialize(PerPartitionAutomaticFailoverInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            PerPartitionAutomaticFailoverDiagnostics snapshot = value.getDiagnosticsSnapshot();
            gen.writeStartObject();
            if (snapshot != PerPartitionAutomaticFailoverDiagnostics.EMPTY) {
                gen.writeStringField("currWriteRegion", snapshot.getCurrentWriteRegion());
                gen.writeArrayFieldStart("failedRegions");
                for (String failedRegion : snapshot.getFailedRegions()) {
                    gen.writeString(failedRegion);
                }
                gen.writeEndArray();
                gen.writeStringField("since", DiagnosticsInstantSerializer.fromInstant(snapshot.getSince()));
            }
            gen.writeEndObject();
        }
    }
}
