// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for ContainerRegistryAudience. */
public final class ContainerRegistryAudience extends ExpandableStringEnum<ContainerRegistryAudience> {
    /**
     * Creates a new instance of {@link ContainerRegistryAudience} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link ContainerRegistryAudience} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public ContainerRegistryAudience() {
    }

    /**
     * Audience value used to get management (ARM) token scope for Azure Container Registry in Azure China cloud.
     */
    public static final ContainerRegistryAudience AZURE_RESOURCE_MANAGER_CHINA = fromString("https://management.chinacloudapi.cn");

    /**
     * Audience value used to get management (ARM) token scope for Azure Container Registry in Azure Germany Germany cloud.
     * @deprecated Germany government cloud is no longer supported.
     */
    @Deprecated
    public static final ContainerRegistryAudience AZURE_RESOURCE_MANAGER_GERMANY = fromString("https://management.microsoftazure.de");

    /**
     * Audience value used to get management (ARM) token scope for Azure Container Registry in Azure US government cloud.
     */
    public static final ContainerRegistryAudience AZURE_RESOURCE_MANAGER_GOVERNMENT = fromString("https://management.usgovcloudapi.net");

    /**
     * Audience value used to get management (ARM) token scope for Azure Container Registry in Azure public cloud.
     */
    public static final ContainerRegistryAudience AZURE_RESOURCE_MANAGER_PUBLIC_CLOUD = fromString("https://management.azure.com");

    /**
     * Creates or finds a ContainerRegistryAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ContainerRegistryAudience.
     */
    public static ContainerRegistryAudience fromString(String name) {
        return fromString(name, ContainerRegistryAudience.class);
    }

    /** @return known ContainerRegistryAudience values. */
    public static Collection<ContainerRegistryAudience> values() {
        return values(ContainerRegistryAudience.class);
    }
}
