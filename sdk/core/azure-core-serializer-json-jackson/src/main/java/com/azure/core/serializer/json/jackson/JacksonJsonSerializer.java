// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.BeanUtil;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Jackson based implementation of the {@link JsonSerializer} and {@link MemberNameConverter} interfaces.
 */
public final class JacksonJsonSerializer implements JsonSerializer, MemberNameConverter {
    private final ClientLogger logger = new ClientLogger(JacksonJsonSerializer.class);

    private final ObjectMapper mapper;
    private final TypeFactory typeFactory;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
        this.typeFactory = mapper.getTypeFactory();
    }

    @Override
    public <T> T deserialize(InputStream stream, TypeReference<T> typeReference) {
        if (stream == null) {
            return null;
        }

        try {
            return mapper.readValue(stream, typeFactory.constructType(typeReference.getJavaType()));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> Mono<T> deserializeAsync(InputStream stream, TypeReference<T> typeReference) {
        return Mono.fromCallable(() -> deserialize(stream, typeReference));
    }

    @Override
    public void serialize(OutputStream stream, Object value) {
        try {
            mapper.writeValue(stream, value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public Mono<Void> serializeAsync(OutputStream stream, Object value) {
        return Mono.fromRunnable(() -> serialize(stream, value));
    }


    @Override
    public String convertMemberName(Member member) {
        if (Modifier.isTransient(member.getModifiers())) {
            return null;
        }

        VisibilityChecker<?> visibilityChecker = mapper.getVisibilityChecker();
        if (member instanceof Field) {
            Field f = (Field) member;

            if (f.isAnnotationPresent(JsonIgnore.class) || !visibilityChecker.isFieldVisible(f)) {
                if (f.isAnnotationPresent(JsonProperty.class)) {
                    logger.info("Field {} is annotated with JsonProperty but isn't accessible to "
                        + "JacksonJsonSerializer.", f.getName());
                }
                return null;
            }

            if (f.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = f.getDeclaredAnnotation(JsonProperty.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? f.getName() : propertyName;
            }

            return f.getName();
        }

        if (member instanceof Method) {
            Method m = (Method) member;

            /*
             * If the method isn't a getter, is annotated with JsonIgnore, or isn't visible to the ObjectMapper ignore
             * it.
             */
            if (!verifyGetter(m)
                || m.isAnnotationPresent(JsonIgnore.class)
                || !visibilityChecker.isGetterVisible(m)) {
                if (m.isAnnotationPresent(JsonGetter.class) || m.isAnnotationPresent(JsonProperty.class)) {
                    logger.info("Method {} is annotated with either JsonGetter or JsonProperty but isn't accessible "
                        + "to JacksonJsonSerializer.", m.getName());
                }
                return null;
            }

            String methodNameWithoutJavaBeans = removePrefix(m);

            /*
             * Prefer JsonGetter over JsonProperty as it is the more targeted annotation.
             */
            if (m.isAnnotationPresent(JsonGetter.class)) {
                String propertyName = m.getDeclaredAnnotation(JsonGetter.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? methodNameWithoutJavaBeans : propertyName;
            }

            if (m.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = m.getDeclaredAnnotation(JsonProperty.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? methodNameWithoutJavaBeans : propertyName;
            }

            // If no annotation is present default to the inferred name.
            return methodNameWithoutJavaBeans;
        }

        return null;
    }

    /*
     * Only consider methods that don't have parameters and aren't void as valid getter methods.
     */
    private static boolean verifyGetter(Method method) {
        Class<?> returnType = method.getReturnType();

        return method.getParameterCount() == 0
            && returnType != void.class
            && returnType != Void.class;
    }

    private static String removePrefix(Method method) {
        return BeanUtil.okNameForGetter(new AnnotatedMethod(null, method, null, null), false);
    }
}
