/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.implementation;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

public class MediaRestProxy implements MediaContract {

    private Client channel;
    private ODataAtomMarshaller marshaller;

    static Log log = LogFactory.getLog(MediaContract.class);
    private static final String jsonRequestType = "application/json; odata=verbose";

    ServiceFilter[] filters;

    @Inject
    public MediaRestProxy(Client channel, OAuthFilter authFilter, RedirectFilter redirectFilter,
            VersionHeadersFilter versionHeadersFilter) {
        this.channel = channel;
        this.filters = new ServiceFilter[0];

        channel.addFilter(redirectFilter);
        channel.addFilter(authFilter);
        channel.addFilter(versionHeadersFilter);

        createMarshaller();
    }

    public MediaRestProxy(Client channel, ServiceFilter[] filters) {
        this.channel = channel;
        this.filters = filters;
        createMarshaller();
    }

    @Override
    public MediaContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new MediaRestProxy(channel, newFilters);
    }

    public Client getChannel() {
        return channel;
    }

    public void setChannel(Client channel) {
        this.channel = channel;
    }

    private WebResource getResource(String entityName) {
        WebResource resource = getChannel().resource(entityName);
        for (ServiceFilter filter : filters) {
            resource.addFilter(new ClientFilterAdapter(filter));
        }
        return resource;
    }

    private void createMarshaller() {
        try {
            this.marshaller = new ODataAtomMarshaller();
        }
        catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public AssetInfo createAsset(String name) throws ServiceException {
        WebResource resource = getResource("Assets");

        AssetType request = new AssetType();
        request.setName(name);

        try {
            return resource.type(MediaType.APPLICATION_ATOM_XML).accept(MediaType.APPLICATION_ATOM_XML)
                    .post(AssetInfo.class, marshaller.marshalEntry(request));
        }
        catch (JAXBException e) {
            throw new ServiceException(e);
        }
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.MediaContract#getAssets()
     */
    @Override
    public List<AssetInfo> getAssets() throws ServiceException {
        WebResource resource = getResource("Assets");

        return resource.type(MediaType.APPLICATION_ATOM_XML).accept(MediaType.APPLICATION_ATOM_XML)
                .get(new GenericType<List<AssetInfo>>() {
                });

    }
}
