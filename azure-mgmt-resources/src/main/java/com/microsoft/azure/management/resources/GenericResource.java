package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;

import java.util.Map;

/**
 * Defines an interface for accessing a generic resource in Azure.
 */
public interface GenericResource extends
        GroupableResource,
        Refreshable<GenericResource>,
        Wrapper<GenericResourceInner> {

    /**
     * Get the plan of the resource.
     *
     * @return the plan of the resource.
     */
    Plan plan();

    /**
     * Get the resource properties.
     *
     * @return the resource properties.
     */
    Object properties();

    /**
     * A generic resource definition allowing region to be specified.
     */
    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * A generic resource definition allowing resource group to be specified.
     */
    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionWithProviderNamespace> {
    }

    /**
     * A generic resource definition allowing provider namespace to be specified.
     */
    interface DefinitionWithProviderNamespace {
        DefinitionWithOrWithoutParentResource withProviderNamespace(String resourceProviderNamespace);
    }

    /**
     * A generic resource definition allowing parent resource to be specified.
     */
    interface DefinitionWithOrWithoutParentResource extends DefinitionWithPlan {
        DefinitionWithPlan withParentResource(String parentResourceId); // ParentResource is optional so user can navigate to DefinitionWithPlan with or without it.
    }

    /**
     * A generic resource definition allowing plan to be specified.
     */
    interface DefinitionWithPlan {
        DefinitionCreatable withPlan(String name, String publisher, String product, String promotionCode);
    }

    /**
     * A deployment definition with sufficient inputs to create a new
     * resource in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends Creatable<GenericResource> {  // Properties, tags are optional
        DefinitionCreatable withProperties(Object properties);
        DefinitionCreatable withTags(Map<String, String> tags);
        DefinitionCreatable withTag(String key, String value);
    }

    //CHECKSTYLE IGNORE TodoComment FOR NEXT 1 LINE
    // TODO: Updatable properties needs to be revised.
    //CHECKSTYLE IGNORE JavadocType FOR NEXT 5 LINES
    interface Update extends UpdateBlank, Appliable<Update> {
    }

    interface UpdateBlank extends Taggable<Update> {
    }
}