// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

final class JacksonVersionMismatchError extends Error {
    JacksonVersionMismatchError(String versionInfo, Throwable cause) {
        super(cause.getMessage() + System.lineSeparator() + versionInfo, cause);
    }
}
