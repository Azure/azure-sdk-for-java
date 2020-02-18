package com.azure.cosmos.implementation;

import com.azure.cosmos.ZonedDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class MetaDataDiagnosticContext {
    public volatile List<MetaDataDiagnostic> metaDataDiagnosticList;

    public void addMetaDataDiagnostic(MetaDataDiagnostic metaDataDiagnostic) {
        if (metaDataDiagnosticList == null) {
            metaDataDiagnosticList = new ArrayList<>();
        }

        metaDataDiagnosticList.add(metaDataDiagnostic);
    }

    public static class MetaDataDiagnostic {
        @JsonSerialize(using = ZonedDateTimeSerializer.class)
        public volatile ZonedDateTime startTimeUTC;
        @JsonSerialize(using = ZonedDateTimeSerializer.class)
        public volatile ZonedDateTime endTimeUTC;
        public volatile MetaDataEnum metaDataName;

        public MetaDataDiagnostic(ZonedDateTime startTimeUTC, ZonedDateTime endTimeUTC, MetaDataEnum metaDataName) {
            this.startTimeUTC = startTimeUTC;
            this.endTimeUTC = endTimeUTC;
            this.metaDataName = metaDataName;
        }
    }

    public enum  MetaDataEnum{
        CollectionLookUp,
        PartitionKeyRangeLookUp,
        ServerAddressLookup,
        MasterAddressLookUp
    }

}
