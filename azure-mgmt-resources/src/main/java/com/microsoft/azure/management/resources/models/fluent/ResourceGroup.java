package com.microsoft.azure.management.resources.models.fluent;

import com.microsoft.azure.management.resources.fluentcore.model.*;
import com.microsoft.azure.management.resources.models.fluent.common.Region;
import com.microsoft.azure.management.resources.models.fluent.common.Taggable;

import java.util.Map;

public interface ResourceGroup extends
        Indexable,
        Refreshable<ResourceGroup>,
        Wrapper<com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup>,
        Deletable {

    String region() throws Exception;
    Map<String, String> tags() throws Exception;
    String provisioningState() throws Exception;
    String name();


    interface DefinitionBlank {
        DefinitionProvisionable withRegion(String regionName);
        DefinitionProvisionable withRegion(Region region);
    }


    interface DefinitionProvisionable extends
            Provisionable<ResourceGroup> {
        DefinitionProvisionable withTags(Map<String, String> tags);
        DefinitionProvisionable withTag(String key, String value);
    }

    interface Update extends
            UpdateBlank,
            Updatable<Update> {
    }

    interface UpdateBlank extends
            Deletable,
            Taggable<Update> {
    }
}
