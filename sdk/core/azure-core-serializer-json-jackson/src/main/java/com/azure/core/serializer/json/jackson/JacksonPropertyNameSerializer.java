// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.PropertyNameSerializer;
import com.azure.core.util.CoreUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Jackson based get property name implementation of the {@link PropertyNameSerializer} interface.
 */
public class JacksonPropertyNameSerializer implements PropertyNameSerializer {
    @Override
    public Mono<String> getSerializerMemberName(Member member) {

        return Mono.fromCallable(() -> {
            String propertyName = null;
            if (member instanceof Field) {
                if (!((Field) member).isAnnotationPresent(JsonProperty.class)) {
                    return member.getName();
                }
                propertyName = ((Field) member).getDeclaredAnnotation(JsonProperty.class).value();
                propertyName = CoreUtils.isNullOrEmpty(propertyName) ? ((Field) member).getName() : propertyName;
            } else if (member instanceof Method) {
                if (!((Method) member).isAnnotationPresent(JsonProperty.class)) {
                    return member.getName();
                }
                propertyName = ((Method) member).getDeclaredAnnotation(JsonProperty.class).value();
                propertyName = CoreUtils.isNullOrEmpty(propertyName) ? ((Method) member).getName() : propertyName;
            }

            return propertyName;
        });
    }
}
