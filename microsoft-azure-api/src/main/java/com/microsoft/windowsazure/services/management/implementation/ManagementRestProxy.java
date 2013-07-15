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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.blob.implementation.RFC1123DateConverter;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.UserAgentFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.microsoft.windowsazure.services.management.ManagementConfiguration;
import com.microsoft.windowsazure.services.management.ManagementContract;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfo;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfoFactory;
import com.microsoft.windowsazure.services.management.models.AffinityGroupInfoListFactory;
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

/**
 * The Class ManagementRestProxy.
 */
public class ManagementRestProxy implements ManagementContract {

    /** The channel. */
    private Client channel;

    /** The uri. */
    private final String uri;

    /** The subscription id. */
    private final String subscriptionId;

    /** The rfc1123 date convert. */
    private final RFC1123DateConverter rfc1123DateConvert = new RFC1123DateConverter();

    /** The log. */
    static Log log = LogFactory.getLog(ManagementContract.class);

    /** The filters. */
    ServiceFilter[] filters;

    /**
     * Instantiates a new management rest proxy.
     * 
     * @param channel
     *            the channel
     * @param uri
     *            the uri
     * @param subscriptionId
     *            the subscription id
     * @param userAgentFilter
     *            the user agent filter
     */
    @Inject
    public ManagementRestProxy(Client channel, @Named(ManagementConfiguration.URI) String uri,
            @Named(ManagementConfiguration.SUBSCRIPTION_ID) String subscriptionId, UserAgentFilter userAgentFilter) {

        this.channel = channel;
        this.filters = new ServiceFilter[0];
        this.uri = uri;
        this.subscriptionId = subscriptionId;
        this.channel.addFilter(userAgentFilter);
    }

    /**
     * Instantiates a new management rest proxy.
     * 
     * @param channel
     *            the channel
     * @param serviceFilter
     *            the service filter
     * @param uri
     *            the uri
     * @param subscriptionId
     *            the subscription id
     * @param keyStorePath
     *            the key store path
     */
    public ManagementRestProxy(Client channel, ServiceFilter[] serviceFilter, String uri, String subscriptionId) {
        this.channel = channel;
        this.filters = serviceFilter;
        this.uri = uri;
        this.subscriptionId = subscriptionId;
    }

    /**
     * Gets the channel.
     * 
     * @return the channel
     */
    public Client getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     * 
     * @param channel
     *            the new channel
     */
    public void setChannel(Client channel) {
        this.channel = channel;
    }

    /**
     * Gets the resource.
     * 
     * @return the resource
     */
    private WebResource getResource() {
        WebResource resource = getChannel().resource(this.uri);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    /**
     * Gets the request id.
     * 
     * @param clientResponse
     *            the client response
     * @return the request id
     */
    private String getRequestId(ClientResponse clientResponse) {
        return clientResponse.getHeaders().getFirst("x-ms-request-id");
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.core.FilterableService#withFilter(com.microsoft.windowsazure.services.core.ServiceFilter)
     */
    @Override
    public ManagementContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new ManagementRestProxy(channel, newFilters, uri, subscriptionId);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.management.ManagementContract#listAffinityGroups()
     */
    @Override
    public ListResult<AffinityGroupInfo> listAffinityGroups() {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups")
                .header("x-ms-version", "2013-03-01").header("x-ms-client-request-id", UUID.randomUUID())
                .get(ClientResponse.class);
        PipelineHelpers.ThrowIfNotSuccess(clientResponse);
        String requestId = getRequestId(clientResponse);
        AffinityGroups affinityGroups = clientResponse.getEntity(AffinityGroups.class);
        List<AffinityGroupInfo> affinityGroupInfoList = AffinityGroupInfoListFactory.getItem(affinityGroups);
        return new ListResult<AffinityGroupInfo>(clientResponse.getStatus(), requestId, affinityGroupInfoList);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.management.ManagementContract#createAffinityGroup(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public CreateAffinityGroupResult createAffinityGroup(String affinityGroupName, String label, String location) {
        return createAffinityGroup(affinityGroupName, label, location, null);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.management.ManagementContract#createAffinityGroup(java.lang.String, java.lang.String, java.lang.String, com.microsoft.windowsazure.services.management.models.CreateAffinityGroupOptions)
     */
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
                .header("x-ms-version", "2013-03-01").header("x-ms-client-request-id", UUID.randomUUID().toString())
                .type(MediaType.APPLICATION_XML).post(ClientResponse.class, createAffinityGroup);
        CreateAffinityGroupResult createAffinityGroupResult = new CreateAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        MultivaluedMap<String, String> headers = clientResponse.getHeaders();
        createAffinityGroupResult.setLocation(headers.getFirst("Location"));
        createAffinityGroupResult.setRegion(headers.getFirst("x-ms-servedbyregion"));
        createAffinityGroupResult.setServer(headers.getFirst("Server"));
        createAffinityGroupResult.setDate(rfc1123DateConvert.parse((headers.getFirst("Date"))));
        return createAffinityGroupResult;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.management.ManagementContract#getAffinityGroup(java.lang.String)
     */
    @Override
    public GetAffinityGroupResult getAffinityGroup(String name) {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups").path(name)
                .header("x-ms-version", "2013-03-01").get(ClientResponse.class);
        PipelineHelpers.ThrowIfError(clientResponse);
        GetAffinityGroupResult getAffinityGroupResult = new GetAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        AffinityGroup affinityGroup = clientResponse.getEntity(AffinityGroup.class);
        AffinityGroupInfo affinityGroupInfo = AffinityGroupInfoFactory.getItem(affinityGroup);
        getAffinityGroupResult.setValue(affinityGroupInfo);
        return getAffinityGroupResult;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.management.ManagementContract#deleteAffinityGroup(java.lang.String)
     */
    @Override
    public DeleteAffinityGroupResult deleteAffinityGroup(String name) {
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups").path(name)
                .header("x-ms-version", "2013-03-01").delete(ClientResponse.class);
        PipelineHelpers.ThrowIfError(clientResponse);
        DeleteAffinityGroupResult deleteAffinityGroupResult = new DeleteAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        return deleteAffinityGroupResult;
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.management.ManagementContract#updateAffinityGroup(java.lang.String, java.lang.String, com.microsoft.windowsazure.services.management.models.UpdateAffinityGroupOptions)
     */
    @Override
    public UpdateAffinityGroupResult updateAffinityGroup(String name, String label,
            UpdateAffinityGroupOptions updateAffinityGroupOptions) {
        UpdateAffinityGroup updateAffinityGroup = new UpdateAffinityGroup();
        updateAffinityGroup.setLabel(label);
        if (updateAffinityGroupOptions != null) {
            updateAffinityGroup.setDescription(updateAffinityGroupOptions.getDescription());
        }
        ClientResponse clientResponse = getResource().path(subscriptionId).path("affinitygroups").path(name)
                .header("x-ms-version", "2011-02-25").put(ClientResponse.class, updateAffinityGroup);
        PipelineHelpers.ThrowIfError(clientResponse);
        UpdateAffinityGroupResult updateAffinityGroupResult = new UpdateAffinityGroupResult(clientResponse.getStatus(),
                getRequestId(clientResponse));
        MultivaluedMap<String, String> headers = clientResponse.getHeaders();
        updateAffinityGroupResult.setRegion(headers.getFirst("x-ms-servedbyregion"));
        updateAffinityGroupResult.setDate(rfc1123DateConvert.parse((headers.getFirst("Date"))));
        return updateAffinityGroupResult;
    }
}
