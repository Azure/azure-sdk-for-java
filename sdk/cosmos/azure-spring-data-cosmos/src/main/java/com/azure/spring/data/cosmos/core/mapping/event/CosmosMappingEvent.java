// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.Nullable;

/*
 * Ported to cosmos from spring-data-mongo
 * https://github.com/spring-projects/spring-data-mongodb/blob/main/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/mapping/event/MongoMappingEvent.java
 */
public class CosmosMappingEvent<T> extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private final @Nullable JsonNode document;
    private final @Nullable String containerName;

    /**
     * Creates new {@link CosmosMappingEvent}.
     *
     * @param source must not be {@literal null}.
     * @param document can be {@literal null}.
     * @param containerName can be {@literal null}.
     */
    public CosmosMappingEvent(T source, @Nullable JsonNode document, @Nullable String containerName) {

        super(source);
        this.document = document;
        this.containerName = containerName;
    }

    /**
     * @return {@literal null} if not set.
     */
    public @Nullable JsonNode getDocument() {
        return document;
    }

    /**
     * Get the container the event refers to.
     *
     * @return {@literal null} if not set.
     */
    public @Nullable String getContainerName() {
        return containerName;
    }

    /*
     * (non-Javadoc)
     * @see java.util.EventObject#getSource()
     */
    @SuppressWarnings({ "unchecked" })
    @Override
    public T getSource() {
        return (T) super.getSource();
    }}
