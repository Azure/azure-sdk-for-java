// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.gremlin.common;

import com.microsoft.azure.spring.data.gremlin.annotation.GeneratedValue;
import com.microsoft.azure.spring.data.gremlin.conversion.source.GremlinSource;
import com.microsoft.azure.spring.data.gremlin.exception.GremlinIllegalConfigurationException;
import com.microsoft.azure.spring.data.gremlin.exception.GremlinInvalidEntityIdFieldException;
import com.microsoft.azure.spring.data.gremlin.exception.GremlinUnexpectedSourceTypeException;
import com.microsoft.azure.spring.data.gremlin.repository.support.GremlinEntityInformation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.tinkerpop.shaded.jackson.databind.MapperFeature;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GremlinUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
    }

    public static ObjectMapper getObjectMapper() {
        return MAPPER;
    }

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

    public static long timeToMilliSeconds(@NonNull Object time) {
        if (time instanceof Date) {
            return ((Date) time).getTime();
        } else {
            throw new UnsupportedOperationException("Unsupported time type");
        }
    }

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

    public static Class<?> toEntityClass(@NonNull String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new GremlinUnexpectedSourceTypeException("failed to retrieve class: " + className, e);
        }
    }
}
