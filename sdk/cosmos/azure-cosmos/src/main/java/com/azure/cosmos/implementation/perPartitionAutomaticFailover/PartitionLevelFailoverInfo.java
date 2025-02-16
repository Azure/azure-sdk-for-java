package com.azure.cosmos.implementation.perPartitionAutomaticFailover;

import com.azure.cosmos.implementation.GlobalEndpointManager;
import com.azure.cosmos.implementation.OperationType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@JsonSerialize(using = PartitionLevelFailoverInfo.PartitionLevelFailoverInfoSerializer.class)
public class PartitionLevelFailoverInfo implements Serializable {

    // Set of URIs which have seen 503s (specific to document writes) or 403/3s
    private final Set<URI> failedLocations = ConcurrentHashMap.newKeySet();

    // The current URI corresponds to the regional endpoint to use as an override
    // todo (abhmohanty): evaluate how to handle such an override for document reads
    private URI current;
    private final GlobalEndpointManager globalEndpointManager;

    PartitionLevelFailoverInfo(URI current, GlobalEndpointManager globalEndpointManager) {
        this.current = current;
        this.globalEndpointManager = globalEndpointManager;
    }

    synchronized boolean tryMoveToNextLocation(List<URI> readLocations, URI failedLocation) {

        if (!failedLocation.equals(this.current)) {
            return true;
        }

        for (URI location : readLocations) {

            if (location.equals(this.current)) {
                continue;
            }

            if (this.failedLocations.contains(location)) {
                continue;
            }

            this.failedLocations.add(failedLocation);
            this.current = location;
            return true;
        }

        return false;
    }

    public URI getCurrent() {
        return this.current;
    }

    static class PartitionLevelFailoverInfoSerializer extends com.fasterxml.jackson.databind.JsonSerializer<PartitionLevelFailoverInfo> {

        @Override
        public void serialize(PartitionLevelFailoverInfo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            gen.writeStartObject();

            if (!value.failedLocations.isEmpty()) {

                StringBuilder sb = new StringBuilder("[");

                for (URI location : value.failedLocations) {
                    sb.append(value.globalEndpointManager.getRegionName(location, OperationType.Read)).append(",");
                }

                sb.deleteCharAt(sb.length() - 1);
                sb.append("]");

                gen.writePOJOField("failedLocations", sb.toString());
            } else {
                gen.writePOJOField("failedLocations", "[]");
            }

            if (value.current != null) {
                gen.writePOJOField("overrideRegion", value.globalEndpointManager.getRegionName(value.current, OperationType.Read));
            }

            gen.writeEndObject();
        }
    }
}
