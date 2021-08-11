package com.azure.spring.data.cosmos.core.mapping.event;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.util.Assert;

public class AfterLoadEvent<T> extends CosmosMappingEvent<JsonNode> {

    private static final long serialVersionUID = 1L;
    private final Class<T> type;

    /**
     * Creates a new {@link AfterLoadEvent} for the given {@link JsonNode}, type and collectionName.
     *
     * @param document must not be {@literal null}.
     * @param type must not be {@literal null}.
     * @param containerName must not be {@literal null}.
     * @since 1.8
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
