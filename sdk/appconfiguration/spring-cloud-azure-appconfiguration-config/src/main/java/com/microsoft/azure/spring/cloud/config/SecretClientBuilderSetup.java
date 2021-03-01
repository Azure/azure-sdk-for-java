// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;

import com.azure.security.keyvault.secrets.SecretClientBuilder;

public interface SecretClientBuilderSetup {

    public void setup(SecretClientBuilder builder, String uri);

}
