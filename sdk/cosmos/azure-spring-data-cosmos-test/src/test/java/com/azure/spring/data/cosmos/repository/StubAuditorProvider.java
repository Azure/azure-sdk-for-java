// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class StubAuditorProvider implements AuditorAware<String> {

    private String currentAuditor;

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(currentAuditor);
    }

    public void setCurrentAuditor(String currentAuditor) {
        this.currentAuditor = currentAuditor;
    }

}
