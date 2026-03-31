// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;

@JsonSerialize(using = PerPartitionAutomaticFailoverInfoHolder.PerPartitionFailoverInfoHolderSerializer.class)
public class PerPartitionAutomaticFailoverInfoHolder implements Serializable {

    public static final PerPartitionAutomaticFailoverInfoHolder EMPTY = new PerPartitionAutomaticFailoverInfoHolder();

    private final Utils.ValueHolder<PartitionLevelAutomaticFailoverInfo> partitionLevelFailoverInfoValueHolder = new Utils.ValueHolder<>();

    public synchronized PartitionLevelAutomaticFailoverInfo getPartitionLevelFailoverInfo() {
        return partitionLevelFailoverInfoValueHolder.v;
    }

    public synchronized void setPartitionLevelFailoverInfo(PartitionLevelAutomaticFailoverInfo partitionLevelAutomaticFailoverInfo) {
        this.partitionLevelFailoverInfoValueHolder.v = partitionLevelAutomaticFailoverInfo;
    }

    public static class PerPartitionFailoverInfoHolderSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PerPartitionAutomaticFailoverInfoHolder> {

        @Override
        public void serialize(PerPartitionAutomaticFailoverInfoHolder value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            PartitionLevelAutomaticFailoverInfo partitionLevelAutomaticFailoverInfo = value.getPartitionLevelFailoverInfo();

            if (partitionLevelAutomaticFailoverInfo != null) {
                gen.writeStartObject();

                gen.writeObjectField("perPartitionAutomaticFailoverCtx", value.getPartitionLevelFailoverInfo());

                gen.writeEndObject();
            } else {
                gen.writeNull();
            }
        }
    }
}
