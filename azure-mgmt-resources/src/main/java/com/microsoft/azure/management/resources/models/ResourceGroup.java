package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.management.resources.ResourceAdapter;
import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.models.implementation.api.ResourceGroupInner;

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
        DefinitionProvisionable withLocation(String regionName);
        DefinitionProvisionable withLocation(Region region);
    }

    interface DefinitionProvisionable extends Provisionable<ResourceGroup> {
        DefinitionProvisionable withTags(Map<String, String> tags);
        DefinitionProvisionable withTag(String key, String value);
    }

    interface Update extends UpdateBlank, Updatable<Update> {
    }

    interface UpdateBlank extends Taggable<Update> {
    }

    /**************************************************************
     * Adapter to other resources
     **************************************************************/

    <T extends ResourceAdapter> T resourceAdapter(T.Builder<T> adapterBuilder);
}
