package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.util.Map;

/**
 * Defines an interface for accessing a resource group in Azure.
 */
public interface ResourceGroup extends
        Indexable,
        Refreshable<ResourceGroup>,
        Wrapper<ResourceGroupInner> {

    /**
     * Get the name of the resource group.
     *
     * @return the name of the resource group.
     */
    String name();

    /**
     * Get the resource group provisioning state.
     *
     * @return the resource group provisioning state.
     */
    String provisioningState();

    /**
     * Get the location of the resource group.
     *
     * @return the location of the resource group.
     */
    String location();

    /**
     * Get the tags attached to the resource group.
     *
     * @return the tags attached to the resource group.
     */
    Map<String, String> tags();

    /**
     * A resource group definition allowing location to be set.
     */
    interface DefinitionBlank {
        DefinitionCreatable withLocation(String regionName);
        DefinitionCreatable withLocation(Region region);
    }

    /**
     * A resource group definition with sufficient inputs to create a new
     * resource group in the cloud, but exposing additional optional inputs to
     * specify.
     */
    interface DefinitionCreatable extends Creatable<ResourceGroup> {
        DefinitionCreatable withTags(Map<String, String> tags);
        DefinitionCreatable withTag(String key, String value);
    }

    //CHECKSTYLE IGNORE JavadocType FOR NEXT 5 LINES
    interface Update extends UpdateBlank, Appliable<Update> {
    }

    interface UpdateBlank extends Taggable<Update> {
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
