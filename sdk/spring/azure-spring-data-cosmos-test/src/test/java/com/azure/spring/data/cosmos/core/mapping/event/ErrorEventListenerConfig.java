// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping.event;

import org.springframework.context.annotation.Bean;

public class ErrorEventListenerConfig {

    @Bean
    ThrowErrorEventListener throwErrorEventListener() {
        return new ThrowErrorEventListener();
    }

}
