// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation;

import com.azure.spring.cloud.service.implementation.storage.common.StorageRetryProperties;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import java.util.stream.Stream;

class SpringCloudAzureServiceRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        ReflectionHints reflectionHints = hints.reflection();
        Stream.of(StorageRetryProperties.class)
            .forEach(type -> reflectionHints.registerType(type,
                builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_METHODS)));
    }

}
