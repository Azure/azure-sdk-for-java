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
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.management.ManagementConfiguration;
import com.microsoft.windowsazure.services.management.ManagementContract;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.ListResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ManagementRestProxy implements ManagementContract {

    private Client channel;
    private String uri;
    private String subscriptionId;
    static Log log = LogFactory.getLog(ManagementContract.class);

    ServiceFilter[] filters;

    @Inject
    public ManagementRestProxy(Client channel, @Named(ManagementConfiguration.URI) String uri,
            @Named(ManagementConfiguration.SUBSCRIPTIONID) String subscriptionId) {

        this.channel = channel;
        this.filters = new ServiceFilter[0];
        this.uri = uri;
    }

    public ManagementRestProxy(Client channel, ServiceFilter[] serviceFilter) {
        this.channel = channel;
        this.filters = serviceFilter;
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

    @Override
    public ManagementContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new ManagementRestProxy(channel, newFilters);
    }

    @Override
    public ListResult<AffinityGroupInfo> listAffinityGroups() {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .header("x-ms-version", "2013-03-01").get(ClientResponse.class);
        String requestId = clientResponse.getHeaders().getFirst("x-ms-request-id");
        UUID requestIdUUID = UUID.fromString(requestId);
        return new ListResult<AffinityGroupInfo>(null);
    }

    @Override
    public void listVirtualMachines(String subscriptionId) {
        // TODO Auto-generated method stub

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

}
