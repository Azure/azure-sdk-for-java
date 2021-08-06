// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/** Defines values for ContainerRegistryAudience. */
public class ContainerRegistryAudience extends ExpandableStringEnum<ContainerRegistryAudience> {
    /** Static value AzureResourceManagerChina for ContainerRegistryAudience. */
    public static final ContainerRegistryAudience AZURERESOURCEMANAGERCHINA = fromString("https://management.chinacloudapi.cn");

    /** Static value AzureResourceManagerGermany for ContainerRegistryAudience. */
    public static final ContainerRegistryAudience AZURERESOURCEMANAGERGERMANY = fromString("https://management.microsoftazure.de");

    /** Static value AzureResourceManagerGovernment for ContainerRegistryAudience. */
    public static final ContainerRegistryAudience AZURERESOURCEMANAGERGOVERNMENT = fromString("https://management.usgovcloudapi.net");

    /** Static value AzureResourceManagerPublicCloud for ContainerRegistryAudience. */
    public static final ContainerRegistryAudience  AZURERESOURCEMANAGERPUBLICCLOUD  = fromString("https://management.azure.com");

    /**
     * Creates or finds a ContainerRegistryAudience from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding ArtifactArchitecture.
     */
    @JsonCreator
    public static ContainerRegistryAudience fromString(String name) {
        return fromString(name, ContainerRegistryAudience.class);
    }

    /** @return known ArtifactArchitecture values. */
    public static Collection<ContainerRegistryAudience> values() {
        return values(ContainerRegistryAudience.class);
    }
}
