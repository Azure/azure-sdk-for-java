package com.microsoft.azure.tables.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The TableServiceStats model. */
@Fluent
public final class TableServiceStats {
    /*
     * Geo-Replication information for the Secondary Storage Service.
     */
    @JsonProperty(value = "GeoReplication")
    private GeoReplication geoReplication;

    /**
     * Get the geoReplication property: Geo-Replication information for the Secondary Storage Service.
     *
     * @return the geoReplication value.
     */
    public GeoReplication getGeoReplication() {
        return this.geoReplication;
    }

    /**
     * Set the geoReplication property: Geo-Replication information for the Secondary Storage Service.
     *
     * @param geoReplication the geoReplication value to set.
     * @return the TableServiceStats object itself.
     */
    public TableServiceStats setGeoReplication(GeoReplication geoReplication) {
        this.geoReplication = geoReplication;
        return this;
    }
}
