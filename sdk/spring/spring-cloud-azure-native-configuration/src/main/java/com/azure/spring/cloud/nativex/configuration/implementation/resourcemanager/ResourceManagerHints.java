// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.nativex.configuration.implementation.resourcemanager;

import com.azure.spring.cloud.resourcemanager.implementation.crud.ResourceCrud;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.DefaultEventHubsProvisioner;
import com.azure.spring.cloud.resourcemanager.provisioning.EventHubsProvisioner;
import org.springframework.nativex.hint.NativeHint;
import org.springframework.nativex.hint.TypeHint;
import org.springframework.nativex.type.NativeConfiguration;

@NativeHint(
    trigger = ResourceCrud.class,
    types = {
        @TypeHint(
            types = {
                com.azure.resourcemanager.AzureResourceManager.class,
                EventHubsProvisioner.class,
                DefaultEventHubsProvisioner.class
            })
    }
)
public class ResourceManagerHints implements NativeConfiguration {
}
