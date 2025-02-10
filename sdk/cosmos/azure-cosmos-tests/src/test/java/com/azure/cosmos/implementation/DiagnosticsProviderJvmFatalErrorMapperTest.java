// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class DiagnosticsProviderJvmFatalErrorMapperTest {
    @Test(groups = "unit")
    public void noFatalErrorMapperRegistered() {
        Exception mappedException =
            DiagnosticsProviderJvmFatalErrorMapper.getMapper().mapFatalError(new OutOfMemoryError("Test"));
        assertThat(mappedException).isNull();
    }

    @Test(groups = "unit")
    public void fatalErrorMapperRegistered() {
        DiagnosticsProviderJvmFatalErrorMapper.getMapper().registerFatalErrorMapper(
            (error) -> new NullPointerException(error.getMessage()));

        Exception mappedException =
            DiagnosticsProviderJvmFatalErrorMapper.getMapper().mapFatalError(new OutOfMemoryError("Test"));
        assertThat(mappedException).isNotNull();
        assertThat(mappedException).isInstanceOf(NullPointerException.class);

        // test the exception mapper throw exception
        DiagnosticsProviderJvmFatalErrorMapper.getMapper().registerFatalErrorMapper(
            (error) -> { throw new RuntimeException("Failed during mapping"); });
        mappedException = DiagnosticsProviderJvmFatalErrorMapper.getMapper().mapFatalError(new OutOfMemoryError("Test"));
        assertThat(mappedException).isNull();
    }
}
