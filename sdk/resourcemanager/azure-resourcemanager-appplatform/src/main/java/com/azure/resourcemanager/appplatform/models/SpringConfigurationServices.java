// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasParent;

/**
 * (Enterprise Tier Only)
 * Entry point for Tanzu Configuration Service API.
 */
@Fluent
public interface SpringConfigurationServices
    extends HasManager<AppPlatformManager>,
        HasParent<SpringService> {
}
