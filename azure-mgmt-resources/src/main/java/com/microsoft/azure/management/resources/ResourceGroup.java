/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.io.IOException;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure resource group.
 */
public interface ResourceGroup extends
        Indexable,
        Resource,
        Refreshable<ResourceGroup>,
        Wrapper<ResourceGroupInner>,
        Updatable<ResourceGroup.Update> {
    /**
     * @return the name of the resource group
     */
    String name();

    /**
     * @return the provisioning state of the resource group
     */
    String provisioningState();

    /**
     * @return the region of the resource group
     */
    String region();

    /**
     * @return the tags attached to the resource group
     */
    Map<String, String> tags();

    /**
     * Captures the specified resource group as a template.
     *
     * @param options the export options
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @return the exported template result
     */
    ResourceGroupExportResult exportTemplate(ResourceGroupExportTemplateOptions options) throws CloudException, IOException;

    /**
     * A resource group definition allowing location to be set.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionCreatable> {
    }

    /**
     * A resource group definition with sufficient inputs to create a new
     * resource group in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends
            Creatable<ResourceGroup>,
            Resource.DefinitionWithTags<DefinitionCreatable> {
    }

    /**
     * The template for a resource group update operation, containing all the settings that can be modified.
     */
    interface Update extends
        Appliable<ResourceGroup>,
        Resource.UpdateWithTags<Update> {
    }

    /**
     * Connects to other resources inside the resource group.
     *
     * @param adapterBuilder the builder for building a connector.
     * @param <T> the type of the resource connector.
     * @return the connector with access to other resource types.
     */
    <T extends ResourceConnector> T connectToResource(ResourceConnector.Builder<T> adapterBuilder);
}
