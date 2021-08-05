// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * An instance of Remote Dependency represents an interaction of the monitored component with a remote component/service
 * like SQL or an HTTP endpoint.
 */
@Fluent
public final class RemoteDependencyData extends MonitorDomain {
    /*
     * Identifier of a dependency call instance. Used for correlation with the
     * request telemetry item corresponding to this dependency call.
     */
    @JsonProperty(value = "id")
    private String id;

    /*
     * Name of the command initiated with this dependency call. Low cardinality
     * value. Examples are stored procedure name and URL path template.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * Result code of a dependency call. Examples are SQL error code and HTTP
     * status code.
     */
    @JsonProperty(value = "resultCode")
    private String resultCode;

    /*
     * Command initiated by this dependency call. Examples are SQL statement
     * and HTTP URL with all query parameters.
     */
    @JsonProperty(value = "data")
    private String data;

    /*
     * Dependency type name. Very low cardinality value for logical grouping of
     * dependencies and interpretation of other fields like commandName and
     * resultCode. Examples are SQL, Azure table, and HTTP.
     */
    @JsonProperty(value = "type")
    private String type;

    /*
     * Target site of a dependency call. Examples are server name, host
     * address.
     */
    @JsonProperty(value = "target")
    private String target;

    /*
     * Request duration in format: DD.HH:MM:SS.MMMMMM. Must be less than 1000
     * days.
     */
    @JsonProperty(value = "duration", required = true)
    private String duration;

    /*
     * Indication of successful or unsuccessful call.
     */
    @JsonProperty(value = "success")
    private Boolean success;

    /*
     * Collection of custom properties.
     */
    @JsonProperty(value = "properties")
    private Map<String, String> properties;

    /*
     * Collection of custom measurements.
     */
    @JsonProperty(value = "measurements")
    private Map<String, Double> measurements;

    /**
     * Get the id property: Identifier of a dependency call instance. Used for correlation with the request telemetry
     * item corresponding to this dependency call.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Identifier of a dependency call instance. Used for correlation with the request telemetry
     * item corresponding to this dependency call.
     *
     * @param id the id value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the name property: Name of the command initiated with this dependency call. Low cardinality value. Examples
     * are stored procedure name and URL path template.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Name of the command initiated with this dependency call. Low cardinality value. Examples
     * are stored procedure name and URL path template.
     *
     * @param name the name value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the resultCode property: Result code of a dependency call. Examples are SQL error code and HTTP status code.
     *
     * @return the resultCode value.
     */
    public String getResultCode() {
        return this.resultCode;
    }

    /**
     * Set the resultCode property: Result code of a dependency call. Examples are SQL error code and HTTP status code.
     *
     * @param resultCode the resultCode value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    /**
     * Get the data property: Command initiated by this dependency call. Examples are SQL statement and HTTP URL with
     * all query parameters.
     *
     * @return the data value.
     */
    public String getData() {
        return this.data;
    }

    /**
     * Set the data property: Command initiated by this dependency call. Examples are SQL statement and HTTP URL with
     * all query parameters.
     *
     * @param data the data value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setData(String data) {
        this.data = data;
        return this;
    }

    /**
     * Get the type property: Dependency type name. Very low cardinality value for logical grouping of dependencies and
     * interpretation of other fields like commandName and resultCode. Examples are SQL, Azure table, and HTTP.
     *
     * @return the type value.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type property: Dependency type name. Very low cardinality value for logical grouping of dependencies and
     * interpretation of other fields like commandName and resultCode. Examples are SQL, Azure table, and HTTP.
     *
     * @param type the type value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the target property: Target site of a dependency call. Examples are server name, host address.
     *
     * @return the target value.
     */
    public String getTarget() {
        return this.target;
    }

    /**
     * Set the target property: Target site of a dependency call. Examples are server name, host address.
     *
     * @param target the target value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Get the duration property: Request duration in format: DD.HH:MM:SS.MMMMMM. Must be less than 1000 days.
     *
     * @return the duration value.
     */
    public String getDuration() {
        return this.duration;
    }

    /**
     * Set the duration property: Request duration in format: DD.HH:MM:SS.MMMMMM. Must be less than 1000 days.
     *
     * @param duration the duration value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Get the success property: Indication of successful or unsuccessful call.
     *
     * @return the success value.
     */
    public Boolean isSuccess() {
        return this.success;
    }

    /**
     * Set the success property: Indication of successful or unsuccessful call.
     *
     * @param success the success value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setSuccess(Boolean success) {
        this.success = success;
        return this;
    }

    /**
     * Get the properties property: Collection of custom properties.
     *
     * @return the properties value.
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Set the properties property: Collection of custom properties.
     *
     * @param properties the properties value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the measurements property: Collection of custom measurements.
     *
     * @return the measurements value.
     */
    public Map<String, Double> getMeasurements() {
        return this.measurements;
    }

    /**
     * Set the measurements property: Collection of custom measurements.
     *
     * @param measurements the measurements value to set.
     * @return the RemoteDependencyData object itself.
     */
    public RemoteDependencyData setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
        return this;
    }
}
