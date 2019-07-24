// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.directconnectivity.Protocol;
import org.mockito.Mockito;

/**
 * This can be used for testing.
 */
public class ConfigsBuilder {
    private Configs configs = Mockito.spy(new Configs());

    public static ConfigsBuilder instance() {
        return new ConfigsBuilder();
    }

    public ConfigsBuilder withProtocol(Protocol protocol) {
        Mockito.doReturn(protocol).when(configs).getProtocol();
        return this;
    }

    public Configs build() {
        return configs;
    }
}
