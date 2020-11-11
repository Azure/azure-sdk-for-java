// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config;


import com.azure.data.appconfiguration.ConfigurationClientBuilder;

public interface ConfigurationClientBuilderSetup {

    void setup(ConfigurationClientBuilder builder, String endpoint);

}
