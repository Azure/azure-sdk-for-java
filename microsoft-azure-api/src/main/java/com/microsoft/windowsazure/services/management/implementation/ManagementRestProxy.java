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
    public CreateAffinityGroupResult createAffinityGroup(String expectedAffinityGroupName, String expectedLabel,
            String expectedLocation) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GetAffinityGroupResult getAffinityGroup(String affinityGroupName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteAffinityGroup(String affinityGroupName) {
        // TODO Auto-generated method stub

    }

    @Override
    public CreateAffinityGroupResult createAffinityGroup(String expectedAffinityGroupName, String expectedLabel,
            String expectedLocation, CreateAffinityGroupOptions createAffinityGroupOptions) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UpdateAffinityGroupResult updateAffinityGroup(String expectedAffinityGroupLabel,
            UpdateAffinityGroupOptions updateAffinityGroupOptions) {
        // TODO Auto-generated method stub
        return null;
    }

}
