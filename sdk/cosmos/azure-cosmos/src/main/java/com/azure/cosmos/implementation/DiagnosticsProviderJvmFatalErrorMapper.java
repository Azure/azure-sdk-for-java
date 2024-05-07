// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class DiagnosticsProviderJvmFatalErrorMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsProviderJvmFatalErrorMapper.class);
    private static DiagnosticsProviderJvmFatalErrorMapper diagnosticsProviderJvmFatalErrorMapper =
        new DiagnosticsProviderJvmFatalErrorMapper();

    private final AtomicReference<Function<Error, Exception>> fatalErrorMapper;
    private final AtomicLong mapperExecutionCount;

    public DiagnosticsProviderJvmFatalErrorMapper() {
        this.fatalErrorMapper = new AtomicReference<>();
        this.mapperExecutionCount = new AtomicLong(0);
    }

    public void registerFatalErrorMapper(Function<Error, Exception> fatalErrorMapper) {
        LOGGER.info("Register diagnostics provider fatal error mapper");
        this.fatalErrorMapper.set(fatalErrorMapper);
    }

    public Exception mapFatalError(Error error) {
        if (error == null || this.fatalErrorMapper.get() == null) {
            return null;
        }

        return this.mapToException(error);
    }

    private Exception mapToException(Error error) {
        try {
            // increase counter when mapper func being called, this info will be reflected in diagnostics as well
            this.mapperExecutionCount.getAndIncrement();

            Exception mappedException = this.fatalErrorMapper.get().apply(error);
            if (mappedException != null) {
                LOGGER.info("Mapping from Error {} to Exception {}", error.getClass(), mappedException.getClass());
                return mappedException;
            } else {
                LOGGER.info("Mapped exception being null.");
            }
        } catch (Exception mapException) {
            LOGGER.error("Map fatal error failed. ", mapException);
        }

        return null;
    }

    public static DiagnosticsProviderJvmFatalErrorMapper getMapper() {
        return diagnosticsProviderJvmFatalErrorMapper;
    }

    public long getMapperExecutionCount() {
        return mapperExecutionCount.get();
    }
}
