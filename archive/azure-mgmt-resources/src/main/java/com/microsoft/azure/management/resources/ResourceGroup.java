/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.implementation.ResourceGroupInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;

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
    Observable<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions options);

    /**
     * Captures the specified resource group as a template asynchronously.
     *
     * @param options the export options
     * @param callback the callback to call on success or failure with result as parameter
     *
     * @return a handle to cancel the request
     */
    ServiceFuture<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions options, ServiceCallback<ResourceGroupExportResult> callback);

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
