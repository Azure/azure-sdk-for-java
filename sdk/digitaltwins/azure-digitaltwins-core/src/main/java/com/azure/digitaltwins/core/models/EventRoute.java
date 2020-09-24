// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The EventRoute model. Event routes are used for defining where published telemetry gets sent to. As an example, an
 * event route can point towards an Azure EventHub as a consumer of published telemetry.
 */
// This class exists so that the public APIs don't directly consume a generated type and so that we can avoid exposing a validate() method
// that the generated type comes with when client side validation is enabled.
@Fluent
public class EventRoute {
    /*
     * The id of the event route.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /*
     * The name of the endpoint this event route is bound to.
     */
    @JsonProperty(value = "endpointName", required = true)
    private String endpointName;

    /*
     * An expression which describes the events which are routed to the
     * endpoint.
     */
    @JsonProperty(value = "filter")
    private String filter;

    /**
     * Creates an instance of EventRoute class.
     * @param endpointName the name of the endpoint that this event route connects to.
     */
    @JsonCreator
    public EventRoute(@JsonProperty(value = "endpointName", required = true) String endpointName) {
        this.endpointName = endpointName;
    }

    /**
     * Get the id property: The id of the event route.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the endpointName property: The name of the endpoint this event route is bound to.
     *
     * @return the endpointName value.
     */
    public String getEndpointName() {
        return this.endpointName;
    }

    /**
     * Get the filter property: An expression which describes the events which are routed to the endpoint.
     *
     * @return the filter value.
     */
    public String getFilter() {
        return this.filter;
    }

    /**
     * Set the filter property: An expression which describes the events which are routed to the endpoint.
     *
     * @param filter the filter value to set.
     * @return the EventRoute object itself.
     */
    public EventRoute setFilter(String filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets the event route's Id.
     *
     * @param id The event route's Id to set.
     * @return the EventRoute object itself.
     */
    public EventRoute setId(String id) {
        this.id = id;
        return this;
    }
}
