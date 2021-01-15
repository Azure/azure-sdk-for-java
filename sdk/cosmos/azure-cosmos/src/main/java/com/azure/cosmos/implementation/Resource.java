// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.nio.ByteBuffer;
import java.time.Instant;

/**
 * Represents the base resource in the Azure Cosmos DB database service.
 */
public class Resource extends JsonSerializable {
    private String altLink;

    public static void validateResource(Resource resource) {
        if (!StringUtils.isEmpty(resource.getId())) {
            if (resource.getId().indexOf('/') != -1
                    || resource.getId().indexOf('\\') != -1
                    || resource.getId().indexOf('?') != -1
                    || resource.getId().indexOf('#') != -1) {
                throw new IllegalArgumentException("Id contains illegal chars.");
            }

            if (resource.getId().endsWith(" ")) {
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
        this.setId(resource.getId());
        this.setResourceId(resource.getResourceId());
        this.setSelfLink(resource.getSelfLink());
        this.setAltLink(resource.getAltLink());
        this.setTimestamp(resource.getTimestamp());
        this.setETag(resource.getETag());
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
     * {@link JsonSerializable}
     */
    protected Resource(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the resource.
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
     * Constructor.
     *
     * @param byteBuffer {@link ByteBuffer} encoding of the json string that represents the resource.
     */
    protected Resource(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    /**
     * Constructor.
     *
     * @param bytes byte[] encoding of the json string that represents the resource.
     */
    protected Resource(byte[] bytes) {
        super(bytes);
    }


    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return super.getString(Constants.Properties.ID);
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     * @return the resource.
     */
    public Resource setId(String id) {
        super.set(Constants.Properties.ID, id);
        return this;
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    public String getResourceId() {
        return super.getString(Constants.Properties.R_ID);
    }

    /**
     * Set the ID associated with the resource.
     *
     * @param resourceId the ID associated with the resource.
     * @return the resource.
     */
    public Resource setResourceId(String resourceId) {
        super.set(Constants.Properties.R_ID, resourceId);
        return this;
    }

    /**
     * Get the self-link associated with the resource.
     *
     * @return the self link.
     */
    public String getSelfLink() {
        return super.getString(Constants.Properties.SELF_LINK);
    }

    /**
     * Set the self-link associated with the resource.
     *
     * @param selfLink the self link.
     */
    public Resource setSelfLink(String selfLink) {
        super.set(Constants.Properties.SELF_LINK, selfLink);
        return this;
    }

    /**
     * Get the last modified timestamp associated with the resource.
     *
     * @return the timestamp.
     */
    public Instant getTimestamp() {
        Long seconds = super.getLong(Constants.Properties.LAST_MODIFIED);
        if (seconds == null) {
            return null;
        }
        return Instant.ofEpochSecond(seconds.longValue());
    }

    /**
     * Set the last modified timestamp associated with the resource.
     *
     * @param timestamp the timestamp.
     */
    public Resource setTimestamp(Instant timestamp) {
        long seconds = timestamp.getEpochSecond();
        super.set(Constants.Properties.LAST_MODIFIED, seconds);
        return this;
    }

    /**
     * Get the entity tag associated with the resource.
     *
     * @return the e tag.
     */
    public String getETag() {
        return super.getString(Constants.Properties.E_TAG);
    }

    /**
     * Set the self-link associated with the resource.
     *
     * @param eTag the e tag.
     */
    Resource setETag(String eTag) {
        super.set(Constants.Properties.E_TAG, eTag);
        return this;
    }

    /**
     * Sets the alt-link associated with the resource from the Azure Cosmos DB
     * service.
     */
    public Resource setAltLink(String altLink) {
        this.altLink = altLink;
        return this;
    }

    /**
     * Gets the alt-link associated with the resource from the Azure Cosmos DB
     * service.
     */
    public String getAltLink() {
        return this.altLink;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
