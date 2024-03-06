// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.base.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class DiagnosticsProviderJvmFatalErrorMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnosticsProviderJvmFatalErrorMapper.class);
    private static DiagnosticsProviderJvmFatalErrorMapper diagnosticsProviderJvmFatalErrorMapper =
        new DiagnosticsProviderJvmFatalErrorMapper();

    private final AtomicReference<Function<Error, Exception>> fatalErrorMapper;

    public DiagnosticsProviderJvmFatalErrorMapper() {
        this.fatalErrorMapper = new AtomicReference<>();
    }

    public void registerFatalErrorMapper(Function<Error, Exception> fatalErrorMapper) {
        LOGGER.info("Register diagnostics provider fatal error mapper");
        this.fatalErrorMapper.set(fatalErrorMapper);
    }

    public Exception mapFatalError(Error error) {
        if (error == null) {
            return null;
        }

        if (this.fatalErrorMapper.get() != null) {
            Exception mappedException = this.fatalErrorMapper.get().apply(error);
            LOGGER.info("Mapping from Error {} to Exception {}", error.getClass(), mappedException.getClass());

            return mappedException;
        }

        return null;
    }

    public static DiagnosticsProviderJvmFatalErrorMapper getMapper() {
        return diagnosticsProviderJvmFatalErrorMapper;
    }
}
