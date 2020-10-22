// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.conversion.script;

import com.azure.spring.data.gremlin.annotation.GeneratedValue;
import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.common.GremlinEntityType;
import com.azure.spring.data.gremlin.common.GremlinUtils;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedEntityTypeException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class GremlinScriptLiteralHelper {

    static String generateEntityWithRequiredId(@NonNull Object id, GremlinEntityType type) {
        Assert.notNull(id, "id should not be null");
        Assert.isTrue(type == GremlinEntityType.EDGE || type == GremlinEntityType.VERTEX, "should be edge/vertex type");

        final String prefix = (type == GremlinEntityType.VERTEX) ? "V" : "E";

        if (id instanceof String) {
            return prefix + String.format("('%s')", id);
        } else if (id instanceof Integer) {
            return prefix + String.format("(%d)", id);
        } else if (id instanceof Long) {
            return prefix + String.format("(%d)", id);
        }

        throw new GremlinInvalidEntityIdFieldException("Only String/Integer/Long of id is supported");
    }

    static String generatePropertyWithRequiredId(@NonNull Object id) {
        if (id instanceof String) {
            return String.format("property(id, '%s')", id);
        } else if (id instanceof Integer) {
            return String.format("property(id, %d)", id);
        } else if (id instanceof Long) {
            return String.format("property(id, %d)", id);
        }

        throw new GremlinInvalidEntityIdFieldException("Only String/Integer/Long of id is supported");
    }

    static String generateAsWithAlias(@NonNull String alias) {
        return String.format("as('%s')", alias);
    }

    static String generateAddEntityWithLabel(@NonNull String label, GremlinEntityType type) {
        Assert.isTrue(type == GremlinEntityType.EDGE || type == GremlinEntityType.VERTEX, "should be edge/vertex type");

        final String prefix = (type == GremlinEntityType.VERTEX) ? "addV" : "addE";

        return prefix + String.format("('%s')", label);
    }

    static List<String> completeScript(@NonNull List<String> scriptList) {
        return Collections.singletonList(String.join(Constants.GREMLIN_PRIMITIVE_INVOKE, scriptList));
    }

    public static String generateHasLabel(@NonNull String label) {
        return String.format("has(label, '%s')", label);
    }

    /**
     * Generate the hasId statement.
     *
     * @param id The id object.
     * @return The hasId statement.
     * @throws GremlinInvalidEntityIdFieldException If the id type is not supported.
     */
    public static String generateHasId(@NonNull Object id) {
        if (id instanceof String) {
            return String.format("hasId('%s')", id);
        } else if (id instanceof Integer) {
            return String.format("hasId(%d)", id);
        } else if (id instanceof Long) {
            return String.format("hasId(%d)", id);
        } else {
            throw new GremlinInvalidEntityIdFieldException("the type of @Id/id field should be String/Integer/Long");
        }
    }

    /**
     * Generate the hasId statement.
     *
     * @param id The id object.
     * @param idFiled The id field.
     * @return The hasId statement.
     * @throws GremlinInvalidEntityIdFieldException If the id type is not supported.
     */
    public static String generateHasId(@NonNull Object id, @NonNull Field idFiled) {
        if (!idFiled.isAnnotationPresent(GeneratedValue.class)) {
            return generateHasId(id);
        } else if (id instanceof String) {
            return String.format("hasId('%s')", id);
        } else if (id instanceof Integer) {
            return String.format("hasId(%dL)", id);
        } else if (id instanceof Long) {
            return String.format("hasId(%dL)", id);
        } else {
            throw new GremlinInvalidEntityIdFieldException("the type of @Id/id field should be String/Integer/Long");
        }
    }

    private static String generateProperty(@NonNull String name, @NonNull String value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_PROPERTY_STRING, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Integer value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_PROPERTY_NUMBER, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Boolean value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_PROPERTY_BOOLEAN, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Long value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_PROPERTY_NUMBER, name, value);
    }

    private static String generateProperty(@NonNull String name, @NonNull Object value) {
        if (value instanceof Integer) {
            return generateProperty(name, (Integer) value);
        } else if (value instanceof Boolean) {
            return generateProperty(name, (Boolean) value);
        } else if (value instanceof String) {
            return generateProperty(name, (String) value);
        } else if (value instanceof Date) {
            return generateProperty(name, GremlinUtils.timeToMilliSeconds(value));
        } else {
            final String propertyScript;

            try {
                propertyScript = generateProperty(name, GremlinUtils.getObjectMapper().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new GremlinUnexpectedEntityTypeException("Failed to write object to String", e);
            }

            return propertyScript;
        }
    }

    protected static List<String> generateProperties(@NonNull final Map<String, Object> properties) {
        final List<String> scripts = new ArrayList<>();

        properties.entrySet().stream().filter(e -> e.getValue() != null)
            .forEach(e -> scripts.add(generateProperty(e.getKey(), e.getValue())));

        return scripts;
    }

    private static String generateHas(@NonNull String name, @NonNull Integer value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_HAS_NUMBER, name, value);
    }

    private static String generateHas(@NonNull String name, @NonNull Boolean value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_HAS_BOOLEAN, name, value);
    }

    private static String generateHas(@NonNull String name, @NonNull String value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_HAS_STRING, name, value);
    }

    private static String generateHas(@NonNull String name, @NonNull Long value) {
        return String.format(Constants.GREMLIN_PRIMITIVE_HAS_NUMBER, name, value);
    }

    // TODO: should move to query method part.

    /**
     * Generate the has statement.
     *
     * @param name The subject part of the has statement.
     * @param value The object part of the has statement.
     * @return The generated has statement.
     * @throws GremlinUnexpectedEntityTypeException If the value could not be written to a string.
     */
    public static String generateHas(@NonNull String name, @NonNull Object value) {

        if (value instanceof Integer) {
            return generateHas(name, (Integer) value);
        } else if (value instanceof Boolean) {
            return generateHas(name, (Boolean) value);
        } else if (value instanceof String) {
            return generateHas(name, (String) value);
        } else if (value instanceof Date) {
            return generateHas(name, GremlinUtils.timeToMilliSeconds(value));
        } else {
            final String hasScript;

            try {
                hasScript = generateHas(name, GremlinUtils.getObjectMapper().writeValueAsString(value));
            } catch (JsonProcessingException e) {
                throw new GremlinUnexpectedEntityTypeException("Failed to write object to String", e);
            }

            return hasScript;
        }
    }
}
