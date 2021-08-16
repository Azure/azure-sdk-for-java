// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import org.springframework.data.auditing.DateTimeProvider;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class StubDateTimeProvider implements DateTimeProvider {

    private OffsetDateTime now = OffsetDateTime.now();

    @Override
    public Optional<TemporalAccessor> getNow() {
        return Optional.of(now);
    }

    public void setNow(OffsetDateTime now) {
        this.now = now;
    }
}
