/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;

import com.azure.security.keyvault.secrets.SecretClientBuilder;

public interface SecretClientBuilderSetup {

    public void setup(SecretClientBuilder builder, String uri);

}
