/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.management.implementation;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.management.ManagementConfiguration;
import com.microsoft.windowsazure.services.management.ManagementContract;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.CreateAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.DeleteAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.GetAffinityGroupResult;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions;
import com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ManagementRestProxy implements ManagementContract {

    private Client channel;
    private final String uri;
    private final String subscriptionId;
    private final String keyStorePath;
    static Log log = LogFactory.getLog(ManagementContract.class);

    ServiceFilter[] filters;

    @Inject
    public ManagementRestProxy(Client channel, @Named(ManagementConfiguration.URI) String uri,
            @Named(ManagementConfiguration.SUBSCRIPTION_ID) String subscriptionId,
            @Named(ManagementConfiguration.KEYSTORE_PATH) String keyStorePath) {

        this.channel = channel;
        this.filters = new ServiceFilter[0];
        this.uri = uri;
        this.subscriptionId = subscriptionId;
        this.keyStorePath = keyStorePath;
    }

    public ManagementRestProxy(Client channel, ServiceFilter[] serviceFilter, String uri, String subscriptionId,
            String keyStorePath) {
        this.channel = channel;
        this.filters = serviceFilter;
        this.uri = uri;
        this.subscriptionId = subscriptionId;
        this.keyStorePath = keyStorePath;
    }

    public Client getChannel() {
        return channel;
    }

    public void setChannel(Client channel) {
        this.channel = channel;
    }

    private WebResource getResource() {
        WebResource resource = getChannel().resource(this.uri);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    private UUID getRequestId(ClientResponse clientResponse) {
        String requestId = clientResponse.getHeaders().getFirst("x-ms-request-id");
        return UUID.fromString(requestId);
    }

    @Override
    public ManagementContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new ManagementRestProxy(channel, newFilters, uri, subscriptionId, keyStorePath);
    }

    @Override
    public ListResult<AffinityGroupInfo> listAffinityGroups() {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .header("x-ms-version", "2013-03-01").get(ClientResponse.class);
        PipelineHelpers.ThrowIfNotSuccess(clientResponse);
        UUID requestId = getRequestId(clientResponse);
        AffinityGroups affinityGroups = clientResponse.getEntity(AffinityGroups.class);
        List<AffinityGroupInfo> affinityGroupInfoList = AffinityGroupInfoListFactory.getItem(affinityGroups);
        return new ListResult<AffinityGroupInfo>(clientResponse.getStatus(), requestId, affinityGroupInfoList);
    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String affinityGroupName, String label, String location) {
        return createAffinityGroup(affinityGroupName, label, location, null);
    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String affinityGroupName, String label, String location,
            CreateAffinityGroupOptions createAffinityGroupOptions) {

        CreateAffinityGroup createAffinityGroup = new CreateAffinityGroup();
        createAffinityGroup.setName(affinityGroupName);
        createAffinityGroup.setLabel(label);
        createAffinityGroup.setLocation(location);
        if (createAffinityGroupOptions != null) {
            createAffinityGroup.setDescription(createAffinityGroup.getDescription());
        }
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .header("x-ms-version", "2013-03-01").put(ClientResponse.class, createAffinityGroup);
        CreateAffinityGroupResult createAffinityGroupResult = new CreateAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        return createAffinityGroupResult;
    }

    @Override
    public GetAffinityGroupResult getAffinityGroup(String affinityGroupName) {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .path(affinityGroupName).header("x-ms-version", "2013-03-01").get(ClientResponse.class);
        PipelineHelpers.ThrowIfError(clientResponse);
        GetAffinityGroupResult getAffinityGroupResult = new GetAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        AffinityGroup affinityGroup = clientResponse.getEntity(AffinityGroup.class);
        AffinityGroupInfo affinityGroupInfo = AffinityGroupInfoFactory.getItem(affinityGroup);
        getAffinityGroupResult.setValue(affinityGroupInfo);
        return getAffinityGroupResult;
    }

    @Override
    public DeleteAffinityGroupResult deleteAffinityGroup(String affinityGroupName) {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .path(affinityGroupName).header("x-ms-version", "2013-03-01").delete(ClientResponse.class);
        PipelineHelpers.ThrowIfError(clientResponse);
        DeleteAffinityGroupResult deleteAffinityGroupResult = new DeleteAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        return deleteAffinityGroupResult;
    }

    @Override
    public UpdateAffinityGroupResult updateAffinityGroup(String affinityGroupName, String affinityGroupLabel,
            UpdateAffinityGroupOptions updateAffinityGroupOptions) {
        UpdateAffinityGroup updateAffinityGroup = new UpdateAffinityGroup();
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .path(affinityGroupName).header("x-ms-version", "2011-02-25")
                .put(ClientResponse.class, updateAffinityGroup);
        PipelineHelpers.ThrowIfError(clientResponse);
        UpdateAffinityGroupResult updateAffinityGroupResult = new UpdateAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        return updateAffinityGroupResult;
    }

}
