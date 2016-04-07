package com.microsoft.azure.management.resources.models;

import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Taggable;
import com.microsoft.azure.management.resources.models.implementation.api.ResourceGroupInner;

import java.util.Map;

public interface ResourceGroup extends
        Indexable,
        Refreshable<ResourceGroup>,
        Wrapper<ResourceGroupInner>,
        Deletable {

    /***********************************************************
     * Getters
     ***********************************************************/

    String name();
    String provisioningState() throws Exception;
    String region() throws Exception;
    Map<String, String> tags() throws Exception;

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    interface DefinitionBlank {
        DefinitionProvisionable withRegion(String regionName);
        DefinitionProvisionable withRegion(Region region);
    }

    interface DefinitionProvisionable extends Provisionable<ResourceGroup> {
        DefinitionProvisionable withTags(Map<String, String> tags);
        DefinitionProvisionable withTag(String key, String value);
    }

    interface Update extends UpdateBlank, Updatable<Update> {
    }

    interface UpdateBlank extends Deletable, Taggable<Update> {
    }
}
