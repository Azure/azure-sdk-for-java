package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceGroupImpl extends
        IndexableRefreshableWrapperImpl<ResourceGroup, ResourceGroupInner>
        implements
        ResourceGroup,
        ResourceGroup.DefinitionBlank,
        ResourceGroup.DefinitionProvisionable,
        ResourceGroup.Update  {

    private final ResourceGroupsInner client;
    private final ResourceManagementClientImpl serviceClient;

    public ResourceGroupImpl(final ResourceGroupInner innerModel, final ResourceManagementClientImpl serviceClient) {
        super(innerModel.name(), innerModel);
        this.client = serviceClient.resourceGroups();
        this.serviceClient = serviceClient;
    }

    /***********************************************************
     * Getters
     ***********************************************************/

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String provisioningState() {
        return this.inner().properties().provisioningState();
    }

    @Override
    public String location() {
        return this.inner().location();
    }

    @Override
    public Map<String, String> tags() {
        return Collections.unmodifiableMap(this.inner().tags());
    }

    /**************************************************************
     * Setters (fluent interface)
     **************************************************************/

    @Override
    public ResourceGroupImpl withLocation(String regionName) {       //  FLUENT: implementation of ResourceGroup.DefinitionBlank
        this.inner().setLocation(regionName);                        //
        return this;
    }

    @Override
    public ResourceGroupImpl withLocation(Region region) {            //  FLUENT: implementation of ResourceGroup.DefinitionBlank
        return this.withLocation(region.toString());                  //
    }

    @Override
    public ResourceGroupImpl withTags(Map<String, String> tags) {     //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable
        this.inner().setTags(new HashMap<>(tags));    //                  ResourceGroup.Update.UpdateBlank.Taggable<Update>
        return this;
    }

    @Override
    public ResourceGroupImpl withTag(String key, String value) {      //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable
        if(this.inner().tags() == null) {                             //  ResourceGroup.Update.UpdateBlank.Taggable<Update>
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().tags().put(key, value);
        return this;
    }

    @Override
    public ResourceGroupImpl withoutTag(String key) {                //  FLUENT: implementation of ResourceGroup.Update.UpdateBlank.Taggable<Update>
        this.inner().tags().remove(key);                             //
        return this;
    }

    /************************************************************
     * Verbs
     ************************************************************/

    @Override
    public ResourceGroupImpl apply() throws Exception {             //  FLUENT: implementation of ResourceGroup.Update.Updatable<T>
        ResourceGroupInner params = new ResourceGroupInner();
        ResourceGroupInner group;

        params.setTags(this.inner().tags());

        // Figure out the location, since the SDK requires on the params explicitly even though it cannot be changed
        if(this.inner().location() != null) {
            params.setLocation(this.inner().location());
        } else if(null == (group = client.get(this.id).getBody())) {
            throw new Exception("Resource group not found");
        } else {
            params.setLocation(group.location());
        }

        client.createOrUpdate(this.id, params);
        return this;
    }
    
    @Override
    public ResourceGroupImpl provision() throws Exception {          //  FLUENT: implementation of ResourceGroup.DefinitionProvisionable.Provisionable<ResourceGroup>
        ResourceGroupInner params = new ResourceGroupInner();
        params.setLocation(this.inner().location());
        params.setTags(this.inner().tags());
        client.createOrUpdate(this.id, params);
        return this;
    }

    @Override
    public List<Provisionable<?>> prerequisites() {
        return null;
    }

    @Override
    public ResourceGroupImpl refresh() throws Exception {            //  FLUENT: implementation of ResourceGroup.Refreshable<ResourceGroup>
        this.setInner(client.get(this.id).getBody());
        return this;
    }

    @Override
    public <T extends ResourceConnector> T connectToResource(ResourceConnector.Builder<T> adapterBuilder) {
        return adapterBuilder.create(this.serviceClient.restClient(), this.serviceClient.subscriptionId(), this);
    }
}
