// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

import com.azure.spring.data.gremlin.annotation.GeneratedValue;
import com.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.azure.spring.data.gremlin.exception.GremlinIllegalConfigurationException;
import com.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.azure.spring.data.gremlin.repository.support.GremlinEntityInformation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.tinkerpop.shaded.jackson.databind.MapperFeature;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

public class GremlinUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

    /**
     * Create an instance for class type provided.
     *
     * @param type The class for the target type.
     * @param <T> The class type.
     * @return The created instance.
     * @throws IllegalArgumentException If fails to instantiate.
     */
    public static <T> T createInstance(@NonNull Class<T> type) {
        final T instance;

        try {
            instance = type.newInstance();
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("can not access type constructor", e);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("failed to create instance of given type", e);
        }

        return instance;
    }

    /**
     * Get the id field from a domain class.
     *
     * @param domainClass The target domain class.
     * @param <T> The type of the id field.
     * @return The {@link Field} of the id field.
     * @throws GremlinIllegalConfigurationException If more than one field is configured with {@link GeneratedValue}
     *     annotation.
     * @throws GremlinInvalidEntityIdFieldException If more than one {@link Id} annotated field are configured, no
     *     id field is configured, or the id field type is not allowed.
     * @throws GremlinIllegalConfigurationException If field other than the id field is configured with {@link
     *     GeneratedValue} annotation.
     */
    public static <T> Field getIdField(@NonNull Class<T> domainClass) {
        final Field idField;
        final List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(domainClass, Id.class);
        final List<Field> generatedValueFields =
            FieldUtils.getFieldsListWithAnnotation(domainClass, GeneratedValue.class);

        if (generatedValueFields.size() > 1) {
            throw new GremlinIllegalConfigurationException("Only one field, the id field, can have the optional "
                + "@GeneratedValue annotation!");
        }

        if (idFields.isEmpty()) {
            idField = ReflectionUtils.findField(domainClass, Constants.PROPERTY_ID);
        } else if (idFields.size() == 1) {
            idField = idFields.get(0);
        } else {
            throw new GremlinInvalidEntityIdFieldException("only one @Id field is allowed");
        }

        if (idField == null) {
            throw new GremlinInvalidEntityIdFieldException("no field named id in class");
        } else if (idField.getType() != String.class
            && idField.getType() != Long.class && idField.getType() != Integer.class) {
            throw new GremlinInvalidEntityIdFieldException("the type of @Id/id field should be String/Integer/Long");
        }

        if (generatedValueFields.size() == 1 && !generatedValueFields.get(0).equals(idField)) {
            throw new GremlinIllegalConfigurationException("Only the id field can have the optional "
                + "@GeneratedValue annotation!");
        }

        return idField;
    }

    /**
     * Convert time object to milliseconds.
     *
     * @param time The time object.
     * @return The milliseconds.
     * @throws UnsupportedOperationException If the source type is not supported.
     */
    public static long timeToMilliSeconds(@NonNull Object time) {
        if (time instanceof Date) {
            return ((Date) time).getTime();
        } else {
            throw new UnsupportedOperationException("Unsupported time type");
        }
    }

    /**
     * Convert object to equivalent long value.
     *
     * @param object The source object.
     * @return The equivalent long value. If the source type is Date, it will convert to the milliseconds.
     * @throws UnsupportedOperationException If the source type is not supported.
     */
    public static long toPrimitiveLong(@NonNull Object object) {
        if (object instanceof Date) {
            return timeToMilliSeconds(object);
        } else if (object instanceof Integer) {
            return (long) (int) object;
        } else if (object instanceof Long) {
            return (long) object;
        } else {
            throw new UnsupportedOperationException("Unsupported object type to long");
        }
    }

    public static <T> GremlinSource<T> toGremlinSource(@NonNull Class<T> domainClass) {
        return new GremlinEntityInformation<>(domainClass).createGremlinSource();
    }

    public static List<List<String>> toParallelQueryList(@NonNull List<String> queries) {
        final List<List<String>> parallelQueries = new ArrayList<>();
        List<String> parallelQuery = new ArrayList<>();

        for (final String query : queries) {
            if (query.equals(Constants.GREMLIN_QUERY_BARRIER)) {
                parallelQueries.add(parallelQuery);
                parallelQuery = new ArrayList<>();
            } else {
                parallelQuery.add(query);
            }
        }

        parallelQueries.add(parallelQuery);

        return parallelQueries;
    }

    /**
     * Convert a {@link String} represented class name to a Java {@link Class} type.
     *
     * @param className The class name.
     * @return The {@link Class} type.
     * @throws GremlinUnexpectedSourceTypeException If the class could not be found.
     */
    public static Class<?> toEntityClass(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new GremlinUnexpectedSourceTypeException("failed to retrieve class: " + className, e);
        }
    }
}
