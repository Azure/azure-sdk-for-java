// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Represents the base resource in the Azure Cosmos DB database service.
 */
public class Resource extends JsonSerializable {
    private String altLink;

    static void validateResource(Resource resource) {
        if (!StringUtils.isEmpty(resource.id())) {
            if (resource.id().indexOf('/') != -1 || resource.id().indexOf('\\') != -1 ||
                    resource.id().indexOf('?') != -1 || resource.id().indexOf('#') != -1) {
                throw new IllegalArgumentException("Id contains illegal chars.");
            }

            if (resource.id().endsWith(" ")) {
                throw new IllegalArgumentException("Id ends with a space.");
            }
        }
    }

    /**
     * Copy constructor.
     * 
     * @param resource resource to by copied.
     */
    protected Resource(Resource resource) {
        this.id(resource.id());
        this.resourceId(resource.resourceId());
        this.selfLink(resource.selfLink());
        this.altLink(resource.altLink());
        this.timestamp(resource.timestamp());
        this.etag(resource.etag());
    }

    /**
     * Constructor.
     */
    protected Resource() {
        super();
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     *                   {@link JsonSerializable}
     */
    Resource(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Constructor.
     *
     * @param jsonString   the json string that represents the resource.
     * @param objectMapper the custom object mapper
     */
    Resource(String jsonString, ObjectMapper objectMapper) {
        // TODO: Made package private due to #153. #171 adding custom serialization options back.
        super(jsonString, objectMapper);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the resource.
     */
    protected Resource(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String id() {
        return super.getString(Constants.Properties.ID);
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     * @return the resource.
     */
    public Resource id(String id) {
        super.set(Constants.Properties.ID, id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    public String resourceId() {
        return super.getString(Constants.Properties.R_ID);
    }

    // TODO: make private
    /**
     * Set the ID associated with the resource.
     *
     * @param resourceId the ID associated with the resource.
     * @return the resource.
     */
    public Resource resourceId(String resourceId) {
        super.set(Constants.Properties.R_ID, resourceId);
        return this;
    }

    /**
     * Get the self-link associated with the resource.
     *
     * @return the self link.
     */
    public String selfLink() {
        return super.getString(Constants.Properties.SELF_LINK);
    }

    /**
     * Set the self-link associated with the resource.
     *
     * @param selfLink the self link.
     */
    Resource selfLink(String selfLink) {
        super.set(Constants.Properties.SELF_LINK, selfLink);
        return this;
    }

    /**
     * Get the last modified timestamp associated with the resource.
     *
     * @return the timestamp.
     */
    public OffsetDateTime timestamp() {
        Long seconds = super.getLong(Constants.Properties.LAST_MODIFIED);
        if (seconds == null)
            return null;
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(seconds.longValue()), ZoneOffset.UTC);
    }

    /**
     * Set the last modified timestamp associated with the resource.
     *
     * @param timestamp the timestamp.
     */
    Resource timestamp(OffsetDateTime timestamp) {
        long seconds = timestamp.toEpochSecond();
        super.set(Constants.Properties.LAST_MODIFIED, seconds);
        return this;
    }

    /**
     * Get the entity tag associated with the resource.
     *
     * @return the e tag.
     */
    public String etag() {
        return super.getString(Constants.Properties.E_TAG);
    }

    /**
     * Set the self-link associated with the resource.
     *
     * @param eTag the e tag.
     */
    Resource etag(String eTag) {
        super.set(Constants.Properties.E_TAG, eTag);
        return this;
    }

    /**
     * Sets the alt-link associated with the resource from the Azure Cosmos DB
     * service.
     * 
     * @param altLink
     */
    Resource altLink(String altLink) {
        this.altLink = altLink;
        return this;
    }

    /**
     * Gets the alt-link associated with the resource from the Azure Cosmos DB
     * service.
     */
    String altLink() {
        return this.altLink;
    }
}
