// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
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
 * Jackson based implementation of the {@link JsonSerializer} interface.
 */
public final class JacksonJsonSerializer implements MemberNameConverter, JsonSerializer {
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
        if (member instanceof Field) {
            Field f = (Field) member;
            if (f.isAnnotationPresent(JsonIgnore.class)) {
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
            String methodNameWithoutJavaBeans = removePrefix(m);
            if (m.isAnnotationPresent(JsonIgnore.class)) {
                return null;
            }
            if (m.isAnnotationPresent(JsonProperty.class)) {
                String propertyName = m.getDeclaredAnnotation(JsonProperty.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? methodNameWithoutJavaBeans : propertyName;
            }
            return methodNameWithoutJavaBeans;
        }

        return null;
    }

    private String removePrefix(Method method) {
        return BeanUtil.okNameForGetter(
                new AnnotatedMethod(null, method, null, null), false);
    }
}
