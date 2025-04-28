// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;

@JsonSerialize(using = PerPartitionFailoverInfoHolder.PerPartitionFailoverInfoHolderSerializer.class)
public class PerPartitionFailoverInfoHolder implements Serializable {

    private final Utils.ValueHolder<PartitionLevelFailoverInfo> partitionLevelFailoverInfoValueHolder = new Utils.ValueHolder<>();

    public synchronized PartitionLevelFailoverInfo getPartitionLevelFailoverInfo() {
        return partitionLevelFailoverInfoValueHolder.v;
    }

    public synchronized void setPartitionLevelFailoverInfo(PartitionLevelFailoverInfo partitionLevelFailoverInfo) {
        this.partitionLevelFailoverInfoValueHolder.v = partitionLevelFailoverInfo;
    }

    public static class PerPartitionFailoverInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionFailoverInfoHolder> {

        @Override
        public void serialize(PerPartitionFailoverInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            PartitionLevelFailoverInfo partitionLevelFailoverInfo = value.getPartitionLevelFailoverInfo();

            if (partitionLevelFailoverInfo != null) {
                gen.writeStartObject();

                gen.writeObjectField("perPartitionAutomaticFailoverCtx", value.getPartitionLevelFailoverInfo());

                gen.writeEndObject();
            }
        }
    }
}
