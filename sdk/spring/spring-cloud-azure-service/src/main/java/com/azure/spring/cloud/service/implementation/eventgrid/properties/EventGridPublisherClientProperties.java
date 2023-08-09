// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventgrid.properties;

import com.azure.messaging.eventgrid.EventGridServiceVersion;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import com.azure.spring.cloud.core.provider.authentication.KeyProvider;
import com.azure.spring.cloud.core.provider.authentication.SasTokenProvider;

public interface EventGridPublisherClientProperties extends AzureProperties, RetryOptionsProvider, KeyProvider,
    SasTokenProvider {

    String getEndpoint();

    EventGridServiceVersion getServiceVersion();

}
