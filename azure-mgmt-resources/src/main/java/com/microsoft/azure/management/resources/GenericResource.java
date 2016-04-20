package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;

import java.util.Map;

public interface GenericResource extends
        GroupableResource,
        Refreshable<GenericResource>,
        Wrapper<GenericResourceInner> {

    /***********************************************************
     * Getters
     ***********************************************************/

    Plan plan() throws Exception;
    Object properties() throws Exception;

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithProviderNamespace> {
    }

    interface DefinitionWithProviderNamespace {
        DefinitionWithOrWithoutParentResource withProviderNamespace(String resourceProviderNamespace);
    }


    interface DefinitionWithOrWithoutParentResource extends DefinitionWithPlan {
        DefinitionWithPlan withParentResource(String parentResourceId); // ParentResource is optional so user can navigate to DefinitionWithPlan with or without it.
    }

    interface DefinitionWithPlan {
        DefinitionProvisionable withPlan(String name, String publisher, String product, String promotionCode);
    }

    interface DefinitionProvisionable extends Provisionable<GenericResource> {  // Properties, tags are optional
        DefinitionProvisionable withProperties(Object properties);
        DefinitionProvisionable withTags(Map<String, String> tags);
        DefinitionProvisionable withTag(String key, String value);
    }

    // TODO: Updatable properties needs to be revised.
    interface Update extends UpdateBlank, Updatable<Update> {
    }

    interface UpdateBlank extends Taggable<Update> {
    }
}