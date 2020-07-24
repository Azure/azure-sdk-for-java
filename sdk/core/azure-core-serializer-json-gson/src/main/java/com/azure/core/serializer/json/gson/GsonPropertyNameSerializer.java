// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.PropertyNameSerializer;
import com.azure.core.util.CoreUtils;
import com.google.gson.annotations.SerializedName;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * GSON based get property name implementation of the {@link PropertyNameSerializer} interface.
 */
public class GsonPropertyNameSerializer implements PropertyNameSerializer {

    @Override
    public Mono<String> getSerializerMemberName(Member member) {
        return Mono.fromCallable(() -> {
            if (member instanceof Field) {
                if (!((Field) member).isAnnotationPresent(SerializedName.class)) {
                    return member.getName();
                }
                String propertyName = ((Field) member).getDeclaredAnnotation(SerializedName.class).value();
                return CoreUtils.isNullOrEmpty(propertyName) ? ((Field) member).getName() : propertyName;
            } else if (member instanceof Method) {
                return member.getName();
            }
            return null;
        });
    }
}
