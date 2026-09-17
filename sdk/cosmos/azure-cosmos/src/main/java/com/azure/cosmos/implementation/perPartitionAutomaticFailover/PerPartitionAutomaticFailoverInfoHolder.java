// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.DiagnosticsInstantSerializer;
import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;

@JsonSerialize(using = PerPartitionAutomaticFailoverInfoHolder.PerPartitionFailoverInfoHolderSerializer.class)
public class PerPartitionAutomaticFailoverInfoHolder implements Serializable {

    public static final PerPartitionAutomaticFailoverInfoHolder EMPTY
        = new PerPartitionAutomaticFailoverInfoHolder(PerPartitionAutomaticFailoverDiagnostics.EMPTY);

    private final Utils.ValueHolder<PerPartitionAutomaticFailoverDiagnostics> diagnosticsSnapshotValueHolder
        = new Utils.ValueHolder<>();

    public PerPartitionAutomaticFailoverInfoHolder() {
        this(PerPartitionAutomaticFailoverDiagnostics.EMPTY);
    }

    private PerPartitionAutomaticFailoverInfoHolder(PerPartitionAutomaticFailoverDiagnostics diagnosticsSnapshot) {
        this.diagnosticsSnapshotValueHolder.v = diagnosticsSnapshot;
    }

    synchronized PerPartitionAutomaticFailoverDiagnostics getDiagnosticsSnapshot() {
        return this.diagnosticsSnapshotValueHolder.v;
    }

    public synchronized void setPartitionLevelFailoverInfo(PartitionLevelAutomaticFailoverInfo partitionLevelAutomaticFailoverInfo) {
        if (this == EMPTY) {
            return;
        }

        this.diagnosticsSnapshotValueHolder.v = partitionLevelAutomaticFailoverInfo == null
            ? PerPartitionAutomaticFailoverDiagnostics.EMPTY
            : partitionLevelAutomaticFailoverInfo.snapshot();
    }

    public synchronized PerPartitionAutomaticFailoverInfoHolder snapshot() {
        PerPartitionAutomaticFailoverDiagnostics snapshot = this.diagnosticsSnapshotValueHolder.v;
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
                gen.writeStringField("since", DiagnosticsInstantSerializer.fromInstant(snapshot.getSince()));
            }
            gen.writeEndObject();
        }
    }
}
