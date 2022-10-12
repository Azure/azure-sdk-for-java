// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.auditing.DateTimeProvider;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

public class StubDateTimeProvider implements DateTimeProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(StubDateTimeProvider.class);
    private OffsetDateTime now = OffsetDateTime.now();
    private volatile int modifiedTimes = 0;

    @Override
    public Optional<TemporalAccessor> getNow() {
        LOGGER.info("Current thread {}  get now as {}", Thread.currentThread().getName(), now);
        return Optional.of(now);
    }

    public void setNow(OffsetDateTime now) {
        LOGGER.info("Current thread {}  set now to {}", Thread.currentThread().getName(), now);
        this.now = now;
        modifiedTimes++;
    }

    public int getModifiedTimes() {
        return modifiedTimes;
    }
}
