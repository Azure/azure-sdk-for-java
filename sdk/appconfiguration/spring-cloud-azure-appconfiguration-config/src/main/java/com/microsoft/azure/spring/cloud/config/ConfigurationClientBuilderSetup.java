/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.config;


import com.azure.data.appconfiguration.ConfigurationClientBuilder;

public interface ConfigurationClientBuilderSetup {

    public void setup(ConfigurationClientBuilder builder, String endpoint);

}
