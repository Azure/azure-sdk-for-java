// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;

public class StubReactiveAuditorProvider implements ReactiveAuditorAware<String> {

    private String currentAuditor = "reactiveAuditor";

    @Override
    public Mono<String> getCurrentAuditor() {
        return Mono.just(currentAuditor);
    }

    public void setCurrentAuditor(String currentAuditor) {
        this.currentAuditor = currentAuditor;
    }
}
