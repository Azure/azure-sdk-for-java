// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.Assert;

/**
 * Spring Application Event that fires for all reads after a document is loaded and before it is serialized to
 * a domain object
 *
 * Ported to cosmos from spring-data-mongo
 * https://github.com/spring-projects/spring-data-mongodb/blob/main/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/core/mapping/event/AfterLoadEvent.java
 */
public class AfterLoadEvent<T> extends CosmosMappingEvent<JsonNode> {

    private static final long serialVersionUID = 1L;
    private final Class<T> type;

    /**
     * Creates a new {@link AfterLoadEvent} for the given {@link JsonNode}, type and collectionName.
     *
     * @param document must not be {@literal null}.
     * @param type must not be {@literal null}.
     * @param containerName must not be {@literal null}.
     */
    public AfterLoadEvent(JsonNode document, Class<T> type, String containerName) {
        super(document, document, containerName);
        Assert.notNull(type, "Type must not be null!");
        this.type = type;
    }

    /**
     * Returns the type for which the {@link AfterLoadEvent} shall be invoked for.
     *
     * @return never {@literal null}.
     */
    public Class<T> getType() {
        return type;
    }

}
