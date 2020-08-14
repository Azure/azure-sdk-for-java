// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.resources.fluent.inner.ResourceGroupInner;
import reactor.core.publisher.Mono;

/**
 * An immutable client-side representation of an Azure resource group.
 */
@Fluent
public interface ResourceGroup extends
        Indexable,
        Resource,
        Refreshable<ResourceGroup>,
        HasInner<ResourceGroupInner>,
        Updatable<ResourceGroup.Update>,
        HasName {

    /**
     * @return the provisioning state of the resource group
     */
    String provisioningState();

    /**
     * Captures the specified resource group as a template.
     *
     * @param options the export options
     * @return the exported template result
     */
    ResourceGroupExportResult exportTemplate(ResourceGroupExportTemplateOptions options);

    /**
     * Captures the specified resource group as a template asynchronously.
     *
     * @param options the export options
     * @return a representation of the deferred computation of this call returning the result of the template export
     */
    Mono<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions options);


    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the resource group definition stages.
     */
    interface DefinitionStages {
        /**
         * A resource group definition allowing location to be set.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithCreate> {
        }

        /**
         * A resource group definition with sufficient inputs to create a new
         * resource group in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<ResourceGroup>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * Grouping of all the resource group update stages.
     */
    interface UpdateStages {
    }

    /**
     * The template for a resource group update operation, containing all the settings that can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource group in Azure.
     */
    interface Update extends
            Appliable<ResourceGroup>,
            Resource.UpdateWithTags<Update> {
    }
}
