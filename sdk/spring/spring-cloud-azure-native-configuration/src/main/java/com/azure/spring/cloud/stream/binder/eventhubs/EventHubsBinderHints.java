// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsBinderConfiguration;
//import com.azure.spring.cloud.stream.binder.eventhubs.config.EventHubsBinderHealthIndicatorConfiguration;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsBindingProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsExtendedBindingProperties;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.function.FunctionConfiguration;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.ResourceHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = FunctionConfiguration.class,
    types = @TypeHint(
        types = {
            EventHubsBinderConfiguration.class,
//            EventHubsBinderHealthIndicatorConfiguration.class,
            SpelExpressionConverterConfiguration.class,
            EventHubsBindingProperties.class,
            EventHubsExtendedBindingProperties.class
        })
)
@TypeHint(typeNames = {
    "org.springframework.cloud.stream.function.BindableFunctionProxyFactory",
})
@ResourceHint(patterns = "META-INF/spring.binders")
public class EventHubsBinderHints implements NativeConfiguration {
}
