/**
 * Copyright 2011 Microsoft Corporation
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
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.pipeline.ClientFilterAdapter;
import com.microsoft.windowsazure.services.media.MediaServicesContract;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyResult;
import com.microsoft.windowsazure.services.media.models.CreateAssetResult;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

public class MediaServicesRestProxy implements MediaServicesContract {

    private Client channel;
    static Log log = LogFactory.getLog(MediaServicesContract.class);

    ServiceFilter[] filters;

    public MediaServicesRestProxy(Client channel, OAuthFilter authFilter, RedirectFilter redirectFilter) {
        this.channel = channel;
        this.filters = new ServiceFilter[0];
        channel.addFilter(redirectFilter);
        channel.addFilter(authFilter);
    }

    public MediaServicesRestProxy(Client channel, ServiceFilter[] filters) {
        this.channel = channel;
        this.filters = filters;
    }

    @Override
    public MediaServicesContract withFilter(ServiceFilter filter) {
        ServiceFilter[] newFilters = Arrays.copyOf(filters, filters.length + 1);
        newFilters[filters.length] = filter;
        return new MediaServicesRestProxy(channel, newFilters);
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

    @Override
    public CreateAssetResult createAsset(String name) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteAsset(AssetInfo asset) throws ServiceException {
        // TODO Auto-generated method stub

    }

    @Override
    public CreateAccessPolicyResult createAccessPolicy(String name, int durationInMinutes,
            EnumSet<AccessPolicyPermission> permissions) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }

}
