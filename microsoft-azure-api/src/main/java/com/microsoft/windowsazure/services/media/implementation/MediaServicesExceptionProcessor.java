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

import java.util.EnumSet;

import javax.inject.Inject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.core.ServiceFilter;
import com.microsoft.windowsazure.services.core.utils.ServiceExceptionFactory;
import com.microsoft.windowsazure.services.media.MediaServicesContract;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyResult;
import com.microsoft.windowsazure.services.media.models.CreateAssetResult;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * Wrapper implementation of <code>MediaServicesContract</code> that
 * translates exceptions into ServiceExceptions.
 * 
 */
public class MediaServicesExceptionProcessor implements MediaServicesContract {

    private final MediaServicesContract next;
    static Log log = LogFactory.getLog(MediaServicesContract.class);

    public MediaServicesExceptionProcessor(MediaServicesContract next) {
        this.next = next;
    }

    @Inject
    public MediaServicesExceptionProcessor(MediaServicesRestProxy next) {
        this.next = next;
    }

    @Override
    public MediaServicesContract withFilter(ServiceFilter filter) {
        return new MediaServicesExceptionProcessor(next.withFilter(filter));
    }

    private ServiceException processCatch(ServiceException e) {
        log.warn(e.getMessage(), e.getCause());
        return ServiceExceptionFactory.process("MediaServices", e);
    }

    @Override
    public CreateAssetResult createAsset(String name) throws ServiceException {
        try {
            return next.createAsset(name);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public void deleteAsset(AssetInfo asset) throws ServiceException {
        try {
            next.deleteAsset(asset);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }

    @Override
    public CreateAccessPolicyResult createAccessPolicy(String name, int durationInMinutes,
            EnumSet<AccessPolicyPermission> permissions) throws ServiceException {
        try {
            return next.createAccessPolicy(name, durationInMinutes, permissions);
        }
        catch (UniformInterfaceException e) {
            throw processCatch(new ServiceException(e));
        }
        catch (ClientHandlerException e) {
            throw processCatch(new ServiceException(e));
        }
    }
}
