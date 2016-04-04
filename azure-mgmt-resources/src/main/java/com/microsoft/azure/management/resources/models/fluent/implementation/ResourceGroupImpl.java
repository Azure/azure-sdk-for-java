package com.microsoft.azure.management.resources.models.fluent.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.collection.implementation.EntitiesImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.management.resources.models.fluent.ResourceGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResourceGroupImpl 	extends
        IndexableRefreshableWrapperImpl<ResourceGroup, com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup>
        implements
        ResourceGroup,
        ResourceGroup.DefinitionBlank,
        ResourceGroup.DefinitionProvisionable,
        ResourceGroup.Update  {

    private final EntitiesImpl<Azure> collection;

    public ResourceGroupImpl(com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup azureGroup, EntitiesImpl<Azure> collection) {
        super(azureGroup.getName(), azureGroup);
        this.collection = collection;
    }

    /***********************************************************
     * Getters
     ***********************************************************/

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String provisioningState() throws Exception {
        return this.inner().getProperties().getProvisioningState();
    }

    @Override
    public String region() throws Exception {
        return this.inner().getLocation();
    }

    @Override
    public Map<String, String> tags() throws Exception {
        return Collections.unmodifiableMap(this.inner().getTags());
    }

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    @Override
    public ResourceGroupImpl withRegion(String regionName) {       //  FLUENT: implementation of ResourceGroup.DefinitionBlank
        this.inner().setLocation(regionName);                      //
        return this;
    }

    @Override
    public ResourceGroupImpl withRegion(Region region) {            //  FLUENT: implementation of ResourceGroup.DefinitionBlank
        return this.withRegion(region.toString());                  //
    }

    @Override
    public ResourceGroupImpl withTags(Map<String, String> tags) {   //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable
        this.inner().setTags(new HashMap<String, String>(tags));    //                   ResourceGroup.Update.UpdateBlank.Taggable<Update>
        return this;
    }

    @Override
    public ResourceGroupImpl withTag(String key, String value) {    //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable
        if(this.inner().getTags() == null) {                        //                   ResourceGroup.Update.UpdateBlank.Taggable<Update>
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().getTags().put(key, value);
        return this;
    }

    @Override
    public ResourceGroupImpl withoutTag(String key) {               //  FLUENT: implementation of ResourceGroup.Update.UpdateBlank.Taggable<Update>
        this.inner().getTags().remove(key);                         //
        return this;
    }

    /************************************************************
     * Verbs
     ************************************************************/

    @Override
    public ResourceGroupImpl apply() throws Exception {             //  FLUENT: implementation of ResourceGroup.Update.Updatable<T>
        com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup params =
                new com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup();
        ResourceGroup group;

        params.setTags(this.inner().getTags());

        // Figure out the region, since the SDK requires on the params explicitly even though it cannot be changed
        if(this.inner().getLocation() != null) {
            params.setLocation(this.inner().getLocation());
        } else if(null == (group = this.collection.azure().resourceGroups().get(this.id))) {
            throw new Exception("Resource group not found");
        } else {
            params.setLocation(group.region());
        }

        this.collection.azure().resourceManagementClient().resourceGroups().createOrUpdate(this.id, params);
        return this;
    }

    @Override
    public void delete() throws Exception {                         //  FLUENT: implementation of ResourceGroup.Update.UpdateBlank.Delete
        this.collection.azure().resourceGroups().delete(this.id);
    }

    @Override
    public ResourceGroupImpl provision() throws Exception {         //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable.Provisionable<ResourceGroup>
        com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup params =
                new com.microsoft.azure.management.resources.models.dto.toplevel.ResourceGroup();
        params.setLocation(this.inner().getLocation());
        params.setTags(this.inner().getTags());
        this.collection.azure().resourceManagementClient().resourceGroups().createOrUpdate(this.id, params);
        return this;
    }

    @Override
    public ResourceGroupImpl refresh() throws Exception {           //  FLUENT: implementation of ResourceGroup.Refreshable<ResourceGroup>
        this.setInner(this.collection.azure().resourceManagementClient().resourceGroups().get(this.id).getBody());
        return this;
    }
}
