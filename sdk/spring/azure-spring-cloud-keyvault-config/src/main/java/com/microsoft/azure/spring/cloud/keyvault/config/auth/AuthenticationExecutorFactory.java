// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.keyvault.config.auth;

public interface AuthenticationExecutorFactory {
    AuthenticationExecutor create(Credentials credentials);
}

