package com.microsoft.azure.management.resources;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.util.Map;

public interface ResourceGroup extends
        Indexable,
        Refreshable<ResourceGroup>,
        Wrapper<ResourceGroupInner>{

    /***********************************************************
     * Getters
     ***********************************************************/

    String name();
    String provisioningState();
    String location();
    Map<String, String> tags();

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    interface DefinitionBlank {
        DefinitionCreatable withLocation(String regionName);
        DefinitionCreatable withLocation(Region region);
    }

    interface DefinitionCreatable extends Creatable<ResourceGroup> {
        DefinitionCreatable withTags(Map<String, String> tags);
        DefinitionCreatable withTag(String key, String value);
    }

    interface Update extends UpdateBlank, Appliable<Update> {
    }

    interface UpdateBlank extends Taggable<Update> {
    }

    /**************************************************************
     * Adapter to other resources
     **************************************************************/

    <T extends ResourceConnector> T connectToResource(ResourceConnector.Builder<T> adapterBuilder);
}
