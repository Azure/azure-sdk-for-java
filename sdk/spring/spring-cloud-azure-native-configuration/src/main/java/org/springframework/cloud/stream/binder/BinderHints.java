// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package org.springframework.cloud.stream.binder;

import org.springframework.cloud.stream.function.FunctionConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = FunctionConfiguration.class,
    types = {
        @TypeHint(
            types = {
                Binder.class,
                AbstractExtendedBindingProperties.class
            }),
        @TypeHint(
            typeNames = {
                "org.springframework.cloud.stream.function.BindableFunctionProxyFactory",
                "org.springframework.boot.actuate.health.HealthIndicator"
            })
    },
    resources = @ResourceHint(patterns = "META-INF/spring.binders")
)
public class BinderHints implements NativeConfiguration {
}
