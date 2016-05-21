package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;

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
        DefinitionCreatable withPlan(String name, String publisher, String product, String promotionCode);
    }

    interface DefinitionCreatable extends 
        Creatable<GenericResource>,
        Resource.DefinitionWithTags<DefinitionCreatable> {  
        
        DefinitionCreatable withProperties(Object properties);
    }
}